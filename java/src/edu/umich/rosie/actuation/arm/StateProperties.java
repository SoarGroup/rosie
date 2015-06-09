package edu.umich.rosie.actuation.arm;

import java.util.HashMap;
import java.util.HashSet;

import sml.Identifier;
import sml.StringElement;

public class StateProperties {
	protected class WMStructure{
		public Identifier propId;

		public StringElement valueWme;
		public String value;
		
		public WMStructure(Identifier parentId, String name, String value){
			propId = parentId.CreateIdWME("property");
			propId.CreateStringWME("name", name);
			propId.CreateStringWME("type", PerceptualProperty.STATE_TYPE);
			
			this.value = value;
			valueWme = propId.CreateStringWME("value", value);
		}
		public void update(String value){
			if(!value.equals(this.value)){
				this.value = value;
				valueWme.Update(value);
			}
		}
		public void destroy(){
			propId.DestroyWME();
			propId = null;
			valueWme = null;
		}
	}

	private HashMap<String, WMStructure> stateProperties;
	
    public StateProperties(){
    	stateProperties = new HashMap<String, WMStructure>();
    }

    public String getProperty(String propName){
    	if(stateProperties.containsKey(propName)){
    		return stateProperties.get(propName).value;
    	} else {
    		return null;
    	}
    }   
    
    public void updateProperties(Identifier parentId, String[] stateInfo){
		HashSet<String> propsToRemove = new HashSet<String>(stateProperties.keySet());
		
    	for(String nameValPair : stateInfo){
    		String[] nameValSplit = nameValPair.split("=");
    		if(nameValSplit.length < 2){
    			continue;
    		}
    		String propName = nameValSplit[0].toLowerCase();
    		String propVal = nameValSplit[1].toLowerCase();
    		
    		if(stateProperties.containsKey(propName)){
    			propsToRemove.remove(propName);
    			stateProperties.get(propName).update(propVal);
    		} else {
    			stateProperties.put(propName, new WMStructure(parentId, propName, propVal));
    		}
    	}
    	
    	for(String propName : propsToRemove){
    		stateProperties.get(propName).destroy();
    		stateProperties.remove(propName);
    	}
    }
    
    public void destroy(){
    	for(WMStructure propStruct : stateProperties.values()){
    		propStruct.destroy();
    	}
    	stateProperties.clear();
    }
}
