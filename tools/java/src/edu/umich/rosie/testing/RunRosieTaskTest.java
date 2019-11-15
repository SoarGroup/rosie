/*
 * This program takes two arguments:
 * 	args[0] is a path to the config file and is required to run
 * 
 */

package edu.umich.rosie.testing;

import java.io.*;
import java.util.Properties;

import sml.*;
import sml.Agent.PrintEventInterface;
import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.ActionStackConnector;


public class RunRosieTaskTest
{

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

		SoarAgent soarAgent = new SoarAgent(props);
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	LanguageConnector language = new LanguageConnector(soarAgent, props, internalPasser);
    	soarAgent.setLanguageConnector(language);

		ActionStackConnector asConn = new ActionStackConnector(soarAgent, true);
		soarAgent.addConnector(asConn);

		soarAgent.createAgent();

		try {
			// Open up the test-output.txt file to write the information we want during the test
			final Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File("test-output.txt"))));

			// Write the action stack information (when the agent begins/ends each task and subtask)
			asConn.addOutputListener(new ActionStackConnector.OutputListener(){
				public void handleOutput(String message){
					try{
						writer.write(message + "\n");
					} catch (IOException e){ }
				}
			});

			// Write each script sentence that the agent processes
            long printCallbackId = soarAgent.getAgent().RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, new PrintEventInterface(){
				public void printEventHandler(int eventID, Object data, Agent agent, String message) {
					if (message.indexOf("NEW-SENTENCE") == 0) {
						try{
							writer.write("I: \"" + message.split(":")[1].trim() + "\"\n");
						} catch (IOException e){ };
					}
				}
			}, null);

			// Write each sentence that the agent says as output
			internalPasser.addMessageListener(new IMessagePasser.IMessageListener(){
				public void receiveMessage(IMessagePasser.RosieMessage message){
					try {
						writer.write("R: \"" + message.message + "\"\n");
					} catch (IOException e){ }
				}
			});

			// Run until an interrupt (should only happen once all script entences are processed)
			soarAgent.sendCommand("run");
			soarAgent.getAgent().UnregisterForPrintEvent(printCallbackId);
			writer.close();
		} catch (Exception e){ }
		soarAgent.kill();
    }

}

