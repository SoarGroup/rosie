package edu.umich.insoar.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import sml.Agent;
import probcog.lcmtypes.*;
import april.jmat.LinAlg;
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
    
    // Information about the bounding box
    // Center of the bounding box (XYZ)
    protected double[] bboxPos;
    
    // Orientation of the bounding box (RPY)
    protected double[] bboxRot;
    
    // Size of the bounding box (dX, dY, dZ)
    protected double[] bboxSize;
    
    protected double[] centroid;
    
    
    protected Map<String, PerceptualProperty> perceptualProperties;
    
    protected StateProperties stateProperties;
    
    private StringBuilder svsCommands;
    
    protected boolean isStale = false;
    
    protected object_data_t lastData = null;
    
    protected boolean isNew = false;
    
    public WorldObject(object_data_t object){
        name = null;
        id = object.id;
        bboxPos = new double[3];
        bboxRot = new double[3];
        bboxSize = new double[3];
        centroid = new double[3];
        perceptualProperties = new HashMap<String, PerceptualProperty>();
        stateProperties = new StateProperties(getIdString());
        
        svsCommands = new StringBuilder();
        
        create(object);
    }
    
    // ID: Get
    public int getId(){
        return id;
    }
    
    public String getIdString(){
    	return (new Integer(id)).toString();
    }
    
    // Name: Get/Set
    public String getName(){
        return name;
    }
    
    public void setName(String name){
    	this.name = name;
    	svsCommands.append(SVSCommands.changeProperty(getIdString(), "name", name));
    }
    
    // Pose: Get
    // Pose is a 3-tuple consisting of XYZ
    
    public double[] getPos(){
        return bboxPos;
    }
    
    public void setPos(double[] pos){
    	this.bboxPos = pos;
    	svsCommands.append(SVSCommands.changePos(getIdString(), pos));
    }
    
    // Rot: Get
    // Rot is a 3-tuple representing the rotation of the object in Roll-Pitch-Yaw
    
    public double[] getRot(){
    	return bboxRot;
    }
    
    // Size: Get
    // Size is a 3-tuple representing the size of the object's bounding box (XYZ)
    
    public double[] getSize(){
    	return bboxSize;
    }
    
    
    // Set Bounding Box Info
    public void setBBox(double[] xyzrpy, double[] size){
    	for(int i = 0; i < 3; i++){
    		this.bboxPos[i] = xyzrpy[i];
    		this.bboxRot[i] = xyzrpy[3+i];
    		this.bboxSize[i] = size[i];
    	}
    	
    	svsCommands.append(SVSCommands.changePos(getIdString(), bboxPos));
    	svsCommands.append(SVSCommands.changeRot(getIdString(), bboxRot));
    	svsCommands.append(SVSCommands.changeSize(getIdString(), bboxSize));
    }
    
    public void moveObject(double[] newPos){
    	double[] diff = LinAlg.subtract(newPos, bboxPos);
    	this.bboxPos = newPos;
    	this.centroid = LinAlg.add(centroid, diff);
    	svsCommands.append(SVSCommands.changePos(getIdString(), bboxPos));
    }

    
    public synchronized void updateSVS(Agent agent){
    	if(svsCommands.length() > 0){
        	//System.out.println(svsCommands.toString());
        	agent.SendSVSInput(svsCommands.toString());
        	svsCommands = new StringBuilder();
    	}
    }
    
    private synchronized void create(object_data_t objectData){ 
    	lastData = objectData;
    	
		id = objectData.id;

		svsCommands.append(SVSCommands.add(getIdString(), bboxPos, bboxRot, bboxSize));
		
		setBBox(objectData.bbox_xyzrpy, objectData.bbox_dim);
		for(int i = 0; i < 3; i++){
	          centroid[i] = objectData.pos[i];
	    }
		
		
		for(categorized_data_t category : objectData.cat_dat){
			if(category.cat.cat == category_t.CAT_LOCATION){
				this.name = category.label[0].toLowerCase();
				svsCommands.append(SVSCommands.addProperty(getIdString(), "name", name));
				continue;
			}
			String propName = PerceptualProperty.getPropertyName(category.cat.cat);
			PerceptualProperty pp = new PerceptualProperty(getIdString(), category);
			perceptualProperties.put(propName, pp);
			pp.updateSVS(svsCommands);
		}
		
		stateProperties.updateProperties(objectData.state_values);
		stateProperties.updateSVS(svsCommands);
		
		isNew = true;
		svsCommands.append(SVSCommands.addProperty(getIdString(), "newly-created", "true"));
        
        //System.out.println("CREATE " + id);
    }
    
    public synchronized void update(object_data_t objectData){   
    	lastData = objectData;
        
        setBBox(objectData.bbox_xyzrpy, objectData.bbox_dim);
        for(int i = 0; i < 3; i++){
            centroid[i] = objectData.pos[i];
        }
        
        
        if(isStale){
        	isStale = false;
        	svsCommands.append(SVSCommands.deleteProperty(getIdString(), "stale"));
        }
        
        if(isNew){
        	isNew = false;
            svsCommands.append(SVSCommands.deleteProperty(getIdString(), "newly-created"));
        }
        
        for(categorized_data_t category : objectData.cat_dat){
        	if(category.cat.cat == category_t.CAT_LOCATION){
        		if(!category.label[0].toLowerCase().equals(name)){
        			this.name = category.label[0].toLowerCase();
        			svsCommands.append(SVSCommands.changeProperty(getIdString(), "name", name));
        		}
        		continue;
        	}
        	String propName = PerceptualProperty.getPropertyName(category.cat.cat);
        	if(perceptualProperties.containsKey(propName)){
        		perceptualProperties.get(propName).updateProperty(category);
        	} else {
        		PerceptualProperty pp = new PerceptualProperty(getIdString(), category);
        		perceptualProperties.put(propName, pp);
        	}
        }
        
        for(PerceptualProperty pp : perceptualProperties.values()){
        	pp.updateSVS(svsCommands);
        }
        
        stateProperties.updateProperties(objectData.state_values);
        stateProperties.updateSVS(svsCommands);
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
    		//System.out.println("STALE  " + id);
        	isStale = true;
        	svsCommands.append(SVSCommands.addProperty(getIdString(), "stale", "true"));
    	}
    }
    
    public synchronized void delete(){
    	//System.out.println("DELETE " + id);
    	svsCommands.append(SVSCommands.delete(getIdString()));
    }
    
    public synchronized object_data_t createObjectData(){
    	object_data_t objectData = new object_data_t();
    	objectData.id = id;
    	objectData.utime = TimeUtil.utime();
    	objectData.bbox_xyzrpy = new double[]{bboxPos[0], bboxPos[1], bboxPos[2], bboxRot[0], bboxRot[1], bboxRot[2]};
    	objectData.bbox_dim = bboxSize;
    	objectData.pos = new double[]{centroid[0], centroid[1], centroid[2], 0, 0, 0};
        
        int i = 0;
        objectData.num_cat = perceptualProperties.size();
        objectData.cat_dat = new categorized_data_t[objectData.num_cat];
        for(PerceptualProperty pp : perceptualProperties.values()){
        	objectData.cat_dat[i++] = pp.getCatDat();
        }
    	return objectData;
    }
}
