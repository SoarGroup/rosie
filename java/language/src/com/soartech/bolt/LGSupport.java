package com.soartech.bolt;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;
import net.sf.jlinkgrammar.parser;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;

public class LGSupport implements OutputEventInterface, RunEventInterface {
	private static Agent agent;
	public static String dictionaryPath = ""; 
	private static Identifier lgInputRoot;
	private static int sentenceCount = -1;
	private static int currentOutputSentenceCount = -1;
	private static boolean phraseMode = false;
	private static Map<Integer, Vector<Identifier> > inputWMEs = new TreeMap<Integer, Vector<Identifier> >();
	
	public static parser theParser;
	private static TreeMap<Integer, ArrayList<Linkage> > currentParses = new TreeMap<Integer, ArrayList<Linkage> >();
	
	private Vector<String> inputSentences = new Vector<String>();

	public LGSupport(Agent _agent, String dictionary) {
		agent = _agent;
		dictionaryPath = dictionary;
		
		theParser = new parser();
		
		// make a root lg-input WME
		if (agent != null) {
			lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");

			agent.AddOutputHandler("preprocessed-sentence", this, null);
			agent.AddOutputHandler("sentence-done", this, null);
			
			// generic output event where we can safely add sentences to input while in the Soar thread
			agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
		}
	}

	public void handleSentence(String sentence) {
		if (agent == null) {
			// no Soar, run parser directly on sentence
			theParser.parseSentence(sentence);
			addParsesToWM();
		}
		else {
			synchronized (inputSentences) {
				// this may not be called from the Soar thread, so wait for an input event to add to WM
				inputSentences.add(sentence);
			}
		}
	}
	
	
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		synchronized (inputSentences) {	
			for (String sentence: inputSentences) {
				System.out.println("sending to Soar for preprocessing (output event): " + sentence);
	
				// load the sentence into WM
				sentenceCount++;
				originalSentenceToWM(sentence.toLowerCase());
				
				// Soar rules may modify it
				
				// wait for preprocessed sentence on output
			}
			inputSentences.clear();
		}
	}
	
	
	private void originalSentenceToWM(String sentence) {
		Identifier root = agent.CreateIdWME(lgInputRoot, "original-sentence");
        agent.CreateIntWME(root, "sentence-count", sentenceCount);
        
        inputWMEs.put(sentenceCount, new Vector<Identifier>());
        inputWMEs.get(sentenceCount).add(root);
        
        Identifier wordsWME = agent.CreateIdWME(root, "words");

        // replace all nonword characters, except hyphens, with a single space
        sentence = sentence.replaceAll("([^a-zA-Z_0-9-])", " $1");
        //System.out.println("padded: " + sentence);
        String[] words = sentence.split("\\s+");
        
        for (int i=0; i<words.length; i++) {
        	Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
        	agent.CreateStringWME(wordWME, "wvalue", words[i]);
        	agent.CreateIntWME(wordWME, "wcount", i);
        	//System.out.println("wd " + words[i]);
        }

	}
	
	private void clearAllInputWMEs() {
		for (Integer sc: inputWMEs.keySet()) {
			for (Identifier id: inputWMEs.get(sc)) {
				agent.DestroyWME(id);
			}
		}
		inputWMEs.clear();
	}
	
	public void clear() {
		clearAllInputWMEs();
		sentenceCount = -1;
	}
	
	public void outputEventHandler(Object data, String agentName,
			String attributeName, WMElement pWmeAdded) {
		String commandName = pWmeAdded.GetAttribute();
		
		if (commandName.equals("preprocessed-sentence")) {	
			ArrayList<String> sentences = preprocessedSentencesFromWM(pWmeAdded);
			if(sentences == null){
				System.err.println("NULL SENTENCE ON OL");
				return;
			}
			
			currentParses.clear();
			for (String sentence: sentences) {
				sentence = sentence.trim(); // some substitutions in Soar may cause leading spaces
			
				// call LG Parser
				theParser.parseSentence(sentence);
				
				// add noun-phrase parses
				// NOUN-PHRASE-WALL should be in words.v.4.1, it is a verb that will let any valid noun phrase attach to it
				// Also make sure the first letter of the input isn't capitalized, otherwise LG won't recognize it since it
				// is a capitalized word in the middle of the sentence.
				phraseMode = true;
				theParser.parseSentence("NOUN-PHRASE-WALL " + sentence.substring(0,2).toLowerCase() + sentence.substring(2)); 
				phraseMode = false;
			}
			
			addParsesToWM();
			
			// AM: Causes the preprocessed sentence to be removed from the ol link
			pWmeAdded.ConvertToIdentifier().AddStatusComplete();
		}
		else if (commandName.equals("sentence-done")) {
			String param = pWmeAdded.ConvertToIdentifier().GetParameterValue("sentence-count");
			if (param == null) {
				System.err.println("LGSupport.preprocessedSentenceFromWM: no sentence-count");
				return;
			}
			
			int count = Integer.parseInt(param);
			
			if (inputWMEs.containsKey(count)) {
				for (Identifier id: inputWMEs.get(count)) {
					agent.DestroyWME(id);
				}
				inputWMEs.remove(count);
			}
			// else this was probably a sentence-count for the second part of a split-and sentence, and there are no corresponding input WMEs
			
			pWmeAdded.ConvertToIdentifier().AddStatusComplete();
		}
		else {
			System.out.println("ERROR: handler got bad command: " + commandName);
		}
	}	
	
	private ArrayList<String> preprocessedSentencesFromWM(WMElement pWmeAdded) {
		ArrayList<String> results = new ArrayList<String>();
				
		if (pWmeAdded == null) {
			System.err.println("LGSupport.preprocessedSentenceFromWM: pWmeAdded is null");
			return null;
		}
		WMElement currentWME = pWmeAdded.ConvertToIdentifier().FindByAttribute("start", 0);
		String param = pWmeAdded.ConvertToIdentifier().GetParameterValue("sentence-count");
		if (param == null) {
			System.err.println("LGSupport.preprocessedSentenceFromWM: no sentence-count");
			return null;
		}
		
		currentOutputSentenceCount = Integer.parseInt(param);
		
		if (!inputWMEs.containsKey(currentOutputSentenceCount)) {
			// currentOutputSentenceCount may not be the regular sentence-count, since Soar may have a preprocessing backlog and give preprocessed
			// results for an old sentence. But it should always be for a sentence where the original-sentence exists and has not been cleared.
			System.out.println("ERROR: Soar returned a preprocessed sentence that Java never provided to it (bad sentence-count)");
			inputWMEs.put(currentOutputSentenceCount, new Vector<Identifier>()); // carry on anyway
		}
		
		results = sentencesFromWMRecursive("", currentWME.ConvertToIdentifier(), 0);
		for (String result: results) {
			System.out.println("got preprocessed sentence: " + result);
		}
		return results;
	}
	
	// agent can add in multiple word values, so we want a set of all possible alternate sentences
	private ArrayList<String> sentencesFromWMRecursive(String prefix, Identifier wordId, int child) {
		WMElement nextWord = wordId.FindByAttribute("word", child);
		if (nextWord == null) {
			return null;
		}
		
		String wordValue = nextWord.GetValueAsString();		
		String newPrefix = prefix + " " + wordValue;
		
		// try to get an alternate version of this word and add all its sentences to our result
		ArrayList<String> subList = sentencesFromWMRecursive(prefix, wordId, child + 1);	
		ArrayList<String> results = null;
		
		WMElement nextWME = wordId.FindByAttribute("next", 0);
		if (nextWME == null) {
			// end of the sentence
			results = new ArrayList<String>();
			results.add(newPrefix);
		}
		else {
			results = sentencesFromWMRecursive(newPrefix, nextWME.ConvertToIdentifier(), 0);
		}
		if (subList != null) 
			results.addAll(subList);
		
		return results;
	}
	
	
	// called from parser.java
	// doIt (called above) will call this once for each linkage (parse)
	public static void loadLinkage(Linkage thisLinkage, int idx, Sentence sent) {
        // combine all sublinkages to one
        thisLinkage.linkage_compute_union();
        
        // store the linkage (parse) indexed by the unused word cost
        // only those parses with minimal skipped words will be added to WM
        int unusedCost = thisLinkage.linkage_unused_word_cost();

        ArrayList<Linkage> parsesForCost = currentParses.get(unusedCost);
        if (parsesForCost == null) {
        	parsesForCost = new ArrayList<Linkage>();
        	parsesForCost.add(thisLinkage);
        	currentParses.put(unusedCost, parsesForCost);
        }
        else {
        	parsesForCost.add(thisLinkage);
        }
        return;
	}
        
   private static void addParsesToWM() {
	   int unusedCost = currentParses.firstKey();
	   
	   System.out.println("minimal unused cost: " + unusedCost);
	   
	   for (Linkage thisLinkage: currentParses.get(unusedCost)) {
		    int     rWordIndex;
	        int     lWordIndex;
	        String  linkLabel;
		   
	        Sentence sent = thisLinkage.sent;
	        int numWords = sent.sentence_length();
	        
	        // identify guessed words. AFAIK, the only way these show up is in the linkage words,
	        // which are what printed in the parse picture:
	
	        //        +------------------Xp-----------------+
	        //        |         +---------Os---------+      |
	        //        +----Wi---+         +-----A----+      |
	        //        |         |         |          |      |
	        //    LEFT-WALL xget[?].v xthe[?].a xblock[?].n .
	        
	        // So we need to go through each word and look for a ? as the fourth-to-last character.
	        
	        int numGuesses = 0;        
	
	        boolean isGuess[] = new boolean[numWords];
	        boolean isSkip[] = new boolean[numWords];
	        
	        
	        for (int i=0; i<numWords; i++) {
	        	String wordAsPrinted = thisLinkage.word[i];
	        	char c = 'x';
	        	if (wordAsPrinted.length() > 3) {
	        		c = wordAsPrinted.charAt(wordAsPrinted.length()-4);
	        	}
	        	if (c == '?') {
	        		isGuess[i] = true;
	        		numGuesses++;
	        	}
	        	else {
	        		isGuess[i] = false;
	        	}
	        	c = wordAsPrinted.charAt(0);
	        	if (c == '[') {
	        		isSkip[i] = true;
	        	} 
				else {
	        		isSkip[i] = false;
	        	}
	        }
	         
	        String message = thisLinkage.linkage_print_diagram();
	        System.out.println(message);
	        
	        int disCost = thisLinkage.linkage_disjunct_cost();
	        int parseCount = nextParseCount(currentOutputSentenceCount);
	        
	        System.out.println("^^ parse " + parseCount + ": DIS = " + disCost + " UNUSED = " + unusedCost);
	
			// this ideally should be injected into the Soar print stream,
			// but that doesn't seem possible. Echo command doesn't seem to do it.
			
			if (agent != null) {
				// null agent valid if run to print the parse alone
			
		        int numLinks = thisLinkage.linkage_get_num_links();
		        
		        // make a root for this sentence
		        Identifier sentenceRoot = agent.CreateIdWME(lgInputRoot, "parsed-sentence");
		        inputWMEs.get(currentOutputSentenceCount).add(sentenceRoot);
		        
		        // make a wme for the count
		        agent.CreateIntWME(sentenceRoot, "sentence-count", currentOutputSentenceCount);
		       
		        agent.CreateIntWME(sentenceRoot, "parse-count", parseCount);
		        	
		        // make a wme for the words
		        Identifier wordsWME = agent.CreateIdWME(sentenceRoot, "words");
		        
		        // now load the words
						
				int globalOffset = 0;
				if (phraseMode) {
					globalOffset--;
				}
	
				if (sent.sentence_get_word(0).equals("LEFT-WALL")) {
					// this may push LEFT-WALL to index -1, but nothing is looking for
					// that anyway so its OK if rules think the sentence starts at 0
					globalOffset--;
				}
		
		        for (int wordx = 0; wordx < numWords; wordx++) {
		            // add ^word information for this link
		            String wordval = sent.sentence_get_word(wordx);
		 
		            Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
		
		            agent.CreateIntWME(wordWME, "wcount", wordx);
		            agent.CreateStringWME(wordWME, "wvalue", wordval);
		            
		            if (isGuess[wordx]) {
		            	agent.CreateStringWME(wordWME, "guessed", "true");
		            }
		            if(isSkip[wordx]) {
		            	agent.CreateStringWME(wordWME, "skipped", "true");
		            }
		            
		            // if parsing as a phrase, we have added an extra word at the beginning
		            // Soar needs to know which words are equivalent across parses, so the
		            // phraseMode flag allows this to start phrases at index 0 rather than 1
		            // so word indices are equivalent
		            
		            // don't change the wcount, though, since LGSoar wants that to always start at 0
								agent.CreateIntWME(wordWME, "global-wcount", wordx + globalOffset);
		        }
		            
		        // make a wme for the links
		        Identifier linksWME = agent.CreateIdWME(sentenceRoot, "links");
		        agent.CreateIntWME(sentenceRoot, "unused-word-cost", unusedCost);
		        agent.CreateIntWME(sentenceRoot, "expensive-link-cost", disCost);
		        agent.CreateIntWME(sentenceRoot, "unknown-word-cost", numGuesses);
		        
		        String noStarsPattern = "\\*";
		        String noCaratPattern = "\\^"; // carat is apparently "match nothing except *". occurs for lots of conjunctions.
		        String idiomPattern = "ID.*";
		        String pattern = "([A-Z]+)([a-z]*)";
		        
		        // now load the links
		        for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
		            rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
		            lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
		            linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
		           
		            // SBW 3/8/12
		            // remove all *'s from the link names
		            // these indicate "any subtype in this position"
		            // not sure what to do with them, but they definitely shouldn't be stuck to the main type
		            linkLabel = linkLabel.replaceAll(noStarsPattern, "");
		            linkLabel = linkLabel.replaceAll(noCaratPattern, "");
		            
		            String ltype = linkLabel;
		            ltype = ltype.replaceAll(pattern, "$1");
		            ltype = ltype.replaceAll(idiomPattern, "ID");
		            String lsubtype = linkLabel;
		            lsubtype = lsubtype.replaceAll(pattern, "$2");
		            
		            // add ^link information for this link
		            Identifier linkWME = agent.CreateIdWME(linksWME, "link");
		                        
		            agent.CreateStringWME(linkWME, "lvalue", linkLabel);
		            
		            agent.CreateIntWME(linkWME, "lwleft", lWordIndex);
		            agent.CreateIntWME(linkWME, "lwright", rWordIndex);	
		            
		            agent.CreateStringWME(linkWME, "ltype", ltype);
		           
		            // make a separate WME for each subtype
		            // assumption: subtype ordering doesn't matter
		        	for (int i=0; i< lsubtype.length(); i++) {
		        		agent.CreateStringWME(linkWME, "ltypesub", lsubtype.substring(i, i+1));
		        	}
		          
		        }
			}
	   }
	}
	
	private static int lastSentenceCount = -1;
	private static int currentParseCount = -1;
	
	private static int nextParseCount(int sentenceCount) {
		// can't just use LG parse indices, since the same sentence can be reparsed at Soar's request,
		// reparses should have the same sentence count and extend the parse indices of the original
	
		if (sentenceCount != lastSentenceCount) {
			currentParseCount = 0;
			lastSentenceCount = sentenceCount;
		}
		else {
			currentParseCount++;
		}
		return currentParseCount;
	}

}
