package edu.umich.insoar.language;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sml.Identifier;
import sml.WMElement;
import edu.umich.insoar.world.WMUtil;


public class LingObject {
    public static String TYPE = "LingObject";
	private String determiner = null;
	private Set<String> adjective;
	private String pronoun = null;
	private String noun;
	private Identifier rootId;
	
	public String getDeterminer(){
	    return determiner;
	}
	public void setDeterminer(String det){
	    determiner = det;
	}
	
	public Set<String> getAdjectives(){
	    return adjective;
	}
	
	public String getNoun(){
	    return noun;
	}
	
	public String getPronoun(){
		return pronoun;
	}
	
	public Identifier getRoot(){
		return rootId;
	}
	
	public static LingObject createFromSoarSpeak(Identifier id){
		
		/*
		 * SM: Had to change this to work correctly with goal definitions with multiple predicates (ObjectRelation, ObjectState)
		 * It is likely that this will produce incorrect parse for ObjectResponse
		 * 
		 */
		
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
    
    public void setValue(String attr, String val){
    	rootId.CreateStringWME(attr, val);
    }
}
