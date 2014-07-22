package edu.umich.insoar.world;

import com.soartech.bolt.testing.ActionType;

import edu.umich.insoar.ChatFrame;
import edu.umich.insoar.language.BOLTDictionary;
import edu.umich.insoar.language.Parser;
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
    
    // Represents an invalid id, or that no message is on the input-link
    private final Integer INVALID_ID = -1;
    

    private BOLTDictionary dictionary;
    

    private int messageNumber;

    private Parser parser;

    public Messages(String dictionaryFile, String grammarFile){
        latestMessage = "";
        latestMessageId = INVALID_ID;   
        messageChanged = false;
        dictionary = new BOLTDictionary(dictionaryFile); 
        parser = new Parser(dictionary, grammarFile);
    }


    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {        
        if(!messageChanged){
            return;
        }
        
        if(messageId != null){
            messageId.DestroyWME();
        }
        
        messageId = parentIdentifier.CreateIdWME("message");
        messageNumber = latestMessageId;
        messageId.CreateIntWME("id", latestMessageId);
        //messageId.CreateStringWME("type", latestMessage);
        if(!parser.getSoarSpeak(latestMessage, messageId)){
        	messageId.DestroyWME();
        	messageId = null;
        	ChatFrame.Singleton().addMessage("Can you repeat that?", ActionType.Agent);
        }
        messageChanged = false;
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
