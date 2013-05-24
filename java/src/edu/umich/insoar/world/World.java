package edu.umich.insoar.world;

import java.util.HashSet;
import java.util.Set;

import sml.Identifier;
import abolt.lcmtypes.observations_t;
public class World implements IInputLinkElement
{
    public static World Singleton(){
    	return instance;
    }
    private static World instance = null;
    
    private ObjectCollection objects;
    
    private WorldTime worldTime;
    
    private Messages messages;
    
    private PointedObject pointedObject;
    
    private Set<IInputLinkElement> inputLinkElements;
    
    private SVSConnector svsConnector;

    private RobotArm robotArm;
    
    private int last_click_id = -1;


    public World(){
    	instance = this;
    	
        inputLinkElements = new HashSet<IInputLinkElement>();
        
        objects = new ObjectCollection();
        
        svsConnector = new SVSConnector();

        worldTime = new WorldTime();
        
        messages = new Messages();
        
        pointedObject = new PointedObject(-1);
        
        robotArm = new RobotArm();
        
        //TODO debugging
        //todelete = -1;
        inputLinkElements.add(objects);
        inputLinkElements.add(worldTime);
        inputLinkElements.add(messages);
        inputLinkElements.add(pointedObject);
        inputLinkElements.add(robotArm);
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        for(IInputLinkElement element : inputLinkElements){
            element.updateInputLink(parentIdentifier);
        }
    }

    @Override
    public synchronized void destroy()
    {
        for(IInputLinkElement element : inputLinkElements){
            element.destroy();
        }
    }
    
    public void destroyMessage(){
    	messages.destroy();
    }
    
    public void destroyMessage(Integer id){
    	if (messages.getIdNumber() == id){
    		messages.destroy();
    	}
    	else return;
    }

    public synchronized void newObservation(observations_t observation){
        objects.newObservation(observation);
        worldTime.newObservation(observation);
        if(last_click_id != observation.click_id) {
        	pointedObject.setObjectID(observation.click_id);
        }
        last_click_id = observation.click_id;
    }
    
    public synchronized void newMessage(String message){
        messages.addMessage(message);
    }
    
    public synchronized void setPointedObjectID(int id) {
    	pointedObject.setObjectID(id);
    }
    
    public int getPointedObjectID(){
        return pointedObject.getObjectID();
    }
    
    public WorldObject getObject(Integer id){
        return objects.getObject(id);
    }
    
    public long getTime(){
        return worldTime.getMicroseconds();
    }
    
    public int getSteps(){
        return worldTime.getSteps();
    }
    
    public RobotArm getRobotArm(){
    	return robotArm;
    }
}
