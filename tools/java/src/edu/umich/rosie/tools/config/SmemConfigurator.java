package edu.umich.rosie.tools.config;

import java.io.*;
import java.util.*;

/************************************************************************
 *                      SmemConfigurator
 *
 * A tool that pulls together all the smem files that a rosie agent needs
 * It supports two main features:
 *    String-based LTI's: It will map string lti's to numerical values
 *       (e.g. "@red" becomes "@100023")
 *       This is useful to link smem structures across different files
 *       without hard-coding numbers
 *    Template filling:
 *	     You can specify smem templates that will be filled with different values
 *
 * It defines an smem config file format, which is a text file with the following options:
 *
 * source-file <agent-file>
 *    This tells the agent to source the given file without modifications or copying
 *    It assumes a relative path from $ROSIE_HOME/agent
 * 
 * include-file <agent-file>
 *    This tells the agent to copy the given file to the /smem dir while re-mapping id's
 *    It assumes a relative path from $ROSIE_HOME/agent
 *
 * config-file <config-file>
 *    This will recursively run the configurator using the given file
 *    It assumes a relative path from $ROSIE_HOME/agent
 *
 * template <template_name> _PARAM-1_ _PARAM-2_ ... _PARAM-N_ _PARAM-LIST_*{
 *    value1 value2 ... valueN listVal1 listVal2 ...
 *    // Can have as many lines as desired
 * }
 * This will create smem add commands using the given template (from agent/init-smem/templates)
 *   filled out with 1 copy for each line between the braces
 *   See SmemTemplate.java for more information 
 *
 * *********************************************************************/

public class SmemConfigurator {
	private static final String SMEM_SOURCE_FILE = "smem_source.soar";

	private static HashMap<String, String> ltiMap = new HashMap<String, String>();
	
	// Maps a single word to a unique LTI number starting at 100000
	// (e.g. map @red1 --> @100110)
	public static String mapLTI(String word){
		if(!word.startsWith("@")){
			word = "@" + word;
		}
		if (ltiMap.containsKey(word)){
			return ltiMap.get(word);
		}
		String lti = "@" + new Integer(100000 + ltiMap.size()).toString();
		ltiMap.put(word, lti);
		return lti;
	}

	// Replace all lti references in a string with a numerical equivalent
	// (e.g. map "(@red1 ^property @color1)" --> "(@100110 ^handle @100120)"
	public static String mapLTIs(String text){
		// Split on newlines
		String[] lines = text.split("\\n");
		StringBuilder sb = new StringBuilder();

		// Map each line in the text
		for(String line : lines){
			// Don't map comment lines
			if(line.trim().startsWith("#")){
				sb.append(line + "\n");
				continue;
			}

			String mappedLine = line;
			// Strip parenthesis and split into word
			String[] words = line.replaceAll("[()]", "").split("\\s+");
			for(String word : words){
				// If a word equals @string, replace it with the numerical mapped LTI
				if(word.length() >= 2 && word.startsWith("@") && !Character.isDigit(word.charAt(1))){
					String lti = mapLTI(word);
					mappedLine = mappedLine.replaceAll(word, lti);
				}
			}
			sb.append(mappedLine + "\n");
		}
		
		return sb.toString();
	}	

	// The overall smem configuration script
	// Will read any config files and create new files inside the agent/smem directory, 
	// It will map any string LTI's to numbers (@red -> @100010)
	//    and instantiate specified templates
	// It also creates a root smem source file and returns its name
	public static String configureSmem(RosieConfig config) throws IOException {
		// Create the smem directory
		File smemDir = new File(config.agentDir, "/smem");
		if(!smemDir.exists()){
			smemDir.mkdir();
		}

		File rosieAgentDir = new File(config.rosieHome, "agent");
		List<String> rosieFiles = new ArrayList<String>();
		Map<String, File> createdFiles = new HashMap<String, File>();

		// Parse the default smem config file
		if(config.useDefaultSmemConfig){
			File defaultSmemConfig = new File(config.rosieHome + "/agent/init-smem/default-smem-config.txt");
			parseSmemConfigurationFile(defaultSmemConfig, smemDir, rosieAgentDir, createdFiles, rosieFiles);
		}

		// Parse the custom smem config file
		if(config.smemConfigFile != null){
			parseSmemConfigurationFile(config.smemConfigFile, smemDir, rosieAgentDir, createdFiles, rosieFiles);
		}

		// Copy the custom smem file
		File customSmemFile = new File(config.agentDir + "/" + config.customSmemFile.getName());
		copySmemFile(config.customSmemFile, customSmemFile);
		
		// Create a file that sources any created files in the /smem directory
		writeSmemSubdirFile(smemDir, createdFiles);
		
		String smemSourceFilename = writeSmemSourceFile(config.agentDir, config.rosieHome, rosieFiles, customSmemFile);
		return smemSourceFilename;
	}

	// Given a source file and an output file, 
	// copy the source to the output, 
	// re-mapping LTI's to their integer versions
	public static void copySmemFile(File sourceFile, File outputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		Writer writer = new BufferedWriter(new FileWriter(outputFile));
		
		String line;
		while((line = reader.readLine()) != null){
			writer.write(mapLTIs(line));
		}
		reader.close();
		writer.close();
	}


	//------------------------------------------- PRIVATE -----------------------------------------------//
	

	// Writes the root soar file that will source all smem knowledge needed by the agent
	//   Returns the name of the created file
	private static String writeSmemSourceFile(String agentDir, String rosieHome, 
				List<String> rosieFiles, File customSmemFile) throws IOException {

		String smemSourceFilename = agentDir + "/" + SMEM_SOURCE_FILE;
		Writer sourceWriter = new BufferedWriter(new FileWriter(smemSourceFilename));

		// Preamble
		sourceWriter.write("# This file was autogenerated by the SmemConfiguration\n");
		sourceWriter.write("# This file will source different files that will initialize the agent's semantic memory\n\n");

		// Source any rosie files 
		sourceWriter.write("# Sourcing smem files in Rosie agent directory\n");
		sourceWriter.write("pushd " + rosieHome + "/agent\n");
		for(String filename : rosieFiles){
			sourceWriter.write("   source " + filename + "\n");
		}
		sourceWriter.append("popd\n\n");

		// Source the customSmemFile, if it exists
		if (customSmemFile != null && customSmemFile.exists()){
			sourceWriter.write("# Sourcing custom smem information specific to this agent\n");
			sourceWriter.write("source " + customSmemFile.getAbsolutePath().replaceAll("\\\\", "/") + "\n\n");
		}

		// Source the smem directory and all created files inside
		sourceWriter.write("# This file will source the smem files that were created by the SmemConfiguator tool\n");
		sourceWriter.write("pushd " + agentDir + "/smem\n");
		sourceWriter.write("source " + SMEM_SOURCE_FILE + "\n");
		sourceWriter.write("popd\n\n");

		sourceWriter.close();

		return smemSourceFilename;
	}

	// Writes a soar file that sources all the smem files created in the agent/smem directory
	private static void writeSmemSubdirFile(File smemDir, Map<String, File> createdFiles) throws IOException {
		File sourceFile = new File(smemDir, SMEM_SOURCE_FILE);
		Writer sourceWriter = new BufferedWriter(new FileWriter(sourceFile));

		// Preamble
		sourceWriter.write("# This file was autogenerated by the SmemConfiguration tool\n");
		sourceWriter.write("# This file will source any files created in the agent/smem directory\n\n");

		// Source each created file
		for(File f : createdFiles.values()){
			sourceWriter.write("source " + f.getName() + "\n");
		}

		sourceWriter.close();
	}

	// Parses an smem configuration file, 
	// createdFiles is a map to files created in the smem directory, it will add more as needed
	// rosieFiles is a list of rosie agent files to source (relative to rosie/agent dir)
	private static void parseSmemConfigurationFile(File configFile, File smemDir, File rosieAgentDir, Map<String, File> createdFiles, List<String> rosieFiles) {
		if(configFile == null || !configFile.exists()){
			return;
		}

		BufferedReader reader;
		try{
		// Smem config file reader
		reader = new BufferedReader(new FileReader(configFile));
		File templateDir = new File(rosieAgentDir, "/init-smem/templates");

		// Loop through each line in the file
		String line;
		while((line = reader.readLine()) != null){
			line = line.trim();
			// Ignore lines starting with #
			if(line.startsWith("#")){
				continue;
			}
			String[] args = line.split(" ");
			
			try {
				// Create items to load into smem using a template file (in init-smem/templates)
				// template <template_name> _PARAM-1_ _PARAM-2_ ... _PARAM-N_ _PARAM-LIST_*{
				//    value1 value2 ... valueN listVal1 ...
				// }
				if(line.startsWith("template") && args.length > 1){
					File outputFile = new File(smemDir, "/" + args[1] + ".soar");
					File templateFile = new File(templateDir, "/" + args[1] + ".txt");
					SmemTemplate template = new SmemTemplate(args, templateFile);
					writeTemplateFile(template, reader, outputFile, createdFiles);
				}
				// Source the given file (without configuring)
				// source-file <path-from-agent-dir>
				else if (line.startsWith("source-file") && args.length > 1){
					rosieFiles.add(args[1]);
				}
				// Specifies another configuration file to parse (relative to agent directory)
				//   Hooray recursion!
				// config-file <filename>
				else if (line.startsWith("config-file") && args.length > 1){
					File subConfigFile = new File(rosieAgentDir, args[1]);
					parseSmemConfigurationFile(subConfigFile, smemDir, rosieAgentDir, createdFiles, rosieFiles);
				}
				// Include a semantic memory file to source, while replacing handle lti's (@handle1)
				// include-file <path-from-agent-dir>
				else if (line.startsWith("include-file") && args.length > 1){
					String filename = args[1];
					File sourceFile = new File(rosieAgentDir, filename);
					File outputFile = new File(smemDir, "/" + sourceFile.getName());
					if(sourceFile.exists()){
						copySmemFile(sourceFile, outputFile);
						createdFiles.put(outputFile.getName(), outputFile);
					}
				}
			} catch (IOException e){
				System.err.println("ERROR in " + line);
				System.err.println(e.getMessage());
			}
		}	
		
		} catch (IOException e){
			System.err.println("Could not read smem config file: " + configFile.getName());
		}
	}

	/* Given a template file specification and an open file,
	 * read line by line until a closing brace is encountered, 
	 * each line serves as information to use to fill out a copy of the template */
	private static void writeTemplateFile(SmemTemplate template, BufferedReader reader, File outputFile, Map<String, File> createdFiles){
		// If we have created this file before, append instead of writing over
		boolean append = createdFiles.containsKey(outputFile.getAbsolutePath());
		try {
			Writer outputWriter = new BufferedWriter(new FileWriter(outputFile, append));
			if(!append){
				outputWriter.write("### AUTOGENERATED smem file for the " + template.name + " template ###\n");
			}
			outputWriter.write("smem --add {\n");
		
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(line.length() == 0 || line.startsWith("#")){
					continue;
				}
				if(line.startsWith("}")){
					break;
				}
				
				// fill out the template using the next line
				String filledTemplate = template.fillFromLine(line);
				filledTemplate = mapLTIs(filledTemplate);
				outputWriter.write(filledTemplate);
				outputWriter.write("\n");
			}
			
			outputWriter.write("}\n");
			outputWriter.close();
		} catch (Exception e){
			System.err.println("ERROR reading template file " + template.name);
			System.err.println(e.getMessage());
		}
		if(!append){
			createdFiles.put(outputFile.getAbsolutePath(), outputFile);
		}
	}
}
