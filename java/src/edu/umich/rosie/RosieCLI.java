package edu.umich.rosie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.soar.SoarAgent;

@SuppressWarnings("serial")
public class RosieCLI
{
	private SoarAgent soarAgent;

	private LanguageConnector language;

    public RosieCLI(Properties props)
    {
    	soarAgent = new SoarAgent(props);
    	
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	language = new LanguageConnector(soarAgent, props, internalPasser);
    	soarAgent.setLanguageConnector(language);

    	soarAgent.createAgent();
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
        
        RosieCLI cli = new RosieCLI(props);
		//cli.run();
		while(true){
//	cli.isRunning()){
			try{
				Thread.sleep(10);
			} catch (InterruptedException e){
				break;
			}
		}
    }
}
