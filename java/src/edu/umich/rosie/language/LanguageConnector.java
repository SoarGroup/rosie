package edu.umich.rosie.language;

import edu.umich.insoar.ChatPanel;
import edu.umich.rosie.SoarAgent;
import edu.umich.rosie.SoarUtil;
import april.util.TimeUtil;
import sml.Agent;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;

public class LanguageConnector extends AgentConnector {
	public enum MessageType{
		AGENT_MESSAGE, INSTRUCTOR_MESSAGE
	};
	
    private TextToSpeech tts;  
    private SpeechToText stt;
    
    private SoarAgent soarAgent;
    private ChatPanel chat;
    
	private Messages messages;
	
	Identifier languageId = null;
	
	public LanguageConnector(SoarAgent agent, String speechFile){
        this.tts = new TextToSpeech();
        this.stt = new SpeechToText(speechFile, agent);
        this.soarAgent = agent;
       
        messages = new Messages();
    	
        String[] outputHandlerStrings = { "send-message" };

        for (String outputHandlerString : outputHandlerStrings)
        {
        	soarAgent.getAgent().AddOutputHandler(outputHandlerString, this, null);
        }
        
        soarAgent.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
	}
	
	public void setChat(ChatPanel chat){
		this.chat = chat;
	}
	
	public ChatPanel getChat(){
		return chat;
	}
	
	public TextToSpeech getTTS(){
		return tts;
	}
	
	public SpeechToText getSTT(){
		return stt;
	}
	
	public synchronized void registerNewMessage(String message, MessageType msgType){
		switch(msgType){
    	case INSTRUCTOR_MESSAGE:
    		messages.addMessage(message);
    		break;
    	case AGENT_MESSAGE:
    		tts.speak(message);
    		break;
    	}
   		chat.registerNewMessage(message, msgType);
    	sendLCMChatMessage(message, msgType);
	}
    
    private void sendLCMChatMessage(String message, MessageType msgType){
// AM: TODO
//    	// Print message for logging
//    	chat_message_t chat_message = new chat_message_t();
//    	chat_message.utime = TimeUtil.utime();
//    	chat_message.message = message;
//    	chat_message.sender = sender;
//    	LCM.getSingleton().publish("CHAT_MESSAGES", chat_message);
    }
    
    public void clear(){
    	messages.destroy();
    }
    
    public void destroyMessage(int id){
    	messages.destroy();
    }
    
    public void newMessage(String message){
    	messages.addMessage(message);
    }
    
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
   		messages.updateInputLink(agent.GetInputLink());
    }

    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
    	synchronized(this){
    		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
            {
                return;
            }
    		Identifier id = wme.ConvertToIdentifier();
            System.out.println(wme.GetAttribute());
            

            try{
	            if (wme.GetAttribute().equals("send-message"))
	            {
	                processOutputLinkMessage(id);
	            }
	            soarAgent.getAgent().Commit();
            } catch (IllegalStateException e){
            	System.out.println(e.getMessage());
            }
    	}
    }

	private void processOutputLinkMessage(Identifier messageId)
    {	
		if (messageId == null)
        {
            return;
        }

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
        message = AgentMessageParser2.translateAgentMessage(messageId);
        if(message != null && !message.equals("")){
        	this.registerNewMessage(message, MessageType.AGENT_MESSAGE);
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
        this.registerNewMessage(message.substring(0, message.length() - 1), MessageType.AGENT_MESSAGE);

        messageId.CreateStringWME("status", "complete");
    }
}
