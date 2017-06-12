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
import java.util.Map;

public class SmemConfigurator {
	static class Template {
		String name;
		ArrayList<String> compoundPart = new ArrayList<String>();
		ArrayList<String> singularPart = new ArrayList<String>();
		ArrayList<String> paramNames = new ArrayList<String>();
		String listParamName = null;
		
		public Template(String[] spec, File templateFile) throws FileNotFoundException, IOException {
			this.name = spec[1];
			
			for(int i = 2; i < spec.length - 1; i++){
				if(spec[i].contains("*")){
					listParamName = spec[i].replace("*", "");
					break;
				} else {
					paramNames.add(spec[i]);
				}
			}
			
			if(!templateFile.exists()){
				throw new FileNotFoundException(templateFile.getAbsolutePath());
			}

			BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));
			
			String line;
			Boolean hitSingular = false;
			while((line = templateReader.readLine()) != null){
				if(line.trim().toUpperCase().equals("#SINGULAR")){
					hitSingular = true;
				} else if(hitSingular){
					singularPart.add(line);
				} else {
					compoundPart.add(line);
				}
			}

			templateReader.close();
		}
		
		public String fillFromLine(String line){
			String filledLines = "";
			
			String[] values = line.split(" ");
			if(values.length < paramNames.size()){
				System.err.println("Error in template " + name + ": not enough values in line '" + line + "'");
				return filledLines;
			}

			if(listParamName != null){
				for(int i = paramNames.size(); i < values.length; i++){
					String listValue = values[i];
					for(String templateLine : compoundPart){
						String strippedValue = listValue.replaceAll("'", "");
						// Remove bad characters (like apostrophes)
						// but if replacing something inside Soar quotes, keep the characters in
						String filledLine = templateLine.replaceAll("\\|" + listParamName + "\\|", "|" + listValue + "|");
						filledLine = filledLine.replaceAll(listParamName, strippedValue);
						filledLines += "  " + filledLine + "\n";
					}
				}
			}
			
			for(String templateLine : singularPart){
				filledLines += "  " + templateLine + "\n";
			}
			
			for(int i = 0; i < paramNames.size(); i++){
				filledLines = filledLines.replaceAll(paramNames.get(i), values[i]);
			}
			
			return filledLines;
		}
	}
	
	
	public static HashMap<String, String> ltiMap = new HashMap<String, String>();
	
	public static File configureSmem(File inputFile, File outputDir, File agentDir, String agentName) throws IOException {
		File smemDir = new File(outputDir, "/smem");
		if(!smemDir.exists()){
			smemDir.mkdir();
		}

		File templateDir = new File(agentDir, "/init-smem/templates");

		// File that will source all the soar files inside the smem directory
		File smemSourceFile = new File(smemDir, "/smem_source.soar");
		Writer sourceWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(smemDir, "/smem_source.soar"))));

		// Smem config file reader
		BufferedReader configReader = new BufferedReader(new FileReader(inputFile));
		
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
					Template template = new Template(args, templateFile);
					writeTemplateFile(template, configReader, outputFile);
					sourceWriter.write("source " + outputFile.getName() + "\n\n");
				}
				// Include a semantic memory file to source, while replacing handle lti's (@handle1)
				// include-file <path-from-agent-dir>
				else if (line.startsWith("include-file") && args.length > 1){
					String filename = args[1];
					File sourceFile = new File(agentDir, filename);
					File outputFile = new File(smemDir, "/" + sourceFile.getName());
					writeSmemFile(sourceFile, outputFile);
					sourceWriter.write("source " + outputFile.getName() + "\n\n");
				}
				// Include a semantic memory file to source, while replacing handle lti's (@handle1)
				//    and is able to be filtered (specify which items you want/don't want)
				// filter-file <path-from-agent-dir>
				else if(line.startsWith("filter-file") && args.length > 1){
					String filename = args[1];
					File sourceFile = new File(agentDir, filename);
					File outputFile = new File(smemDir, "/" + sourceFile.getName());
					HashMap<String, ArrayList<String> > items = parseFile(sourceFile);
					filterSmemFile(outputFile, items);
					sourceWriter.write("source " + outputFile.getName() + "\n\n");
				}
			} catch (IOException e){
				System.err.println("ERROR in " + line);
				System.err.println(e.getMessage());
			}
		}
		
		// Smem config file reader
		BufferedReader smemWordsReader = new BufferedReader(new FileReader(
				new File(agentDir, "/language-comprehension/smem-words/smem-words_to_process.soar")));
		
		while((line = smemWordsReader.readLine()) != null){
			line = line.trim();
			String[] args = line.split(" ");
			
			try {
				if(line.startsWith("source") && args.length > 1){
					String filename = args[1];
					File sourceFile = new File(agentDir, "/language-comprehension/smem-words/" + filename);
					File outputFile = new File(smemDir, "/" + sourceFile.getName());
					writeSmemFile(sourceFile, outputFile);
					sourceWriter.write("source " + outputFile.getName() + "\n\n");
				}
			} catch (IOException e){
				System.err.println("ERROR in loading file from smem-words_to_process.soar: " + line);
				System.err.println(e.getMessage());
			}
		}
		
		smemWordsReader.close();
		
		sourceWriter.close();
		
		return smemSourceFile;
	}
	
	public static String mapLTIs(String line){
		// Replace all lti references with a numerical equivalent
		// (e.g. map @red1 --> @100110)
		String[] words = line.split("\\s+");
		for(String word : words){
			word = word.replace("(", "").replace(")", "");
			if(word.length() >= 2 && word.startsWith("@") && !Character.isDigit(word.charAt(1))){
				String lti;
				if (ltiMap.containsKey(word)){
					lti = ltiMap.get(word);
				} else {
					lti = "@" + new Integer(100000 + ltiMap.size()).toString();
					ltiMap.put(word, lti);
				}
				line = line.replace(word, lti);
			}
		}
		return line;
	}
	
	private static ArrayList<String> readTemplateFile(File templateDir, String templateName) throws IOException, FileNotFoundException {
		File templateFile = new File(templateDir, "/" + templateName + ".txt");
		if(!templateFile.exists()){
			throw new FileNotFoundException(templateFile.getAbsolutePath());
		}

		BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));
		ArrayList<String> template = new ArrayList<String>();
		
		String line;
		while((line = templateReader.readLine()) != null){
			template.add(line);
		}

		templateReader.close();
		return template;
	}
	
	private static void writeTemplateFile(Template template, BufferedReader configReader, File outputFile){
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
	
	private static HashMap<String, ArrayList<String> > parseFile(File file) throws IOException {
		HashMap<String, ArrayList<String> > concepts = new HashMap<String, ArrayList<String> >();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String fullline;
		ArrayList<String> currentLines = new ArrayList<String>();
		String handle = null;
		while ((fullline = reader.readLine()) != null){
			String line = fullline.trim();
			// Ignore lines starting with #!
			if (line.startsWith("#!") || line.startsWith("smem --add {") || line.startsWith("}")){
				continue;
			}
			
			// New concept definition
			if (line.contains("+")){
				// Wrap up old definition
				if(handle != null){
					if(concepts.containsKey(handle)){
						System.err.println("ERROR: Concept map already contains the key " + handle);
					} else {
						boolean content = false;
						for(String l : currentLines){
							if(!l.trim().startsWith("#")){
								content = true;
								break;
							}
						}
						if(content){
							concepts.put(handle, currentLines);
						}
					}
				}
				
				// Start a new concept definition block
				int startIndex = line.indexOf("+") + 1;
				int endIndex = line.indexOf(" ", startIndex);
				if(endIndex == -1){
					endIndex = line.length();
				}
				handle = line.substring(startIndex, endIndex);
				currentLines = new ArrayList<String>();
				currentLines.add(line);
			} else if(line.length() > 0){
				// Add the line to the current block
				currentLines.add(mapLTIs(fullline));
			}
		}
		
		reader.close();

		return concepts;
	}

	private static void writeSmemFile(File sourceFile, File outputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
		
		String line;
		while((line = reader.readLine()) != null){
			writer.write(mapLTIs(line) + "\n");
		}
		reader.close();
		writer.close();
	}
	
	private static void filterSmemFile(File smemFile, HashMap<String, ArrayList<String> > items) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(smemFile)));
		for(String handle : items.keySet()){
			writer.write("smem --add {\n");
			for(String line : items.get(handle)){
				writer.write("  " + line + "\n");
			}
			writer.write("}\n\n");
		}
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
    		SmemConfigurator.configureSmem(inputFile, outputDir, rosieDir, agentName);
    	} catch (Exception e){
    		System.err.println("ERROR in SmemConfigurator.configureSmem");
    		System.err.println(e.getMessage());
    	}
    }

}
