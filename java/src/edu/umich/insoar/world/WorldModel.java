package edu.umich.insoar.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import lcm.lcm.LCM;

import sml.Agent.RunEventInterface;
import sml.Agent;
import sml.Identifier;
import sml.smlRunEventId;
import probcog.lcmtypes.*;
import april.util.TimeUtil;
import edu.umich.insoar.SoarAgent;
public class WorldModel implements RunEventInterface
{    
    
    // Mapping from object ids to objects
    private Map<Integer, WorldObject> objects;
    
    private Map<Integer, Integer> merged;
   
    private observations_t newObservation = null;
    
    private SoarAgent soarAgent;

    public WorldModel(SoarAgent soarAgent){
    	this.soarAgent = soarAgent;
    	
        // Setup Input Link Events
        soarAgent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);

        objects = new HashMap<Integer, WorldObject>();
        
        merged = new HashMap<Integer, Integer>();
  
        soarAgent.getAgent().SendSVSInput("a eye object world b .01 p 0 0 0");
    }    
    
    public void reset(){
    	for(WorldObject object : objects.values()){
    		object.delete();
    		object.updateSVS(soarAgent.getAgent());
    	}
    	objects.clear();
    	merged.clear();
    	soarAgent.commitChanges();
    }

    public synchronized void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	if(newObservation == null){
    		return;
    	}
    	double[] eye = newObservation.eye;
    	agent.SendSVSInput(String.format("c eye p %f %f %f", eye[0], eye[1], eye[2]));
    	
    	Set<Integer> staleObjects = new HashSet<Integer>();
    	for(WorldObject object : objects.values()){
    		staleObjects.add(object.getId());
    	}
    	
    	//Set<Integer> staleMerged = new HashSet<Integer>(merged.keySet());
        
        // update each object from the object_data_t
        for(object_data_t objectData : newObservation.observations){
        	Integer id = merged.get(objectData.id);
        	if(id == null){
        		// Not a merged object, just use the plain old id
        		id = objectData.id;
        	} else {
        		// The reported id has been merged, use the stored id instead
        		objectData.id = id;
        	}
        	
        	WorldObject object = objects.get(id);
        	
        	if(object == null){
        		// Not a regular object or merged, must be new
                object = new WorldObject(objectData);
                objects.put(objectData.id, object);
        	} else if(object != null){
        		staleObjects.remove(id);
        		object.update(objectData);
        	}
        }
        
        // Mark all old objects as stale
        for(Integer id : staleObjects){
        	objects.get(id).markStale();
        }
        
        // Remove all stale merged objects
//        for(Integer id : staleMerged){
//        	merged.remove(id);
//        }
        
        // Update the SVS link with the new information
        for(WorldObject object : objects.values()){
        	object.updateSVS(agent);
        }
        
        newObservation = null;
    }
    
    public synchronized void mergeObject(Integer originalId, Integer copyId){
    	WorldObject original = objects.get(originalId);
    	WorldObject copy = objects.get(copyId);
    	if(original == null || copy == null){
    		return;
    	}
    	original.mergeObject(copy);
    	original.updateSVS(soarAgent.getAgent());
    	removeObject(copyId);
    	merged.put(copyId, original.id);
    }
    
    public synchronized void removeObject(Integer id){
    	WorldObject object = objects.get(id);
    	if(object != null){
    		object.delete();
    		objects.remove(id);
    		object.updateSVS(soarAgent.getAgent());
    	}
//    	Set<Integer> mergedToRemove = new HashSet<Integer>();
//    	for(Map.Entry<Integer, WorldObject> e : merged.entrySet()){
//    		if(e.getValue() == object){
//    			mergedToRemove.add(e.getKey());
//    		}
//    	}
//    	
//    	for(Integer oldId : mergedToRemove){
//    		merged.remove(oldId);
//    	}
    }
    
    public synchronized void moveObject(Integer id, double x, double y, double z){
    	WorldObject object = objects.get(id);
    	if(object != null){
    		//System.out.println("MOVING OBJECT " + id + " to " + x + ", " + y + ", " + z);
    		object.moveObject(new double[]{x, y, z});
    		object.updateSVS(soarAgent.getAgent());
    		soarAgent.commitChanges();
    	}
    }
    
    public synchronized void newObservation(observations_t observation){
    	this.newObservation = observation;
    }
    
    public synchronized void sendObservation(){
    	soar_objects_t outgoingObs = new soar_objects_t();
    	outgoingObs.utime = TimeUtil.utime();
    	outgoingObs.num_objects = 0;
    	outgoingObs.objects = new object_data_t[objects.size()];
    	for(WorldObject obj : objects.values()){
    		outgoingObs.objects[outgoingObs.num_objects++] = obj.createObjectData();
    	}
    	
    	LCM.getSingleton().publish("SOAR_OBJECTS", outgoingObs);
    }
}
