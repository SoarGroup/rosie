package com.soartech.bolt.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ParseScript {
	public static Script parse(File f) {
		Script script = new Script();

		try {
			Scanner s = new Scanner(f);
			if(s.hasNext("#!BechtelFormat")) {
				s.nextLine();
				return parseBechtelFormatScript(script, s);
			} else {
				return parseDefaultFormatScript(script, s);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
	
	public static Script parseDefaultFormatScript(Script script, Scanner s) {
		while(s.hasNextLine()) {
			String scriptLine = s.nextLine();
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
	
	public static Script parseBechtelFormatScript(Script script, Scanner s) {
		while(s.hasNextLine()) {
			String line = s.nextLine();
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
