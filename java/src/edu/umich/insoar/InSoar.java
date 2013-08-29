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

		
		language = new LanguageConnector(soarAgent, lgSupport);
        perception = new PerceptionConnector(soarAgent);   
        motorSystem = new MotorSystemConnector(soarAgent);
        
        // Setup ChatFrame
        chatFrame = new ChatFrame(language, soarAgent);
        chatFrame.addMenu(soarAgent.createMenu());
        chatFrame.addMenu(perception.createMenu());  
        chatFrame.addMenu(motorSystem.createMenu());
        chatFrame.addMenu(chatFrame.setupScriptMenu());
        
        soarAgent.setWorldModel(perception.world);
        
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
	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		try {
			Thread.sleep(throttleMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
