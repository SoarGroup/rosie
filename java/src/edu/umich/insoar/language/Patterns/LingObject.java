package edu.umich.insoar.language.Patterns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;

import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;

import sml.*;


public class LingObject extends LinguisticEntity {
    public static String TYPE = "LingObject";
	private String determiner = null;
	private Set<String> adjective;
	private String noun;
	
	public String getDeterminer(){
	    return determiner;
	}
	
	public Set<String> getAdjectives(){
	    return adjective;
	}
	
	public String getNoun(){
	    return noun;
	}
	
	public void extractLinguisticComponents(String string, Map tagsToWords){
		adjective = new HashSet();
		Pattern p = Pattern.compile("DT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			determiner = tagsToWords.get(m.group()).toString();
		}
		
		p = Pattern.compile("JJ\\d*");
		m = p.matcher(string);
		while(m.find()){
			adjective.add(tagsToWords.get(m.group()).toString());
		}
		
		p = Pattern.compile("NN\\d*");
		m = p.matcher(string);
		while(m.find()){
			noun = tagsToWords.get(m.group()).toString();
		}
	}
	
	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		id.CreateStringWME("type", "object-message");
	    id.CreateStringWME("originator", "instructor");
	    Identifier fieldsId = id.CreateIdWME("information");
		Identifier objectId = fieldsId.CreateIdWME("object");
	    objectId.CreateStringWME("word", noun);
		if (determiner != null){
			if(determiner.equals("the")){
				determiner = "DEF";
			}
			objectId.CreateStringWME("specifier", determiner);
		}
		if (adjective != null){
			Iterator itr = adjective.iterator();
			while (itr.hasNext()){
				objectId.CreateStringWME("word", itr.next().toString());
			}
		}
	}
	
	public static LingObject createFromSoarSpeak(Identifier id){
	    // Assumes id is the root of the object
        if(id == null){
            return null;
        }
	    LingObject lingObject = new LingObject();
        lingObject.noun = WMUtil.getValueOfAttribute(id, "word");
        lingObject.adjective = WMUtil.getAllValuesOfAttribute(id, "adjective");
        lingObject.determiner = WMUtil.getValueOfAttribute(id, "determiner");
        //use "an" instead of "a" if the following word begins with a vowel
        //should this be in soar rules instead?
        //JK Need to check if there is a determiner first or will get NPE
        if(lingObject.determiner != null && lingObject.determiner.equals("a")) {
        	String adj;
        	try{
        		adj = lingObject.adjective.iterator().next();
        	} catch (Exception e) {
        		adj = null;
        	}
        	String n = lingObject.noun;
        	if(adj != null && adj.length() > 0) {
        		if(adj.matches("^[aeiouAEIOU].*")) {
        			lingObject.determiner = "an";
        		}
        	} else if(n != null) {
        		if(n.matches("^[aeiouAEIOU].*")) {
        			lingObject.determiner = "an";
        		}
        	}
        }
        return lingObject;
	}
	
	public static LingObject createFromSoarSpeak(Identifier id, String name){
	    // Assumes id ^name <objectId> is the root of the object
        if(id == null){
            return null;
        }
        Identifier objectId = WMUtil.getIdentifierOfAttribute(id, name);
        return LingObject.createFromSoarSpeak(objectId);
	}
	
    public static Set<LingObject> createAllFromSoarSpeak(Identifier id, String name){
        Set<LingObject> lingObjects = new HashSet<LingObject>();
        for(int index = 0; index < id.GetNumberChildren(); index++){
            WMElement wme = id.GetChild(index);
            if(wme.GetAttribute().equals(name)){
                lingObjects.add(LingObject.createFromSoarSpeak(wme.ConvertToIdentifier()));
            }
        }
        return lingObjects;
    }
    
    @Override
    public String toString(){
    	if(noun == null){
    		noun = "object";
    	}
    	if(determiner == null){
    		determiner = "";
    	}
        String adjString = "";
        for(Iterator<String> i = adjective.iterator(); i.hasNext(); ){
            adjString += i.next() + " ";
        }        
        return String.format("%s %s%s", determiner, adjString, noun);
    }
}
