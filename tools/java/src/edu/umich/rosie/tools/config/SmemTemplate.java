package edu.umich.rosie.tools.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// Wrapper for a single smem template that will read the specification header and 
//    a template file and then allow a user to 
//    create instantiations of the template using by replacing values given as input
//
// A template specification looks like:
// template <template_name> _PARAM-1_ _PARAM-2_ ... _PARAM-N_ _PARAM-LIST_* {
//
// When given a line of values, will find-replace each PARAM in the template file
//    with the correponsing value and return the result
//
// The last parameter can be a list parameter, which is designated by a * at the end
//   This will accept multiple values, and create a block for each one
// You can specify a line #SINGULAR in the template which will not duplicate any lines after it
//
// Example:
//
// TEMPLATE FILE: color.txt 
// -----------------------------------
// (<_WORD_-word> ^spelling |_WORD_|
//                ^referent @_COLOR_)
// #SINGULAR
// (@_COLOR_ ^handle _COLOR_
//           ^category @color1)
// -----------------------------------
//
// TEMPLATE SPECIFICATION:
// template color _COLOR_ _WORD_* {
//
// Then if you called template.fillFromLine("red1 red rouge crimson")
// It would return the string:
// "(<red-word> ^spelling |red|
//              ^referent @red1)
//  (<rouge-word> ^spelling |rouge|
//                ^referent @red1)
//  (<crimson-word> ^spelling |crimson|
//                  ^referent @red1)
//  (@red1 ^handle red1
//         ^category @color1)"

public class SmemTemplate {
	String name;
	ArrayList<String> compoundPart = new ArrayList<String>();
	ArrayList<String> singularPart = new ArrayList<String>();
	ArrayList<String> paramNames = new ArrayList<String>();
	String listParamName = null;
	
	public SmemTemplate(String[] spec, File templateFile) throws FileNotFoundException, IOException {
		// Spec is a list of strings as specified above
		// template color _COLOR_ _WORD_* {
		// would be passed in as spec = String[]{ "template", "color", "_COLOR_", "_WORD_*", "{" }
		this.name = spec[1];
		
		for(int i = 2; i < spec.length - 1; i++){
			if(spec[i].equals("{")){
				break;
			}
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
		
		// Read the template and split into compound and singular parts (before and after #SINGULAR line)
		String line;
		Boolean hitSingular = false;
		while((line = templateReader.readLine()) != null){
			if(line.trim().toUpperCase().equals("#SINGULAR")){
				hitSingular = true;
			} else if(hitSingular || listParamName == null){
				singularPart.add(line);
			} else {
				compoundPart.add(line);
			}
		}

		templateReader.close();
	}
	
	// Given a string with template values, will return the template filled out with those values
	// Where each value matches the template parameter in the specification
	public String fillFromLine(String line){
		StringBuilder sb = new StringBuilder();
		
		String[] values = line.split(" ");
		if(values.length < paramNames.size()){
			System.err.println("Error in template " + name + ": not enough values in line '" + line + "'");
			return "";
		}

		// Read the final words in the string that go past the normal parameters
		//    and create a copy of the compound section for each one
		if(listParamName != null){
			for(int i = paramNames.size(); i < values.length; i++){
				String listValue = values[i];
				for(String templateLine : compoundPart){
					String strippedValue = listValue.replaceAll("'", "");
					// Remove bad characters (like apostrophes)
					// but if replacing something inside Soar quotes, keep the characters in
					String filledLine = templateLine.replaceAll("\\|" + listParamName + "\\|", "|" + listValue + "|");
					filledLine = filledLine.replaceAll(listParamName, strippedValue);
					sb.append("  " + filledLine + "\n");
				}
			}
		}
		
		// Copy the singular part of the template
		for(String templateLine : singularPart){
			sb.append("  " + templateLine + "\n");
		}
		
		// Replace each parameter with the associated value
		String filledLines = sb.toString();
		for(int i = 0; i < paramNames.size(); i++){
			filledLines = filledLines.replaceAll(paramNames.get(i), values[i]);
		}
		
		return filledLines;
	}
}

