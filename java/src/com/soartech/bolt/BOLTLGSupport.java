package com.soartech.bolt;

import sml.Agent;

public class BOLTLGSupport {
	private LGSupport lgSupport = null;
	
	public BOLTLGSupport(Agent agent, String dictionary) {
		lgSupport = new LGSupport(agent, dictionary);
	}
	
	public void handleInput(String input) {
		// BOLT-specific message preprocessing goes here:
		// - split multiple sentences
		// - rewrite some sentences for workarounds
		
		input = input.toLowerCase();
		
		// no, this is a red block => no. this is a red block.
		input = input.replace("no,", "no.");
		input = input.replace("yes,", "yes.");

		String[] sentences = input.split("\\.");
		
		for (String sentence : sentences) {
			lgSupport.handleSentence(sentence);
		}
	}
	
	public void clear() {
		lgSupport.clear();
	}
}
