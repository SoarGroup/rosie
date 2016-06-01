package edu.umich.rosie.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sml.Identifier;
import sml.WMElement;
import edu.umich.rosie.soar.SoarUtil;

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
		} else if(type.equals("object-description")){
			return translateObjectDescription(fieldsId);
		} else if(type.equals("predicate-description")){
			return translatePredicateDescription(fieldsId);
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
	
	public static String translatePredicateDescription(Identifier fieldsId) {
		Identifier descId = SoarUtil.getIdentifierOfAttribute(fieldsId, "predicate");
		if(descId == null){
			return "The value of this predicate is unknown";
		}
		String obj_att="", obj_attribute_val="";
		
		for(int c = 0; c < descId.GetNumberChildren(); c++){
			WMElement el = descId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase();
			if(att.equals("attribute")){
				obj_att = val;
			}
			else if(att.equals("value")){
				val = val.replace("1", "");
				obj_attribute_val = val;
			}
			else{
				continue;
			}
		}
		
		String desc = "The " + obj_att + " is " + obj_attribute_val + ".";
		return desc;
		
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
      return ("Please place the " + item + " on the ground.").replaceAll("\\d", "");
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
		Identifier descId = SoarUtil.getIdentifierOfAttribute(fields, "object");
		if(descId == null){
			return "An object";
		}
		String desc = generateObjectDescription(descId); 
		if(desc.charAt(0) == 'a' || desc.charAt(0) == 'e' || desc.charAt(0) == 'i' || 
				desc.charAt(0) == 'o' || desc.charAt(0) == 'u'){
			return "An " + desc + ".";
		} else {
			return "A " + desc + ".";
		}
		
	}
	public static boolean startsWithVowel(String adj){
		return (adj.startsWith(" a") || adj.startsWith(" e") || adj.startsWith(" i") || 
				adj.startsWith(" o") || adj.startsWith(" u"));
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
		counter++;
		if (counter == 1)
			return "Give me a task.";
		else 
			return "Test me or give me another task.";
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
				root = val;
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
