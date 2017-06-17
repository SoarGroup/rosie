package edu.umich.rosie.tools.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SmemConfigurator {
	private HashMap<String, String> ltiMap; 
	private ArrayList<String> filesToInclude;
	private HashSet<String> blockList;
	private String blockListType;
	private File smemDir;

	public SmemConfigurator(File outputDir) {
		ltiMap = new HashMap<String, String>();
		filesToInclude = new ArrayList<String>();
		blockList = new HashSet<String>();
		blockListType = "exclude-list";

		smemDir = new File(outputDir, "/smem");
		if(!smemDir.exists()){
			smemDir.mkdir();
		}
	}
	
	public File configure(File configFile, File agentDir) throws IOException {
		// File that will source all the soar files inside the smem directory
		File smemSourceFile = new File(smemDir, "/smem_source.soar");
		Writer sourceWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(smemDir, "/smem_source.soar"))));
		
		// Read the smem config file
		if(configFile != null && configFile.exists()){
			ArrayList<File> createdFiles = parseSmemConfigurationFile(configFile, agentDir, smemDir);
			for(File f : createdFiles){
				sourceWriter.write("source " + f.getName() + "\n\n");
			}
		}
		
		// Read Language Comprehension words
		File languageFile = new File(agentDir, "/language-comprehension/smem-words/smem-words_to_process.soar");
		if(languageFile.exists()){
			ArrayList<File> createdFiles = parseLanguageSourceFile(languageFile, agentDir, smemDir);
			for(File f : createdFiles){
				sourceWriter.write("source " + f.getName() + "\n\n");
			}
		}
		
		sourceWriter.close();
		
		return smemSourceFile;
	}
	
	public String mapLTIs(String line){
		// Replace all lti references in a string with a numerical equivalent
		// (e.g. map @red1 --> @100110)
		
		// Don't map comment lines
		if(line.trim().startsWith("#")){
			return line;
		}
		
		String[] words = line.split("\\s+");
		for(int i = 0; i < words.length; i++){
			String word = words[i].replace("(", "").replace(")", "");
			if(word.length() >= 2 && word.startsWith("@") && !Character.isDigit(word.charAt(1))){
				String lti;
				if (ltiMap.containsKey(word)){
					lti = ltiMap.get(word);
				} else {
					lti = "@" + new Integer(100000 + ltiMap.size()).toString();
					ltiMap.put(word, lti);
				}
				words[i] = lti;
				line = line.replace(word + " ", lti + " ");
				line = line.replace(word + ")", lti + ")");
				if(line.endsWith(word)){
					line = line.substring(0, line.length() - word.length()) + lti;
				}
			}
		}
		return line;
	}	
	
	private ArrayList<File> parseSmemConfigurationFile(File configFile, File agentDir, File smemDir) {
		ArrayList<File> createdFiles = new ArrayList<File>();
		
		BufferedReader configReader;
		try{

		// Smem config file reader
		configReader = new BufferedReader(new FileReader(configFile));

		File templateDir = new File(agentDir, "/init-smem/templates");
		
		String line;
		while((line = configReader.readLine()) != null){
			line = line.trim();
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
					writeTemplateFile(template, configReader, outputFile);
					createdFiles.add(outputFile);
				}
				// Include a semantic memory file to source, while replacing handle lti's (@handle1)
				// include-file <path-from-agent-dir>
				else if (line.startsWith("include-file") && args.length > 1){
					String filename = args[1];
					filesToInclude.add(filename);
				}
				// Creates a list of tagged blocks (smem --add commands) 
				//    That you want to filter
				//    include-list: only include smem blocks listed below
				//    exclude-list: include all smem blocks except those listed
				// smem-block-list << include-list exclude-list >> {
				//    block-name1
				//    block-name2
				//    ...
				// }
				else if(line.startsWith("smem-block-list")){
					if(args.length == 1 || !(args[1].equals("include-list") || args[1].equals("exclude-list"))){
						System.err.println("smem-block-list must have type include-list or exclude-list");
					} else {
						blockListType = args[1];
					}
					while((line = configReader.readLine()) != null){
						String trimmedLine = line.trim();
						if (trimmedLine.equals("}")) break;
						if (!trimmedLine.startsWith("#")){
							blockList.add(trimmedLine);
						}
					}
				}
			} catch (IOException e){
				System.err.println("ERROR in " + line);
				System.err.println(e.getMessage());
			}
		}	
		
		for(String filename : filesToInclude){
			File sourceFile = new File(agentDir, filename);
			File outputFile = new File(smemDir, "/" + sourceFile.getName());
			writeSmemFile(sourceFile, outputFile);
			createdFiles.add(outputFile);
		}
		} catch (IOException e){
			System.err.println("Could not read smem config file: " + configFile.getName());
		}
		return createdFiles;
	}

	private ArrayList<File> parseLanguageSourceFile(File languageFile, File agentDir, File smemDir) {
		ArrayList<File> createdFiles = new ArrayList<File>();
		
		try{
			BufferedReader smemWordsReader = new BufferedReader(new FileReader(languageFile));
					
			String line;
			while((line = smemWordsReader.readLine()) != null){
				line = line.trim();
				String[] args = line.split(" ");
				
				try {
					if(line.startsWith("source") && args.length > 1){
						String filename = args[1];
						File sourceFile = new File(agentDir, "/language-comprehension/smem-words/" + filename);
						File outputFile = new File(smemDir, "/" + sourceFile.getName());
						writeSmemFile(sourceFile, outputFile);
						createdFiles.add(outputFile);
					}
				} catch (IOException e){
					System.err.println("ERROR in loading file from smem-words_to_process.soar: " + line);
					System.err.println(e.getMessage());
				}
			}
			
			smemWordsReader.close();
		}catch (IOException e){
			System.err.println("Error reading " + languageFile.getName());
		}
		
		return createdFiles;
	}
	
	private void writeTemplateFile(SmemTemplate template, BufferedReader configReader, File outputFile){
		try {
			Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			outputWriter.write("### AUTOGENERATED smem file for the " + template.name + " template ###\n");
			outputWriter.write("smem --add {\n");
		
			String line;
			while((line = configReader.readLine()) != null){
				line = line.trim();
				if(line.length() == 0 || line.startsWith("#")){
					continue;
				}
				if(line.startsWith("}")){
					break;
				}
				
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
	}

	public void writeSmemFile(File sourceFile, File outputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
		
		boolean write = true;
		
		String line;
		while((line = reader.readLine()) != null){
			if(line.trim().startsWith("#") && line.contains("+@")){
				int i = line.indexOf("+@");
				String blockName = line.substring(i+2);
				if(blockName.contains(" ")){
					blockName = blockName.substring(0, blockName.indexOf(" "));
				}
				if(blockListType.equals("include-list") && !blockList.contains(blockName)){
					write = false;
				} else if(blockListType.equals("exclude-list") && blockList.contains(blockName)){
					write = false;
				}
			}
			if(write){
				writer.write(mapLTIs(line) + "\n");
			}
			if(line.trim().startsWith("}")){
				write = true;
			}
		}
		reader.close();
		writer.close();
	}
	
    public static void main(String[] args) {
    	if (args.length < 2){
    		System.err.println("SmemConfigurator requires 2 argument:\n" + 
    							"  1: The input filename with the smem configuration information\n" + 
    							"  2: The name to use when creating files (e.g. smem_actions_<name>.soar\n" + 
    							"  3 [OPT]: The root rosie agent directory (Defaults to $ROSIE_HOME)\n");
    		System.exit(1);
    	}
    	
    	File inputFile = new File(args[0]);
    	String agentName = args[1];
    	
    	File rosieDir = null;
    	if (args.length >= 3){
    		rosieDir = new File(args[2]);
    	} else {
    		String rosieHome = System.getenv("ROSIE_HOME");
    		if (rosieHome == null){
    			System.err.println("$ROSIE_HOME environment variable is not set");
    			System.exit(1);
    		}
    		rosieDir = new File(rosieHome);
    	}
    	
    	File outputDir = new File(System.getProperty("user.dir"));
    	
    	try {
    		SmemConfigurator smem = new SmemConfigurator(outputDir);;
    		smem.configure(inputFile, rosieDir);
    	} catch (Exception e){
    		System.err.println("ERROR in SmemConfigurator.configureSmem");
    		System.err.println(e.getMessage());
    	}
    }
}
