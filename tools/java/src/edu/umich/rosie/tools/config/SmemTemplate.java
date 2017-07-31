package edu.umich.rosie.tools.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SmemTemplate {
	String name;
	ArrayList<String> compoundPart = new ArrayList<String>();
	ArrayList<String> singularPart = new ArrayList<String>();
	ArrayList<String> paramNames = new ArrayList<String>();
	String listParamName = null;
	
	public SmemTemplate(String[] spec, File templateFile) throws FileNotFoundException, IOException {
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

