package edu.umich.insoar.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import sml.Agent;
import probcog.lcmtypes.*;
import probcog.util.BoundingBox;
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
    
    protected ArrayList<object_data_t> lastData;
    
    
    protected Map<String, PerceptualProperty> perceptualProperties;
    
    protected StateProperties stateProperties;
    
    private StringBuilder svsCommands;
    
    public WorldObject(Integer id, ArrayList<object_data_t> objDatas){
        name = null;
        this.id = id;
        bboxPos = new double[3];
        bboxRot = new double[3];
        bboxSize = new double[3];
        centroid = new double[3];
        perceptualProperties = new HashMap<String, PerceptualProperty>();
        stateProperties = new StateProperties(getIdString());
        
        svsCommands = new StringBuilder();
        
        create(id, objDatas);
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
    
    
    public ArrayList<object_data_t> getLastDatas(){
    	return lastData;
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
    
    private synchronized void create(Integer id, ArrayList<object_data_t> objDatas){ 
    	lastData = objDatas;
    	
    	svsCommands.append(SVSCommands.add(getIdString()));
		svsCommands.append(SVSCommands.addProperty(getIdString(), "object-source", "perception"));
		svsCommands.append(SVSCommands.addProperty(getIdString(), "num-sources", new Integer(objDatas.size()).toString()));
    	
    	updateBbox(objDatas);
		updateProperties(objDatas);
		
    }
    
    public synchronized void update(ArrayList<object_data_t> objDatas){   
    	lastData = objDatas;
    	svsCommands.append(SVSCommands.changeProperty(getIdString(),  "num-sources",  new Integer(objDatas.size()).toString()));
        updateBbox(objDatas);
        updateProperties(objDatas);
    }
    
    
    private void updateProperties(ArrayList<object_data_t> objDatas){
    	if(objDatas.size() > 1){
    		// We don't actually know how to combine the properties, so we report as unknown for now
    		HashMap<String, Double> unknownValues = new HashMap<String, Double>();
    		unknownValues.put("unknown", 0.0);

    		for(PerceptualProperty p : perceptualProperties.values()){
    			p.updateProperty(PerceptualProperty.getCatDat(p.getPropertyName(), unknownValues));
    		}
    		
    		stateProperties.updateProperties(new String[0]);

    	} else {
    		// Just one object, update using that
    		object_data_t objectData = objDatas.get(0);
    		
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

    		stateProperties.updateProperties(objectData.state_values);
    	}
         
        for(PerceptualProperty pp : perceptualProperties.values()){
        	pp.updateSVS(svsCommands);
        }
         
        stateProperties.updateSVS(svsCommands);
    }
    
    private void updateBbox(ArrayList<object_data_t> objDatas){
    	if(objDatas.size() == 1){
    		// Nothing fancy, just update using the given information
    
    		object_data_t objectData = objDatas.get(0);
        	
   	     	setBBox(objectData.bbox_xyzrpy, objectData.bbox_dim);
   	     	for(int i = 0; i < 3; i++){
   	     		centroid[i] = objectData.pos[i];
   	     	}
    	} else {
    		// Combine multiple bounding boxes into 1,
    		// we generate all the points on the corners of each bbox
    		// then calculate a new oriented bbox based on those
    		
    		ArrayList<double[]> points = new ArrayList<double[]>();
    		
    		for(object_data_t objectData : objDatas){
    			// Position + Rotation Matrix
    			double[][] pr = LinAlg.xyzrpyToMatrix(objectData.bbox_xyzrpy);
    			// Position + Rotation + Scaling Matrix
    			double[][] prs = LinAlg.matrixAB(pr, LinAlg.scale(objectData.bbox_dim[0]/2, objectData.bbox_dim[1]/2, objectData.bbox_dim[2]/2));

    			// Center of the box
    			double[] p = new double[]{objectData.bbox_xyzrpy[0], objectData.bbox_xyzrpy[1], objectData.bbox_xyzrpy[2]};
    			// Each Axis of the box
    			double[] x = new double[]{prs[0][0], prs[1][0], prs[2][0]};
    			double[] y = new double[]{prs[0][1], prs[1][1], prs[2][1]};
    			double[] z = new double[]{prs[0][2], prs[1][2], prs[2][2]};
    			
    			for(int i = -1; i <= 1; i += 2){ 
    				for(int j = -1; j <= 1; j += 2){
    					for(int k = -1; k <= 1; k += 2){
    						// Each corner of the box
    						double[] v = LinAlg.add(LinAlg.scale(x, i),  LinAlg.add(LinAlg.scale(y, j), LinAlg.scale(z, k)));
    						points.add(LinAlg.add(p, v));
    					}
    				}
    			}
    		}
    		
			
			BoundingBox bbox = BoundingBox.getMinimalXY(points);
			
    		setBBox(bbox.xyzrpy, bbox.lenxyz);
    		for(int i = 0; i < 3; i++){
    			centroid[i] = bbox.xyzrpy[i];
    		}
    	}
    }
}
