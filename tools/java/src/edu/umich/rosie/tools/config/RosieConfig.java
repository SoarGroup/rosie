package edu.umich.rosie.tools.config;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

public class RosieConfig {

	// A custom exception thrown if there is an error in the configuration file
	@SuppressWarnings("serial")
	public static class RosieConfigException extends Exception {
		public RosieConfigException(String message){
			super(message);
		}
	}

	// A set of property names used in the config file
	public static final HashSet<String> PROP_NAMES = new HashSet<String>(
			Arrays.asList("agent-name", "agent-dir", "domain",
					"parser", "parser-test", "hypothetical",
					"simulate-perception",
					"sentence-source", "sentences-file", "world-file", "smem-config-file", 
					"custom-soar-file", "custom-smem-file", 
					"map-info-file", "object-info-file", "internal-world-file", "waypoint-map-file"));
	
	// agent-name = <string> [OPTIONAL] - The name of the agent (used as root of file names)
	//   DEFAULT - The name of the config file (minus extension)
	public String agentName;
	
	// agent-dir = <string>  [OPTIONAL] - The directory the agent should be created in
	//    DEFAULT - The directory containing the config file
	public String agentDir;
	
	// domain = << magicbot tabletop internal fetch ai2thor cozmo >>  [REQUIRED]
	//    The environment the agent is in (determines perception/action rules)
	public String domain;
	public static final HashSet<String> VALID_DOMAINS = new HashSet<String>(
			Arrays.asList("magicbot", "tabletop", "internal", "fetch", "ai2thor", "cozmo"));

	// simulate-perception << true false >> [OPTIONAL]
	//    Only relevant for domain=internal, whether more detailed perception is simulated
	public Boolean simulate_perception;
	
	// parser = << laird lucia >>  [OPTIONAL] - The parser the agent should use
	//    DEFAULT - laird
	public String parser;
	public static final String DEFAULT_PARSER = "laird";
	public static final HashSet<String> VALID_PARSERS = new HashSet<String>(
			Arrays.asList("laird", "lucia"));
	
	// parser-test = << true false >>  [OPTIONAL] - Whether to parse in test mode
	//    DEFAULT - false
	public String parser_test;
	public static final String DEFAULT_PARSER_TEST = "false";
	public static final HashSet<String> VALID_PARSER_TESTS = new HashSet<String>(
			Arrays.asList("true", "false"));
	
	// hypothetical = << true false >>  [OPTIONAL] - Whether to parse hypothetically in test mode
	//    DEFAULT - false
	public String hypothetical;
	public static final String DEFAULT_HYPOTHETICAL = "false";
	public static final HashSet<String> VALID_HYPOTHETICALS = new HashSet<String>(
			Arrays.asList("true", "false"));
	
	// sentence-source = << chat scripts >>  [OPTIONAL] - Where sentences come from
	//    DEFAULT - chat
	public String sentenceSource;
	public static final String DEFAULT_SENTENCE_SRC = "chat";
	public static final HashSet<String> VALID_SENTENCE_SRCS = new HashSet<String>(
			Arrays.asList("chat", "scripts"));
	
	// sentences-file = <filename> [OPTIONAL] 
	//    A file with a list of sentences
	//    (File used by SentencesGenerator)
	public File sentencesFile;
	
	// world-file = <filename> [OPTIONAL]
	//    A file with info about the top-state world 
	//    (File used by WorldGenerator)
	public File worldFile;

	// smem-config-file = <filename> [OPTIONAL]
	//     A file with info about what concepts to include from smem
	//    (File used by SmemConfigurator)
	public File smemConfigFile;
	
	// custom-soar-file = <filename> [OPTIONAL]
	//     A file containing soar code that will be sourced by the agent
	public File customSoarFile;

	// custom-smem-file = <filename> [OPTIONAL]
	//     A file containing smem adds that will be sourced by the agent
	public File customSmemFile;

	// map-info-file = <filename> [OPTIONAL]
	//     A file containing information about the map the agent is driving in (room regions)
	public File mapInfoFile;

	// object-info-file = <filename> [OPTIONAL]
	//     A file containing information used to simulate or annotate object properties 
	public File objectInfoFile;

	// internal-world-file = <filename> [OPTIONAL]
	//     A file inside agent/manage-world-state/world/internal-worlds used to initialize the top-state world
	public File internalWorldFile;

	// waypoint-map-file = <filename> [OPTIONAL]
	//     A file inside agent/manage-world-state/world/maps which defines a waypoint map used for navigation
	public File waypointMapFile;

	// Any other properties in the file will be put into the created rosie.config file
	public HashMap<String, String> otherSettings;
	
	// rosieHome - the directory containing rosie
	public String rosieHome;
	
	public RosieConfig(File configFile, Properties props, String rosieHome) throws RosieConfigException {
		String configDir = configFile.getParent();
		System.out.println(configDir);
		
		// rosieHome
		this.rosieHome = rosieHome; 

		// agent-name
		if (props.containsKey("agent-name")){
			this.agentName = props.getProperty("agent-name");
		} else {
			this.agentName = configFile.getName();
			if (this.agentName.lastIndexOf(".") > 0){
				this.agentName = this.agentName.substring(0, this.agentName.lastIndexOf("."));
			}
		}
		
		// agent-dir
		if (props.containsKey("agent-dir")){
			this.agentDir = props.getProperty("agent-dir").replaceAll("\\\\", "/");
		} else {
			this.agentDir = configFile.getParent() + "/agent";
			this.agentDir = this.agentDir.replaceAll("\\\\", "/");
		}
		
		// domain
		if (props.containsKey("domain")){
			this.domain = props.getProperty("domain").toLowerCase();
			if(!VALID_DOMAINS.contains(this.domain)){
				throw new RosieConfigException("domain " + this.domain + " is not valid\n" + 
						"Must be one of: " + VALID_DOMAINS.toString());
			}
		} else {
			throw new RosieConfigException("Missing domain parameter");
		}

		// simulate-perception
		System.out.println(this.domain);
		if (this.domain.equals("internal")){
			System.out.println("simulate-perception");
			System.out.println(props.getProperty("simulate-perception"));
			this.simulate_perception = props.getProperty("simulate-perception", "false").toLowerCase().equals("true");
		} else {
			this.simulate_perception = false;
		}
		
		// parser
		if (props.containsKey("parser")){
			this.parser = props.getProperty("parser").toLowerCase();
			if(!VALID_PARSERS.contains(this.parser)){
				throw new RosieConfigException("parser " + this.parser + " is not valid\n" + 
						"Must be one of: " + VALID_PARSERS.toString());
			}
		} else {
			parser = DEFAULT_PARSER;
		}
		
		// parser-test
		if (props.containsKey("parser-test")){
			this.parser_test = props.getProperty("parser-test").toLowerCase();
			if(!VALID_PARSER_TESTS.contains(this.parser_test)){
				throw new RosieConfigException("parser_test " + this.parser_test + " is not valid\n" + 
						"Must be one of: " + VALID_PARSER_TESTS.toString());
			}
		} else {
			parser_test = DEFAULT_PARSER_TEST;
		}
		
		// hypothetical
		if (props.containsKey("hypothetical")){
			this.hypothetical = props.getProperty("hypothetical").toLowerCase();
			if(!VALID_HYPOTHETICALS.contains(this.hypothetical)){
				throw new RosieConfigException("hypothetical " + this.hypothetical + " is not valid\n" + 
						"Must be one of: " + VALID_HYPOTHETICALS.toString());
			}
		} else {
			hypothetical = DEFAULT_HYPOTHETICAL;
		}
		
		// sentence-source
		if (props.containsKey("sentence-source")){
			this.sentenceSource = props.getProperty("sentence-source").toLowerCase();
			if(!VALID_SENTENCE_SRCS.contains(this.sentenceSource)){
				throw new RosieConfigException("sentence-source " + this.sentenceSource + " is not valid\n" + 
						"Must be one of: " + VALID_SENTENCE_SRCS.toString());
			}
		} else {
			this.sentenceSource = DEFAULT_SENTENCE_SRC;
		}
		
		// sentences-file
		if (props.containsKey("sentences-file")){
			this.sentencesFile = new File(configDir + "/" + props.getProperty("sentences-file"));
		} else {
			this.sentencesFile = null;
		}
		
		// world-file
		if (props.containsKey("world-file")){
			this.worldFile = new File(configDir + "/" + props.getProperty("world-file"));
		} else {
			this.worldFile = null;
		}
		
		// smem-config-file
		if (props.containsKey("smem-config-file")){
			this.smemConfigFile = new File(configDir + "/" + props.getProperty("smem-config-file"));
		} else {
			this.smemConfigFile = null;
		}
		
		// custom-soar-file
		if (props.containsKey("custom-soar-file")){
			this.customSoarFile = new File(configDir + "/" + props.getProperty("custom-soar-file"));
		} else {
			this.customSoarFile = null;
		}
		
		// custom-smem-file
		if (props.containsKey("custom-smem-file")){
			this.customSmemFile = new File(configDir + "/" + props.getProperty("custom-smem-file"));
		} else {
			this.customSmemFile = null;
		}
		
		// map-info-file
		if (props.containsKey("map-info-file")){
			this.mapInfoFile = new File(configDir + "/" + props.getProperty("map-info-file"));
		} else {
			this.mapInfoFile = null;
		}
		
		// object-info-file
		if (props.containsKey("object-info-file")){
			this.objectInfoFile = new File(configDir + "/" + props.getProperty("object-info-file"));
		} else {
			this.objectInfoFile = null;
		}
		
		// internal-world-file
		if (props.containsKey("internal-world-file")){
			this.internalWorldFile = new File(this.rosieHome + "/agent/manage-world-state/simulate-perception/internal-worlds/" + props.getProperty("internal-world-file"));
		} else {
			this.internalWorldFile = null;
		}
		
		// waypoint-map-file
		if (props.containsKey("waypoint-map-file")){
			this.waypointMapFile = new File(this.rosieHome + "/agent/manage-world-state/world/maps/" + props.getProperty("waypoint-map-file"));
		} else {
			this.waypointMapFile = null;
		}
		
		// otherSettings
		// Anything else in the config file will be 
		this.otherSettings = new HashMap<String, String>();
		for(Map.Entry<Object, Object> e : props.entrySet()){
			String name = (String)e.getKey();
			String val = (String)e.getValue();
			if(!PROP_NAMES.contains(name)){
				this.otherSettings.put(name, val);
			}
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("agent-name = " + this.agentName + "\n");
		sb.append("agent-dir = " + this.agentDir + "\n");
		sb.append("rosie-home = " + this.rosieHome + "\n");
		sb.append("domain = " + this.domain + "\n");
		sb.append("simulate-perception = " + new Boolean(this.simulate_perception).toString() + "\n");
		sb.append("parser = " + this.parser + "\n");
		sb.append("parser-test = " + this.parser_test + "\n");
		sb.append("hypothetical = " + this.hypothetical + "\n");
		sb.append("sentence-source = " + this.sentenceSource + "\n");

		if(this.sentencesFile != null){
			sb.append("sentences-file = " + this.sentencesFile.getName() + "\n");
			if(!this.sentencesFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("sentences-file = None\n");
		}

		if(this.worldFile != null){
			sb.append("world-file = " + this.worldFile.getName() + "\n");
			if(!this.worldFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("world-file = None\n");
		}

		if(this.smemConfigFile != null){
			sb.append("smem-config-file = " + this.smemConfigFile.getName() + "\n");
			if(!this.smemConfigFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("smem-config-file = None\n");
		}

		if(this.customSoarFile != null){
			sb.append("custom-soar-file = " + this.customSoarFile.getName() + "\n");
			if(!this.customSoarFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("custom-soar-file = None\n");
		}

		if(this.customSmemFile != null){
			sb.append("custom-smem-file = " + this.customSmemFile.getName() + "\n");
			if(!this.customSmemFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("custom-smem-file = None\n");
		}

		if(this.mapInfoFile != null){
			sb.append("map-info-file = " + this.mapInfoFile.getName() + "\n");
			if(!this.mapInfoFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("map-info-file = None\n");
		}

		if(this.objectInfoFile != null){
			sb.append("object-info-file = " + this.objectInfoFile.getName() + "\n");
			if(!this.objectInfoFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("object-info-file = None\n");
		}

		if(this.internalWorldFile != null){
			sb.append("internal-world-file = " + this.internalWorldFile.getName() + "\n");
			if(!this.internalWorldFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("internal-world-file = None\n");
		}

		if(this.waypointMapFile != null){
			sb.append("waypoint-map-file = " + this.waypointMapFile.getName() + "\n");
			if(!this.waypointMapFile.exists()){
				sb.append("  !!! File does not exist !!!\n");
			}
		} else {
			sb.append("waypoint-map-file = None\n");
		}
		
		
		for(Map.Entry<String, String> e : this.otherSettings.entrySet()){
			sb.append(e.getKey() + " = " + e.getValue() + "\n");
		}
		
		return sb.toString();
	}
}
