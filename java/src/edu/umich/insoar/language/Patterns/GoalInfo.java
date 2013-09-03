package edu.umich.insoar.language.Patterns;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class GoalInfo extends LinguisticEntity{
    public static String TYPE = "GoalInfo";
	private Set<ObjectRelation> objectRelation;
	private Set<ObjectState> objectState;

	
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		
		objectRelation = new HashSet();
		objectState = new HashSet();
		//Pattern p = Pattern.compile();
		
		Pattern p = Pattern.compile("REL\\d*");
		Matcher m = p.matcher(string);
		while(m.find()){
			objectRelation.add((ObjectRelation) tagsToWords.get(m.group()));
		}
		
		p = Pattern.compile("STT\\d*");
		m = p.matcher(string);
		while(m.find()){
			objectState.add((ObjectState) tagsToWords.get(m.group()));
		}
		
	}
	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier messageId = id;
		messageId.CreateStringWME("type", "goal-relation-message");
		Identifier goalId = messageId.CreateIdWME("information");
		Iterator<ObjectRelation> itr = objectRelation.iterator();
		while(itr.hasNext()){
			ObjectRelation rel = itr.next();
			rel.translateToSoarSpeak(goalId, "relation");
		}
		Iterator<ObjectState> itro = objectState.iterator();
		while(itro.hasNext()){
			ObjectState state = itro.next();
			state.translateToSoarSpeak(goalId, "state-predicate");
		}
		
	}
	
	
    
}
