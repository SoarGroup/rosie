package edu.umich.insoar.language;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sml.Identifier;
import sml.WMElement;
import edu.umich.insoar.language.Patterns.LingObject;
import edu.umich.insoar.language.Patterns.ObjectRelation;
import edu.umich.insoar.world.WMUtil;

public class AgentMessageParser
{
	static int counter = 0;
    public static String translateAgentMessage(Identifier id){
        String message = null;
        
        
        String type = WMUtil.getValueOfAttribute(id, "type");
        System.out.println(type);
        Identifier fieldsId = WMUtil.getIdentifierOfAttribute(id, "fields");
        if(type == null){
            return null; 
        } else if(type.equals("different-attribute-question")){
            message = translateDifferentAttributeQuestion(fieldsId);
        } else if(type.equals("value-question")){
            message = translateValueQuestion(fieldsId);
        } else if(type.equals("common-attribute-question")){
            message = translateCommonAttributeQuestion(fieldsId);
        } else if(type.equals("attribute-presence-question")){
            message = translateAttributePresenceQuestion(fieldsId);
        } else if(type.equals("ask-property-name")){
        	
            message = translateCategoryQuestion(fieldsId);
        } else if(type.equals("category-of-property")){
            message = translateCategoryPropertyQuestion(fieldsId);
        } else if(type.equals("how-to-measure")){
        	message = String.format("How do I measure %s?", WMUtil.getValueOfAttribute(fieldsId, "property"));
        } else if(type.equals("ambiguous-category")){
        	message = translateAmbiguousCategory(fieldsId);
        } else if(type.equals("describe-object")){
            message = translateDescription(fieldsId);
        } else if(type.equals("dont-know")){
        	message = "I don't know";
        } else if(type.equals("no-prep")){
            message = "I don't know that preposition.";
        } else if(type.equals("single-word-response")){
        	message = WMUtil.getValueOfAttribute(fieldsId, "word");
        	if(message.equals("dontknow")){
        		message = "I don't know";}
        } else if(type.equals("no-object")){
        	message = "I do not see the object you are talking about";
        } else if(type.equals("count-response")){
        	int count = Integer.parseInt(WMUtil.getValueOfAttribute(fieldsId, "count"));
        	message = "There " + (count == 1 ? "is" : "are") + " " + count;
        } else if(type.equals("unknown-message")){
        	message = "I was not able to understand your last message";
        } else if(type.equals("teaching-request")){
        	message = translateTeachingRequest(fieldsId);
        } else if(type.equals("which-question")){
        	message = translateWhichQuestion(fieldsId);
        } else if(type.equals("missing-object")){
        	message = translateMissingObjectQuestion(fieldsId);
// ---------------------- task related queries ---------------------------------
        } else if(type.equals("get-next-task")){
        	message = translateNextTaskPrompt();
        } else if(type.equals("get-next-subaction")){
        	message = translateNextSubtaskQuery();
        } else if(type.equals("confirmation")){
        	message = "Okay.";
        } else if (type.equals("get-goal")){
        	message = translateGoalQuery();
        } else if (type.equals("restart-task-instruction")){
        	message = "These actions do not lead to the goal you described. Please teach me again.";
        } else if (type.equals("successful-task-learning")){
        	message = "I got it";
        } else if (type.equals("failure-exploration")){
					message = "";
//message = "I cannot figure it out.";
        } else if (type.equals(("begin-exploration"))){
        	//message = "Let me see.";
        } 
// ----------------------------------------------------------------------------------
        else if(type.equals("request-index-confirmation")){
        	message = translateRequestIndexConfirmation(fieldsId);
        } else if(type.equals("describe-scene")){
            message = translateSceneQuestion(fieldsId);
        } else if(type.equals("describe-scene-objects")){
            message = translateSceneObjectsQuestion(fieldsId);
            
        } else if(type.equals("list-objects")){
            message = translateObjectsQuestion(fieldsId);
        } else if(type.equals("describe-goal-params")){
            message = translateGoalDemoQuestion(fieldsId);
        } else if(type.equals("goal-demo-done")){
            message = "Ok I understand the goal.";
        } else if(type.equals("location-unknown")){
            message = "Relative location of object unknown";
        } else if(type.equals("play-game")){
            message = "Shall we play a game?";
        } else if(type.equals("game-start")){
            message = "Ok I know that game.  Tell me \"your turn\" when it's my turn.";
        } else if(type.equals("game-new-params")){
            message = "Tell me the name of an action, failure state, or goal.";
        } else if(type.equals("game-new-action2")){
            message = "Ok tell me the name of a legal action in this game.";
        } else if(type.equals("game-new-action")){
            String gameName = WMUtil.getValueOfAttribute(fieldsId, "game-name");
            message = "I don't know how to play " + gameName + 
                    ". Is it a multiplayer game?";
        } else if(type.equals("game-new-verb")){
            message = "What is an associated verb for this action?";
        } else if(type.equals("game-new-goal")){
            message = "Ok tell me the name of the goal in the game.";
        } else if(type.equals("game-new-failure")){
            message = "Ok tell me the name of a failure state in the game. (or none)";
        } else if(type.equals("game-new-parameter1")){
            message = "Ok describe an object for this action.\n";
        } else if(type.equals("game-new-parameter")){
        	String stateType = WMUtil.getValueOfAttribute(fieldsId, "state-type");
            message = "Ok describe an object for this " + stateType + " state.";
        } else if(type.equals("game-new-condition")){
        	String obj = WMUtil.getValueOfAttribute(fieldsId, "type");
            message = "Ok list a condition for the " + obj + ", or another object.";
        } else if(type.equals("game-new-heuristic")){
            message = "Are there any heuristics you can teach me? (or finished)";
        } else if(type.equals("game-final-state")){
            message = "Please demonstrate the final goal state.";
        } else if(type.equals("game-learned")){
            message = "Ok I have now learned the basics of the game.";
        } else if(type.equals("game-over")){
            message = "Game Over. Shall we play another?";
        } else if(type.equals("puzzle-solved")){
            message = "That was easy!";
        } 
        return message;
    }
    
    private static String translateGoalQuery() {
    	return "This is a new task for me. What is the goal of this task?";
	}

	private static String translateNextSubtaskQuery() {
		return "How do I proceed?";
	}

	private static String translateNextTaskPrompt() {
		counter++;
		if (counter == 1)
			return "Give me a task.";
		else 
			return "Test me or give me another task.";
	}

	private static String translateTeachingRequest(Identifier id){
    	LingObject obj = LingObject.createFromSoarSpeak(id, "description");
    	String prep = WMUtil.getValueOfAttribute(id, "preposition");
    	if (prep != null)
    	    return "I don't know the preposition " + prep + ". Please teach me with examples";
    	else {
    	    return "I don't see " + obj.toString() + ". Please teach me to recognize one";
    	}
    }
    
    private static String translateDifferentAttributeQuestion(Identifier id){
        Set<String> exceptions = WMUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        LingObject differentObject = LingObject.createFromSoarSpeak(id, "different-object");
        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "similar-object");
        String message = String.format("How does %s differ from ", differentObject.toString());
        for(LingObject obj : similarObjects){
            message += obj.toString() + "; ";
        }
        return exceptionStr + message;
    }
    
    private static String translateCommonAttributeQuestion(Identifier id){
        Set<String> exceptions = WMUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "object");
        String message = "What do ";
        for(LingObject obj : similarObjects){
            message += obj.toString() + "; ";
        }
        
        return exceptionStr + message + " have in common?";
    }
    
    private static String translateAttributePresenceQuestion(Identifier id){
        Set<String> exceptions = WMUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        LingObject object = LingObject.createFromSoarSpeak(id, "object");
        String message = String.format("What attribute does %s have?", object.toString());

        return exceptionStr + message;
    }
    
    private static String translateCategoryQuestion(Identifier id){
        String word = WMUtil.getValueOfAttribute(id, "word");
        return String.format("What kind of attribute is %s?", word);
    }

    private static String translateCategoryPropertyQuestion(Identifier id){
        String word = WMUtil.getValueOfAttribute(id, "word");
        return String.format("What type of property is %s?", word);
    }
    
    private static String translateAmbiguousCategory(Identifier id){
    	Set<String> cats = WMUtil.getAllValuesOfAttribute(id, "result");
    	String word = WMUtil.getValueOfAttribute(id, "word");
    	String s = "By " + word + " do you mean ";
    	int i = 0;
    	for(String cat : cats){
    		if((++i) == cats.size()){
    			s += "or " + cat + "?";
    		} else {
    			s += cat + ", ";
    		}
    	}
    	return s;
    }
    
    private static String translateSceneObjectsQuestion(Identifier id){
        Identifier objects = WMUtil.getIdentifierOfAttribute(id, "objects");
        
        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
        String message = "The objects in the scene are";
        Iterator<LingObject> it = object.iterator();
        if (object.isEmpty())
            return "There are no objects in the scene.";
        while(it.hasNext())
        {
            String obj = it.next().toString();
            if (!it.hasNext() && object.size() > 1)
                message+= " and";
            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
                    obj.startsWith(" o") || obj.startsWith(" u"))
            {
                message += " an";
            }
            else
            {
                message += " a";
            }
            message += obj;
            if (it.hasNext() && object.size() > 2)
                message+= ",";
        }
        return message;
    }
    
    private static String translateGoalDemoQuestion(Identifier id){
        Identifier description = WMUtil.getIdentifierOfAttribute(id, "description");
        
        List<ObjectRelation> sentence = ObjectRelation.createAllFromSoarSpeak(description, "sentence");
        String message = "The goal state is:\n";
        
        Iterator<ObjectRelation> it = sentence.iterator();
        if (sentence.isEmpty())
            return "I did not understand that.";
        while(it.hasNext())
        {
            String sent = it.next().toString();
            message += sent + "\n";
        }
        return message;
    }
    private static String translateObjectsQuestion(Identifier id){
        Identifier objects = WMUtil.getIdentifierOfAttribute(id, "objects");
        
        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
        String message = "";
        
        Iterator<LingObject> it = object.iterator();
        if (object.isEmpty())
            return "Nothing.";
        while(it.hasNext())
        {
            String obj = it.next().toString();
            if (!it.hasNext() && object.size() > 1)
                message+= " and";
            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
                    obj.startsWith(" o") || obj.startsWith(" u"))
            {
                message += " an";
            }
            else
            {
                message += " a";
            }
            message += obj;
            if (it.hasNext() && object.size() > 2)
                message+= ",";
        }
        return message;
    }
    private static String translateSceneQuestion(Identifier id){
      String prep = WMUtil.getValueOfAttribute(id, "prep");
      String prep2 = prep.replaceAll("-", " ");
      String object1 = LingObject.createFromSoarSpeak(id, "object1").toString();
      String object2 = LingObject.createFromSoarSpeak(id, "object2").toString();
      return "The" + object1 + " is " + prep2 + " the" + object2;
  }
    
    private static String translateValueQuestion(Identifier id){
        Identifier attRelationId = WMUtil.getIdentifierOfAttribute(id, "attribute-relation");
        String objString = LingObject.createFromSoarSpeak(attRelationId, "object1").toString();
        String attribute = WMUtil.getValueOfAttribute(attRelationId, "word");
        
        return String.format("What %s is %s?", attribute, objString);
    }
    
    private static String getExceptionString(Set<String> exceptions){
        String exceptionStr = "";
        if (exceptions.size() > 0)
        {
            exceptionStr = "Other than ";
            for(String exception : exceptions){
                exceptionStr += exception + ", ";
            }
            exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
            exceptionStr += "; ";
        }
        return exceptionStr;
    }
    
    private static String translateDescription(Identifier id){
    	
    	if(id == null){
            return null;
        }
    	
    	//kind of a hack :(
    	Identifier objectId = WMUtil.getIdentifierOfAttribute(id, "object");
    	if (objectId == null)
    		return "nothing";
    	// CK: choose a/an correctly
    	String ret = LingObject.createFromSoarSpeak(id, "object").toString();
    	if(ret.matches("^ [aeiouAEIOU].*")) {
    		ret = "An"+ret;
    	} else {
    		ret = "A"+ret;
    	}
        return ret;
    }
    
    private static String translateMissingObjectQuestion(Identifier id){
    	Identifier objectId = WMUtil.getIdentifierOfAttribute(id, "description");
    	if (objectId == null)
    		return "Can you help me find the object I just lost?";
        return "I can't find " + LingObject.createFromSoarSpeak(id, "description") + ". Can you help?";
    }
    
    private static String translateWhichQuestion(Identifier id){
    	Identifier objectId = WMUtil.getIdentifierOfAttribute(id, "description");
    	if (objectId == null)
    		return "Which one?";
        return "Which " + LingObject.createFromSoarSpeak(id, "description") + "?";
    }

    private static String translateRequestIndexConfirmation(Identifier id){
    	LingObject obj = LingObject.createFromSoarSpeak(id, "object");
    	return "Is this " + obj.toString() + "?";
    }
    
}
