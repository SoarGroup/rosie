package edu.umich.rosie.tools.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class RosieAgentConfigurator {
	public static void ConfigureAgent(RosieConfig config){
		try{
			String agentConfigFilename = config.agentDir + "/rosie." + config.agentName + ".config";
			Writer agentConfigFile = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(agentConfigFilename), "utf-8"));

			String agentSourceFilename = config.agentDir + "/source_" + config.agentName + ".soar";
			Writer agentSourceFile = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(agentSourceFilename), "utf-8"));
			agentSourceFile.write("pushd " + config.agentDir + "\n\n");

			String smemSourceFilename = config.agentDir + "/smem_source_" + config.agentName + ".soar";
			Writer smemSourceFile = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(smemSourceFilename), "utf-8"));
			smemSourceFile.write("pushd " + config.agentDir + "\n\n");
			
			// Write the agent config file
			agentConfigFile.write("agent-name = " + config.agentName + "\n\n");
			agentConfigFile.write("agent-source = " + agentSourceFilename + "\n");
			agentConfigFile.write("smem-source = " + smemSourceFilename + "\n\n");
			
			// Sentences
			if (config.sentenceSource.equals("chat") && config.sentencesFile.exists()){
				agentConfigFile.write("messages-file = " + config.sentencesFile.getAbsolutePath() + "\n");
			}
			if (config.sentenceSource.equals("scripts") && config.sentencesFile.exists()){
				File sentenceSourceFile = new File(config.agentDir + "/sentences_" + config.agentName + ".soar");
				SentencesGenerator.generateRosieSentences(config.sentencesFile, sentenceSourceFile);
				agentSourceFile.write("source " + sentenceSourceFile.getAbsolutePath() + "\n");
			}
			
			// World
			if (config.worldFile.exists()){
				File rosieWorldFile = new File(config.agentDir + "/world_" + config.agentName + ".soar");
				WorldGenerator.generateRosieWorld(config.worldFile, rosieWorldFile);
				agentSourceFile.write("source " + rosieWorldFile.getAbsolutePath() + "\n");
			}
			
			// Smem
			if (config.smemConfigFile.exists()){
				File rosieDir = new File(config.rosieHome + "/agent");
				File outputDir = new File(config.agentDir);
				ArrayList<String> smemFiles = SmemConfigurator.configureSmem(config.smemConfigFile, outputDir, rosieDir, config.agentName);
				for (String filename : smemFiles){
					smemSourceFile.write("source " + filename + "\n\n");
				}
			}
			
			// Custom soar file
			if (config.customSoarFile.exists()){
				agentSourceFile.write("source " + config.customSoarFile.getAbsolutePath() + "\n");
			}
			
			// Custom smem file
			if (config.customSmemFile.exists()){
				smemSourceFile.write("source " + config.customSmemFile.getAbsolutePath() + "\n");
			}
			
			// Finish writing the agent config file
			for(Map.Entry<String, String> e : config.otherSettings.entrySet()){
				agentConfigFile.write(e.getKey() + " = " + e.getValue() + "\n");
			}
			agentConfigFile.close();
			
			// Finish writing the agent source file
			agentSourceFile.write("popd\n");
			agentSourceFile.close();

			// Finish writing the smem source file
			smemSourceFile.write("popd\n");
			smemSourceFile.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {
    	if (args.length == 0){
    		System.err.println("RosieAgentConfigurator expects 1 argument\n" + 
    							"  1: the filename of the rosie configuration file\n" + 
    							"  2 [OPT]: the rosie directory (defaults from $ROSIE_HOME environment variable");
    		System.exit(0);
    	}
    	
    	String configFilename = args[0];
    	
    	File configFile = new File(System.getProperty("user.dir") + "/" + configFilename);
    	if(!configFile.exists()){
    		System.err.println("\nThe file " + configFilename + " does not exist");
    		System.exit(0);
    	}

    	
        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(configFile));
		} catch (IOException e) {
			System.out.println("IO Error reading config file " + configFilename);
			e.printStackTrace();
			return;
		}
        
    	String rosieHome = null;
    	if (args.length >= 2){
    		rosieHome = args[2];
    	} else {
    		rosieHome = System.getenv("ROSIE_HOME");
    		if (rosieHome == null){
    			System.err.println("$ROSIE_HOME environment variable is not set");
    			System.exit(1);
    		}
    	}
        
        try{
        	RosieConfig config = new RosieConfig(configFile, props, rosieHome);
        	System.out.println(config.toString());
        	ConfigureAgent(config);
        } catch (RosieConfig.RosieConfigException e){
        	System.err.println("Rosie Configuration Error: " + e.getMessage());
        }
    }
}
