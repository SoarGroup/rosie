package edu.umich.insoar;

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

public class InSoar implements PrintEventInterface, RunEventInterface
{
	public static final boolean DEBUG_TRACE = false;
    
    private Kernel kernel;
    
    private SoarAgent soarAgent;
   
    private PrintWriter logWriter;

    private ChatFrame chatFrame;
    
    private LanguageConnector language;
    
    private MotorSystemConnector motorSystem;
    
    private PerceptionConnector perception;
    
    private int throttleMS = 0;

    public InSoar(String agentName, boolean headless)
    {     

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
		
		language = new LanguageConnector(soarAgent, lgSupport, dictionaryFile, grammarFile);
        perception = new PerceptionConnector(soarAgent);   
        motorSystem = new MotorSystemConnector(soarAgent);
        
        // Setup ChatFrame
        chatFrame = new ChatFrame(language, soarAgent);
        chatFrame.addMenu(soarAgent.createMenu());
        chatFrame.addMenu(perception.createMenu());  
        chatFrame.addMenu(motorSystem.createMenu());
        chatFrame.addMenu(chatFrame.setupScriptMenu());
        
        soarAgent.setWorldModel(perception.world);
        
        if(DEBUG_TRACE){
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_OUTPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, this);
			soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, this);
        }
        
        chatFrame.showFrame();   
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
