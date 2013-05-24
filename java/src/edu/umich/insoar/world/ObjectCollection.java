package edu.umich.insoar.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import abolt.lcmtypes.object_data_t;
import abolt.lcmtypes.observations_t;
import sml.Identifier;

/**
 * Represents the collection of objects in the world, and adds them to the input link
 * Combines both sensables and objects into a uniform WorldObject type
 * 
 * @author mininger
 * 
 */
public class ObjectCollection implements IInputLinkElement
{
    // Root Identifier to add objects to
    private Identifier objectsId;
    
    // Mapping from object ids to objects
    private Map<Integer, WorldObject> objects;
    
    // True if a new observations_t has arrived since the last update
    private boolean hasChanged;
    
    public ObjectCollection(){
        objectsId = null;
        objects = new HashMap<Integer, WorldObject>();
        hasChanged = false;
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectsId == null){
            objectsId = parentIdentifier.CreateIdWME("objects");
        }
        
        if(hasChanged){
            for(WorldObject object : objects.values()){
            	synchronized(object){
                    object.updateInputLink(objectsId);
            	}
            }
        }
        hasChanged = false;
    }

    @Override
    public synchronized void destroy()
    {
        if(objectsId != null){
            for(WorldObject object : objects.values()){
                object.destroy();
            }
            objectsId.DestroyWME();
            objectsId = null;
        }
    }
    
    public synchronized void newObservation(observations_t observation){
    	Set<Integer> objectsToRemove = new HashSet<Integer>();
    	for(WorldObject object : objects.values()){
    		objectsToRemove.add(object.getId());
    	}
        
        // update each object from the object_data_t
        for(object_data_t objectData : observation.observations){
            objectsToRemove.remove(objectData.id);
            WorldObject object = objects.get(objectData.id);
            if(object == null){
                object = new WorldObject(objectData);
                objects.put(objectData.id, object);
                SVSConnector.Singleton().addObject(object);
            } else {
            	synchronized(object){
                    object.newObjectData(objectData);
            	}
            }   
        }
        
        // update each object from the sensables
        for(String sensable : observation.sensables){
            sensable = sensable.toLowerCase();
            
            Integer id = Integer.parseInt(WorldObject.getSensableId(sensable));
            if(id == null){
                continue;
            }
            objectsToRemove.remove(id);
            WorldObject object = objects.get(id);
            if(object == null){
                object = new WorldObject(sensable);
                objects.put(id, object);
                SVSConnector.Singleton().addObject(object);
            } else {
            	synchronized(object){
                    object.newSensableString(sensable);
            	}
            }   
        }
        
        for(Integer id : objectsToRemove){
        	SVSConnector.Singleton().removeObject(objects.get(id));
            objects.get(id).destroy();
            objects.remove(id);
        }
        hasChanged = true;
    }
    
    public synchronized WorldObject getObject(Integer id){
        return objects.get(id);
    }
}
