package edu.umich.rosie;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import edu.umich.rosie.actuation.MobileActuationConnector;
import edu.umich.rosie.gui.RosieGUI;
import edu.umich.rosie.gui.RosieGUI.RosieConfig;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.perception.MobilePerceptionConnector;
import probcog.rosie.actuation.*;
import probcog.rosie.language.*;
import probcog.rosie.perception.*;
import sml.*;
import sml.Agent.PrintEventInterface;
import sml.Agent.RunEventInterface;

public class SoarAgent implements RunEventInterface, PrintEventInterface {

	private MobilePerceptionConnector perceptionConn;
	private MobileActuationConnector actuationConn;
	private LanguageConnector languageConn;

	private Kernel kernel;
	private Agent agent;

	private RosieGUI.RosieConfig config;

	private boolean isRunning = false;

	private boolean queueStop = false;

	private PrintWriter logWriter;

	public SoarAgent(RosieGUI.RosieConfig config){
		this.config = config;

		initSoar();

		perceptionConn = new MobilePerceptionConnector(this);
		actuationConn = new MobileActuationConnector(this);
		languageConn = new LanguageConnector(this, config.speechFile);
	}
	
	public MobilePerceptionConnector getPerceptionConnector(){
		return perceptionConn;
	}
	
	public MobileActuationConnector getActuationConnector(){
		return actuationConn;
	}
	
	public LanguageConnector getLanguageConnector(){
		return languageConn;
	}
	
	private void initSoar(){
    	kernel = Kernel.CreateKernelInNewThread();
        if (kernel == null){
           throw new IllegalStateException("CreateKernelInNewThread() returned null");
        }
        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);

		// Initialize Soar Agent
        agent = kernel.CreateAgent(config.agentName);
        if (agent == null){
           throw new IllegalStateException("Kernel created null agent");
        }

        if (config.spawnDebugger){
        	boolean success = agent.SpawnDebugger(kernel.GetListenerPort());
        	System.out.println("Spawn Debugger: " + (success ? "SUCCESS" : "FAIL"));
        }

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, null);
        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);

		if(config.writeLog){
			try {
				logWriter = new PrintWriter(new FileWriter("rosie-log.txt"));
				agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		agent.ExecuteCommandLine(String.format("watch %d", config.watchLevel));

        sourceAgent();
    }

	public Agent getAgent(){
		return agent;
	}

	public boolean isRunning(){
		return isRunning;
	}

	/**
     * Spawns a new thread that invokes a run command on the agent
     */
	public void start(){
		if(isRunning){
			return;
		}
    	class AgentThread implements Runnable{
    		public void run(){
    	    	isRunning = true;
    			sendCommand("run");
    			isRunning = false;
    		}
    	}
    	Thread agentThread = new Thread(new AgentThread());
    	agentThread.start();
	}

	public void stop(){
		queueStop = true;
	}

	public void kill(){
		agent.KillDebugger();
		//kernel.DestroyAgent(agent);
    	// SBW removed DestroyAgent call, it hangs in headless mode for some reason
    	// (even when the KillDebugger isn't there)
    	// I don't think there's any consequence to simply exiting instead.
	}


	public String sendCommand(String command){
		return agent.ExecuteCommandLine(command);
	}
	public void resetAgent(){
		if(isRunning){
			return;
		}
		reinitAgent();
		sourceAgent();
	}


	public void backup(String sessionName){
		if(isRunning){
			return;
		}
    	System.out.println("---- Performing Backup [" + sessionName + "] ----");

    	String storeEpmem = String.format("epmem --backup saved-agents/%s_epmem.db", sessionName);
    	System.out.println("  epmem: " + agent.ExecuteCommandLine(storeEpmem));

    	String storeSmem = String.format("smem --backup saved-agents/%s_smem.db", sessionName);
    	System.out.println("  smem: " + agent.ExecuteCommandLine(storeSmem));

    	String storeChunks = String.format("command-to-file saved-agents/%s_chunks.soar pc -f", sessionName);
    	System.out.println("  chunks: " + agent.ExecuteCommandLine(storeChunks));

    	System.out.println("---- Completed Backup -----");
	}

	public void restore(String sessionName){
		if(isRunning){
			return;
		}
		reinitAgent();

		System.out.println(String.format("---- Restoring Agent [%s] ----", sessionName));
    	agent.ExecuteCommandLine("smem --set database file");
    	agent.ExecuteCommandLine("epmem --set database file");

    	String sourceEpmem = String.format("epmem --set path saved-agents/%s_epmem.db", sessionName);
    	System.out.println("  epmem:" + agent.ExecuteCommandLine(sourceEpmem));

    	String sourceSmem = String.format("smem --set path saved-agents/%s_smem.db", sessionName);
    	System.out.println("  smem:" + agent.ExecuteCommandLine(sourceSmem));

    	String sourceChunks = String.format("source saved-agents/%s_chunks.soar", sessionName);
    	System.out.println("  chunks: " + agent.ExecuteCommandLine(sourceChunks));

    	if(config.agentSource != null){
    		String sourceRules = String.format("source %s", config.agentSource);
    		System.out.println("  rules: " + agent.ExecuteCommandLine(sourceRules));
    	}
    	System.out.println("---- Completed Restore ----");
	}

	private void reinitAgent(){
		System.out.println("------- RESET AGENT -------");
		System.out.println("  excise --all: " + agent.ExecuteCommandLine("excise --all"));
    	//System.out.println("  smem --init:  " + agent.ExecuteCommandLine("smem --init"));
    	//System.out.println("  epmem --init: " + agent.ExecuteCommandLine("epmem --init"));
    	//System.out.println("  init-soar: " + agent.ExecuteCommandLine("init-soar"));
    	System.out.println("---------------------------");
	}

	private void sourceAgent(){
        agent.ExecuteCommandLine("smem --set database memory");
        agent.ExecuteCommandLine("epmem --set database memory");
		if(config.smemSource != null){
			String res = agent.ExecuteCommandLine("source " + config.smemSource);
			System.out.println(res);
		}
		if(config.agentSource != null){
			String res = agent.ExecuteCommandLine("source " + config.agentSource + " -v");
			System.out.println(res);
		}
	}

	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		smlRunEventId eventId = smlRunEventId.swigToEnum(arg0);
		switch(eventId){
		case smlEVENT_BEFORE_INPUT_PHASE:
			// Stop the agent
			if(queueStop){
				sendCommand("stop");
				queueStop = false;
			}
			if(config.throttleMS > 0){
				try {
					Thread.sleep(config.throttleMS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			break;
		case smlEVENT_AFTER_INPUT_PHASE:
			// Commit any changes made to input link
		case smlEVENT_AFTER_OUTPUT_PHASE:
			// Commit any changes made to the output link
			if(agent.IsCommitRequired()){
				agent.Commit();
			}
			break;
		}
	}

	@Override
	public void printEventHandler(int eventID, Object data, Agent agent, String message) {
		synchronized(logWriter) {
			logWriter.print(message);
		}
	}
}
