package edu.umich.rosie.soar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.*;

import edu.umich.rosie.soarobjects.Time;


import sml.*;
import sml.Agent.PrintEventInterface;
import sml.Agent.RunEventInterface;

public class SoarAgent implements RunEventInterface, PrintEventInterface {
    public static class AgentConfig{
        public String agentName;
        public String agentSource;
        public String smemSource;

        public boolean spawnDebugger;
        public int watchLevel;
        public int throttleMS;
        
        public boolean remoteConnection;
        
        public String speechFile;

        public Boolean verbose;
        public Boolean writeLog;
        public Boolean writeStandardOut;

        public AgentConfig(Properties props){
            spawnDebugger = props.getProperty("spawn-debugger", "true").equals("true");
            writeStandardOut = props.getProperty("write-to-stdout", "false").equals("true");
           
            agentName = props.getProperty("agent-name", "SoarAgent");
            agentSource = props.getProperty("agent-source", null);
            smemSource = props.getProperty("smem-source", null);
            verbose = props.getProperty("verbose", "true").equals("true");
            remoteConnection = props.getProperty("remote-connection", "false").equals("true");

            try{
                watchLevel = Integer.parseInt(props.getProperty("watch-level", "1"));
            } catch (NumberFormatException e){
                watchLevel = 1;
            }

            try{
                throttleMS = Integer.parseInt(props.getProperty("decision-throttle-ms", "0"));
            } catch(NumberFormatException e){
                throttleMS = 0;
            }

            speechFile = props.getProperty("speech-file", "audio_files/sample");

            writeLog = props.getProperty("enable-log", "false").equals("true");
        }
    }

    private AgentConnector perceptionConn;
    private AgentConnector actuationConn;
    private AgentConnector languageConn;
    
    private Time time;

    private Kernel kernel;
    private Agent agent;

    private AgentConfig config;

    private boolean debuggerSpawned = false;
    private boolean isRunning = false;

    private boolean queueStop = false;

    private PrintWriter logWriter;

    private long printCallbackId = -1;
    private ArrayList<Long> runEventCallbackIds;

    public SoarAgent(Properties props){
        this.config = new AgentConfig(props);

        runEventCallbackIds = new ArrayList<Long>();
        
        time = new Time();
        
        if(this.config.remoteConnection){
        	int port = Kernel.kDefaultSMLPort;
			kernel = Kernel.CreateRemoteConnection(true, null, port, false);
			if (kernel == null){
			   throw new IllegalStateException("CreateRemoveConnection() returned null");
			}
			System.out.println("CreatedConnection");
        } else {
			kernel = Kernel.CreateKernelInNewThread();
			if (kernel == null){
			   throw new IllegalStateException("CreateKernelInNewThread() returned null");
			}
        }

        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);
    }
    
    // Soar Stuff
    public Agent getAgent(){
        return agent;
    }
    
    public Kernel getKernel(){
        return kernel;
    }

    public boolean isRunning(){
        return isRunning;
    }

    // Perception
    public void setPerceptionConnector(AgentConnector conn){
        this.perceptionConn = conn;
    }
    public AgentConnector getPerceptionConnector(){
        return perceptionConn;
    }
    
    // Actuation
    public void setActuationConnector(AgentConnector conn){
        this.actuationConn = conn;
    }
    public AgentConnector getActuationConnector(){
        return actuationConn;
    }
    
    // Language
    public void setLanguageConnector(AgentConnector conn){
        this.languageConn = conn;
    }
    public AgentConnector getLanguageConnector(){
        return languageConn;
    }
    
    public void createAgent(){
        System.out.println("SoarAgent::createAgent()");
        // Initialize Soar Agent
        if(config.remoteConnection){
			agent = kernel.GetAgentByIndex(0);
			if (agent == null){
			   throw new IllegalStateException("Remote Kernel did not have an agent");
			}
        } else {
			agent = kernel.CreateAgent(config.agentName);
			if (agent == null){
			   throw new IllegalStateException("Kernel created null agent");
			}
        }
        
        if (config.spawnDebugger){
            debuggerSpawned = agent.SpawnDebugger(kernel.GetListenerPort());
            System.out.println("Spawn Debugger: " + (debuggerSpawned ? "SUCCESS" : "FAIL"));
        }

        runEventCallbackIds.add(agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null));
        runEventCallbackIds.add(agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, null));
        runEventCallbackIds.add(agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null));

        if(config.writeLog){
            try {
                logWriter = new PrintWriter(new FileWriter("rosie-log.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(config.writeStandardOut || config.writeLog){
            printCallbackId = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, null);
        }

        if(!config.remoteConnection){
        	sourceAgent();
        }
        agent.ExecuteCommandLine(String.format("w %d", config.watchLevel));
        
        if(perceptionConn != null){
            perceptionConn.connect();
        }
        if(actuationConn != null){
            actuationConn.connect();
        }
        if(languageConn != null){
            languageConn.connect();
        }

        System.out.print("\n");
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
        System.out.println("SoarAgent::kill()");
        queueStop = true;
        while(isRunning){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Long callbackId : runEventCallbackIds){
            agent.UnregisterForRunEvent(callbackId);
        }
        runEventCallbackIds.clear();
        
        if(printCallbackId != -1){
            agent.UnregisterForPrintEvent(printCallbackId);
        }
        printCallbackId = -1;

        time.removeFromWM();
        if(perceptionConn != null){
            perceptionConn.disconnect();
        }
        if(actuationConn != null){
            actuationConn.disconnect();
        }
        if(languageConn != null){
            languageConn.disconnect();
        }

        if(debuggerSpawned){
            agent.KillDebugger();
            debuggerSpawned = false;
        }
        
        if(!config.remoteConnection){
        	kernel.DestroyAgent(agent);
        }

        //kernel.DestroyAgent(agent);
        // SBW removed DestroyAgent call, it hangs in headless mode for some reason
        // (even when the KillDebugger isn't there)
        // I don't think there's any consequence to simply exiting instead.
        kernel.Shutdown();

		if(logWriter != null){
			logWriter.flush();
			logWriter.close();
		}
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
        	System.out.println("------------- SOURCING SMEM ---------------");
            String res = agent.ExecuteCommandLine("source " + config.smemSource);
            parseSmemSourceInfo(res);
        }

        if(config.agentSource != null){
        	System.out.println("---------- SOURCING PRODUCTIONS -----------");
            String res = agent.ExecuteCommandLine("source " + config.agentSource + " -v");
            if(config.verbose){
                parseAgentSourceInfo(res);
            } else {
                System.out.println("Sourced Productions");
            }
        }
    }

    private void parseSmemSourceInfo(String info){
        String[] lines = info.split("\n");
        for(String line : lines){
            line = line.trim();
            if(line.length() == 0){
                continue;
            }
            if(line.startsWith("Knowledge")){
                continue;
            }
            System.out.println(line);
        }
        System.out.println("Loaded Semantic Memory");
    }

    private void parseAgentSourceInfo(String info){
    	ArrayList<String> replaced = new ArrayList<String>();
        String[] lines = info.split("\n");
        for(String line : lines){
            if(line.trim().length() == 0){
                continue;
            }
            if(line.startsWith("*") || line.startsWith("#")){
                continue;
            }
            if(line.startsWith("Knowledge added")){
                continue;
            }
            if(line.startsWith("Sourcing")){
                continue;
            }
            if(line.startsWith("Replacing")){
            	replaced.add(line.substring(21, line.length()));
            	continue;
            }
            System.out.println(line);
        }
        System.out.println("DUPLICATE RULES:");
        for(String rule : replaced){
        	System.out.println("  " + rule);
        }

    }

    @Override
    public void runEventHandler(int id, Object data, Agent agent, int phase) {
        smlRunEventId eventId = smlRunEventId.swigToEnum(id);
        switch(eventId){
        case smlEVENT_BEFORE_INPUT_PHASE:
            // Stop the agent
            if(queueStop){
                agent.StopSelf();
                queueStop = false;
            }
            if(time.isAdded()){
                time.updateWM();
            } else {
                time.addToWM(agent.GetInputLink());
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
        if(config.writeStandardOut){
            System.out.print(message);
        }
        if(config.writeLog){
            synchronized(logWriter) {
				logWriter.print(message);
            }
        }
    }
}
