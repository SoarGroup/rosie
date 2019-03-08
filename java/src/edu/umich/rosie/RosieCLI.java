/*
 * This program takes two arguments:
 * 	args[0] is a path to the config file and is required to run
 * 	args[1] is optional.  If it is set to DEBUG then the program
 * 		is run in a single SoarJavaDebugger window and the thread
 * 		exits when the window is closed.
 * 		Otherwise, two windows are launched and the thread
 * 		does not exit when the windows are closed.
 * 		PL 3/8/2019.
 * 
 */

package edu.umich.rosie;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.soar.SoarAgent;

public class RosieCLI
{
	private SoarAgent soarAgent;

	private LanguageConnector language;

    public RosieCLI(Properties props, boolean debug)
    {
    	soarAgent = new SoarAgent(props);
    	
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	language = new LanguageConnector(soarAgent, props, internalPasser);
    	soarAgent.setLanguageConnector(language);

    	soarAgent.createAgent(debug);
    }

	public void run(){
		soarAgent.sendCommand("run 2000");
		//soarAgent.start();
	}

	public boolean isRunning(){
		return soarAgent.isRunning();
	}

    public static void main(String[] args) {
    	if(args.length == 0){
    		System.err.println("RosieCLI: Need path to config file as argument");
    		System.exit(1);
    	}

        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(args[0]));
		} catch (IOException e) {
			System.out.println("File not found: " + args[0]);
			e.printStackTrace();
			return;
		}
        
        //	Run in debugger mode if args[1] says so
        boolean debug = args.length > 1 && args[1].equals("DEBUG");
        
        @SuppressWarnings("unused")
		RosieCLI cli = new RosieCLI(props, debug);
 		//cli.run();
		while(!debug){
//    	cli.isRunning()){
    			try{
    				Thread.sleep(10);
    			} catch (InterruptedException e){
    				break;
    			}
    	}
    }
        
        
}
