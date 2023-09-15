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
 * pushd <dir>
 * popd
 *    These work like soar's usage of pushd/popd, 
 *      where it maintains a directory stack relative to $ROSIE_HOME/agent
 *    directories can be pushed and popped to source files relative to them
 *
 * source <agent-file>
 *    This tells the agent to source the given file without modifications or copying
 *    It assumes a relative path using the directory stack starting from $ROSIE_HOME/agent
 * 
 * process-file <smem-file>
 *    This tells the agent to copy the given file to the /smem dir while re-mapping id's
 *    It assumes a relative path using the directory stack starting from $ROSIE_HOME/agent
 *
 * include-config <config-file>
 *    This will recursively run the configurator using the given file
 *    It assumes a relative path using the directory stack starting from $ROSIE_HOME/agent
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
		String lti = "@" + String.valueOf(100000 + ltiMap.size());
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
	// It also creates a root smem source file and returns it
	public static File configureSmem(RosieConfig config) throws IOException {
		// Create the smem directory
		File outputDir = config.agentDir;
		File smemDir = new File(outputDir, "/smem");
		if(!smemDir.exists()){
			smemDir.mkdir();
		}

		File templateDir = new File(config.rosieAgentDir, "/init-smem/templates");
		Stack<File> dirStack = new Stack<File>();
		List<File> rosieFiles = new ArrayList<File>();
		Map<String, File> createdFiles = new HashMap<String, File>();

		// Parse any the custom smem config file
		for(File smemConfigFile : config.smemConfigFiles){
			dirStack.push(new File(smemConfigFile.getParent()));
			parseSmemConfigurationFile(smemConfigFile, smemDir, templateDir, dirStack, createdFiles, rosieFiles);
			dirStack.pop();
		}

		// Add any other smem files
		for(File smemFile : config.sourceSmemFiles){
			File outFile = new File(outputDir, smemFile.getName());
			copySmemFile(smemFile, outFile);
		}

		// Create a file that sources all smem files needed by the agent
		File sourceFile = writeSmemSourceFile(outputDir, config.rosieAgentDir, config.sourceSmemFiles, rosieFiles, createdFiles);
		return sourceFile;
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
	
	// Will get the full absoulte path the given file, with windows \ replaced by unix /
	private static String getFullPath(File f){
		return f.getAbsolutePath().replaceAll("\\\\", "/");
	}

	// Writes the root soar file that will source all smem knowledge needed by the agent
	//   Returns the name of the created file
	private static File writeSmemSourceFile(File outputDir, File rosieAgentDir, 
				List<File> smemFiles, List<File> rosieFiles, Map<String, File> createdFiles) throws IOException {

		File sourceFile = new File(outputDir, SMEM_SOURCE_FILE);
		Writer sourceWriter = new BufferedWriter(new FileWriter(sourceFile));

		// Preamble
		sourceWriter.write("# This file was autogenerated by the SmemConfiguration\n");
		sourceWriter.write("# This file will source different files that will initialize the agent's semantic memory\n\n");


		// Source any smem files that were copied to the agent directory
		sourceWriter.write("# Source any source-smem-file files that were copied by the SmemConfiguator tool\n");
		sourceWriter.write("pushd " + getFullPath(outputDir) + "\n");
		for(File f : smemFiles){
			sourceWriter.write("   source " + f.getName() + "\n");
		}
		sourceWriter.write("popd\n\n");

		// Source any rosie files 
		sourceWriter.write("# Sourcing smem files in Rosie agent directory\n");
		sourceWriter.write("pushd " + getFullPath(rosieAgentDir) + "\n");
		for(File file : rosieFiles){
			if(!file.exists()){
				System.err.println("WARNING! SmemConfigurator was told to source the following file but it doesn't exist:");
				System.err.println(file.getAbsolutePath());
			} else {
				String relPath = file.getAbsolutePath().replace(rosieAgentDir.getAbsolutePath(), "");
				relPath = relPath.replaceAll("\\\\", "/");
				if(relPath.charAt(0) == '/'){
					relPath = relPath.substring(1);
				}
				sourceWriter.write("   source " + relPath + "\n");
			}
		}
		sourceWriter.append("popd\n\n");

		// Source the smem directory and all created files inside
		sourceWriter.write("# Source the smem files that were created by the SmemConfiguator tool\n");
		sourceWriter.write("pushd " + getFullPath(outputDir) + "\n");
		for(File f : createdFiles.values()){
			sourceWriter.write("   source smem/" + f.getName() + "\n");
		}
		sourceWriter.write("popd\n\n");

		sourceWriter.close();

		return sourceFile;
	}

	// Parses an smem configuration file, 
	// createdFiles is a map to files created in the smem directory, it will add more as needed
	// rosieFiles is a list of rosie agent files to source (relative to rosie/agent dir)
	private static void parseSmemConfigurationFile(File configFile, File smemDir, File templateDir, Stack<File> dirStack, 
			Map<String, File> createdFiles, List<File> rosieFiles) {
		if(configFile == null || !configFile.exists()){
			System.out.println("ERROR: File " + configFile.getName() + " does not exist!");
			return;
		}

		BufferedReader reader;
		try{
		// Smem config file reader
		reader = new BufferedReader(new FileReader(configFile));

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
				// Push a directory onto the current directory stack, relative to the agent home directory
				// pushd <relative-dir>
				else if(line.startsWith("pushd") && args.length > 1){
					dirStack.push(new File(dirStack.peek(), args[1]));
				}
				// Pops a directory from the current directory stack
				// popd
				else if(line.startsWith("popd")){
					if(dirStack.size() > 1){
						dirStack.pop();
					}
				}
				// Source the given file (without configuring)
				// source <path-from-agent-dir>
				else if (line.startsWith("source") && args.length > 1){
					rosieFiles.add(new File(dirStack.peek(), args[1]));
				}
				// Specifies another configuration file to parse (relative to agent directory)
				//   Hooray recursion!
				// include-config <filename>
				else if (line.startsWith("include-config") && args.length > 1){
					File subConfigFile = new File(dirStack.peek(), args[1]);
					parseSmemConfigurationFile(subConfigFile, smemDir, templateDir, dirStack, createdFiles, rosieFiles);
				}
				// Include a semantic memory file to source, while replacing handle lti's (@handle1)
				// process-file <path-from-agent-dir>
				else if (line.startsWith("process-file") && args.length > 1){
					File sourceFile = new File(dirStack.peek(), args[1]);
					File outputFile = new File(smemDir, "/" + sourceFile.getName());
					if(sourceFile.exists()){
						copySmemFile(sourceFile, outputFile);
						createdFiles.put(outputFile.getAbsolutePath(), outputFile);
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
