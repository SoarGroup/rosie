package edu.umich.insoar.world;

import java.util.ArrayList;

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
    private ArrayList<Identifier> messageLL;

    // Latest message received
    private String latestMessage;
    
    // True if a new message was received since the last update
    private Boolean messageChanged;
    
    private BOLTDictionary dictionary;

    private int messageNumber;

    private Parser parser;

    public Messages(String dictionaryFile, String grammarFile){
        latestMessage = "";
        messageChanged = false;
        dictionary = new BOLTDictionary(dictionaryFile); 
        parser = new Parser(dictionary, grammarFile);
        messageLL = new ArrayList<Identifier>();
        messageNumber = 0;
    }


    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {        
        if(!messageChanged){
            return;
        }
        destroy();
        
        latestMessage = latestMessage.toLowerCase().trim();
        
        for(int i = messageLL.size()-1; i >= 0; i--){
        	messageLL.get(i).DestroyWME();
        }
        messageLL.clear();
        
        Identifier rootID = parentIdentifier.CreateIdWME("message");
        rootID.CreateIntWME("id", messageNumber);
        messageLL.add(rootID);
        
        Identifier firstID = rootID.CreateIdWME("word-list");
        firstID.CreateStringWME("word", "*");
        messageLL.add(firstID);
        
        int i = 1;
        
        String[] words = latestMessage.split("[^0-9a-zA-Z]");	// Only consider numbers + letters for words
        for(String word : words){
        	if(word.isEmpty()){
        		continue;
        	}
        	Identifier nextID = messageLL.get(i++).CreateIdWME("next");
        	nextID.CreateStringWME("word", word);
        	messageLL.add(nextID);
        }
        
        char punc = latestMessage.trim().charAt(latestMessage.length()-1);
        if(punc != '.' && punc != '?' && punc != '!'){
        	punc = '.';
        }
        
        Identifier lastID = messageLL.get(i++).CreateIdWME("next");
        lastID.CreateStringWME("word", Character.toString(punc));
        messageLL.add(lastID);
        
    }

    @Override
    public synchronized void destroy()
    {
    	for(int i = messageLL.size()-1; i >= 0; i--){
        	messageLL.get(i).DestroyWME();
        }
        messageLL.clear();
        messageChanged = false;
    }
    
    public synchronized void addMessage(String message){
        messageNumber++;
        latestMessage = message;
        messageChanged = true;
    }
}
