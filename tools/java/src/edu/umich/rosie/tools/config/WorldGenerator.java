package edu.umich.rosie.tools.config;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.lang.StringBuilder;
import java.lang.String;

public class WorldGenerator {
	
	public static void generateRosieWorld(File inputFile, File outputFile){
		try{
			Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			
			String gFile = inputFile.getName().replace(".world", "");
			
			//outputWriter.write("# Placeholder for auto-generated world file");
			outputWriter.write("\nsp {top-state*apply*create-internal-world*" + gFile + "\n");
			outputWriter.write("   (state <s> ^superstate nil\n");
			outputWriter.write("              ^operator <o>)\n");
			outputWriter.write("   (<o> ^name create-internal-world)\n");
//			outputWriter.write("        ^type " + gFile + ")\n");
//			outputWriter.write("   (<ts> ^world <wo2>)\n");
			outputWriter.write("-->\n");
//			outputWriter.write("   (<ts> ^world <wo2> -)\n");
			outputWriter.write("   (<s> ^world <wo>)\n");
			outputWriter.write("   (<wo> ^objects <objs> ^predicates <preds> ^robot <ro>)\n");
			outputWriter.write("   (<ro> ^handle rosie ^item-type object ^arm.action wait ^predicate.handle rosie)\n");
			
			outputWriter.write("   (<objs> ^object <self>");
			
			try (BufferedReader br = new BufferedReader(new FileReader(inputFile)))
				{
					String line;
					boolean hasPreds = false;
					List<String> objs = new ArrayList<String>();
					List<String> preds = new ArrayList<String>();
					
					//initial object list
					br.mark(30000);
					while ((line = br.readLine()) != null) {
						if (line.contains("objid"))
						{
							String [] parts = line.split(" ");
							objs.add("<o" + parts[1] + ">");
							outputWriter.write(" <o" + parts[1]+ ">");
						}
						else if (line.contains("predicate"))
						{
							hasPreds = true;
						}
						//outputWriter.write(")\n");
					}
					
					outputWriter.write(")\n");
					outputWriter.write("   (<self> ^type object ^handle self ^predicates.type object\n");

					//initial predicate list
					if (hasPreds)
					{
						outputWriter.write(")\n   (<preds> ^predicate");
						br.reset();
						while ((line = br.readLine()) != null) {
							if (line.contains("predicate ") || line.contains("predicate-set "))
							{
								String [] parts = line.split(" ");
								preds.add(parts[1]);
								outputWriter.write(" <" + parts[1]+ ">");

							}
						}
					}
					StringBuilder predicates = new StringBuilder("");
					
					int flag = 0;
					int index = -1;
					int index2 = -1;
					br.reset();
					while ((line = br.readLine()) != null)
					{
						if (line.contains("objid")) {
							outputWriter.write(")\n");
							index= index +1;
							outputWriter.write("(" + objs.get(index) + " ^item-type object ^handle object-" + index + " ^predicates <pr" + index + ">)\n");
							outputWriter.write("(<pr" + index + "> ^visible true ");
							flag = 1;   
						} else if (line.contains("predicate ")) {
							if (flag == 1){
								index=-1;
							}
							outputWriter.write(")\n" + predicates);
							predicates = new StringBuilder("");
							index= index +1;
							outputWriter.write("(<" + preds.get(index) + "> ^item-type predicate ^handle " + preds.get(index) + " ^instance ");
							flag = 2;
						} else if (line.contains("predicate-set ")) {
							if (flag == 1){
                                index=-1;
							}
							outputWriter.write(")\n" + predicates);
							predicates = new StringBuilder("");
							index= index +1;
							outputWriter.write("(<" + preds.get(index)+ "> ^item-type predicate ^handle " + preds.get(index) + " ^instance ");
							flag = 3;
						} else if (!line.trim().isEmpty()) {
							String [] words =  line.split(" ");
							
							if (flag == 1){
                                outputWriter.write("^" + words[0] + " " + words[1] + " ");
							}
							else if (flag == 2) {
                                index2 = index2 +1;
                                outputWriter.write("<ins" + index2 + "> ");
                                predicates.append("(<ins" + index2 + "> ^1 <o" + words[0] + "> ^2 <o" + words[1] + ">)\n");
							} else if (flag == 3) {
                                index2 = index2 +1;
                                outputWriter.write("<ins" + index2 + "> ");
                                predicates.append("(<ins" + index2 + "> ^1 <set" + index2 + ">)\n(<set" + index2 + ">  ^object");
								String[] arr = line.split(" ");  
                                for (String word : arr)
								{
									predicates.append(" <o" + word + ">");
								}
                                predicates.append(")\n");
							}
						}
					}
					outputWriter.write(")\n" + predicates);
					outputWriter.write("}\n");

				}
			
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
