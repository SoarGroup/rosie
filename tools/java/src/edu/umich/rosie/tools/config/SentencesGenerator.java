package edu.umich.rosie.tools.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class SentencesGenerator {
	
	public static void generateRosieSentences(File inputFile, File outputFile){
		try{
			//Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
			//		new FileOutputStream(outputFile), "utf-8"));
			
			//outputWriter.write("# Placeholder for auto-generated sentences file");
			ANTLRInputStream input;
			
			InputStream fileStream = Files.newInputStream(inputFile.toPath());
			//InputStream fileStream = Files.newInputStream(Paths.get(inputFile));
			input = new ANTLRInputStream(fileStream);
			
			//  Create a lexer
			RegressLexer lexer = new RegressLexer(input);
			//  Create a token stream
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			//  Create a parser
			RegressParser parser = new RegressParser(tokens);

			//  Now parse the input to get a parse tree
			ParseTree tree = parser.corpus();

			//  Walk the tree to generate the translation
			ParseTreeWalker walker = new ParseTreeWalker();
			walker.walk(new TranslateSentence(inputFile.toString(), outputFile.toString()), tree);


			//outputWriter.close();
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
