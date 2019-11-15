/*
 * This program takes two arguments:
 * 	args[0] is a path to the config file and is required to run
 * 
 */

package edu.umich.rosie;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import sml.Agent;
import sml.smlPrintEventId;
import sml.Agent.PrintEventInterface;
import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.soar.debugger.SWTApplication;
import edu.umich.rosie.connectors.ActionStackConnector;
import edu.umich.rosie.connectors.TaskTestWriter;

public class RosieCLI
{
	private SoarAgent soarAgent;

	private LanguageConnector language;

    public RosieCLI(Properties props)
    {
    	soarAgent = new SoarAgent(props);
    	
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	language = new LanguageConnector(soarAgent, props, internalPasser);
		soarAgent.addConnector(language);

		// print-action-stack = true
		// Create an ActionStackConnector and print any task events to the console
		if(props.getProperty("print-action-stack", "false").equals("true")){
			ActionStackConnector asConn = new ActionStackConnector(soarAgent);
			soarAgent.addConnector(asConn);
			asConn.registerForTaskEvent(new ActionStackConnector.TaskEventListener(){
				public void taskEventHandler(String taskInfo){
					System.out.println(taskInfo);
				}
			});
		}

		// task-test-output-filename = <filename>
		// Will add a TaskTestWriter and create an output file in the task testing format
		String taskTestFilename = props.getProperty("task-test-output-filename", null);
		if(taskTestFilename != null){
			soarAgent.addConnector(new TaskTestWriter(soarAgent, taskTestFilename));
		}

    	soarAgent.createAgent();
    }

	public void run(){
		soarAgent.sendCommand("run");
	}

	public void shutdown(){
		soarAgent.kill();
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
        
        boolean spawnDebugger = props.getProperty("spawn-debugger", "false").equals("true");
		props.setProperty("spawn-debugger", "false");
        
        RosieCLI cli = new RosieCLI(props);
		if(spawnDebugger){
			try {
				SWTApplication swtApp = new SWTApplication();
				swtApp.startApp(new String[]{"-remote"});
				cli.shutdown();
				System.exit(0); // is there a better way to get the Soar thread to stop? 
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			cli.run();
			cli.shutdown();
		}
    }
}
