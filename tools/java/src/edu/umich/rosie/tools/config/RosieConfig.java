package edu.umich.rosie.tools.config;

import java.io.*;
import java.util.*;

public class RosieConfig {

	// A custom exception thrown if there is an error in the configuration file
	@SuppressWarnings("serial")
	public static class RosieConfigException extends Exception {
		public RosieConfigException(String message){
			super(message);
		}
	}
	
	// agent-dir = <string>  [OPTIONAL] - The directory the agent should be created in
	//    DEFAULT - The directory containing the config file
	public File agentDir = null;

	// agent-name = <string> [REQUIRED] - The name of the agent (used as root of file names)
	//   DEFAULT - The name of the config file (minus extension)
	public String agentName = null;
	
	// source-soar-file = <file> [ANY NUMBER]
	//     Extra soar files that should be sourced by the agent
	//     Can prefix with ${config_dir} or ${rosie_agent}
	public List<File> sourceSoarFiles = new ArrayList<File>();

	// source-smem-file = <file> [ANY NUMBER]
	//     A smem file relative to the config directory that should be sourced by the agent (after being mapped)
	public List<File> sourceSmemFiles = new ArrayList<File>();

	// smem-config-file = <file> [ANY NUMBER]
	//     An smem config file that should be included (can define templates/files to be mapped)
	//     Can prefix with ${config_dir} or ${rosie_agent}
	public List<File> smemConfigFiles = new ArrayList<File>();
	
	// domain = << magicbot tabletop internal fetch ai2thor cozmo >>  [OPTIONAL]
	//    The environment the agent is in (determines perception/action rules)
	public String domain = null;
	public static final HashSet<String> VALID_DOMAINS = new HashSet<String>(
			Arrays.asList("magicbot", "tabletop", "internal", "fetch", "ai2thor", "cozmo"));
	
	// world-file = <filename> [OPTIONAL]
	//    A file with info about the top-state world 
	//    (File used by WorldGenerator)
	public File worldFile = null;

	
	// parser = << laird lucia >>  [OPTIONAL] - The parser the agent should use
	//    DEFAULT - laird
	public String parser = "laird";
	public static final HashSet<String> VALID_PARSERS = new HashSet<String>(
			Arrays.asList("laird", "lucia"));
	
	// parser-test = << true false >>  [OPTIONAL] - Whether to parse in test mode
	//    DEFAULT - false
	public String parser_test = "false";
	public static final HashSet<String> VALID_PARSER_TESTS = new HashSet<String>(
			Arrays.asList("true", "false"));
	
	// hypothetical = << true false >>  [OPTIONAL] - Whether to parse hypothetically in test mode
	//    DEFAULT - false
	public String hypothetical = "false";
	public static final HashSet<String> VALID_HYPOTHETICALS = new HashSet<String>(
			Arrays.asList("true", "false"));
	
	// sentence-source = << chat scripts >>  [OPTIONAL] - Where sentences come from
	//    DEFAULT - chat
	public String sentenceSource = "chat";
	public static final HashSet<String> VALID_SENTENCE_SRCS = new HashSet<String>(
			Arrays.asList("chat", "scripts"));
	
	// sentences-file = <filename> [OPTIONAL] 
	//    A file with a list of sentences
	//    (File used by SentencesGenerator)
	public File sentencesFile = null;


	// Any other properties in the file will be passed on to the created rosie-client.config file
	public HashMap<String, String> clientSettings = new HashMap<String, String>();
	

	// SETTINGS SET DURING CREATION
	
	// rosieAgentDir - the directory containing the rosie agent
	public final File rosieAgentDir;

	// sourceFile - the full path of the file this config was created from
	public final File sourceFile;

	// sourceDir - the directory containing this config file
	public final File sourceDir;
	
	private String cleanPath(String path){
		return path.replaceAll("\\\\", "/");
	}

	public RosieConfig(File configFile, String rosieHome) throws RosieConfigException {
		this.sourceFile = configFile;
		this.sourceDir = configFile.getParentFile();
		this.agentDir = new File(this.sourceDir, "agent");
		this.rosieAgentDir = new File(rosieHome + "/agent");
		validateFile("ROSIE_HOME", this.rosieAgentDir);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			String line;
			int lineNum = 0;
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(line.length() > 0 && line.charAt(0) != '#'){
					String[] args = line.split(" ");
					if(args.length < 3 || !args[1].equals("=")){
						throw new IOException("Error parsing setting on line " + String.valueOf(lineNum) + ":\n" + line);
					}
					handleSetting(args[0], args[2]);
				}
				lineNum += 1;
			}
			reader.close();
		} catch (IOException e){
			throw new RosieConfigException("Error reading the config file:\n" + e);
		}

		this.validate();
	}

	private void handleSetting(String settingName, String settingValue) {
		settingValue = settingValue.replace("${agent-dir}", this.agentDir.getAbsolutePath());
		settingValue = settingValue.replace("${config-dir}", this.sourceDir.getAbsolutePath());
		settingValue = settingValue.replace("${rosie-agent}", this.rosieAgentDir.getAbsolutePath());
		settingValue = cleanPath(settingValue);

		if(settingName.equals("agent-name")){
			this.agentName = settingValue;
		}
		else if(settingName.equals("agent-dir")){
			this.agentDir = new File(this.sourceDir, settingValue);
		}
		else if(settingName.equals("source-soar-file")){
			File soarFile = new File(settingValue);
			if(!soarFile.exists()){
				soarFile = new File(this.sourceDir, settingValue);
			}
			this.sourceSoarFiles.add(soarFile);
		}
		else if(settingName.equals("source-smem-file")){
			File smemFile = new File(settingValue);
			if(!smemFile.exists()){
				smemFile = new File(this.sourceDir, settingValue);
			}
			this.sourceSmemFiles.add(smemFile);
		}
		else if(settingName.equals("smem-config-file")){
			File smemConfigFile = new File(settingValue);
			if(!smemConfigFile.exists()){
				smemConfigFile = new File(this.sourceDir, settingValue);
			}
			this.smemConfigFiles.add(smemConfigFile);
		}
		else if(settingName.equals("domain")){
			this.domain = settingValue.toLowerCase();
		}
		else if(settingName.equals("world-file")){
			this.worldFile = new File(this.sourceDir + "/" + settingValue);
		}
		else if(settingName.equals("parser")){
			this.parser = settingValue.toLowerCase();
		}
		else if(settingName.equals("parser-test")){
			this.parser_test = settingValue.toLowerCase();
		}
		else if(settingName.equals("hypothetical")){
			this.hypothetical = settingValue.toLowerCase();
		}
		else if(settingName.equals("sentence-source")){
			this.sentenceSource = settingValue.toLowerCase();
		}
		else if(settingName.equals("sentences-file")){
			this.sentencesFile = new File(this.sourceDir, settingValue);
		}
		else {
			this.clientSettings.put(settingName, settingValue);
		}

	}

	private void validate() throws RosieConfigException {
		// agentName
		if(this.agentName == null){
			throw new RosieConfigException("agent-name is required");
		}

		// domain
		if(this.domain != null){
			validateOptions("domain", this.domain, VALID_DOMAINS);
		}

		validateOptions("parser", this.parser, VALID_PARSERS);
		validateOptions("parser-test", this.parser_test, VALID_PARSER_TESTS);
		validateOptions("hypothetical", this.hypothetical, VALID_HYPOTHETICALS);
		validateOptions("sentence-source", this.sentenceSource, VALID_SENTENCE_SRCS);

		validateFile("world-file", this.worldFile);
		validateFile("sentences-file", this.sentencesFile);

		for(File f : this.sourceSoarFiles){
			validateFile("source-soar-file", f);
		}
		for(File f : this.sourceSmemFiles){
			validateFile("source-smem-file", f);
		}
		for(File f : this.smemConfigFiles){
			validateFile("smem-config-file", f);
		}

	}

	private void validateOptions(String name, String value, HashSet<String> options) throws RosieConfigException {
		if(!options.contains(value)){
			throw new RosieConfigException(name + " " + value + " is not valid\n" + 
					"Must be one of: " + options.toString());
		}
	}

	private void validateFile(String name, File file) throws RosieConfigException {
		if(file != null && !file.exists()){
			throw new RosieConfigException(name + " file " + file.getName() + " does not exist");
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("agent-name = " + this.agentName + "\n");
		sb.append("agent-dir = " + this.agentDir.getName() + "\n");
		sb.append("domain = " + this.domain + "\n");
		sb.append("parser = " + this.parser + "\n");
		sb.append("parser-test = " + this.parser_test + "\n");
		sb.append("hypothetical = " + this.hypothetical + "\n");
		sb.append("sentence-source = " + this.sentenceSource + "\n");
		sb.append("sentences-file = " + (this.sentencesFile == null ? "None" : this.sentencesFile.getName()) + "\n");
		sb.append("world-file = " + (this.worldFile == null ? "None" : this.worldFile.getName()) + "\n");

		sb.append("\nSourcing the following soar files:\n");
		for(File f : this.sourceSoarFiles){
			sb.append("   > " + f.getAbsolutePath() + "\n");
		}

		sb.append("\nSourcing the following local smem files:\n");
		for(File f : this.sourceSmemFiles){
			sb.append("   > " + f.getAbsolutePath() + "\n");
		}

		sb.append("\nIncluding the following smem config files:\n");
		for(File f : this.smemConfigFiles){
			sb.append("   > " + f.getAbsolutePath() + "\n");
		}

		sb.append("\nOther client settings added to rosie-client.config\n");	
		for(Map.Entry<String, String> e : this.clientSettings.entrySet()){
			sb.append("   > " + e.getKey() + " = " + e.getValue() + "\n");
		}
		
		return sb.toString();
	}
}
