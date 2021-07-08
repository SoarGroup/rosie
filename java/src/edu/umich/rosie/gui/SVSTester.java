package edu.umich.rosie.gui;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import edu.umich.rosie.soar.SVSCommands;
import edu.umich.rosie.soar.SoarClient;
import sml.Kernel;
import sml.Agent;

public class SVSTester {
	public static void main(String[] args) {
    	if(args.length == 0){
    		System.err.println("SVSTester: Need path to config file as argument");
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
        
        new SVSTester(props);
    }

	public SVSTester(Properties props){
		SoarClient client = new SoarClient(props);
		client.createAgent();
		StringBuilder sb = new StringBuilder();
		sb.append(SVSCommands.addBox("box1", new double[]{1, 1, 0}, new double[]{0.5, 0.5, 0.5}));
		sb.append(SVSCommands.addBox("box2", new double[]{1, -0.5, 0}, new double[]{0.5, 0.5, 0.5}));
		sb.append(SVSCommands.addBox("box3", new double[]{1, 0, 0}, new double[]{0.5, 0.5, 0.5}));
		client.getAgent().SendSVSInput(sb.toString());
		while(true){}
	}
}
