package edu.umich.insoar;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Agent.PrintEventInterface;
import sml.Agent.RunEventInterface;
import sml.Kernel;
import sml.smlPrintEventId;
import sml.smlRunEventId;
import abolt.lcmtypes.observations_t;
import abolt.lcmtypes.robot_action_t;
import abolt.lcmtypes.robot_command_t;
import abolt.lcmtypes.training_data_t;
import abolt.lcmtypes.training_label_t;
import april.util.TimeUtil;

import com.soartech.bolt.BOLTLGSupport;

import edu.umich.insoar.world.World;

public class InSoar implements LCMSubscriber, PrintEventInterface, RunEventInterface
{
	public static InSoar Singleton(){
		return sboltInstance;
	}
	private static InSoar sboltInstance = null;
	
	private InputLinkHandler inputLink;
	
	private OutputLinkHandler outputLink;

    private ChatFrame chatFrame;

    private World world;
	
    private LCM lcm;
    
    private Kernel kernel;
    
    private SoarAgent soarAgent;
   
    private PrintWriter logWriter;
    
    private int throttleMS = 0;
    
    private boolean running = false;
    

    public InSoar(String agentName, boolean headless)
    {
    	sboltInstance = this;
        // LCM Channel, listen for observations_t
        try
        {
            lcm = new LCM();
            lcm.subscribe("OBSERVATIONS", this);
            lcm.subscribe("ROBOT_ACTION", this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        

        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader("sbolt.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        String useLGProp = props.getProperty("enable-lgsoar");
        
        boolean useLG = false;
        String lgSoarDictionary = "";
        if (useLGProp != null && useLGProp.equals("true")) {
        	lgSoarDictionary = props.getProperty("lgsoar-dictionary");
        	String lgSoarSource = props.getProperty("language-productions");
        	if (lgSoarSource != null && lgSoarDictionary != null) {
        		useLG = true;
        	}
        	else {
        		System.out.println("ERROR: LGSoar misconfigured, not enabled.");
        	}
        }

        kernel = Kernel.CreateKernelInNewThread();
        soarAgent = new SoarAgent(kernel, agentName, props, useLG);

        
        if (!headless) {
        	System.out.println("Spawn Debugger: " + soarAgent.getAgent().SpawnDebugger(kernel.GetListenerPort()));
        	// Requires the SOAR_HOME environment variable
        }
        
        BOLTLGSupport lgSupport = null;
        
        if (useLG) {
        	lgSupport = new BOLTLGSupport(soarAgent.getAgent(), lgSoarDictionary);
        }
        
        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);

		String doLog = props.getProperty("enable-log");
		if (doLog != null && doLog.equals("true")) {
			try {
				logWriter = new PrintWriter(new FileWriter("sbolt-log.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			soarAgent.getAgent().RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
		}
		
		String watchLevel = props.getProperty("watch-level");
		if (watchLevel != null) {
			soarAgent.getAgent().ExecuteCommandLine("watch " + watchLevel);
		}

		String throttleMSString = props.getProperty("decision-throttle-ms");
		if (throttleMSString != null) {
			throttleMS = Integer.parseInt(throttleMSString);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_DECISION_CYCLE, this, this);
		}
      
        world = new World();

        // Setup InputLink
        inputLink = new InputLinkHandler(soarAgent, lgSupport);

        // Setup OutputLink
        outputLink = new OutputLinkHandler(soarAgent);

        // Setup ChatFrame
        chatFrame = new ChatFrame(lgSupport, soarAgent);

        chatFrame.showFrame();
        
    }
    
    public SoarAgent getSoarAgent(){
    	return soarAgent;
    }

    public Kernel getKernel(){
    	return kernel;
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
    	if(channel.equals("ROBOT_ACTION") && world != null && world.getRobotArm() != null){
    		try {
    			robot_action_t action = new robot_action_t(ins);
				world.getRobotArm().newRobotAction(action);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else if(channel.equals("OBSERVATIONS") && inputLink != null){
    		synchronized(inputLink){
                observations_t obs = null;
                try {
                    obs = new observations_t(ins);
                    world.newObservation(obs);
                }
                catch (IOException e){
                    e.printStackTrace();
                    return;
                }
    		}
    	}
    }

    /**
     * Sends out training_data_t over LCM
     */
    public static void broadcastTrainingData(List<training_label_t> newLabels)
    {
    	if(newLabels != null){
        	training_data_t trainingData = new training_data_t();
        	trainingData.utime = TimeUtil.utime();
        	trainingData.num_labels = newLabels.size();
        	trainingData.labels = new training_label_t[newLabels.size()];
        	for(int i = 0; i < newLabels.size(); i++){
        		trainingData.labels[i] = newLabels.get(i);
        	}
        	LCM.getSingleton().publish("TRAINING_DATA", trainingData);
    	}
    }
    
    public static void broadcastRobotCommand(robot_command_t command){
        LCM.getSingleton().publish("ROBOT_COMMAND", command);
    }

    public static void main(String[] args)
    {
    	boolean headless = false;
    	if (args.length > 0 && args[0].equals("--headless")) {
    		// it might make sense to instead always make the parameter
    		// be the properties filename, and load all others there
    		// (currently, properties filename is hardcoded)
    		headless = true;
    	}
    	new InSoar("insoar-agent", headless);
    }
    
	@Override
	public void printEventHandler(int eventID, Object data, Agent agent, String message) {
		synchronized(logWriter) {
			logWriter.print(message);
		}
	}
	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		try {
			Thread.sleep(throttleMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
