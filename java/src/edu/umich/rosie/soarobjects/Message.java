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
    	this.message = message.trim().replace("\u200b", "");
    	this.messageNum = messageNum;
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

		message = message.replaceAll(",", " ,");

        // Extract quoted string to add as a single symbol
        String quote = null;
        int beginQuote = message.indexOf('"');
        if(beginQuote >= 0){
        	int endQuote = message.indexOf('"', beginQuote + 1);
        	if(endQuote >= 0){
        		quote = message.substring(beginQuote+1, endQuote);
        		message = message.substring(0, beginQuote) + "_XXX_" + message.substring(endQuote+1);
        	}
        }
        
        Identifier nextID = messageId.CreateIdWME("next");
        String[] words = message.split(" ");
        for(String word : words){
        	if(word.isEmpty()){
        		continue;
        	}
        	if(word.equals("_XXX_")){
        		word = quote;
        		nextID.CreateStringWME("quoted", "true");
        	}
			if(word.indexOf(':') > 0){
				nextID.CreateStringWME("clocktime", "true");
				nextID.CreateIntWME("hour", new Integer(word.split("-")[0]));
				nextID.CreateIntWME("minute", new Integer(word.split("-")[1]));
			}

        	nextID.CreateStringWME("spelling", word.toLowerCase());
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
