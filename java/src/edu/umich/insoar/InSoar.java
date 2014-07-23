package edu.umich.insoar;

import java.io.File;
import java.io.FileNotFoundException;
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
import probcog.lcmtypes.*;
import april.util.TimeUtil;

import com.soartech.bolt.BOLTLGSupport;

import edu.umich.insoar.world.WorldModel;

public class InSoar implements RunEventInterface
{
	private static long startTime = -1;
	
	public static long GetSoarTime(){
		if(startTime == -1){
			startTime = TimeUtil.utime();
		}
		return TimeUtil.utime() - startTime;
	}
	
	public static final boolean DEBUG_TRACE = false;
    
    private Kernel kernel;
    
    private SoarAgent soarAgent;
   
    private PrintWriter logWriter;

    private ChatFrame chatFrame;
    
    private LanguageConnector language;
    
    private MotorSystemConnector motorSystem;
    
    private Environment environment;
    
    private PerceptionConnector perception;
    
    private ISpyRunner ispyRunner;
    
    private int throttleMS = 0;

	private Logger logger;

    public InSoar(String agentName, String propsFile, boolean headless)
    {     

        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(propsFile));
		} catch (IOException e) {
			System.out.println("File not found: " + propsFile);
			e.printStackTrace();
		}
        
        String useLGProp = props.getProperty("enable-lgsoar");
        
        boolean useLG = false;
        String lgSoarDictionary = "";
        if (useLGProp != null && useLGProp.trim().equals("true")) {
        	lgSoarDictionary = props.getProperty("lgsoar-dictionary");
        	String lgSoarSource = props.getProperty("language-productions").trim();
        	if (lgSoarSource != null && lgSoarDictionary != null) {
        		useLG = true;
        	}
        	else {
        		System.out.println("ERROR: LGSoar misconfigured, not enabled.");
        	}
        }
        
        soarAgent = new SoarAgent(agentName, props, useLG, headless);

        BOLTLGSupport lgSupport = null;
        
        if (useLG) {
        	try{
        	lgSupport = new BOLTLGSupport(soarAgent.getAgent(), lgSoarDictionary);
        	} catch (RuntimeException e){
        		System.err.println("Couldn't open lg dictionary");
        		useLG = false;
        		lgSupport = null;
        	}
        }

	
		
		
		String watchLevel = props.getProperty("watch-level");
		if (watchLevel != null) {
			soarAgent.getAgent().ExecuteCommandLine("watch " + watchLevel);
		 }

		String throttleMSString = props.getProperty("decision-throttle-ms");
		if (throttleMSString != null) {
			throttleMS = Integer.parseInt(throttleMSString.trim());
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_DECISION_CYCLE, this, this);
		}

		String dictionaryFile = props.getProperty("dictionary-file");
		if(dictionaryFile == null){
			System.err.println("ERROR: No dictionary-file specified in sbolt.props");
			soarAgent.kill();
			return;
		}
		
		String grammarFile = props.getProperty("grammar-file");
		if(grammarFile == null){
			System.err.println("ERROR: No grammar-file specified in sbolt.props");
			soarAgent.kill();
			return;
		}
		
		String classifiersFile = props.getProperty("classifiers-file");
		if(classifiersFile != null){
        	perception_command_t cmd = new perception_command_t();
			cmd.utime = InSoar.GetSoarTime();
    		cmd.command = "LOAD_CLASSIFIERS=" + classifiersFile;
            LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
		}
		
		String speechFile = props.getProperty("speech-file");
		if(speechFile == null){
			speechFile = "/home/aaron/demo/speech/sample";
		}
		
		language = new LanguageConnector(soarAgent, lgSupport, dictionaryFile, grammarFile);
        perception = new PerceptionConnector(soarAgent);   
        environment = new Environment(motorSystem);
        motorSystem = new MotorSystemConnector(soarAgent, perception);
        try {
			logger = new Logger(soarAgent, props.getProperty("enable-logs"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Setup ChatFrame

        chatFrame = new ChatFrame(language, soarAgent, logger, speechFile);

        // chatFrame = new ChatFrame(language, soarAgent); SM: enabled logging
        
        ispyRunner = new ISpyRunner(soarAgent, chatFrame);
        
        // Setup menus
        chatFrame.addMenu(soarAgent.createMenu());
        chatFrame.addMenu(perception.createMenu());  
        chatFrame.addMenu(motorSystem.createMenu());
        chatFrame.addMenu(ispyRunner.createMenu());
        chatFrame.addMenu(chatFrame.setupScriptMenu());
        chatFrame.addMenu(environment.createMenu());
            
        soarAgent.setWorldModel(perception.world);
        
        if(DEBUG_TRACE){
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_OUTPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, this);
        }
        
        chatFrame.showFrame();   
        
        
        
        perception_command_t command = new perception_command_t();
		command.utime = InSoar.GetSoarTime();
		command.command = "reset=time";
		LCM.getSingleton().publish("GUI_COMMAND", command);
    }
    
    public static void main(String[] args)
    {
    	String propsFile = "sbolt.properties";
    	boolean headless = false;
    	for(int i = 0; i < args.length; i++){
    		if(args[i].equals("--headless")){
    			headless = true;
    		} else if(args[i].equals("-c") && args.length > i+1){
    			propsFile = args[i+1];
    			i++;
    		}
    	}

    	new InSoar("rosie", propsFile, headless);
    }
	
    long prevTime = 0;
    long outputTime = 0;
    long outputInputTime = 0;
    long inputTime = 0;
    long dcTime = 0;
    private long getElapsed(long prevTime){
    	return (TimeUtil.utime() - prevTime)/1000;
    }
	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		smlRunEventId runEventId = smlRunEventId.swigToEnum(arg0);
		switch(runEventId){
		case smlEVENT_AFTER_DECISION_CYCLE:
			try {
				Thread.sleep(throttleMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case smlEVENT_BEFORE_OUTPUT_PHASE:
			prevTime = TimeUtil.utime();
			break;
		case smlEVENT_AFTER_OUTPUT_PHASE:
			dcTime = getElapsed(dcTime);
			outputTime = getElapsed(prevTime);
			
			System.out.println("PHASE TIMERS");
			System.out.println(String.format("%-20s : %d", "  PRE-INPUT", outputInputTime));
			System.out.println(String.format("%-20s : %d", "  INPUT", inputTime));
			System.out.println(String.format("%-20s : %d", "  OUTPUT", outputTime));
			System.out.println(String.format("%-20s : %d", "  TOTAL", dcTime));
			System.out.println("=============================================================");
			
			dcTime = TimeUtil.utime();
			prevTime = TimeUtil.utime();
			break;
		case smlEVENT_BEFORE_INPUT_PHASE:
			outputInputTime = getElapsed(prevTime);
			prevTime = TimeUtil.utime();
			break;
		case smlEVENT_AFTER_INPUT_PHASE:
			inputTime = getElapsed(prevTime);
			prevTime = TimeUtil.utime();
			break;
		}
	}
}
