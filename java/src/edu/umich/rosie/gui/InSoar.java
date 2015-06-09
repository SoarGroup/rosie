package edu.umich.rosie.gui;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.List;
//import java.util.Properties;
//
//import lcm.lcm.LCM;
//import lcm.lcm.LCMDataInputStream;
//import lcm.lcm.LCMSubscriber;
//import sml.Agent;
//import sml.Agent.PrintEventInterface;
//import sml.Agent.RunEventInterface;
//import sml.Kernel;
//import sml.smlPrintEventId;
//import sml.smlRunEventId;
//import probcog.lcmtypes.*;
//import april.util.TimeUtil;
//import edu.umich.insoar.ChatFrame;
//import edu.umich.rosie.Logger;
//import edu.umich.rosie.SoarAgent;
//import edu.umich.rosie.actuation.EnvironmentMenu;
//import edu.umich.rosie.actuation.ArmActuationConnector;
//import edu.umich.rosie.actuation.arm.WorldModel;
//import edu.umich.rosie.language.LanguageConnector;
//import edu.umich.rosie.perception.ArmPerceptionConnector;
//
//public class InSoar implements RunEventInterface
//{
//	private static long startTime = -1;
//	
//	public static long GetSoarTime(){
//		if(startTime == -1){
//			startTime = TimeUtil.utime();
//		}
//		return TimeUtil.utime() - startTime;
//	}
//	
//	public static final boolean DEBUG_TRACE = false;
//    
//    private SoarAgent soarAgent;
//   
//    private ChatFrame chatFrame;
//    
//    private LanguageConnector language;
//    
//    private ArmActuationConnector motorSystem;
//    
//    private EnvironmentMenu environment;
//    
//    private ArmPerceptionConnector perception;
//    
//    private ISpyRunner ispyRunner;
//    
//    private int throttleMS = 0;
//
//	private Logger logger;
//
//    public InSoar(String agentName, String propsFile, boolean headless)
//    {     
//
//        // Load the properties file
//        Properties props = new Properties();
//        try {
//			props.load(new FileReader(propsFile));
//		} catch (IOException e) {
//			System.out.println("File not found: " + propsFile);
//			e.printStackTrace();
//		}
//        
//        soarAgent = new SoarAgent(agentName, props, headless);
//
//		String watchLevel = props.getProperty("watch-level");
//		if (watchLevel != null) {
//			soarAgent.getAgent().ExecuteCommandLine("watch " + watchLevel);
//		 }
//
//		String throttleMSString = props.getProperty("decision-throttle-ms");
//		if (throttleMSString != null) {
//			throttleMS = Integer.parseInt(throttleMSString.trim());
//			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_DECISION_CYCLE, this, this);
//		}
//		
//		String classifiersFile = props.getProperty("classifiers-file");
//		if(classifiersFile == null){
//			classifiersFile = "default";
//		}
//		
//		String speechFile = props.getProperty("speech-file");
//		if(speechFile == null){
//			speechFile = "audio_files/sample";
//		}
//		
//		language = new LanguageConnector(soarAgent);
//        perception = new ArmPerceptionConnector(soarAgent, classifiersFile);   
//        environment = new EnvironmentMenu(motorSystem);
//        motorSystem = new ArmActuationConnector(soarAgent, perception);
//        try {
//			logger = new Logger(soarAgent, props.getProperty("enable-logs"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        
//        // Setup ChatFrame
//
//        chatFrame = new ChatFrame(language, soarAgent, logger, speechFile);
//
//        // chatFrame = new ChatFrame(language, soarAgent); SM: enabled logging
//        
//        ispyRunner = new ISpyRunner(soarAgent, chatFrame);
//        
//        // Setup menus
//        chatFrame.addMenu(soarAgent.createMenu());
//        chatFrame.addMenu(perception.createMenu());  
//        chatFrame.addMenu(motorSystem.createMenu());
//        chatFrame.addMenu(ispyRunner.createMenu());
//        chatFrame.addMenu(chatFrame.setupScriptMenu());
//        chatFrame.addMenu(environment.createMenu());
//            
//        soarAgent.setWorldModel(perception.world);
//        
//        if(DEBUG_TRACE){
//			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_OUTPUT_PHASE, this, this);
//			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, this);
//			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, this);
//			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, this);
//        }
//        
//        chatFrame.showFrame();   
//        
//        
//        
//        perception_command_t command = new perception_command_t();
//		command.utime = InSoar.GetSoarTime();
//		command.command = "reset=time";
//		LCM.getSingleton().publish("GUI_COMMAND", command);
//    }
//    
//    public static void main(String[] args)
//    {
//    	String propsFile = "sbolt.properties";
//    	boolean headless = false;
//    	for(int i = 0; i < args.length; i++){
//    		if(args[i].equals("--headless")){
//    			headless = true;
//    		} else if(args[i].equals("-c") && args.length > i+1){
//    			propsFile = args[i+1];
//    			i++;
//    		}
//    	}
//
//    	new InSoar("rosie", propsFile, headless);
//    }
//	
//    long prevTime = 0;
//    long outputTime = 0;
//    long outputInputTime = 0;
//    long inputTime = 0;
//    long dcTime = 0;
//    private long getElapsed(long prevTime){
//    	return (TimeUtil.utime() - prevTime)/1000;
//    }
//	@Override
//	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
//		smlRunEventId runEventId = smlRunEventId.swigToEnum(arg0);
//		switch(runEventId){
//		case smlEVENT_AFTER_DECISION_CYCLE:
//			try {
//				Thread.sleep(throttleMS);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			break;
//		case smlEVENT_BEFORE_OUTPUT_PHASE:
//			prevTime = TimeUtil.utime();
//			break;
//		case smlEVENT_AFTER_OUTPUT_PHASE:
//			dcTime = getElapsed(dcTime);
//			outputTime = getElapsed(prevTime);
//			
//			System.out.println("PHASE TIMERS");
//			System.out.println(String.format("%-20s : %d", "  PRE-INPUT", outputInputTime));
//			System.out.println(String.format("%-20s : %d", "  INPUT", inputTime));
//			System.out.println(String.format("%-20s : %d", "  OUTPUT", outputTime));
//			System.out.println(String.format("%-20s : %d", "  TOTAL", dcTime));
//			System.out.println("=============================================================");
//			
//			dcTime = TimeUtil.utime();
//			prevTime = TimeUtil.utime();
//			break;
//		case smlEVENT_BEFORE_INPUT_PHASE:
//			outputInputTime = getElapsed(prevTime);
//			prevTime = TimeUtil.utime();
//			break;
//		case smlEVENT_AFTER_INPUT_PHASE:
//			inputTime = getElapsed(prevTime);
//			prevTime = TimeUtil.utime();
//			break;
//		}
//	}
//}
