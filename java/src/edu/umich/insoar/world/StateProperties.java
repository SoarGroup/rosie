package edu.umich.insoar.world;

import java.util.HashMap;
import java.util.HashSet;

import sml.Identifier;

public class StateProperties {
	private HashMap<String, String> properties;
	
	private StringBuilder svsCommands;
	
	private String parentName;
    
    public StateProperties(String parentName){
    	this.parentName = parentName;
    	
    	this.properties = new HashMap<String, String>();
    	this.svsCommands = new StringBuilder();
    }

    public String getProperty(String propName){
    	if(properties.containsKey(propName)){
    		return properties.get(propName);
    	} else {
    		return null;
    	}
    }   
    
    public void updateSVS(StringBuilder svsCommands){
    	svsCommands.append(this.svsCommands.toString());
    	this.svsCommands = new StringBuilder();
    }
    
    public void updateProperties(String[] stateInfo){
		HashSet<String> propsToRemove = new HashSet<String>(properties.keySet());
		
    	for(String nameValPair : stateInfo){
    		String[] nameValSplit = nameValPair.split("=");
    		if(nameValSplit.length < 2){
    			continue;
    		}
    		String propName = nameValSplit[0].toLowerCase();
    		String propVal = nameValSplit[1].toLowerCase();
    		
    		if(properties.containsKey(propName)){
        		propsToRemove.remove(propName);
        		if(!properties.get(propName).equals(propVal)){
            		svsCommands.append(SVSCommands.changeProperty(parentName, propName + ".value", propVal));
        		}
    		} else {
    			svsCommands.append(SVSCommands.addProperty(parentName, propName + ".type", "state"));
    			svsCommands.append(SVSCommands.addProperty(parentName, propName + ".value", propVal));
    		}
    		properties.put(propName, propVal);
    	}
    	
    	for(String propName : propsToRemove){
    		svsCommands.append(SVSCommands.deleteProperty(parentName, propName));
    		properties.remove(propName);
    	}
    }
    
    public void deleteProperties(){
    	for(String propName : properties.keySet()){
    		svsCommands.append(SVSCommands.deleteProperty(parentName, propName));
    	}
    	properties.clear();
    }
}
