package edu.umich.insoar.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Agent;
import probcog.lcmtypes.*;
import april.util.TimeUtil;

/**
 * A single Object in the world, can be created using either a sensable or an object_data_t
 * 
 * @author mininger
 * 
 */
public class WorldObject 
{
    public static String getSensableId(String sensable){
        // Matches against id=ID followed by a comma, whitespace, or end of string
        // Assumes ID consists of numbers
        sensable = sensable.toLowerCase();
        Pattern p = Pattern.compile("id=(\\p{Digit})+(,|\\s|\\Z)");
        Matcher m = p.matcher(sensable);
        if(!m.find()){
            return null;
        }
        // m.group() returns a string like "id=ID,"
        // we trim, then split to get the actual ID
        String[] id = m.group().trim().split("(id=)|,");
        if(id.length < 2){
            return null;
        }
        //Note that the first element will be the empty string, we want the second
        return id[1];
    }
    
    // Name of the object (may be null if not named)
    protected String name;
    
    // Id of the object 
    protected int id;
    
    // Pose of the object
    protected double[] pose;
    
    // Bounding box of the object
    protected double[][] bbox;
    
    protected Map<String, String> perceptualProperties;
    
    protected Map<String, Double> perceptualConfidences;
    
    protected Map<String, StateProperty> stateProperties;
    
    private StringBuilder svsCommands;
    
    protected boolean isStale = false;
    
    protected object_data_t lastData = null;
    
    public WorldObject(object_data_t object){
        name = null;
        id = -1;
        pose = new double[3];
        bbox = new double[2][3];
        stateProperties = new HashMap<String, StateProperty>();
        perceptualProperties = new HashMap<String, String>();
        perceptualConfidences = new HashMap<String, Double>();
        
        svsCommands = new StringBuilder();
        
        create(object);
    }
    
    // Accessors
    public String getName(){
        return name;
    }
    
    public int getId(){
        return id;
    }
    
    public String getIdString(){
    	if(id == 0){
    		return "eye";
    	} else {
    		return (new Integer(id)).toString();
    	}
    }
    
    public double[] getPose(){
        return pose;
    }
    
    public double[][] getBBox(){
    	return bbox;
    }
    
    public synchronized String getValue(String attribute){
        return stateProperties.get(attribute).getValue();
    }
    
    public synchronized void updateProperty(String name, String value){
    	name = name.toLowerCase();
    	value = value.toLowerCase();
    	if(stateProperties.containsKey(name)){
    		stateProperties.get(name).update(value);
    	} else {
    		stateProperties.put(name, new StateProperty("state", name, value));
    	}
    }
    
    public synchronized void updateSVS(Agent agent){
    	agent.SendSVSInput(svsCommands.toString());
    	//System.out.println(svsCommands.toString());
    	svsCommands = new StringBuilder();
    }
    
    private synchronized void create(object_data_t objectData){ 
    	lastData = objectData;
    	
        id = objectData.id;
        pose = objectData.pos;
        bbox = objectData.bbox;
        
        svsCommands.append(SVSCommands.add(this));
        
        for(categorized_data_t category : objectData.cat_dat){
        	if(category.len == 0){
        		continue;
        	}
        	String propName = PerceptualProperty.getCategoryName(category.cat.cat);
        	String propValue = category.label[0];
        	String confidence = new Double(category.confidence[0]).toString();
        	perceptualProperties.put(propName, propValue);
        	perceptualConfidences.put(propName, category.confidence[0]);
        	svsCommands.append(SVSCommands.addProperty(this, propName, propValue));
        	svsCommands.append(SVSCommands.addProperty(this, propName + "-conf", confidence));
        }
    }
    
    public synchronized void update(object_data_t objectData){   
    	lastData = objectData;
    	
        pose = objectData.pos;
        svsCommands.append(SVSCommands.changePos(this));
        
        bbox = objectData.bbox;
        svsCommands.append(SVSCommands.changeBBox(this));
        
        if(isStale){
        	isStale = false;
        	svsCommands.append(SVSCommands.deleteProperty(this, "stale"));
        }
        
        HashSet<String> propertiesToRemove = new HashSet<String>(perceptualProperties.keySet());
        
        for(categorized_data_t category : objectData.cat_dat){
        	if(category.len == 0){
        		continue;
        	}
        	String propName = PerceptualProperty.getCategoryName(category.cat.cat);
        	String propValue = category.label[0];
        	String confidence = new Double(category.confidence[0]).toString();
        	String oldValue = perceptualProperties.get(propName);

        	perceptualConfidences.put(propName, category.confidence[0]);
        	if(oldValue == null){
        		// New property we haven't seen before, add it
            	perceptualProperties.put(propName, propValue);
            	svsCommands.append(SVSCommands.addProperty(this, propName, propValue));
            	svsCommands.append(SVSCommands.addProperty(this, propName + "-conf", confidence));
        	} else if(!oldValue.equals(propValue)){
        		// Existing property whose value has changed
            	perceptualProperties.put(propName, propValue);
            	svsCommands.append(SVSCommands.changeProperty(this, propName, propValue));
            	svsCommands.append(SVSCommands.changeProperty(this, propName + "-conf", confidence));
        	} else {
        		// Existing property whose value hasn't changed, only update confidence
            	svsCommands.append(SVSCommands.changeProperty(this, propName + "-conf", confidence));
        	}
    		propertiesToRemove.remove(propName);
        }
        
        for(String propName : propertiesToRemove){
        	// Properties that have disappeared, delete
        	perceptualProperties.remove(propName);
        	perceptualConfidences.remove(propName);
        	svsCommands.append(SVSCommands.deleteProperty(this, propName));
        	svsCommands.append(SVSCommands.deleteProperty(this, propName + "-conf"));
        }
    }
    
    public synchronized void mergeObject(WorldObject obj){	
    	if(obj.lastData != null){
    		update(obj.lastData);
    	}
    	if(obj.isStale){
    		this.markStale();
    	}
    }
    
    public synchronized void markStale(){
    	if(!isStale){
        	isStale = true;
        	svsCommands.append(SVSCommands.addProperty(this, "stale", "true"));
    	}
    }
    
    public synchronized void delete(){
    	svsCommands.append(SVSCommands.delete(this));
    }
    
    public synchronized object_data_t createObjectData(){
    	object_data_t objectData = new object_data_t();
    	objectData.id = id;
    	objectData.utime = TimeUtil.utime();
    	objectData.pos = pose;
    	objectData.bbox = bbox;
    	objectData.num_cat = 0;
    	objectData.cat_dat = new categorized_data_t[perceptualProperties.size()];
    	for(Map.Entry<String, String> prop : perceptualProperties.entrySet()){
    		categorized_data_t catDat = new categorized_data_t();
    		catDat.cat = new category_t();
    		catDat.cat.cat = PerceptualProperty.getCategoryID(prop.getKey());
    		catDat.len = 1;
    		catDat.label = new String[]{ prop.getValue() };
    		catDat.confidence = new double[]{0};
    		objectData.cat_dat[objectData.num_cat++] = catDat;
    	}
    	return objectData;
    }
}
