package edu.umich.rosie.soarobjects;


import edu.umich.rosie.soar.ISoarObject;
import sml.Identifier;
/**
 * Represents a message message from the user on the input link
 * 
 * @author mininger
 * 
 */
public class Message implements ISoarObject
{
    // Latest message received
    private String message;
    private Integer messageNum;
    private boolean added = false;
    
    public Message(String message, Integer messageNum){
    	this.message = message;
    }
    
    // Identifier of the message on the input link
    private Identifier messageId;
    
    public boolean isAdded(){
    	return added;
    }
    
    public void addToWM(Identifier parentID){
    	if(added){
    		removeFromWM();
    	}
        messageId = parentID.CreateIdWME("sentence");
        messageId.CreateIntWME("sentence-number", messageNum);
        messageId.CreateStringWME("complete-sentence", message);
        messageId.CreateStringWME("spelling", "*");
        
        char lastChar = message.charAt(message.length()-1);
        char punct = '.';
        if(lastChar == '.' || lastChar == '!' || lastChar == '?'){
        	message = message.substring(0, message.length()-1);
        	punct = lastChar;
        }
        
        Identifier nextID = messageId.CreateIdWME("next");
        String[] words = message.split(" ");
        for(String word : words){
        	if(word.isEmpty()){
        		continue;
        	}
        	nextID.CreateStringWME("spelling", word);
        	nextID = nextID.CreateIdWME("next");
        }
        nextID.CreateStringWME("spelling", Character.toString(punct));
        nextID.CreateStringWME("next", "nil");
        added = true;
    }
    
    public void updateWM(){
    	// Nothing to do
    }
    
    public void removeFromWM(){
    	if(!added){
    		return;
    	}
    	messageId.DestroyWME();
    	messageId = null;
    	added = false;
    }
}
