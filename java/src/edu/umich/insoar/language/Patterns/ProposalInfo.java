package edu.umich.insoar.language.Patterns;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class ProposalInfo extends LinguisticEntity{
    public static String TYPE = "ProposalInfo";
	private VerbCommand verbCommand;
	private Set<ObjectRelation> objectRelation;

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		objectRelation = new HashSet();
		//Pattern p = Pattern.compile();
		Pattern p = Pattern.compile("VBC\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			verbCommand = (VerbCommand) tagsToWords.get(m.group());
		}
		
		p = Pattern.compile("REL\\d*");
		m = p.matcher(string);
		while(m.find()){
			objectRelation.add((ObjectRelation) tagsToWords.get(m.group()));
		}
	}

	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier goalId = id.CreateIdWME(connectingString);
		verbCommand.translateToSoarSpeak(goalId, "verb-command");
		Iterator<ObjectRelation> itr = objectRelation.iterator();
		while(itr.hasNext()){
			ObjectRelation rel = itr.next();
			rel.translateToSoarSpeak(goalId, "object-relation");
		}
		
	}

}
