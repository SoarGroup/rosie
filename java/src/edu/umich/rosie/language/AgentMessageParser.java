package edu.umich.rosie.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.plaf.basic.BasicSliderUI.ScrollListener;
import com.sun.org.omg.CORBA.IdentifierHelper;

import sml.Identifier;
import sml.WMElement;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soar.StringWME;

public class AgentMessageParser
{
	static int counter = 0;
	
	private static HashMap<String, String> simpleMessages = null;
	
	public static String translateAgentMessage(Identifier id){
		if(simpleMessages == null){
			simpleMessages = new HashMap<String, String>();
			simpleMessages.put("ok", "Ok");
//			simpleMessages.put("unable-to-satisfy", "I was unable to carry out that instruction");
//			simpleMessages.put("unable-to-interpret-message", "I was unable to interpret that last message");
//			simpleMessages.put("missing-object", "I lost the object I was using");
//			simpleMessages.put("index-object-failure", "I couldn't find the referenced object");
//			simpleMessages.put("no-proposed-action", "I couldn't perform the requested action");
//			simpleMessages.put("learn-location-failure", "I was not able to identify my current location");
//			simpleMessages.put("get-goal-info", "What is the goal of that action?");
//			simpleMessages.put("no-action-context-for-goal", "I don't know what action that goal is for");
//			simpleMessages.put("get-next-subaction", "What do I do next?");
			simpleMessages.put("unable-to-satisfy", "I couldn't do that");
			simpleMessages.put("unable-to-interpret-message", "I don't understand.");
			simpleMessages.put("missing-object", "I lost the object I was using. Can you help me find it?");
			simpleMessages.put("index-object-failure", "I couldn't find the referenced object");
			simpleMessages.put("no-proposed-action", "I couldn't do that");
			simpleMessages.put("missing-argument", "I need more information to do that action");
			simpleMessages.put("learn-location-failure", "I don't know where I am.");
			simpleMessages.put("get-goal-info", "What is the goal?");
			simpleMessages.put("no-action-context-for-goal", "I don't know what action that goal is for");
			simpleMessages.put("get-next-subaction", "What do I do next?");
			simpleMessages.put("confirm-pick-up", "I have picked up the object.");
			simpleMessages.put("confirm-put-down", "I have put down the object.");
			simpleMessages.put("find-success", null);//"SUCCESS");
			simpleMessages.put("find-failure", null);//"FAILURE");
      simpleMessages.put("stop-leading", "You can stop following me");
      simpleMessages.put("retrospective-learning-failure", "I was unable to learn the task policy");
		}
		
		String type = SoarUtil.getValueOfAttribute(id, "type");
		if(type == null){
			return null;
		}

		System.out.println("Got Message:" + type);
		if(simpleMessages.containsKey(type)){
			return simpleMessages.get(type);
		}
		
		Identifier fieldsId = SoarUtil.getIdentifierOfAttribute(id, "fields");
		if(type.equals("get-next-task")){
			return translateNextTaskPrompt();
		} else if(type.equals("get-predicate-info")){
			return translateGetPredicateInfo(fieldsId);
		} else if(type.equals("report-successful-training")){
			return translateReportSuccessfulTraining(fieldsId);
		} else if(type.equals("agent-object-description")){
			return translateObjectDescription(fieldsId);
		} else if(type.equals("agent-predicate-description")){
			return translatePredicateDescription(fieldsId);
		} else if(type.equals("feature-relation-description")){
			return translateGetFeatureRelationDescription(fieldsId);
		} else if(type.equals("agent-goal-description")){
			return translateGetAgentGoalDescription(fieldsId);
		} else if(type.equals("agent-failure-description")){
			return translateGetAgentFailureDescription(fieldsId);
		} else if(type.equals("agent-game-action-description")){
			return translateGetAgentGameActionDescription(fieldsId);
		} else if(type.equals("agent-game-concept-definition")){
			return translateGetAgentGameConceptDefinition(fieldsId);
		} else if(type.equals("agent-location-description")){
			return translateLocationDescription(fieldsId);
		} else if(type.equals("get-location-info")){
			return translateGetLocationInfo(fieldsId);
		} else if(type.equals("get-item-request")){
			return translateGetItemRequest(fieldsId);
		} else if(type.equals("put-down-item-request")){
			return translatePutDownItemRequest(fieldsId);
		} else if(type.equals("give-item-request")){
			return translateGiveItemRequest(fieldsId);
		} else if(type.equals("ask-about-item")){
			return translateAskAboutItem(fieldsId);
		} else if(type.equals("describe-goal-state")){
			return translateGoalState(fieldsId);
		} else if(type.equals("describe-final-goal-state")){
			return translateFinalGoalState(fieldsId);
	    } else if(type.equals("say-sentence")){
	    	return translateSaySentence(fieldsId);
	    } else if(type.equals("execution-failure")){
	    	return translateExecutionFailure(fieldsId);
	    } else if(type.equals("cant-find-object")){
	    	return translateCantFindObject(fieldsId);
	    } else if(type.equals("multiple-arguments")){
	    	return translateMultipleArguments(fieldsId);
	    } else if(type.equals("start-leading-request")){
	    	return translateStartLeadingRequest(fieldsId);
      }
		return null;
	}
	
	public static String translateGetAgentGameActionDescription(Identifier fieldsId) {
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
		String actionDescription = "";
		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
		if(descSetId == null)
		{
			return "This action is unknown.";
		}
		else {
			String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
			if (generated.equals("yes"))	{	
                object_descs = getObjectPredicateForGames(descSetId);
				actionDescription += getConditionPredicateForGames(descSetId, object_descs);
				if((actionDescription.length()-5) > 0)
				{
					actionDescription = "If " + actionDescription.substring(0, actionDescription.length() - 5) + ", then ";
				}
				
				// Counter for individual verb WMEs
				int j = 0;
				WMElement verbWME = descSetId.FindByAttribute("verb", j);
				while (verbWME !=  null)
				{
					Identifier verbId = verbWME.ConvertToIdentifier();
					String verbName = SoarUtil.getValueOfAttribute(verbId, "verb-name").replace("1","");
					String verbPrep = SoarUtil.getValueOfAttribute(verbId, "verb-prep").replace("1","");
					if (verbPrep.equals("on"))
					{
						// Add "to" to "on" when it is associated with a verb
						verbPrep += "to";
					}
					
					// Retrieving multiple objects that need to be moved to the destination
					int k = 0;
					List<Integer> param1_list = new ArrayList<Integer>();
					WMElement param1_WME = verbId.FindByAttribute("1", k);
					while (param1_WME != null)
					{
						param1_list.add(Integer.parseInt(param1_WME.GetValueAsString()));
						param1_WME = verbId.FindByAttribute("1", ++k);
					}
					
					Integer param2 = Integer.parseInt(SoarUtil.getValueOfAttribute(verbId, "2"));
					
					// Generating action sentence
					actionDescription += verbName + " ";
					k = 0;
					while(k < param1_list.size())
					{
						List<String> objDesc1 = object_descs.get(param1_list.get(k++));
                                                actionDescription += addArticleForObjectDescription(objDesc1) + objDesc1.get(0) + "and ";						
					}
					
					// PR - Make the following into remove "and" function or something.
					actionDescription = actionDescription.substring(0, actionDescription.length() - 5);
					
					List<String> objDesc2 = object_descs.get(param2);
					actionDescription += " " + verbPrep + " " + addArticleForObjectDescription(objDesc2) + objDesc2.get(0) + "and ";
					
					verbWME = descSetId.FindByAttribute("verb", ++j);
				}
				
				// PR - check negative conditions for the following, when it isn't greater than zero, does it work?
				if ((actionDescription.length()-5) > 0)
				{
					actionDescription = actionDescription.substring(0, actionDescription.length() - 5) + ". ";
				}
				
				return actionDescription;
			}
			else
				return "The action is unknown.";
		}
	}
	
	public static String translateGetAgentGameConceptDefinition(Identifier fieldsId) {
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
		String conceptDefinition = "If ";
		String conceptName = SoarUtil.getValueOfAttribute(descSetId, "sentence");
		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
		if (descSetId == null)
		{
			return "This concept is not defined.";
		}
		else 
		{
			String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
			if (generated.equals("yes"))	{
				object_descs = getObjectPredicateForGames(descSetId);
				conceptDefinition += getConditionPredicateForGames(descSetId, object_descs);
				if ((conceptDefinition.length()-5) > 0)
				{
					conceptDefinition = conceptDefinition.substring(0, conceptDefinition.length() - 5);
				}
				if(SoarUtil.getValueOfAttribute(descSetId, "pronoun") != null)
				{
					conceptDefinition += ", then it is " + conceptName + ".";
				}
				else
				{
					List<String> objDesc1 = (List<String>)object_descs.values().toArray()[0];
					List<String> objDesc2 = (List<String>)object_descs.values().toArray()[1];
					conceptDefinition += ", then " +  addArticleForObjectDescription(objDesc1) + objDesc1.get(0)  + "and " + addArticleForObjectDescription(objDesc2)+ objDesc2.get(0) + "are " + conceptName + ".";
				}
				
				return conceptDefinition;
			}
			else
				return conceptName + "is not defined.";
		}		
	}
	
	public static String translateGetAgentGoalDescription(Identifier fieldsId) {
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
		String goalDescription = "The goal is that ";
		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
		if (descSetId == null)
		{
			return "The goal of this game is unknown.";
		}
		else {
			String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
			if (generated.equals("yes"))	{
				object_descs = getObjectPredicateForGames(descSetId);
				goalDescription += getConditionPredicateForGames(descSetId, object_descs);
				
				if ((goalDescription.length()-5) > 0)
				{
					goalDescription = goalDescription.substring(0, goalDescription.length() - 5) + ".";
				}
				
				return goalDescription;
				
			}
			else
				return "The goal of this game is unknown.";
		}
	}
	
	public static String translateGetAgentFailureDescription(Identifier fieldsId) {
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
		String failureDescription = "If ";
		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
		if (descSetId == null)
		{
			return "The failure points of this game are unknown.";
		}
		else {
			String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
			if (generated.equals("yes"))	{
				object_descs = getObjectPredicateForGames(descSetId);
				failureDescription += getConditionPredicateForGames(descSetId, object_descs);
				
				if ((failureDescription.length()-5) > 0)
				{
					failureDescription = failureDescription.substring(0, failureDescription.length() - 5);
				}
				
				failureDescription += ", then you lose.";
				return failureDescription;				
			}
			else
				return "The failure points of this game are unknown.";
		}
	}
	
	public static String translateGetFeatureRelationDescription(Identifier fieldsId){
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "object");
		if(descSetId == null) {
			return "I do not know this object. Can you describe it for me?";
		}
		String returnDescription = "";
		
		// Verifying if object description was generated successfully
		String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
		if(generated.equals("yes"))	{
			Identifier objectId = SoarUtil.getIdentifierOfAttribute(descSetId, "object");
			returnDescription = generatePropertyDescription(objectId);
			String object_desc = generateObjectDescription(objectId);
			
			// Counter for individual description WMEs
			int i = 0;
			WMElement desc = descSetId.FindByAttribute("description",i);

			// Getting individual relation descriptions for given object
			while (desc != null) {
				int position=0, other_object_position=0;
				String relation_handle = "";
				
				// Get values of position of given object and relation handle
				Identifier descId = desc.ConvertToIdentifier();
				position = Integer.parseInt(SoarUtil.getValueOfAttribute(descId, "position"));
				relation_handle = (SoarUtil.getValueOfAttribute(descId, "handle")).replace("1","");
				
				// Relation of given object w.r.t other objects in the world (this includes location as well)
				if (position == 1) {
					other_object_position = 2;
				} else {
					other_object_position = 1;
				}
				
				// Get other object identifier
				Identifier other_object_Id = SoarUtil.getIdentifierOfAttribute(descId, Integer.toString(other_object_position));
	
				// Description of the related object in terms of size, shape and color
				String other_object_desc = generateObjectDescription(other_object_Id);			
				switch(other_object_desc.charAt(0)) 
				{
					case 'a':
					case 'e':
					case 'i':
					case 'o':
					case 'u':
						other_object_desc = "An " + other_object_desc;
						break;
					default:
						other_object_desc = "A " + other_object_desc;
						break;
				}
				
				if (other_object_position == 2)	
				{
					other_object_desc = " a" + other_object_desc.substring(1,other_object_desc.length());
				}
				
				if (position == 1) 
				{
					returnDescription += "The " + object_desc + " is " + relation_handle + other_object_desc + ". ";
				} else 
				{
					returnDescription += other_object_desc + " is " + relation_handle + " the " + object_desc + ". ";
				}
				
				desc = descSetId.FindByAttribute("description",++i);
			}
		}
		else {
			returnDescription =  "I do not know this object. If it exists, can you describe it for me?";
		}
		
		return returnDescription;
	}
	
	public static String translatePredicateDescription(Identifier fieldsId) {
		Identifier descId = SoarUtil.getIdentifierOfAttribute(fieldsId, "object");
		String desc = "";
		if(descId == null){
			return "The value of this predicate is unknown"; 
		}
		else {
			// Verifying if object description was generated successfully
			String generated = SoarUtil.getValueOfAttribute(descId, "generated");
			if(generated.equals("yes"))	{			
				Identifier predicateId = SoarUtil.getIdentifierOfAttribute(descId, "predicate");
				String obj_att = SoarUtil.getValueOfAttribute(predicateId, "attribute");
				String obj_attribute_val = (SoarUtil.getValueOfAttribute(predicateId, "value")).replace("1", "");
				desc = "The " + obj_att + " is " + obj_attribute_val + ".";
			}
			else {
				String reason = SoarUtil.getValueOfAttribute(descId, "reason");
				if(reason.equals("unknown-attribute")) {
					desc = "This object does not have this property.";
				} else {
					desc = "This object does not exist.";
				}
			}
			
			return desc;
		}
	}
	
	public static String translateLocationDescription(Identifier fieldsId) {
		Identifier descId = SoarUtil.getIdentifierOfAttribute(fieldsId, "location-description");
		
		// Get location
		Identifier locationId = SoarUtil.getIdentifierOfAttribute(descId, "location");
		String locationDesc = generateObjectDescription(locationId);
		
		// Get the handle in/on
		String preposition = (SoarUtil.getValueOfAttribute(descId, "handle")).replace("1","");
		return "It is " + preposition +" the " + locationDesc + ".";
	}
	
	public static String translateGetItemRequest(Identifier fieldsId){
		String item = SoarUtil.getValueOfAttribute(fieldsId, "item");
		if (item != null){
			item = item.replace("1", "");
			return "Please give me the " + item;
		}
		return null;
	}	

  public static String translatePutDownItemRequest(Identifier fieldsId){
		String item = SoarUtil.getValueOfAttribute(fieldsId, "item");
		String loc = SoarUtil.getValueOfAttribute(fieldsId, "location");
    if (item != null && loc != null){
      return ("Please place the " + item + " on the " + loc + ".").replaceAll("\\d", "");
    } else if(item != null){
      return ("Please take the " + item + ".").replaceAll("\\d", "");
    }
    return null;
  }

	public static String translateGiveItemRequest(Identifier fieldsId){
		String item = SoarUtil.getValueOfAttribute(fieldsId, "item");
		String name = SoarUtil.getValueOfAttribute(fieldsId, "person");
		if (item != null && name != null){
			return (name + ", please take the " + item).replaceAll("\\d", "");
		}
		return null;
	}	

	public static String translateAskAboutItem(Identifier fieldsId){
		String item = SoarUtil.getValueOfAttribute(fieldsId, "item");
		if (item != null){
			item = item.replace("1", "");
			return "Is there a " + item + " here?";
		}
		return null;
	}	

	public static String translateGetPredicateInfo(Identifier fieldsId){
		String predicateName = SoarUtil.getValueOfAttribute(fieldsId, "predicate-name");
		if (predicateName == null){
			return "I encountered a new word describing an object, can you help?";
		} else {
			return "Can you tell me what the word '" + predicateName + "' is?";
		}
	}
	
	public static String translateGetLocationInfo(Identifier fieldsId){
		String locationName = SoarUtil.getValueOfAttribute(fieldsId, "location-name");
		if (locationName != null){
			return "Can you take me to the " + locationName + "?\n";
//			return "I don't know where the " + locationName + " is.\n" + 
//					"Can you take me there?";
		} else {
			return "Where is it?";
		}
	}
	
	public static String translateReportSuccessfulTraining(Identifier fields){
		return "Ok";
	}
	
	public static String translateUnableToSatisfy(Identifier fields){
		return "I was unable to carry out that instruction";
	}
	
    public static String translateFinalGoalState(Identifier fields){
		String result = "Ok, I've learned that the goal state is that ";
		result += getGoalState(fields);
		return result;
		
    }
	public static String translateGoalState(Identifier fields){
		
		String result = "I think the goal state is that ";
		result += getGoalState(fields);
		return result;
		
	}

  public static String translateStartLeadingRequest(Identifier fields){
    String name = SoarUtil.getValueOfAttribute(fields, "person");
    if (name == null){
      return "Please follow me.";
    } else {
      return "Please follow me, " + name.replaceAll("\\d", "");
    }
  }
	
	public static String getGoalState(Identifier fields){
		Identifier relationships = SoarUtil.getIdentifierOfAttribute(fields, "relationships");
		ArrayList<String> objects = new ArrayList<String>();
		String result = "";
		String definitive = SoarUtil.getValueOfAttribute(fields, "definitive");
		
	    boolean allDefinitive = false;
	    if (definitive.equals("yes"))
	    	allDefinitive = true;
		for(int index = 0; index < relationships.GetNumberChildren(); index++)
		{
			if (index != 0)
				result+= " and ";

		    WMElement wme = relationships.GetChild(index);
		    Identifier relation = wme.ConvertToIdentifier();
		    
            String preposition = SoarUtil.getValueOfAttribute(relation, "name");
            preposition = preposition.replaceAll("\\d", "");
            
            preposition = preposition.replace('-', ' ');
            //expect more than one, loop
            Identifier object1 = SoarUtil.getIdentifierOfAttribute(relation, "1");
            
            
            String objDescription1 = generateObjectDescriptionNew(object1);
            String objDescription2 = "";
            Identifier object2 = SoarUtil.getIdentifierOfAttribute(relation, "2");
            
            //may have only one object
            if (object2 != null)
            	objDescription2 = generateObjectDescriptionNew(object2);
            
            if (allDefinitive)
            {
            	objDescription1 = "the " + objDescription1;
            	objDescription2 = "the " + objDescription2;
            }
            else
            {
            	if (objects.contains(object1.GetValueAsString()))
            		objDescription1 = "the " + objDescription1;
	            else {
	            	objects.add(object1.GetValueAsString());
	    			if (startsWithVowel(objDescription1))
	    				objDescription1 = "an " + objDescription1;
	    			else
	    				objDescription1 = "a " + objDescription1;
	            }
	            
	            if (objects.contains(object2.GetValueAsString()))
	            	objDescription2 = "the " + objDescription2;
	            else {
	            	objects.add(object2.GetValueAsString());
	    			if (startsWithVowel(objDescription2))
	    				objDescription2 = "an " + objDescription2;
	    			else
	    				objDescription2 = "a " + objDescription2;
	            }
            }
            if (object2 != null)
            	result += objDescription1 + " is " + preposition + " " + objDescription2;
            else
            	result += objDescription1 + " is " + preposition;
		}
		return result;
	}
	
	public static String translateObjectDescription(Identifier fields){
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fields, "object");
		if(descSetId == null){
			return "An object";
		}
		
		// Verifying if object description was generated successfully
		String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
		if(generated.equals("yes"))	{
			String desc="", returnDesc = "";
			
			if (descSetId.GetNumberChildren() == 2)
			{
				Identifier descId = SoarUtil.getIdentifierOfAttribute(descSetId, "object");
				desc = generateObjectDescription(descId); 
				if(desc.charAt(0) == 'a' || desc.charAt(0) == 'e' || desc.charAt(0) == 'i' || 
						desc.charAt(0) == 'o' || desc.charAt(0) == 'u'){
					return "An " + desc;
				} else {
					return "A " + desc;
				}
			}
			
			// Counter for individual description WMEs
			int i = 0;
			WMElement descWME = descSetId.FindByAttribute("object",i);
			
			// Retrieving multiple objects if they exist
			while (descWME != null)
			{
				Identifier descId = descWME.ConvertToIdentifier();
				desc = generateObjectDescription(descId); 
				if(desc.charAt(0) == 'a' || desc.charAt(0) == 'e' || desc.charAt(0) == 'i' || 
						desc.charAt(0) == 'o' || desc.charAt(0) == 'u'){
					desc = "An " + desc;
				} else {
					desc = "A " + desc;
				}
				
				returnDesc += desc + ", ";
				descWME = descSetId.FindByAttribute("object",++i);
			}
			
			returnDesc = returnDesc.substring(0,returnDesc.length()-2); // removing unnecessary comma
			// replacing last comma by and
			int lastcomma = returnDesc.lastIndexOf(',');
			returnDesc = returnDesc.substring(0, lastcomma) + " and" + returnDesc.substring(lastcomma+1);
			return "A" + returnDesc.substring(1).toLowerCase();			
		}
		else {
			return "Nothing";
		}	
	}
	
	public static boolean startsWithVowel(String adj){
		return (adj.startsWith("a") || adj.startsWith("e") || adj.startsWith("i") || 
				adj.startsWith("o") || adj.startsWith("u"));
	}
	
	public static String translateSaySentence(Identifier fieldsId){
		String sentence = SoarUtil.getValueOfAttribute(fieldsId, "sentence");
		if (sentence != null){
			sentence = sentence.replace("1", "");
			return sentence;
		}
		return null;
	}	
	
	public static String translateCantFindObject(Identifier fieldsId){
		Identifier obj = SoarUtil.getIdentifierOfAttribute(fieldsId, "object");
		if(obj == null){
			return "I can't find the object. Can you help?";
		}
		return "I can't find " + worldObjectToString(obj) + ". Can you help?";
	}
	
	public static String translateMultipleArguments(Identifier fieldsId){
		Identifier obj = SoarUtil.getIdentifierOfAttribute(fieldsId, "argument-info");
		if(obj == null){
			return "Can you be more specific? Which object?";
		}
		return "Can you be more specific? Which " + worldObjectToString(obj) + "?";
		
	}
	
	public static String translateExecutionFailure(Identifier fieldsId){
		String info = SoarUtil.getValueOfAttribute(fieldsId, "failure-info");
		String type = SoarUtil.getValueOfAttribute(fieldsId, "failure-type");
		if(info == null || type == null){
			return "I was not able to perform the action";
		}
		if(type.equals("command-error")){
			return "There was a problem sending the " + info + " command";
		} else if(type.equals("execution-failed")){
			return "There was an error while executing the " + info + " command";
		} else if(type.equals("direction-retrieval-failure")){
			return "I do not know about the turn direction " + info;
		} else if(type.equals("invalid-direction-failure")){
			return "The given turn direction " + info + " was not a valid direction";
		} else if(type.equals("unsatisfied-until-clause")){
			return null;
      //"I had to terminate the " + info + " command early";
		} else if(type.equals("svs-filter-error")){
			return "Something went wrong with the " + info + " filter";
		} else if(type.equals("missing-object")){
			return "I didn't see the object I expected to";
		} else if(type.equals("unknown-location")){
			return "I don't know how to get to the " + info + ".";
		} else if(type.equals("no-ask-sentence")){
			return "I don't know what to ask";
		} else if(type.equals("no-say-sentence")){
			return "I don't know what to say";
		} else if(type.equals("unknown-current-location")){
      return "I don't know where I am";
    } else {
			return "I was not able to perform the action";
		}
	}	
	
//    public static String translateAgentMessage(Identifier id){
//        String message = null;
//        
//        
//        String type = SoarUtil.getValueOfAttribute(id, "type");
//        System.out.println(type);
//        Identifier fieldsId = SoarUtil.getIdentifierOfAttribute(id, "fields");
//        if(type == null){
//            return null; 
//        } else if(type.equals("different-attribute-question")){
//            message = translateDifferentAttributeQuestion(fieldsId);
//        } else if(type.equals("value-question")){
//            message = translateValueQuestion(fieldsId);
//        } else if(type.equals("common-attribute-question")){
//            message = translateCommonAttributeQuestion(fieldsId);
//        } else if(type.equals("attribute-presence-question")){
//            message = translateAttributePresenceQuestion(fieldsId);
//        } else if(type.equals("ask-property-name")){
//            message = translateCategoryQuestion(fieldsId);
//        } else if(type.equals("category-of-property")){
//            message = translateCategoryPropertyQuestion(fieldsId);
//        } else if(type.equals("how-to-measure")){
//        	message = String.format("How do I measure %s?", SoarUtil.getValueOfAttribute(fieldsId, "property"));
//        } else if(type.equals("ambiguous-category")){
//        	message = translateAmbiguousCategory(fieldsId);
//        } else if(type.equals("describe-object")){
//            message = translateDescription(fieldsId);
//        } else if(type.equals("dont-know")){
//        	message = "I don't know";
//        } else if(type.equals("no-prep")){
//            message = "I don't know that preposition.";
//        } else if(type.equals("parse-failure-response")){
//        	message = "I didn't understand your last message";
//        } else if(type.equals("single-word-response")){
//        	message = SoarUtil.getValueOfAttribute(fieldsId, "word");
//        	if(message.equals("dontknow")){
//        		message = "I don't know";}
//        } else if(type.equals("no-object")){
//        	message = "I do not see the object you are talking about";
//        } else if(type.equals("count-response")){
//        	int count = Integer.parseInt(SoarUtil.getValueOfAttribute(fieldsId, "count"));
//        	message = "There " + (count == 1 ? "is" : "are") + " " + count;
//        } else if(type.equals("unknown-message")){
//        	message = "I was not able to understand your last message";
//        } else if(type.equals("teaching-request")){
//        	message = translateTeachingRequest(fieldsId);
//        } else if(type.equals("which-question")){
//        	message = translateWhichQuestion(fieldsId);
//        } else if(type.equals("missing-object")){
//        	message = translateMissingObjectQuestion(fieldsId);
//// ---------------------- task related queries ---------------------------------
//        } else if(type.equals("get-next-task")){
//        	message = translateNextTaskPrompt();
//        } else if(type.equals("get-next-subaction")){
//        	message = translateNextSubtaskQuery();
//        } else if(type.equals("confirmation")){
//        	message = "Okay.";
//        } else if (type.equals("get-goal")){
//        	message = translateGoalQuery();
//        } else if (type.equals("restart-task-instruction")){
//        	message = "These actions do not lead to the goal you described. Please teach me again.";
//        } else if (type.equals("successful-task-learning")){
//        	message = "I got it";
//        } else if (type.equals("failure-exploration")){
//        	message = "I cannot figure it out.";
//        } else if (type.equals(("begin-exploration"))){
//        	//message = "Let me see.";
//        } 
//// ----------------------------------------------------------------------------------
//        else if(type.equals("request-index-confirmation")){
//        	message = translateRequestIndexConfirmation(fieldsId);
//        } else if(type.equals("describe-scene")){
//            message = translateSceneQuestion(fieldsId);
//        } else if(type.equals("describe-scene-objects")){
//            message = translateSceneObjectsQuestion(fieldsId);
//            
//        } else if(type.equals("list-objects")){
//            message = translateObjectsQuestion(fieldsId);
//        } else if(type.equals("describe-goal-params")){
//            message = translateGoalDemoQuestion(fieldsId);
//        } else if(type.equals("goal-demo-done")){
//            message = "Ok I understand the goal.";
//        } else if(type.equals("location-unknown")){
//            message = "Relative location of object unknown";
//        } else if(type.equals("play-game")){
//            message = "Shall we play a game?";
//        } else if(type.equals("game-start")){
//            message = "Ok I know that game.  Tell me \"your turn\" when it's my turn.";
//        } else if(type.equals("game-new-params")){
//            message = "Tell me the name of an action, failure state, or goal.";
//        } else if(type.equals("game-new-action2")){
//            message = "Ok tell me the name of a legal action in this game.";
//        } else if(type.equals("game-new-action")){
//            String gameName = SoarUtil.getValueOfAttribute(fieldsId, "game-name");
//            message = "I don't know how to play " + gameName + 
//                    ". Is it a multiplayer game?";
//        } else if(type.equals("game-new-verb")){
//            message = "What is an associated verb for this action?";
//        } else if(type.equals("game-new-goal")){
//            message = "Ok tell me the name of the goal in the game.";
//        } else if(type.equals("game-new-failure")){
//            message = "Ok tell me the name of a failure state in the game. (or none)";
//        } else if(type.equals("game-new-parameter1")){
//            message = "Ok describe an object for this action.\n";
//        } else if(type.equals("game-new-parameter")){
//        	String stateType = SoarUtil.getValueOfAttribute(fieldsId, "state-type");
//            message = "Ok describe an object for this " + stateType + " state.";
//        } else if(type.equals("game-new-condition")){
//        	String obj = SoarUtil.getValueOfAttribute(fieldsId, "type");
//            message = "Ok list a condition for the " + obj + ", or another object.";
//        } else if(type.equals("game-new-heuristic")){
//            message = "Are there any heuristics you can teach me? (or finished)";
//        } else if(type.equals("game-final-state")){
//            message = "Please demonstrate the final goal state.";
//        } else if(type.equals("game-learned")){
//            message = "Ok I have now learned the basics of the game.";
//        } else if(type.equals("game-over")){
//            message = "Game Over. Shall we play another?";
//        } 
//        return message;
//    }
//    
//    private static String translateGoalQuery() {
//    	return "This is a new task for me. What is the goal of this task?";
//	}
//
//	private static String translateNextSubtaskQuery() {
//		return "How do I proceed?";
//	}
//
	private static String translateNextTaskPrompt() {
        return "I am ready for a new task";
	}
	
	// This function lists out the properties of a given object.
	public static String generatePropertyDescription(Identifier descId) {
		
		Identifier predsId = SoarUtil.getIdentifierOfAttribute(descId, "predicates");
		String propertyDesc = "";
		for(int c = 0; c < predsId.GetNumberChildren(); c++){
			WMElement el = predsId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase().replace("1","");
			if (att.equals("shape")) {
				propertyDesc += "The shape is " + val + ". ";
			} else if (att.equals("size")) {
				propertyDesc += "The size is " + val + ". ";
			} else if(att.equals("color")) {
				propertyDesc += "The color is " + val + ". ";
			}			
		}
		
		// This object is a location with no descriptive properties.
		if (propertyDesc.equals(""))
		{
			propertyDesc = "This is the " + predsId.GetParameterValue("name") + ". ";
		}
		
		return propertyDesc;
	}
	
	public static String generateObjectDescription(Identifier descId){
		String root = "block"; // PR - Verify if block is a correct default option
		ArrayList<String> adjectives = new ArrayList<String>();
		Identifier predsId = SoarUtil.getIdentifierOfAttribute(descId, "predicates");
		
		for(int c = 0; c < predsId.GetNumberChildren(); c++){
			WMElement el = predsId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase();
			if(att.equals("name")){
				if (!root.equals("block")){
					adjectives.add(root);
				}
				root = val.replace("1","");
			} else if(att.equals("shape")){
				if (root.equals("block")){
					val = val.replace("1", "");
					root = val;
				} else {
					adjectives.add(val);
				}
			} else if(att.equals("color") || att.equals("size")) {
				adjectives.add(val);
			} else {
				continue;
			}
		}
		
		String desc = "";
		for(String adj : adjectives){
			adj = adj.replace("1", "");
			desc += adj + " ";
		}
		
		desc = desc + root;
		return desc;
	}
	
	public static String worldObjectToString(Identifier objId){
		ArrayList<String> predicates = new ArrayList<String>();
		
		String name = null;
		String shape = null;
		String category = "object";
		
		Identifier predsId = SoarUtil.getIdentifierOfAttribute(objId, "predicates");
		for(int p = 0; p < predsId.GetNumberChildren(); p++){
			WMElement el = predsId.GetChild(p);
			String att = el.GetAttribute().replaceAll("\\d", "");
			String val = el.GetValueAsString().replaceAll("\\d",  "");
			if(att.equals("category")){
				category = val;
			} else if(att.equals("name")){
				name = val;
			} else if(att.equals("shape")){
				shape = val;
			} else if(att.equals("color") || att.equals("modifier")){
				predicates.add(val);
			}
		}
		
		StringBuilder rep = new StringBuilder();
		for(String pred : predicates){
			rep.append(pred + " ");
		}
		if(shape != null){
			rep.append(shape + " ");
		}
		if(name != null){
			rep.append(name);
		} else if(shape == null){
			rep.append(category);
		}
		return rep.toString();
	}

	public static String generateObjectDescriptionNew(Identifier descId){
		String root = SoarUtil.getValueOfAttribute(descId, "type");
		if (root == null)
			root = "object";
		//ArrayList<String> adjectives = (ArrayList<String>) SoarUtil.getAllValuesOfAttribute(descId, "value");
		ArrayList<String> adjectives = new ArrayList<String>();
		
		for(int c = 0; c < descId.GetNumberChildren(); c++){
			WMElement el = descId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase();
			val = val.replaceAll("\\d", "");//replace("1", "");
			if(att.equals("type"))
				continue;
			if(att.equals("name")){
				root = val;
			} else if(att.equals("shape")){
				root = val;
			} else {
				adjectives.add(val);
			}
		}
		
		String desc = "";
		//boolean init = true;
		for(String adj : adjectives){
			/*
			desc = "a";
			if (init && (adj.startsWith(" a") || adj.startsWith(" e") || adj.startsWith(" i") || 
						adj.startsWith(" o") || adj.startsWith(" u")))
				desc = "an";
			init = false;
			*/
			desc += adj + " ";
		}
		desc += root;
		return desc;
	}
	
	public static String addArticleForObjectDescription(List<String> objDesc)
	{
		String article = "";
		Integer numberOfMentions = Integer.parseInt(objDesc.get(3));
		if((numberOfMentions > 0 && (objDesc.get(2)).equals("single")) || (objDesc.get(2)).equals("set"))
		{
			article = "the ";
		}
		else
		{
			if (startsWithVowel(objDesc.get(0)))
			{
				article = "an ";
			}
			else
			{
				article = "a ";
			}
		}
		
		objDesc.set(3, (++numberOfMentions).toString()); // The fourth element indicates the number of times the object has been mentioned
		
		return article;
	}
	
	// Get english description based on the object structures retrieved from smem
	public static String getObjectDescriptionForGames(Identifier objId)
	{
		String description = "";
		String attribute_value = SoarUtil.getValueOfAttribute(objId, "attribute");
		String prev_attribute = "";
		String set = SoarUtil.getValueOfAttribute(objId, "rtype");
		
		while (!(attribute_value.equals("primitive") ||  attribute_value.equals("input-arg")))
		{
			description += SoarUtil.getValueOfAttribute(objId, "name").replaceAll("\\d+.*","") + " ";
			Identifier arg = SoarUtil.getIdentifierOfAttribute(objId, "args");
			Identifier arg1 = SoarUtil.getIdentifierOfAttribute(arg, "1");
			prev_attribute = attribute_value;
			attribute_value = SoarUtil.getValueOfAttribute(arg1, "attribute");
			objId = arg1;
		}
		
		// PR - figure this object thing out, this may not be worth it in this case.
		// This is in case the "object" is directly referred in the statement, it must not be ignored. For e.g. in stack-block, a clear object is larger-than a clear block.
		/*if(!prev_attribute.equals("category"))
		{
			description += SoarUtil.getValueOfAttribute(objId, "name").replaceAll("\\d+.*","") + " ";
		}*/

		if(attribute_value.equals("input-arg"))
		{
			description += "object ";
		}
		
		if(set.equals("set"))
		{
			description = description.substring(0,description.length() - 1) + "s ";
		}
		
		return description;
	}
	
	// Creates object predicate phrases based on the predicates retrieved from specific games
	public static HashMap<Integer, List<String>> getObjectPredicateForGames(Identifier descSetId)
	{
		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
		// Counter for individual object description WMEs
		int i = 0;
		WMElement objDescWME = descSetId.FindByAttribute("obj-desc", i);
		
		// Retrieving multiple objects if they exist
		while (objDescWME != null)
		{					
			String objectDescription = "";
			String article = "";
			String rtype = "";
			Identifier objDescId = objDescWME.ConvertToIdentifier();
			String negative = SoarUtil.getValueOfAttribute(objDescId, "negative");
			Identifier objId1 = SoarUtil.getIdentifierOfAttribute(objDescId, "1");
			Integer param_id = Integer.parseInt(SoarUtil.getValueOfAttribute(objDescId, "param-id"));
			
			// Based on if the rtype is set or single, auxiliaryVerb will be set as "are" or "is"
			String auxiliaryVerb = SoarUtil.getValueOfAttribute(objDescId, "aux-verb");
			rtype = auxiliaryVerb.equals("are ")?"set":"single";
			
			objectDescription += getObjectDescriptionForGames(objId1);
					 
			// Adding preposition to the description			
			String prep = SoarUtil.getValueOfAttribute(objDescId, "prep");
			if (prep == null)
			{
				if (!object_descs.containsKey(param_id))
				{
					// The elements are object_description, associated auxiliary verb,rtype(single,set) and the number of times it is mentioned in the condition predicate is initialized with a zero
					// PR - Hack, this should be able to be done using lambda expressions object_descs.values().stream().anymatch(l -> l.contains(objectDescription)) in java 1.8
					if(object_descs.values().toString().contains(objectDescription))
					{
						objectDescription = "other " + objectDescription;
					}

					List<String> object_descs_values = Arrays.asList(objectDescription, auxiliaryVerb, rtype, "0");
					object_descs.put(param_id, object_descs_values);
				}
				objDescWME = descSetId.FindByAttribute("obj-desc", ++i);
				continue;
			}
			
			// Continue in case that the description is a prepositional phrase
			prep = prep.replace("1","");
			if(negative.equals("true"))
			{
				prep = "not " + prep;
			}
			
			objectDescription += auxiliaryVerb + prep + " ";
			
			// Adding the second object in the condition to the object description
			Identifier objId2 = SoarUtil.getIdentifierOfAttribute(objDescId, "2");
			String object2_Desc = getObjectDescriptionForGames(objId2);
			
			// Setting article for the second object in the predicate in cases where the second object is not referred more than once.
			if(auxiliaryVerb.equals("are "))
			{
				article = "the ";
			}
			else
			{
				if (startsWithVowel(object2_Desc))
				{
					article = "an ";
				}
				else
				{
					article = "a ";
				}
			}
			objectDescription += article + object2_Desc;
			
			if (!object_descs.containsKey(param_id))
			{
				// PR - Hack, this should be able to be done using lambda expressions object_descs.values().stream().anymatch(l -> l.contains(objectDescription)) in java 1.8
				if(object_descs.values().toString().contains(objectDescription))
				{
					objectDescription = "other " + objectDescription;
				}

				List<String> object_descs_values = Arrays.asList(objectDescription, auxiliaryVerb, rtype, "0");
				object_descs.put(param_id, object_descs_values);
			}
			objDescWME = descSetId.FindByAttribute("obj-desc", ++i);
		}
		
		return object_descs;
	}
	
	// Creates english statements based on the conditions and relations between objects specified as a part of conditions attribute in nlp-set in the games.
	// This function combines multiple object predicates together when necessary
	public static String getConditionPredicateForGames(Identifier descSetId, HashMap<Integer, List<String>> object_descs)
	{
		String description = "";
		
		// Counter for individual description WMEs
		int k = 0;
		WMElement conditionVarWME = descSetId.FindByAttribute("description", k);
		while(conditionVarWME != null)
		{
			Identifier conditionId = conditionVarWME.ConvertToIdentifier();
			String negative = SoarUtil.getValueOfAttribute(conditionId, "negative");
			String article1 = "", article2="",article3="";
			Integer paramid1 = Integer.parseInt(SoarUtil.getValueOfAttribute(conditionId, "1"));
			
			// Retrieving object descriptions and their corresponding auxiliary verbs
			List<String> objDesc1 = object_descs.get(paramid1);
			article1 = addArticleForObjectDescription(objDesc1);
			
			// When the condition is represented using only one param-id hence only one predicate is in the condition for e.g.  block on a clear location in one predicate retrieved in the object description
			String param2_string = SoarUtil.getValueOfAttribute(conditionId, "2");
			if(param2_string == null)
			{
				description += article1 + objDesc1.get(0);
				conditionVarWME = descSetId.FindByAttribute("description", ++k);
				description += "and ";
				continue;
			}
			
			Integer paramid2 = Integer.parseInt(param2_string);
			List<String> objDesc2 = object_descs.get(paramid2);
		
			if(objDesc2 != null)
			{
				article2 = addArticleForObjectDescription(objDesc2);
			}
			
			String prep = SoarUtil.getValueOfAttribute(conditionId, "prep");
			
			// When the condition involves two object descriptions w.r.t one that have been combined to form a predicate for e.g. a block on a location that is next to a clear location
			if(prep == null)
			{
				String prep1 = SoarUtil.getValueOfAttribute(conditionId, "prep1").replace("1","");
				String prep2 = SoarUtil.getValueOfAttribute(conditionId, "prep2").replace("1","");
				
				Integer paramid3 = Integer.parseInt(SoarUtil.getValueOfAttribute(conditionId, "3"));
				List<String> objDesc3 = object_descs.get(paramid3);
				article3 = addArticleForObjectDescription(objDesc3);
				
				description +=  article1 + objDesc1.get(0) + objDesc1.get(1) + prep1 + " " + article2 + objDesc2.get(0) + "that " + objDesc2.get(1) + prep2 + " " + article3 + objDesc3.get(0);

				conditionVarWME = descSetId.FindByAttribute("description", ++k);
				description += "and ";
				continue;
			}

			prep = prep.replace("1","");
			if(negative != null && negative.equals("true"))
			{	
				prep = "not " + prep;
			}
			String name = SoarUtil.getValueOfAttribute(conditionId, "name");
								
			// When the condition involves the param-ids/values of two predicates being the same for e.g. the color of A is red/the color of A is the color of B
			if (name != null)
			{
				String equalcondition_article = SoarUtil.getValueOfAttribute(conditionId, "article");
				if (prep.equals("number"))
				{
					description += equalcondition_article + name + " of " + article1 + objDesc1.get(0) + "is " + param2_string + " ";
				}
				else
				{
					description += equalcondition_article + name + " of " + article1 + objDesc1.get(0) + "is " + equalcondition_article + name + " of " + article2 + objDesc2.get(0);
				}
				conditionVarWME = descSetId.FindByAttribute("description", ++k);
				description += "and ";
				continue;
			}
			
			description += article1 + objDesc1.get(0) + objDesc1.get(1) + prep + " " + article2 + objDesc2.get(0);
			
			conditionVarWME = descSetId.FindByAttribute("description", ++k);
			description += "and ";
		}
		
		return description;
	}
	
//
//	private static String translateTeachingRequest(Identifier id){
//    	LingObject obj = LingObject.createFromSoarSpeak(id, "description");
//    	String prep = SoarUtil.getValueOfAttribute(id, "preposition");
//    	if (prep != null)
//    	    return "I don't know the preposition " + prep + ". Please teach me with examples";
//    	else {
//    	    return "I don't see " + obj.toString() + ". Please teach me to recognize one";
//    	}
//    }
//    
//    private static String translateDifferentAttributeQuestion(Identifier id){
//        Set<String> exceptions = SoarUtil.getAllValuesOfAttribute(id, "exception");
//        String exceptionStr = getExceptionString(exceptions);
//        LingObject differentObject = LingObject.createFromSoarSpeak(id, "different-object");
//        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "similar-object");
//        String message = String.format("How does %s differ from ", differentObject.toString());
//        for(LingObject obj : similarObjects){
//            message += obj.toString() + "; ";
//        }
//        return exceptionStr + message;
//    }
//    
//    private static String translateCommonAttributeQuestion(Identifier id){
//        Set<String> exceptions = SoarUtil.getAllValuesOfAttribute(id, "exception");
//        String exceptionStr = getExceptionString(exceptions);
//        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "object");
//        String message = "What do ";
//        for(LingObject obj : similarObjects){
//            message += obj.toString() + "; ";
//        }
//        
//        return exceptionStr + message + " have in common?";
//    }
//    
//    private static String translateAttributePresenceQuestion(Identifier id){
//        Set<String> exceptions = SoarUtil.getAllValuesOfAttribute(id, "exception");
//        String exceptionStr = getExceptionString(exceptions);
//        LingObject object = LingObject.createFromSoarSpeak(id, "object");
//        String message = String.format("What attribute does %s have?", object.toString());
//
//        return exceptionStr + message;
//    }
//    
//    private static String translateCategoryQuestion(Identifier id){
//        String word = SoarUtil.getValueOfAttribute(id, "word");
//        return String.format("What kind of attribute is %s?", word);
//    }
//
//    private static String translateCategoryPropertyQuestion(Identifier id){
//        String word = SoarUtil.getValueOfAttribute(id, "word");
//        return String.format("What type of property is %s?", word);
//    }
//    
//    private static String translateAmbiguousCategory(Identifier id){
//    	Set<String> cats = SoarUtil.getAllValuesOfAttribute(id, "result");
//    	String word = SoarUtil.getValueOfAttribute(id, "word");
//    	String s = "By " + word + " do you mean ";
//    	int i = 0;
//    	for(String cat : cats){
//    		if((++i) == cats.size()){
//    			s += "or " + cat + "?";
//    		} else {
//    			s += cat + ", ";
//    		}
//    	}
//    	return s;
//    }
//    
//    private static String translateSceneObjectsQuestion(Identifier id){
//        Identifier objects = SoarUtil.getIdentifierOfAttribute(id, "objects");
//        
//        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
//        String message = "The objects in the scene are";
//        Iterator<LingObject> it = object.iterator();
//        if (object.isEmpty())
//            return "There are no objects in the scene.";
//        while(it.hasNext())
//        {
//            String obj = it.next().toString();
//            if (!it.hasNext() && object.size() > 1)
//                message+= " and";
//            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
//                    obj.startsWith(" o") || obj.startsWith(" u"))
//            {
//                message += " an";
//            }
//            else
//            {
//                message += " a";
//            }
//            message += obj;
//            if (it.hasNext() && object.size() > 2)
//                message+= ",";
//        }
//        return message;
//    }
//    
//    private static String translateGoalDemoQuestion(Identifier id){
//        Identifier description = SoarUtil.getIdentifierOfAttribute(id, "description");
//        
//        List<ObjectRelation> sentence = ObjectRelation.createAllFromSoarSpeak(description, "sentence");
//        String message = "The goal state is:\n";
//        
//        Iterator<ObjectRelation> it = sentence.iterator();
//        if (sentence.isEmpty())
//            return "I did not understand that.";
//        while(it.hasNext())
//        {
//            String sent = it.next().toString();
//            message += sent + "\n";
//        }
//        return message;
//    }
//    private static String translateObjectsQuestion(Identifier id){
//        Identifier objects = SoarUtil.getIdentifierOfAttribute(id, "objects");
//        
//        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
//        String message = "";
//        
//        Iterator<LingObject> it = object.iterator();
//        if (object.isEmpty())
//            return "Nothing.";
//        while(it.hasNext())
//        {
//            String obj = it.next().toString();
//            if (!it.hasNext() && object.size() > 1)
//                message+= " and";
//            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
//                    obj.startsWith(" o") || obj.startsWith(" u"))
//            {
//                message += " an";
//            }
//            else
//            {
//                message += " a";
//            }
//            message += obj;
//            if (it.hasNext() && object.size() > 2)
//                message+= ",";
//        }
//        return message;
//    }
//    private static String translateSceneQuestion(Identifier id){
//      String prep = SoarUtil.getValueOfAttribute(id, "prep");
//      String prep2 = prep.replaceAll("-", " ");
//      String object1 = LingObject.createFromSoarSpeak(id, "object1").toString();
//      String object2 = LingObject.createFromSoarSpeak(id, "object2").toString();
//      return "The" + object1 + " is " + prep2 + " the" + object2;
//  }
//    
//    private static String translateValueQuestion(Identifier id){
//        Identifier attRelationId = SoarUtil.getIdentifierOfAttribute(id, "attribute-relation");
//        String objString = LingObject.createFromSoarSpeak(attRelationId, "object1").toString();
//        String attribute = SoarUtil.getValueOfAttribute(attRelationId, "word");
//        
//        return String.format("What %s is %s?", attribute, objString);
//    }
//    
//    private static String getExceptionString(Set<String> exceptions){
//        String exceptionStr = "";
//        if (exceptions.size() > 0)
//        {
//            exceptionStr = "Other than ";
//            for(String exception : exceptions){
//                exceptionStr += exception + ", ";
//            }
//            exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
//            exceptionStr += "; ";
//        }
//        return exceptionStr;
//    }
//    
//    private static String translateDescription(Identifier id){
//    	
//    	if(id == null){
//            return null;
//        }
//    	
//    	//kind of a hack :(
//    	Identifier objectId = SoarUtil.getIdentifierOfAttribute(id, "object");
//    	if (objectId == null)
//    		return "nothing";
//    	// CK: choose a/an correctly
//    	String ret = LingObject.createFromSoarSpeak(id, "object").toString();
//    	if(ret.matches("^ [aeiouAEIOU].*")) {
//    		ret = "An"+ret;
//    	} else {
//    		ret = "A"+ret;
//    	}
//        return ret;
//    }
//    
//    private static String translateMissingObjectQuestion(Identifier id){
//    	Identifier objectId = SoarUtil.getIdentifierOfAttribute(id, "description");
//    	if (objectId == null)
//    		return "Can you help me find the object I just lost?";
//        return "I can't find " + LingObject.createFromSoarSpeak(id, "description") + ". Can you help?";
//    }
//    
//    private static String translateWhichQuestion(Identifier id){
//    	Identifier objectId = SoarUtil.getIdentifierOfAttribute(id, "description");
//    	if (objectId == null)
//    		return "Which one?";
//        return "Which " + LingObject.createFromSoarSpeak(id, "description") + "?";
//    }
//
//    private static String translateRequestIndexConfirmation(Identifier id){
//    	LingObject obj = LingObject.createFromSoarSpeak(id, "object");
//    	return "Is this " + obj.toString() + "?";
//    }
//    
}
