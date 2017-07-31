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

import org.antlr.v4.runtime.tree.*;

public class TranslateSentence extends RegressBaseListener {
	//	Parameters
	static final String HEADER_TEXT =
			"#### Test sentences for ROSIE - \r\n"
			+ "#    Built by the SentencesToSoar tool %s\r\n"
			+ "#    from file '%s'.\r\n"
			+ "\r\n"
			+ "#   Apply initialize-rosie to set up the state.\r\n"
			+ "sp {apply*initialize-rosie*sentence-number\r\n"
			+ "   (state <s> ^name rosie\r\n"
		    + "              ^top-state.world-usage internal\r\n"
			+ "              ^operator.name initialize-rosie)\r\n"
			+ "-->\r\n"
			+ "   (<s> ^current-sentence-number 1\r\n"
			+ "        ^max-sentence-number %d\r\n"
			+ "        ^game-scripting true)\r\n"
			+ "}\r\n"
			+ "\r\n";
	
	static final String PRODUCTION_TEXT =
			"sp {elaborate*state*sentence-%d*%s\r\n"
		//	+ "   (state <s> ^name comprehension\r\n"
		//		+ "              ^superstate <ss>\r\n"
			+ "   (state <s> ^name rosie\r\n"
		    + "              ^top-state.world-usage internal)\r\n"
		//   + "              ^superstate <ss>\r\n"
		//+ "              ^segment <seg>)\r\n"
			+ "   (<s> ^current-sentence-number %d)\r\n"
			+ "-->\r\n"
		    + "   (<s> ^current-sentence <seg>)\r\n"
			+ "   (<seg> ^input-sentence <first>\r\n"
			+ "          ^current-word <first>\r\n"
			+ "          ^original-sentence <first>\r\n"
			+ "          ^expected %s)\r\n"
			+ "   (<first> ^spelling |*|\r\n"
			+ "            ^next <w0>\r\n"
			+ "            ^complete-sentence |%s|)\r\n"
			+ "%s"	//	The words go here
			+ "%s"	//	The expectation goes here
			+ "}\r\n"
			+ "\r\n";
	
	static final String WORD_WME =
			"   (<w%d> ^spelling |%s|\r\n"
			+ "         ^next %s)\r\n";
	
//	static final String EXPECTATION_WME =
//			"   (<%s> %s\r\n";
	
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
        	blockBuilder.append(comment.getText() + "\r\n");
        }
        //	Add the failure flag, if any
        if (ctx.FAILED() != null) {
        	blockBuilder.append(ctx.FAILED().getText() + "\r\n");
        }
    }
    
	/**
	 * {@inheritDoc}
	 *
	 * <p>Gather all the data for a sentence.</p>
	 */
	@Override public void enterSentence(RegressParser.SentenceContext ctx) {
        //  Get the words and process them
        List<TerminalNode> words = ctx.WORD();
        int nWords = words.size();
        StringBuilder name = new StringBuilder();
        StringBuilder complete = new StringBuilder();
        for (TerminalNode w: words) {
        	if (name.length() > 0) {
        		name.append('-');
        		complete.append(' ');
        	}
            name.append(w.getText().toLowerCase());
            complete.append(w.getText());
        }
        //	Look for a terminator, which could be TERMINATOR or '.'
        String term = null;
        if (ctx.TERMINATOR() != null) {
        	term = ctx.TERMINATOR().getText();
        	nWords++;	//	Add one more for the terminator
        } else {
        	//	Look for a '.'
            for (ParseTree node: ctx.children) {
            	if (node.getText().equals(".")) {
            		term = ".";
                	nWords++;	//	Add one more for the terminator
            		break;
            	}
            }
        }
        //	Add the word WMEs
        int wordNumber = 0;
    	StringBuilder wordData = new StringBuilder();
        for (TerminalNode w: words) {
        	String nextId = ((wordNumber + 1) < nWords)?
        							String.format("<w%d>", wordNumber + 1)
        							: "nil";
        	String wordWme = String.format(WORD_WME, wordNumber++,
        							w.getText().toLowerCase(),  nextId);
        	wordData.append(wordWme);
        }
        //	Add a terminator WME
        if (term != null) {
        	complete.append(term);
        	String wordWme = String.format(WORD_WME, wordNumber++, term,  "nil");
        	wordData.append(wordWme);
        }
        //	Fill out the sentence variables
        sentenceName = name.toString();
        completeSentence = complete.toString();
        wordWmes = wordData.toString();
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
		StringBuilder indent = new StringBuilder("\r\n       ");
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
		expectBuilder.append(")\r\n");
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
				writer.write(comment.getText() + "\r\n");
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
