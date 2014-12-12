package edu.umich.insoar.testing;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import april.util.TimeUtil;

public class ISpyScriptGenerator {
//	private static String[][] properties = new String[][]{
//		new String[]{ "red", "orange", "yellow", "green", "blue", "purple" },
//		new String[]{ "crescent", "square", "l-block", "arch", "triangle", "rectangle"},
//		new String[]{ "small", "medium", "large"},
//		new String[]{ "light", "moderate", "heavy"},
//		new String[]{ "hot", "warm", "cool", "cold"}
//	};
	
	private static String[][] properties = new String[][]{
	new String[]{ "red", "green", "blue" },
	new String[]{ "small", "medium", "large"},
	new String[]{ "light", "moderate", "heavy"},
};
	
	private static String[] superlatives = new String[]{ "heavy-est", "light-est"};
	
	private static ArrayList<String> generateLines(){
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("#!BechtelFormat");
		lines.add("@ classifier load");
		for(String[] list : properties){
			for(String line : list){
				lines.add(String.format("> Find the %s object", line));
			}
		}
		for(String s : superlatives){
			lines.add(String.format("> Find the %s object", s));
			lines.add(String.format("> Find the %s object", s));
		}
		
		return lines;
	}
	
	public static Script generateNormalScript(String logname){
		ArrayList<String> lines = generateLines();
		Collections.shuffle(lines, new Random(TimeUtil.utime()));

		if(logname != null){
			try{
				PrintWriter writer = new PrintWriter(logname, "UTF-8");
				for(String line : lines){
					writer.println(line);
				}
				writer.close();
			} catch (FileNotFoundException e){
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return ParseScript.parseBechtelFormatScript(lines);
	}
}
