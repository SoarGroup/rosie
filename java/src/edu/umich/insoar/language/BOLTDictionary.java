package edu.umich.insoar.language;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


// This class maintains a user defined dictionary and returns POS tags using that dictionary.

public class BOLTDictionary {
	private Set<String> noun;
	private Set<String> adjective;
	private Set<String> verb;
	private Set<String> determiner;
	private Set<String> preposition;
	private Set<String> attribute;
	private Set<String> pronoun;
	
	// relevant pos tags 
	private String nounTag = "NN";
	private String adjectiveTag = "JJ";
	private String verbTag = "VB";
	private String determinerTag = "DT";
	private String prepositionTag = "PP";
	private String attributeTag = "AT";
	private String pronounTag = "PR";


	public BOLTDictionary(String filepath){
		noun = new HashSet<String>();
		adjective = new HashSet<String>();
		determiner = new HashSet<String>();
		preposition = new HashSet<String>();
		verb = new HashSet<String>();
		attribute = new HashSet<String>();
		pronoun = new HashSet<String>();
		 try {
			BufferedReader in = new BufferedReader(new FileReader(filepath));
			String line;
			while((line = in.readLine()) != null) {
				String[] group = line.split(":");
				String[] words = group[1].split(" ");
				if(group[0].equals("NOUN")) 
					fillSet(noun,words);
				if(group[0].equals("ADJECTIVE")) 
					fillSet(adjective,words);
				if(group[0].equals("VERB")) 
					fillSet(verb,words);
				if(group[0].equals("DETERMINER")) 
					fillSet(determiner,words);
				if(group[0].equals("PREPOSITION")) 
					fillSet(preposition,words);
				if(group[0].equals("ATTRIBUTE")) 
					fillSet(attribute,words);
				if(group[0].equals("PRONOUN"))
					fillSet(pronoun,words);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fillSet(Set<String> POSset, String[] words) {
		for(int i = 0; i < words.length; i++)
			POSset.add(words[i]);
	}

	private boolean isNoun(String string){
		if (noun.contains(string))
			return true;
		return false;
	}
	
	private boolean isAdjective(String string){
		if (adjective.contains(string))
			return true;
		return false;
	}
	
	private boolean isVerb(String string){
		if (verb.contains(string))
			return true;
		return false;
	}
	
	private boolean isDeterminer(String string){
		if (determiner.contains(string))
			return true;
		return false;
	}
	
	private boolean isPreposition(String string){
		if (preposition.contains(string))
			return true;
		return false;
	}
	
	private boolean isAttribute(String string){
		if (attribute.contains(string))
			return true;
		return false;
	}
	
	private boolean isPronoun(String string){
		if (pronoun.contains(string))
			return true;
		return false;
	}
	
	public String getTag(String string){
		String tag;
		if (isNoun(string)) return nounTag;
		if (isAdjective(string)) return adjectiveTag;
		if (isVerb(string)) return verbTag;
		if (isDeterminer(string)) return determinerTag;
		if (isPreposition(string)) return prepositionTag;
		if (isAttribute(string)) return attributeTag;
		if (isPronoun(string)) return pronounTag;
		// return the string back for words that appear verbatim in the MIISI document
		return string;
	}
}
