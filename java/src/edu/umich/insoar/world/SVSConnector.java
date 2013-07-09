package edu.umich.insoar.world;

import java.util.HashMap;
import sml.Agent;

public class SVSConnector {
	public static SVSConnector Singleton(){
		return instance;
	}
	private static SVSConnector instance = null;
    
    private HashMap<Integer, WorldObject> newObjects;
    private HashMap<Integer, WorldObject> existingObjects;
    private HashMap<Integer, WorldObject> oldObjects;
    
    public SVSConnector(){
    	instance = this;
    	
        newObjects = new HashMap<Integer, WorldObject>();
        existingObjects = new HashMap<Integer, WorldObject>();
        oldObjects = new HashMap<Integer, WorldObject>();
    }
    
    public synchronized void addObject(WorldObject obj){
    	if(newObjects.containsKey(obj.getId())){
    		// Trying to add twice, replace the object
    		newObjects.put(obj.getId(), obj);
    	} else if(existingObjects.containsKey(obj.getId())){
    		// That object already exists, so replace the one in the map
    		existingObjects.put(obj.getId(), obj);
    	} else if(oldObjects.containsKey(obj.getId())){
    		//The object is marked for deletion, remove it from the delete list
    		oldObjects.remove(obj.getId());
    		existingObjects.put(obj.getId(), obj);
    	} else {
    		// The object really is new, add it to the new object list
    		newObjects.put(obj.getId(), obj);
    	}
    }
    
    public synchronized void removeObject(WorldObject obj){
    	if(newObjects.containsKey(obj.getId())){
    		// Remove an object you are currently adding
    		newObjects.remove(obj.getId());
    	} else if(existingObjects.containsKey(obj.getId())){
    		// Add the object to the delete list
    		existingObjects.remove(obj.getId());
    		oldObjects.put(obj.getId(), obj);
    	} else if(oldObjects.containsKey(obj.getId())){
    		//The object is already marked for deletion
    		return;
    	} else {
    		// The object doesn't exist anyway
    		return;
    	}
    }
    
    public synchronized void updateSVS(Agent agent)
    {
    	String s = "";
    	
    	// Delete old objects
    	for(WorldObject object : oldObjects.values()){
    		synchronized(object){
                s+= "d " + object.getId() + "\n";
                //System.out.println("d " + id + "\n");
    		}
    	}
    	oldObjects.clear();
    	
    	// Update Existing Objects
    	for(WorldObject object : existingObjects.values()){
    		synchronized(object){
    			if(object.hasChanged){
                    Pose pose = object.pose;
                    s += "c " + object.getId() + " v " + object.getBBox().getFullPoints()
                    		+ " p " + pose.getX() + " " + 
                                pose.getY() + " " + pose.getZ() + "\n";
                    //System.out.println("c " + object.getId() + " p " + pose.getX() + " " + pose.getY() + " " + pose.getZ() + "\n");
                    object.hasChanged = false;
    			}
    		}
    	}
    	
    	// Add New Objects
    	for(WorldObject object : newObjects.values()){
    		synchronized(object){
                Pose pose = object.pose;
                s+= "a " + object.getId() + " object world v ";
                //System.out.println(object.getId());
                s+= object.getBBox().getFullPoints();
                System.out.println("a " + object.getId() + " world v " + object.getBBox().getFullPoints());
                s+= " p " + pose.getX() + " " + pose.getY() + " " + pose.getZ() + "\n";
                
                existingObjects.put(object.getId(), object);
    		}
    	}
    	newObjects.clear();
    	
    	if(!s.isEmpty()){
        	agent.SendSVSInput(s);
    	}
    }
    
    public synchronized void reset(){
    	System.out.println("Performing SVS Reset");
    	for(WorldObject obj : existingObjects.values()){
    		newObjects.put(obj.getId(), obj);
    	}
    	existingObjects.clear();
    	oldObjects.clear();
    }
}
