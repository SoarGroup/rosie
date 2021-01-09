package edu.umich.rosie.tools.config;

/*
 * Convert a sentence with expected output structures
 * to a Soar production in the form John's parser needs.
 *
 */
import java.util.Date;
import java.util.List;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import edu.umich.rosie.tools.config.RegressParser.SentenceWordContext;

import org.antlr.v4.runtime.tree.*;

public class TranslateSentence extends RegressBaseListener {
	//	Parameters
	static final String HEADER_TEXT =
			"#### Test sentences for ROSIE - \n"
			+ "#    Built by the SentencesToSoar tool %s\n"
			+ "#    from file '%s'.\n"
			+ "\n"
			+ "#   Apply initialize-rosie to set up the state.\n"
			+ "sp {top-state*apply*create-scripted-sentences*sentence-number\n"
			+ "   (state <s> ^name rosie\n"
		    + "              ^agent-params.sentence-source scripts\n"
			+ "              ^operator.name create-scripted-sentences)\n"
			+ "-->\n"
			+ "   (<s> ^current-sentence-number 1\n"
			+ "        ^max-sentence-number %d\n"
			+ "        ^game-scripting true)\n"
			+ "}\n"
			+ "\n";
	
	static final String PRODUCTION_TEXT =
			"sp {top-state*elaborate*scripted*sentence-%d*%s\n"
		//	+ "   (state <s> ^name comprehension\n"
		//		+ "              ^superstate <ss>\n"
			+ "   (state <s> ^name rosie\n"
		    + "              ^agent-params.sentence-source scripts)\n"
		//   + "              ^superstate <ss>\n"
		//+ "              ^segment <seg>)\n"
			+ "   (<s> ^current-sentence-number %d)\n"
			+ "-->\n"
		    + "   (<s> ^current-sentence <seg>)\n"
			+ "   (<seg> ^input-sentence <first>\n"
			+ "          ^current-word <first>\n"
			+ "          ^original-sentence <first>\n"
			+ "          ^expected %s)\n"
			+ "   (<first> ^spelling |*|\n"
			+ "            ^next <w0>\n"
			+ "            ^complete-sentence |%s|)\n"
			+ "%s"	//	The words go here
			+ "%s"	//	The expectation goes here
			+ "}\n"
			+ "\n";
	
	static final String WORD_WME =
			"   (<w%d> ^spelling |%s|\n"
			+ "         ^next %s)\n";
	static final String QUOTED_WORD_WME = 
			"   (<w%d> ^spelling |%s|\n"
			+ "         ^quoted true\n"
			+ "         ^next %s)\n";
	static final String TIME_WORD_WME = 
			"   (<w%d> ^spelling |%s|\n"
			+ "        ^clocktime true\n"
			+ "        ^hour %s\n"
			+ "        ^minute %s\n"
			+ "        ^next %s)\n";
			
	
//	static final String EXPECTATION_WME =
//			"   (<%s> %s\n";
	
    //  Private Members
	private String inputFile;
	private String outputFile;
//    final static Path OUTPUT_PATH = Paths.get("Sentences.soar");
    final static Charset ENCODING = StandardCharsets.UTF_8;
    private BufferedWriter writer;
    private int nSentences;
    private StringBuilder blockBuilder;
    private StringBuilder expectBuilder;
    
    //  Parts of a sentence and its expectation
    private String sentenceName;
    private int sentenceNumber;
    private String completeSentence;
    private String wordWmes;
    private String expectedSymbol;
    private String expectation;
    private boolean firstAttr;
    private String attrIndent;

    //	Constructor    
	public TranslateSentence(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Open an output file and write initial comments.</p>
	 */
	@Override public void enterCorpus(RegressParser.CorpusContext ctx) {
        //  Initialize other data
        nSentences = ctx.block().size();
        blockBuilder = new StringBuilder();
        String timeStamp = (new Date()).toString();
		String header = String.format(HEADER_TEXT, timeStamp, inputFile, nSentences);
		blockBuilder.append(header);
        sentenceNumber = 1;
        //  Open the file writer and write the header
        try {
            writer = Files.newBufferedWriter(Paths.get(outputFile), ENCODING);
            writer.write(blockBuilder.toString());
            writer.newLine();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);;
        }
    }
	/**
	 * {@inheritDoc}
	 *
	 * <p>Start building the text of a Soar production.</p>
	 */
	@Override public void enterBlock(RegressParser.BlockContext ctx) {
		//	Initialize
        blockBuilder = new StringBuilder();
        expectedSymbol = "nil";
        expectation = "";
        //	Put in any comments
        List<TerminalNode> comments = ctx.COMMENT();
        for (TerminalNode comment: comments) {
        	blockBuilder.append(comment.getText() + "\n");
        }
        //	Add the failure flag, if any
        if (ctx.FAILED() != null) {
        	blockBuilder.append(ctx.FAILED().getText() + "\n");
        }
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * <p>Gather all the data for a sentence.</p>
	 */
	@Override 
	public void enterSentence(RegressParser.SentenceContext ctx) {
    	StringBuilder wordWmesBuilder = new StringBuilder();
		StringBuilder sentenceBuilder = new StringBuilder();

        //  Get the words and process them
		List<SentenceWordContext> words = ctx.sentenceWord();
        int wordNumber = 0;
		for(SentenceWordContext sWord : words){
			// Each sentence word can either be a WORD or QUOTE
			TerminalNode word = sWord.getToken(RegressParser.WORD, 0);
			TerminalNode quote = sWord.getToken(RegressParser.QUOTE, 0);
			TerminalNode clocktime = sWord.getToken(RegressParser.TIME, 0);
			TerminalNode comma = sWord.getToken(RegressParser.COMMA, 0);
        	String nextId = String.format("<w%d>", wordNumber + 1);

			if(word != null){
				// The word is a single word
				sentenceBuilder.append(" " + word.getText());
				String wordText = word.getText().toLowerCase();
				String wordWme = String.format(WORD_WME, wordNumber, wordText, nextId);
				wordWmesBuilder.append(wordWme);

			} else if(quote != null){
				// The word is a quoted message
				sentenceBuilder.append(" " + quote.getText());

				String quoteWme = String.format(QUOTED_WORD_WME, wordNumber,
										quote.getText().replaceAll("\"", ""), nextId);
				wordWmesBuilder.append(quoteWme);
			} else if (clocktime != null){
				sentenceBuilder.append(" " + clocktime.getText());
				String hour = clocktime.getText().split(":")[0];
				String min = clocktime.getText().split(":")[1];
				String timeWme = String.format(TIME_WORD_WME, wordNumber, clocktime.getText(), hour, min, nextId);
				wordWmesBuilder.append(timeWme);
			} else if (comma != null){
				sentenceBuilder.append(",");
				String wordWme = String.format(WORD_WME, wordNumber, ",", nextId);
				wordWmesBuilder.append(wordWme);
			} else {
				System.err.println("SentenceWord was not a WORD, TIME, or QUOTE");
				return;
			}
			wordNumber += 1;
		}

        //	Look for a terminator, which could be TERMINATOR or '.'
		String term = ".";
		if(ctx.TERMINATOR() != null){
			term = ctx.TERMINATOR().getText();
		}
		sentenceBuilder.append(term);
		wordWmesBuilder.append(String.format(WORD_WME, wordNumber, term, "nil"));

        //	Fill out the sentence variables
        completeSentence = sentenceBuilder.toString().trim();
		sentenceName = completeSentence.replaceAll(" ", "-").replaceAll("[^a-zA-Z0-9-]", ""); // only alphanumeric and dashes
        wordWmes = wordWmesBuilder.toString();

		//System.out.println(completeSentence);
		//System.out.println(sentenceName);
		//System.out.println(wordWmes);
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * <p>Nothing needed here.</p>
	 */
	@Override public void exitSentence(RegressParser.SentenceContext ctx) {
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * <p>Gather all the data for the expectations for this block.</p>
	 */
	@Override public void enterExpected(RegressParser.ExpectedContext ctx) {
		//	Start gathering
		expectBuilder = new StringBuilder();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Format a new expectation.</p>
	 */
	@Override public void enterRhs(RegressParser.RhsContext ctx) {
        //	Add an expectation
		String symbol = ctx.SYMBOL().getText();
		expectBuilder.append(String.format("   (<%s>", symbol));
		//	Remember the symbol if this is the first one
		if (expectedSymbol.equals("nil"))
			expectedSymbol = "<" + symbol + ">";
		//	Remember we're on the first attribute and how much to indent
		firstAttr = true;
		StringBuilder indent = new StringBuilder("\n       ");
		for (int i = 0; i < symbol.length(); i++)
			indent.append(" ");
		attrIndent = indent.toString();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Add an attribute.</p>
	 */
	@Override public void enterAttr(RegressParser.AttrContext ctx) {
		//	Put the correct white space first
		expectBuilder.append((firstAttr)? " " : attrIndent);
        //  Build an attribute with possible dot notation
        List<TerminalNode> words = ctx.WORD();
        boolean first = true;
        for (TerminalNode w: words) {
    		expectBuilder.append((first)? "^" : ".");
    		expectBuilder.append(w.getText());
    		first = false;
        }
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Get ready for the value.</p>
	 */
	@Override public void exitAttr(RegressParser.AttrContext ctx) {
		expectBuilder.append(" ");
		firstAttr = false;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Add a value of various possible kinds.</p>
	 */
	@Override public void enterValue(RegressParser.ValueContext ctx) {
		if (ctx.variable() != null) {
			//	A variable
			expectBuilder.append("<" + ctx.variable().WORD().getText() + ">");
		} else if (ctx.SYMBOL() != null) {
			//	A symbol, with or without '@'
			expectBuilder.append("<" + ctx.SYMBOL().getText() + ">");
		} else if (ctx.WORD() != null) {
			//	A string constant
			//	THIS WILL NOT WORK FOR MULTI-WORD STRINGS BETWEEN |'s
			//	THE GRAMMAR WILL NEED TO BE CHANGED TO HANDLE THAT
			expectBuilder.append(ctx.WORD().getText());
		} else if (ctx.NUMBER() != null) {
			//	A numeric constant
			expectBuilder.append(ctx.NUMBER().getText());
		}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Nothing needed here.</p>
	 */
	@Override public void exitValue(RegressParser.ValueContext ctx) {
		
	}
	
	/**
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Nothing needed here.</p>
	 */
	@Override public void exitRhs(RegressParser.RhsContext ctx) {
		expectBuilder.append(")\n");
	}

	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Finish the gathering.</p>
	 */
	@Override public void exitExpected(RegressParser.ExpectedContext ctx) {
		expectation = expectBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Finish up the current Soar production and write it out.</p>
	 */
	@Override public void exitBlock(RegressParser.BlockContext ctx) {
        //  Build the production
        String production = String.format(PRODUCTION_TEXT,
        					sentenceNumber,
                            sentenceName,
                            sentenceNumber++,
                            expectedSymbol,
                            completeSentence,
                            wordWmes,
                            expectation);
        blockBuilder.append(production);
        //  Write it to the file
        try {
            writer.write(blockBuilder.toString());
            writer.newLine();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);;
        }
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * <p>Close the output file.</p>
	 */
	@Override public void exitCorpus(RegressParser.CorpusContext ctx) {
        //	Put in any final comments
        List<TerminalNode> comments = ctx.COMMENT();
        for (TerminalNode comment: comments) {
        	try {
				writer.write(comment.getText() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        //  Close the file writer
        try {
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);;
        }
    }
    
}
