package edu.umich.rosie.tools.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

public class WorldGenerator {
	
	public static void generateRosieWorld(File inputFile, File outputFile){
		try{
			Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			
			outputWriter.write("# Placeholder for auto-generated world file");

			outputWriter.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}

    public static void main(String[] args) {
    	if (args.length < 2){
    		System.err.println("WorldGenerator expects 2 arguments:\n" + 
    							"  1: The input filename with world info\n" + 
    							"  2: The output filename to create\n");
    		System.exit(0);
    	}
    	
    	File inputFile = new File(args[0]);
    	File outputFile = new File(args[1]);
    	WorldGenerator.generateRosieWorld(inputFile, outputFile);
    }
}
