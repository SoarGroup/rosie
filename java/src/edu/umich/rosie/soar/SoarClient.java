package edu.umich.rosie.soar;

import java.util.*;

import sml.*;
import sml.Agent.PrintEventInterface;
import sml.Agent.RunEventInterface;
import sml.Agent.OutputEventInterface;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.language.LanguageTestWriter;
import edu.umich.rosie.soarobjects.Time;
import edu.umich.rosie.connectors.LogfileWriter;

public class SoarClient implements RunEventInterface, PrintEventInterface, OutputEventInterface {
    public static class ClientConfig{
        public final String agentName;
        public final String agentSource;
        public final String smemSource;

        public final boolean spawnDebugger;
        public final int watchLevel;
		public final boolean startRunning;
        
        public final boolean remoteConnection;
        
        public final String speechFile;

        public enum OutputLevel { NONE, SUMMARY, FULL };
		public final OutputLevel outputLevel;
		public final String logFilename;
        public final boolean writeStandardOut;
        
		public final String languageTestFilename;
        
        public final boolean parserTest;
        public final String parser;

		public final boolean simClock;
		public final int clockStepMS;


        public ClientConfig(Properties props){
			// agent-name = [str] - The name to use when creating the SoarAgent
            agentName = props.getProperty("agent-name", "soaragent");

			// agent-source = [file] - The root soar file to source all the agent productions
            agentSource = props.getProperty("agent-source", null);

			// smem-source = [file] - The root soar file to source all smem add commands
            smemSource = props.getProperty("smem-source", null);

			// source-output = [full|summary|none] - How much output to print when sourcing hte agent
			String sourceOutput = props.getProperty("source-output", "summary");
			if(sourceOutput.equals("full")){
				outputLevel = OutputLevel.FULL;
			} else if(sourceOutput.equals("none")){
				outputLevel = OutputLevel.NONE;
			} else {
				outputLevel = OutputLevel.SUMMARY;
			}

			// parser = [laird|lucia] - Which parser to use
            parser = props.getProperty("parser", "lucia");

			// parser-test = [true|false] - Used for testing the lucia parser
            parserTest = props.getProperty("parser-test", "false").equals("true");

			// watch-level = [int] - The soar watch/trace level (default=1)
			watchLevel = getIntProp(props, "watch-level", 1);

			// spawn-debugger = [true|false] - If true, spawns the java debugger
            spawnDebugger = props.getProperty("spawn-debugger", "true").equals("true");

			// remote-connection = [true|false] - If true will connect to a remote kernel/agent
			//    instead of creating a new one
            remoteConnection = props.getProperty("remote-connection", "false").equals("true");

			// start-running = [true|false] - If true, will automatically run the agent
            startRunning = props.getProperty("start-running", "false").equals("true");

			// write-to-stdout = [true|false] - If true, will print all soar/agent output to standard output
            writeStandardOut = props.getProperty("write-to-stdout", "false").equals("true");

			// log-filename = [file]
			//    If given, Rosie will write all output to the given file
			logFilename = props.getProperty("log-filename", null);

			// CLOCK - A clock structure will be put on the input-link with time info
			// sim-clock = [true|false] - If true (default), the clock will simulate the
			//    time instead of using real-world time
			//    (It will advance a fixed amount of ms per decision cycle)
			//    This is useful for debugging/testing
			simClock = props.getProperty("sim-clock", "true").equals("true");

			// clock-step-ms = [int] (Default=50) - If using a simulated clock, 
			//    this is how many ms it will advance the clock each decision cycle
			clockStepMS = getIntProp(props, "clock-step-ms", 50);
           

			// speech-file = [file] - needed for text to speech (depricated)
            speechFile = props.getProperty("speech-file", "audio_files/sample");


			// language-test-filename = [file] - Used during parser testing
			languageTestFilename = props.getProperty("language-test-filename", null);

        }

		private int getIntProp(Properties props, String propName, int defVal){
			String propVal = props.getProperty(propName);
			if(propVal == null){
				return defVal;
			}
			try{
				return Integer.parseInt(propVal);
			} catch(NumberFormatException e){
				return defVal;
			}
		}
    }

	private HashMap<Class<?>, AgentConnector> connectors;
    
    private Time time;

    private Kernel kernel;
    private Agent agent;

    private ClientConfig config;

    private boolean debuggerSpawned = false;
    private boolean isRunning = false;

    private boolean queueStop = false;


    private long printCallbackId = -1;
    private ArrayList<Long> runEventCallbackIds;

    private ArrayList<Long> outputHandlerCallbackIds;

    public SoarClient(Properties props){
        this.config = new ClientConfig(props);

        runEventCallbackIds = new ArrayList<Long>();
        outputHandlerCallbackIds = new ArrayList<Long>();
		connectors = new HashMap<Class<?>, AgentConnector>();
        
        time = new Time(config.simClock, config.clockStepMS);
        
        if(this.config.remoteConnection){
        	int port = Kernel.kDefaultSMLPort;
			kernel = Kernel.CreateRemoteConnection(true, null, port, false);
			if (kernel == null){
			   throw new IllegalStateException("CreateRemoteConnection() returned null");
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

	public void addConnector(AgentConnector conn){
		this.connectors.put(conn.getClass(), conn);
	}

	@SuppressWarnings("unchecked")
	public <T> T getConnector(Class<T> cls){
		return (T)this.connectors.get(cls);
	}

    protected void onOutputEvent(String attName, Identifier id){
    	if (attName.equals("set-time")){
			processSetTimeMessage(id);
    	}
    }

	protected void processSetTimeMessage(Identifier id){
		Long h = SoarUtil.getChildInt(id, "hour");
		Long m = SoarUtil.getChildInt(id, "minute");
		if(h != null && m != null){
			time.setTime(h, m);
		}
		id.CreateStringWME("status", "success");
	}
	
    public void createAgent(){
        //System.out.println("SoarClient::createAgent()");
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
        outputHandlerCallbackIds.add(agent.AddOutputHandler("set-time", this, null));

        if(config.logFilename != null){
			this.addConnector(new LogfileWriter(this, config.logFilename));
        }

        if(config.writeStandardOut){
            printCallbackId = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, null);
        }

        if(config.languageTestFilename != null){
			this.addConnector(new LanguageTestWriter(this, config.languageTestFilename));
        }

        if(!config.remoteConnection){
        	sourceAgent();
        }
        agent.ExecuteCommandLine(String.format("w %d", config.watchLevel));

		for(AgentConnector conn : this.connectors.values()){
			conn.connect();
		}

        System.out.print("\n");

		if(config.startRunning){
   			start();
		}
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
        System.out.println("SoarClient::kill()");
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

        for(Long callbackId : runEventCallbackIds){
            agent.RemoveOutputHandler(callbackId);
        }
        outputHandlerCallbackIds.clear();
        
        if(printCallbackId != -1){
            agent.UnregisterForPrintEvent(printCallbackId);
        }
        printCallbackId = -1;

        time.removeFromWM();
		for(AgentConnector conn : this.connectors.values()){
			conn.disconnect();
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
			// Source smem structures
            String res = agent.ExecuteCommandLine("source " + config.smemSource);

			// Print the result to stdout, determined by the output level in the config
			switch(config.outputLevel){
				case FULL:
					System.out.println("------------- SOURCING SMEM ---------------");
					System.out.println(res);
					break;
				case SUMMARY:
					printSmemSourceInfo(res);
					break;
				case NONE:
			}
        }

        if(config.agentSource != null){
			// Source the soar rules
        	String res = agent.ExecuteCommandLine("source " + config.agentSource + " -v");

			// Print the result to stdout, determined by the output level in the config
			switch(config.outputLevel){
				case FULL:
					System.out.println("---------- SOURCING PRODUCTIONS -----------");
					System.out.println(config.agentSource);
					System.out.println(res);
					break;
				case SUMMARY:
					printAgentSourceInfo(res);
					break;
				case NONE:
			}

			// Find any rules that were replaced (indicates duplicate rules)
			if(config.outputLevel != ClientConfig.OutputLevel.NONE){
				List<String> replacedRules = findReplacedRules(res);
				if(replacedRules.size() > 0){
					System.out.println("DUPLICATE RULES:");
				}
				for(String rule : replacedRules){
					System.out.println("  " + rule);
				}
			}
        }
    }

    private void printSmemSourceInfo(String info){
        String[] lines = info.split("\n");
		Integer kn_counter = 0;
        for(String line : lines){
            line = line.trim();
            if(line.length() == 0){
                continue;
            }
            if(line.startsWith("Knowledge")){
				kn_counter += 1;
                continue;
            }
            System.out.println(line);
        }
		System.out.println("Knowledge added to semantic memory [" + kn_counter.toString() + " times]");
        System.out.println("Loaded Semantic Memory");
    }

	private List<String> findReplacedRules(String info){
    	ArrayList<String> replaced = new ArrayList<String>();
        String[] lines = info.split("\n");
        for(String line : lines){
            if(line.startsWith("Replacing")){
            	replaced.add(line.substring(21, line.length()));
            }
        }
		return replaced;
	}

    private void printAgentSourceInfo(String info){
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
            	continue;
            }
			System.out.println(line);
        }
    }


    @SuppressWarnings("incomplete-switch")
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
    }

    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
    	if(!wme.IsJustAdded() || !wme.IsIdentifier()){
    		return;
    	}
    	Identifier id = wme.ConvertToIdentifier();
    	onOutputEvent(attributeName, id);
    }
}
