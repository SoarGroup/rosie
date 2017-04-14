package edu.umich.rosie.tools.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SentencesGenerator {
	
	public static void generateRosieSentences(File inputFile, File outputFile){
		try{
			Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			
			outputWriter.write("# Placeholder for auto-generated sentences file");

			outputWriter.close();
		} catch (Exception e){
			e.printStackTrace();
		}	
	}

    public static void main(String[] args) {
    	if (args.length < 2){
    		System.err.println("SentencesGenerator expects 2 arguments:\n" + 
    							"  1: The input filename with a list of sentences\n" + 
    							"  2: The output filename to create\n");
    		System.exit(0);
    	}
    	
    	File inputFile = new File(args[0]);
    	File outputFile = new File(args[1]);
    	SentencesGenerator.generateRosieSentences(inputFile, outputFile);
    }
}
