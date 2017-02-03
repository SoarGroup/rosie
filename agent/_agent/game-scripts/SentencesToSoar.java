/*
 * Convert a file of sentences with expected output structures
 * to a .soar file in the form John's parser needs.
 *
 */
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*; 

public class SentencesToSoar {
	//	Private Static Members
	private static String inputFile;
	private static String outputFile;
	
    //  The main program
    public static void main(String[] args) throws Exception {
    	//	If no args, print usage
    	if (args.length == 0) {
    		System.out.println("Translator to translate a list of sentences with expectations to Soar code.");
    		System.out.println("Usage:");
    		System.out.println("java -jar SentencesToSoar.jar <input-file-name>.txt [-o <output-file-name>]");
    		System.out.println("If no -o option is given, the output will be written to <input-file-name>.soar.");
    		System.out.println("");
    		System.exit(100);
    	}
    	//	Set default input file
    	inputFile = "Sentences.txt";
    	//	Get or use defaults for file names
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equals("-o"))
    			outputFile = args[++i];
    		inputFile = args[i];
    	}
    	//	Set default output file
    	if (outputFile == null) {
    		int extension = inputFile.lastIndexOf('.');
    		if (extension < 1) {
        		System.out.println(String.format("'%s' is not a valid input file name."));
        		System.exit(101);
    		}
    		outputFile = inputFile.substring(0, extension) + ".soar";
    	}
    	//	Report what we're doing
		// System.out.println(String.format("Translating '%s' to '%s'.", inputFile, outputFile));

		//  First set up the machinery
        //  Create an input stream
        ANTLRInputStream input;
        if (args.length > 0) {
            //  Read from a given file
        	InputStream fileStream = Files.newInputStream(Paths.get(inputFile));
            input = new ANTLRInputStream(fileStream);
        } else {
            //  Read from standard in
            input = new ANTLRInputStream(System.in);
        }
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
        walker.walk(new TranslateSentence(inputFile, outputFile), tree);
        
    }
    
}
