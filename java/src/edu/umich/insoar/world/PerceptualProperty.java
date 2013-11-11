package edu.umich.insoar.world;

import java.util.*;
import probcog.lcmtypes.*;
import sml.*;

/**
 * A category for each object, which contains several possible labels and their confidences
 * 
 * @author mininger
 * 
 */
public class PerceptualProperty
{   
	protected static HashMap<Integer, String> propertyNames = null;
	
	public static String getPropertyName(Integer propertyID){
		if(propertyNames == null){
			propertyNames = new HashMap<Integer, String>();
			propertyNames.put(category_t.CAT_COLOR, "color");
			propertyNames.put(category_t.CAT_SHAPE, "shape");
			propertyNames.put(category_t.CAT_SIZE, "size");
			propertyNames.put(category_t.CAT_LOCATION, "name");
		}
		return propertyNames.get(propertyID);
	}
	
	public static Integer getPropertyID(String propertyName){
		if(propertyName.equals("color")){
			return category_t.CAT_COLOR;
		} else if(propertyName.equals("shape")){
			return category_t.CAT_SHAPE;
		} else if(propertyName.equals("size")){
			return category_t.CAT_SIZE;
		} else if(propertyName.equals("name")){
			return category_t.CAT_LOCATION;
		} else {
			return null;
		}
	}
    
    protected String parentName;
    
    protected String propName;
    
    protected Integer propId;
    
    protected HashMap<String, Double> values;
    
    protected StringBuilder svsCommands;

    public PerceptualProperty(String parentName, categorized_data_t category){
    	this.parentName = parentName;
    	this.propName = getPropertyName(category.cat.cat);
    	this.propId = category.cat.cat;
    	this.values = new HashMap<String, Double>();
    	this.svsCommands = new StringBuilder();
    	
        svsCommands.append(SVSCommands.addProperty(parentName, propName + ".type", "visual"));

    	updateProperty(category);
    }
    
    public String getPropertyName(){
    	return propName;
    }
    
    public Integer getPropertyID(){
    	return propId;
    }
    
    public void updateSVS(StringBuilder svsCommands){
    	svsCommands.append(this.svsCommands.toString());
    	this.svsCommands = new StringBuilder();
    }
    
    public void updateProperty(categorized_data_t data){
    	HashSet<String> valuesToRemove = new HashSet<String>(values.keySet());
    	
    	for(int i = 0; i < data.len; i++){
    		String valueName = data.label[i];
    		Double conf = data.confidence[i];
    		String fullName = propName + "." + valueName;
    		
    		if(values.containsKey(valueName)){
    			valuesToRemove.remove(valueName);
    			svsCommands.append(SVSCommands.changeProperty(parentName, fullName, conf.toString()));
    		} else {
    			svsCommands.append(SVSCommands.addProperty(parentName, fullName, conf.toString()));
    		}
    		values.put(valueName, conf);
    	}
    	
    	for(String valueName : valuesToRemove){
    		String fullName = propName + "." + valueName;
    		svsCommands.append(SVSCommands.deleteProperty(parentName, fullName));
    	}
    }
    
    public void deleteProperty(){
    	for(String valueName : values.keySet()){
    		String fullName = propName + "." + valueName;
    		svsCommands.append(SVSCommands.deleteProperty(parentName, fullName));
    	}
    	values.clear();categorized_data_t catDat = new categorized_data_t();
		catDat.cat = new category_t();
    	svsCommands.append(SVSCommands.deleteProperty(parentName, propName + ".type"));
    }
    
    public categorized_data_t getCatDat(){
    	return getCatDat(propName, values);
    }
    
    public static categorized_data_t getCatDat(String propName, HashMap<String, Double> values){
    	categorized_data_t catDat = new categorized_data_t();
		catDat.cat = new category_t();
		catDat.cat.cat = PerceptualProperty.getPropertyID(propName);
		catDat.len = values.size();
		catDat.label = new String[catDat.len];
		catDat.confidence = new double[catDat.len];
		
		int i = 0;
		for(Map.Entry<String, Double> val : values.entrySet()){
			catDat.label[i] = val.getKey();
			catDat.confidence[i] = val.getValue();
			i++;
		}
		
		return catDat;
    }
}
