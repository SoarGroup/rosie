package edu.umich.insoar.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ParseScript {
	public static Script parse(File f) {
		Scanner s;
		try {
			s = new Scanner(f);
		} catch (FileNotFoundException e){
			e.printStackTrace();
			return new Script();
		}
		
		ArrayList<String> lines = readFile(s);
		if(lines.get(0).contains("#!BechtelFormat")){
			return parseBechtelFormatScript(lines);
		} else {
			return parseDefaultFormatScript(lines);
		}
	}

	public static ArrayList<String> readFile(Scanner s){
		ArrayList<String> lines = new ArrayList<String>();
		while(s.hasNextLine()){
			lines.add(s.nextLine());
		}
		return lines;
	}
	
	public static Script parseDefaultFormatScript(ArrayList<String> lines) {
		Script script = new Script();
		for(String scriptLine : lines){
			String[] lineType = scriptLine.split(":");
			ActionType type = ScriptDataMap.getInstance().getType(lineType[0]+":");
			
			if(type == ActionType.Invalid) {
				System.out.println("Ignoring script line: "+scriptLine);
				continue;
			}
			
			
			String action = scriptLine.substring(lineType[0].length()+1).trim();
			script.addAction(new Action(type, action));
		}
		return script;
	}
	
	public static Script parseBechtelFormatScript(ArrayList<String> lines) {
		Script script = new Script();
		for(String line : lines){
			char lineType = line.charAt(0);
			ActionType type = ScriptDataMap.getInstance().getType(lineType);
			
			if(type == ActionType.Invalid) {
				System.out.println("Ignoring script line: "+line);
				continue;
			}
			
			String action = line.substring(1);
			action = action.trim();
			
			script.addAction(new Action(type, action));
		}
		return script;
	}
}
