package edu.umich.rosie.actuation.arm;

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
			propertyNames.put(category_t.CAT_WEIGHT, "weight");
			propertyNames.put(category_t.CAT_TEMPERATURE, "temperature");
		}
		return propertyNames.get(propertyID);
	}
	
	public static final String VISUAL_TYPE = "visual";
	public static final String LINGUISTIC_TYPE = "linguistic";
	public static final String MEASURABLE_TYPE = "measurable";
	public static final String STATE_TYPE = "state";
	protected static HashMap<Integer, String> propertyTypes = null;
	public static String getPropertyType(Integer propertyCategory){
		if(propertyTypes == null){
			propertyTypes = new HashMap<Integer, String>();
			propertyTypes.put(category_t.CAT_COLOR, VISUAL_TYPE);
			propertyTypes.put(category_t.CAT_SHAPE, VISUAL_TYPE);
			propertyTypes.put(category_t.CAT_SIZE, VISUAL_TYPE);
			propertyTypes.put(category_t.CAT_LOCATION, LINGUISTIC_TYPE);
			propertyTypes.put(category_t.CAT_WEIGHT, MEASURABLE_TYPE);
			propertyTypes.put(category_t.CAT_TEMPERATURE, MEASURABLE_TYPE);
		}
		return propertyTypes.get(propertyCategory);
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
		} else if(propertyName.equals("weight")){
			return category_t.CAT_WEIGHT;
		} else if(propertyName.equals("temperature")){
			return category_t.CAT_TEMPERATURE;
		} else {
			return null;
		}
	}
	
	protected Identifier propId;
    
    protected String name;
    protected StringElement nameWme;
    
    protected String type;
    protected StringElement typeWme;
    
    protected Identifier valuesId;
    protected HashMap<String, Double> values;
    protected HashMap<String, FloatElement> valueWmes;
    
    protected FloatElement featureValWme;
    
    public PerceptualProperty(Identifier parentId, categorized_data_t catDat){
    	name = getPropertyName(catDat.cat.cat);
    	nameWme = null;
    	
    	type = getPropertyType(catDat.cat.cat);
    	typeWme = null;

    	valuesId = null;
    	values = new HashMap<String, Double>();
    	valueWmes = new HashMap<String, FloatElement>();

    	featureValWme = null;

    	updateProperty(parentId, catDat);
    }
    
    public String getPropertyName(){
    	return name;
    }
    
    public void updateProperty(Identifier parentId, categorized_data_t catDat){
    	if(propId == null){
    		propId = parentId.CreateIdWME("property");
    		nameWme = propId.CreateStringWME("name", name);
    		typeWme = propId.CreateStringWME("type", type);
    		valuesId = propId.CreateIdWME("values");
    		if(type.equals(MEASURABLE_TYPE)){
    			featureValWme = propId.CreateFloatWME("feature-val", 0.0);
    		}
    	}
    	
    	if(type.equals(MEASURABLE_TYPE)){
    		if(Math.abs(featureValWme.GetValue() - catDat.features[0]) > 0){
    			featureValWme.Update(catDat.features[0]);
    		}
    	}

    	HashSet<String> valuesToRemove = new HashSet<String>(valueWmes.keySet());

    	for(int i = 0; i < catDat.len; i++){
    		String valueName = catDat.label[i];
    		Double conf = catDat.confidence[i];
    		
    		if(valueWmes.containsKey(valueName)){
    			valuesToRemove.remove(valueName);
    			FloatElement el = valueWmes.get(valueName);
    			if(Math.abs(el.GetValue() - conf) > .01){
    				el.Update(conf);
    			}
    		} else {
    			valueWmes.put(valueName, valuesId.CreateFloatWME(valueName, conf));
    		}
   			values.put(valueName, conf);
    	}
    	
    	for(String valueName : valuesToRemove){
    		valueWmes.get(valueName).DestroyWME();
    		valueWmes.remove(valueName);
    		values.remove(valueName);
    	}
    }
    
    public void destroy(){
    	if(propId != null){
    		propId.DestroyWME();
    		propId = null;
    		nameWme = null;
    		typeWme = null;
    		valuesId = null;
    		valueWmes.clear();
    	}
    }
    
    public categorized_data_t getCatDat(){
    	return getCatDat(name, values);
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
