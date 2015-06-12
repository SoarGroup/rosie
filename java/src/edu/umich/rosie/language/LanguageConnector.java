package edu.umich.rosie.language;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;

import april.util.TimeUtil;

import edu.umich.rosie.lcmtypes.interaction_message_t;
import edu.umich.rosie.soar.AgentConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soarobjects.Message;
import sml.Identifier;
import sml.WMElement;


public class LanguageConnector extends AgentConnector implements MessageLogger.IMessageListener{
	public enum MessageType{
		AGENT_MESSAGE, INSTRUCTOR_MESSAGE
	};
	
	private int nextMessageId = 1;
	
    private TextToSpeech tts;  
    private SpeechToText stt;
    
	private Message curMessage;
	private HashSet<Message> messagesToRemove;
	
	Identifier languageId = null;
	
	private MessageLogger messageLogger;
	
	public LanguageConnector(SoarAgent agent, Properties props){
		super(agent);
		
		String speechFile = props.getProperty("speech-file", "audio_file/sample");
		
        this.tts = new TextToSpeech();
        this.stt = new SpeechToText(speechFile, agent);
        
        curMessage = null;
        messagesToRemove = new HashSet<Message>();
        
        messageLogger = new MessageLogger("agent");
    	
        this.setOutputHandlerNames(new String[]{ "send-message" });
	}
	
	@Override
	public void connect(){
		super.connect();
		messageLogger.addMessageListener(this);
	}
	
	@Override
	public void disconnect(){
		super.disconnect();
		messageLogger.removeMessageListener(this);
	}
	
	public TextToSpeech getTTS(){
		return tts;
	}
	
	public SpeechToText getSTT(){
		return stt;
	}
	
	public void sendMessage(String message, MessageType type){
		messageLogger.sendMessage(message, type);
	}
	
	public synchronized void receiveMessage(interaction_message_t message){
		switch(MessageType.valueOf(message.message_type)){
    	case INSTRUCTOR_MESSAGE:
    		if(curMessage != null){
    			messagesToRemove.add(curMessage);
    		}
    		curMessage = new Message(message.message, nextMessageId++);
    		break;
    	case AGENT_MESSAGE:
    		tts.speak(message.message);
    		break;
    	}
	}
    
    @Override
    protected void onInputPhase(Identifier inputLink)
    {
    	if(languageId == null){
    		languageId = inputLink.CreateIdWME("language");
    	}
    	if(curMessage != null){
    		if(curMessage.isAdded()){
    			curMessage.updateWM();
    		} else {
    			curMessage.addToWM(languageId);
    		}
    	}
    	for(Message msg : messagesToRemove){
    		msg.removeFromWM();
    	}
    	messagesToRemove.clear();
    }

    @Override
    protected void onOutputEvent(String attName, Identifier id){
    	if (attName.equals("send-message")){
    		processOutputLinkMessage(id);
    	}
    }

	private void processOutputLinkMessage(Identifier messageId)
    {	
        if (messageId.GetNumberChildren() == 0)
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no children");
        }
        
        if(SoarUtil.getIdentifierOfAttribute(messageId, "first") == null){
        	processAgentMessageStructureCommand(messageId);
        } else {
        	processAgentMessageStringCommand(messageId);
        }
    }
	
    private void processAgentMessageStructureCommand(Identifier messageId)
    {
        String type = SoarUtil.getValueOfAttribute(messageId, "type",
                "Message does not have ^type");
        String message = "";
        message = AgentMessageParser.translateAgentMessage(messageId);
        if(message != null && !message.equals("")){
        	sendMessage(message, MessageType.AGENT_MESSAGE);
        }
        messageId.CreateStringWME("status", "complete");
    }
	
	private void processAgentMessageStringCommand(Identifier messageId){
        String message = "";
        WMElement wordsWME = messageId.FindByAttribute("first", 0);
        if (wordsWME == null || !wordsWME.IsIdentifier())
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no first attribute");
        }
        Identifier currentWordId = wordsWME.ConvertToIdentifier();

        // Follows the linked list down until it can't find the 'rest' attribute
        // of a WME
        while (currentWordId != null)
        {
            Identifier nextWordId = null;
            for (int i = 0; i < currentWordId.GetNumberChildren(); i++)
            {
                WMElement child = currentWordId.GetChild(i);
                if (child.GetAttribute().equals("value"))
                {
                    message += child.GetValueAsString()+ " ";
                }
                else if (child.GetAttribute().equals("next")
                        && child.IsIdentifier())
                {
                    nextWordId = child.ConvertToIdentifier();
                }
            }
            currentWordId = nextWordId;
        }

        if (message == "")
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message was empty");
        }

        message += ".";
       	sendMessage(message, MessageType.AGENT_MESSAGE);

        messageId.CreateStringWME("status", "complete");
    }

	@Override
	protected void onInitSoar() { 
		if(curMessage != null){
			curMessage.removeFromWM();
		}
		if(languageId != null){
			languageId.DestroyWME();
			languageId = null;
		}
	}

	@Override
	public void createMenu(JMenuBar menuBar) {}

}
