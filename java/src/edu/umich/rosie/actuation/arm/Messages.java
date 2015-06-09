package edu.umich.rosie.actuation.arm;


import edu.umich.insoar.testing.ActionType;
import sml.Identifier;
/**
 * Represents the latest message from the user on the input link
 * 
 * @author mininger
 * 
 */
public class Messages implements IInputLinkElement
{
    // Identifier of the message on the input link
    private Identifier messageId;

    // Latest message received
    private String latestMessage;
    
    // Id of the latest message received
    private static int latestMessageId;
    
    // True if a new message was received since the last update
    private Boolean messageChanged;
    

    private int messageNumber;

    public Messages(){
        latestMessage = "";
        latestMessageId = 0;   
        messageChanged = false;
    }


    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {        
        if(!messageChanged){
            return;
        }
        
        latestMessage = latestMessage.trim().toLowerCase();
        if(latestMessage.length() == 0){
        	messageChanged = false;
        	return;
        }
        
        if(messageId != null){
            messageId.DestroyWME();
        }
        
        messageId = parentIdentifier.CreateIdWME("sentence");
        messageNumber = latestMessageId;
        messageId.CreateIntWME("sentence-number", messageNumber);
        messageId.CreateStringWME("complete-sentence", latestMessage);
        messageId.CreateStringWME("spelling", "*");
        
        char lastChar = latestMessage.charAt(latestMessage.length()-1);
        char punct = '.';
        if(lastChar == '.' || lastChar == '!' || lastChar == '?'){
        	latestMessage = latestMessage.substring(0, latestMessage.length()-1);
        	punct = lastChar;
        }
        
        Identifier nextID = messageId.CreateIdWME("next");
        String[] words = latestMessage.split(" ");
        for(String word : words){
        	if(word.isEmpty()){
        		continue;
        	}
        	nextID.CreateStringWME("spelling", word);
        	nextID = nextID.CreateIdWME("next");
        }
        nextID.CreateStringWME("spelling", Character.toString(punct));
        nextID.CreateStringWME("next", "nil");

        messageChanged = false;
    }
    
    @Override
    public synchronized void onInitSoar(){
    	messageId = null;
    }

    @Override
    public synchronized void destroy()
    {
        if(messageId != null){
            messageId.DestroyWME();
            messageId = null;
        }
        messageChanged = false;
    }
    
    public synchronized void addMessage(String message){
        latestMessageId++;
        latestMessage = message;
        messageChanged = true;
    }
    
    public Integer getIdNumber(){
    	return messageNumber;
    }
}
