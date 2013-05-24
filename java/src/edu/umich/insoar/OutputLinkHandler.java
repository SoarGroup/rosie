package edu.umich.insoar;

import java.util.ArrayList;
import java.util.List;

import com.soartech.bolt.testing.ActionType;

import sml.*;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;

import abolt.lcmtypes.*;
import april.util.TimeUtil;

import edu.umich.insoar.language.AgentMessageParser;
import edu.umich.insoar.language.Patterns.LingObject;
import edu.umich.insoar.world.*;

public class OutputLinkHandler implements OutputEventInterface, RunEventInterface
{
	
    private List<training_label_t> newLabels;

    public OutputLinkHandler(SoarAgent soarAgent)
    {
        String[] outputHandlerStrings = { "message", "action", "pick-up", "push-segment", "pop-segment",
                "put-down", "point", "send-message","remove-message","send-training-label", "set-state", 
                "report-interaction", "home"};

        for (String outputHandlerString : outputHandlerStrings)
        {
        	soarAgent.getAgent().AddOutputHandler(outputHandlerString, this, null);
        }
        
        soarAgent.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
        
        newLabels = new ArrayList<training_label_t>();
    }
    
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	Identifier outputLink = agent.GetOutputLink();
    	if(outputLink != null){
        	WMElement waitingWME = outputLink.FindByAttribute("waiting", 0);
        	ChatFrame.Singleton().setReady(waitingWME != null);
    	}
    	if(newLabels.size() > 0){
        	InSoar.broadcastTrainingData(newLabels);
        	newLabels.clear();
    	}
    }
    
    public List<training_label_t> extractNewLabels(){
    	if(newLabels.size() == 0){
    		return null;
    	} else {
    		List<training_label_t> retVal = newLabels;
    		newLabels = new ArrayList<training_label_t>();
    		return retVal;
    	}
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
	            if (wme.GetAttribute().equals("set-state"))
	            {
	                processSetCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("message")) 
	            {
	            	processMessage(id);
	            }
	            else if (wme.GetAttribute().equals("send-message"))
	            {
	                processOutputLinkMessage(id);
	            }
	            else if (wme.GetAttribute().equals("pick-up"))
	            {
	                processPickUpCommand(id);
	            }
	            else if (wme.GetAttribute().equals("put-down"))
	            {
	                processPutDownCommand(id);
	            }
	            else if (wme.GetAttribute().equals("point"))
	            {
	                processPointCommand(id);
	            }
	            else if (wme.GetAttribute().equals("remove-message"))
	            {
	            	processRemoveMesageCommand(id);
	            }
	            else if(wme.GetAttribute().equals("send-training-label"))
	            {
	            	processSendTrainingLabelCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("push-segment"))
	            {
	            	processPushSegmentCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("pop-segment"))
	            {
	            	processPopSegmentCommand(id);
	            }
	            else if(wme.GetAttribute().equals("report-interaction")){
	            	processReportInteraction(id);
	            } else if(wme.GetAttribute().equals("home")){
	            	processHomeCommand(id);
	            }
	            InSoar.Singleton().getSoarAgent().commitChanges();
            } catch (IllegalStateException e){
            	System.out.println(e.getMessage());
            }
    	}
    }
    
    private void processMessage(Identifier messageId) {
        Identifier cur = WMUtil.getIdentifierOfAttribute(messageId, "first");
        String msg = "";
        while(cur != null) {
        	String word = WMUtil.getValueOfAttribute(cur, "value");
        	if(word.equals(".") || word.equals("?") || word.equals("!") || word.equals(")"))
        		msg += word;
        	else if(msg.equals(""))
        		msg += word;
        	else
        		msg += " "+word;
        	cur = WMUtil.getIdentifierOfAttribute(cur, "next");
        }
        
        ChatFrame.Singleton().addMessage(msg, ActionType.Agent);
    }

	private void processRemoveMesageCommand(Identifier messageId) {
		int id = Integer.parseInt(WMUtil.getValueOfAttribute(messageId, "id", "Error (remove-message): No id"));
		World.Singleton().destroyMessage(id);
		messageId.CreateStringWME("status", "complete");
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
        
        if(WMUtil.getIdentifierOfAttribute(messageId, "first") == null){
        	processAgentMessageStructureCommand(messageId);
        } else {
        	processAgentMessageStringCommand(messageId);
        }
    }
	
    private void processAgentMessageStructureCommand(Identifier messageId)
    {
        String type = WMUtil.getValueOfAttribute(messageId, "type",
                "Message does not have ^type");
        String message = "";
        message = AgentMessageParser.translateAgentMessage(messageId);
        if(!message.equals("")){
            ChatFrame.Singleton().addMessage(message, ActionType.Agent);
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
                if (child.GetAttribute().equals("word"))
                {
                    message += child.GetValueAsString() + " ";
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
        ChatFrame.Singleton().addMessage(message.substring(0, message.length() - 1), ActionType.Agent);

        messageId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a pick-up command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command. Expects pick-up
     * ^object-id [int]
     */
    private void processPickUpCommand(Identifier pickUpId)
    {
        String objectIdStr = WMUtil.getValueOfAttribute(pickUpId,
                "object-id", "pick-up does not have an ^object-id attribute");
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = String.format("GRAB=%d", Integer.parseInt(objectIdStr));
        command.dest = new double[6];
        InSoar.broadcastRobotCommand(command);
        pickUpId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a put-down command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command Expects put-down
     * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
     */
    private void processPutDownCommand(Identifier putDownId)
    {
        Identifier locationId = WMUtil.getIdentifierOfAttribute(
                putDownId, "location",
                "Error (put-down): No ^location identifier");
        double x = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "x", "Error (put-down): No ^location.x attribute"));
        double y = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "y", "Error (put-down): No ^location.y attribute"));
        double z = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "z", "Error (put-down): No ^location.z attribute"));
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = "DROP";
        command.dest = new double[]{x, y, z, 0, 0, 0};
        InSoar.broadcastRobotCommand(command);
        putDownId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a set-state command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processSetCommand(Identifier id)
    {
        String objId = WMUtil.getValueOfAttribute(id, "id",
                "Error (set-state): No ^id attribute");
        String name = WMUtil.getValueOfAttribute(id,
                "name", "Error (set-state): No ^name attribute");
        String value = WMUtil.getValueOfAttribute(id, "value",
                "Error (set-state): No ^value attribute");

        String action = String.format("ID=%s,%s=%s", objId, name, value);
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = action;
        command.dest = new double[6];
        InSoar.broadcastRobotCommand(command);

        id.CreateStringWME("status", "complete");
    }

    private void processPointCommand(Identifier pointId)
    {
        Identifier poseId = WMUtil.getIdentifierOfAttribute(pointId, "pose",
        		"Error (point): No ^pose identifier");
        String x = WMUtil.getValueOfAttribute(poseId, "x",
        		"Error (point): No ^pose.x identifier");
        String y = WMUtil.getValueOfAttribute(poseId, "y",
        		"Error (point): No ^pose.y identifier");
        String z = WMUtil.getValueOfAttribute(poseId, "z",
        		"Error (point): No ^pose.z identifier");
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[]{Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z), 0, 0, 0};
    	command.action = "POINT";
    	InSoar.broadcastRobotCommand(command);
        
        pointId.CreateStringWME("status", "complete");
    }
    
    private void processSendTrainingLabelCommand(Identifier id){
    	Integer objId = Integer.parseInt(WMUtil.getValueOfAttribute(id, "id", 
    			"Error (send-training-label): No ^id attribute"));
    	String label = WMUtil.getValueOfAttribute(id, "label", 
    			"Error (send-training-label): No ^label attribute");
    	String category = WMUtil.getValueOfAttribute(id, "category", 
    			"Error (send-training-label): No ^category attribute");
    	training_label_t newLabel = new training_label_t();
    	Integer catNum = PerceptualProperty.getCategoryID(category);
    	if(catNum == null){
    		return;
    	}
    	
    	newLabel.cat = new category_t();
    	newLabel.cat.cat = catNum;
    	newLabel.id = objId;
    	newLabel.label = label;
    	
    	newLabels.add(newLabel);
    	id.CreateStringWME("status", "complete");
    }
    
    private void processPushSegmentCommand(Identifier id){
    	String type = WMUtil.getValueOfAttribute(id, "type", "Error (push-segment): No ^type attribute");
    	String originator = WMUtil.getValueOfAttribute(id, "originator", "Error (push-segment): No ^originator attribute");
    	InSoar.Singleton().getSoarAgent().getStack().pushSegment(type, originator);
    	id.CreateStringWME("status", "complete");
    }
    
    private void processPopSegmentCommand(Identifier id){
    	InSoar.Singleton().getSoarAgent().getStack().popSegment();
    	id.CreateStringWME("status", "complete");
    }
    
    private void processHomeCommand(Identifier id){
    	robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[6];
    	command.action = "HOME";
    	InSoar.broadcastRobotCommand(command);
        
        id.CreateStringWME("status", "complete");
    }
    
    private void processReportInteraction(Identifier id){
    	String type = WMUtil.getValueOfAttribute(id, "type");
    	String originator = WMUtil.getValueOfAttribute(id, "originator");
    	Identifier sat = WMUtil.getIdentifierOfAttribute(id, "satisfaction");
    	String eventName = sat.GetChild(0).GetAttribute();
    	WMElement eventTypeWME = sat.GetChild(0).ConvertToIdentifier().FindByAttribute("type", 0);
    	Identifier context = WMUtil.getIdentifierOfAttribute(id, "context");
    	
    	String message = "";
    	if(type.equals("get-next-task")){
    		message = "I am idle and waiting for you to initiate a new interaction";
    	} else if(type.equals("get-next-subaction")){
    		String verb = WMUtil.getValueOfAttribute(context, "verb");
    		message = "What is the next step in performing '" + verb + "'?";
    	} else if(type.equals("category-of-word")){
    		String word = WMUtil.getValueOfAttribute(context, "word");
    		message = "I do not know the category of " + word + ". " + 
    		"You can say something like 'a shape' or 'blue is a color'";
    	} else if(type.equals("which-question")){
    		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
    		message = "I see multiple examples of '" + objStr + "' and I need clarification";
    	} else if(type.equals("teaching-request")){
    		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
    		message = "Please give me teaching examples of '" + objStr + "' and tell me 'finished' when you are done.";
    	} else if(type.equals("get-goal")){
    		String verb = WMUtil.getValueOfAttribute(context, "verb");
    		message = "Please tell me what the goal of '" + verb + "'is.";
    	}
    	ChatFrame.Singleton().addMessage(message, ActionType.Agent);
        id.CreateStringWME("status", "complete");
    }
}
