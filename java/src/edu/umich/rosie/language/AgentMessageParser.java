package edu.umich.rosie.language;

import java.nio.channels.SeekableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.plaf.basic.BasicSliderUI.ScrollListener;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import sml.Identifier;
import sml.WMElement;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soar.StringWME;

public class AgentMessageParser
{
	static int counter = 0;
	
	// Synonymous generic messages for more natural responses
	private static final List<String> hiMsg = Arrays.asList("Hi", "Hey", "Hello", "Greetings", "What's up");
	private static final List<String> byeMsg = Arrays.asList("bye!", "goodbye!", "so long");
	private static final List<String> affirmMsg = Arrays.asList("ok", "I understand", "all right", "sure", "got it", "correct", "right", "affirmative");
	private static final List<String> negateMsg = Arrays.asList("no", "wrong", "incorrect");
	private static final List<String> uncertainMsg = Arrays.asList("I don't know", "I'm not sure", "I am uncertain");
	private static final List<String> introMsg = Arrays.asList("Hi! My name is Rosie. I am an interactive learning task learning robot build on the soar cognitive architecture");
	
	private static Random rand = new Random();
	
	private static HashMap<String, String> simpleMessages = null;
	
	public static String translateAgentMessage(Identifier id){
		if(simpleMessages == null){
			simpleMessages = new HashMap<String, String>();
			simpleMessages.put("ok", "Ok.");
			simpleMessages.put("unable-to-satisfy", "I couldn't do that.");
			simpleMessages.put("unable-to-interpret-message", "I don't understand.");
			simpleMessages.put("missing-object", "I lost the object I was using. Can you help me find it?");
			simpleMessages.put("index-object-failure", "I couldn't find the referenced object.");
			simpleMessages.put("no-proposed-action", "I couldn't do that.");
			simpleMessages.put("missing-argument", "I need more information to do that action.");
			simpleMessages.put("learn-location-failure", "I don't know where I am.");
			simpleMessages.put("get-goal-info", "What is the goal?");
			simpleMessages.put("no-action-context-for-goal", "I don't know what action that goal is for.");
			simpleMessages.put("confirm-pick-up", "I have picked up the object.");
			simpleMessages.put("confirm-put-down", "I have put down the object.");
			simpleMessages.put("find-success", null);//"SUCCESS");
			simpleMessages.put("find-failure", null);//"FAILURE");
			simpleMessages.put("stop-leading", "You can stop following me.");
			simpleMessages.put("retrospective-learning-failure", "I was unable to learn the task policy.");
			
			//added for games and puzzles
			simpleMessages.put("your-turn", "Your turn.");
			simpleMessages.put("i-win", "I win!");
			simpleMessages.put("i-lose", "I lose.");
			simpleMessages.put("easy", "That was easy!");
			simpleMessages.put("describe-game", "Please setup the game.");
			simpleMessages.put("describe-puzzle", "Please setup the puzzle.");
			simpleMessages.put("setup-goal", "Please setup the goal state.");
			simpleMessages.put("tell-me-go", "Tell me when to go.");
			simpleMessages.put("setup-failure", "Please setup the failure condition.");
			simpleMessages.put("define-actions", "Please describe the actions, goals, and failure conditions."); //Can you define the actions?
			simpleMessages.put("describe-action", "What are the conditions of the action.");
			simpleMessages.put("describe-goal", "Please describe the goal.");
			simpleMessages.put("describe-failure", "Please describe the failure condition.");
			simpleMessages.put("learned-goal", "I've learned the goal.");
			simpleMessages.put("learned-action", "I've learned the action.");
			simpleMessages.put("learned-failure", "I've learned the failure condition.");
			simpleMessages.put("learned-heuristic", "I've learned the heuristic.");
			simpleMessages.put("already-learned-goal", "I know that goal and can recognize it.");
			simpleMessages.put("already-learned-action", "I know that action and can recognize it.");
			simpleMessages.put("already-learned-failure", "I know that failure condition and can recognize it.");
			simpleMessages.put("gotit", "I've found a solution.");
		}
		
		String type = SoarUtil.getValueOfAttribute(id, "type");
		if(type == null){
			return null;
		}

		//System.out.println("Got Message:" + type);
		if(simpleMessages.containsKey(type)){
			return simpleMessages.get(type);
		}
		
		Identifier fieldsId = SoarUtil.getIdentifierOfAttribute(id, "fields");
		if(type.equals("get-next-task")){
			return translateNextTaskPrompt();
		} else if(type.equals("get-next-subaction")){
			return "What do I do next for " + taskHandle(fieldsId) + "?";
		} else if(type.equals("get-next-goal")){
			return "What is the next goal or subtask of " + taskHandle(fieldsId) + "?";
		} else if(type.equals("get-predicate-info")){
			return translateGetPredicateInfo(fieldsId);
		} else if(type.equals("report-successful-training")){
			return translateReportSuccessfulTraining(fieldsId);
		} else if(type.equals("agent-perception-description")){
			return translatePerceptionDescription(fieldsId);
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
		} else if(type.equals("agent-detect-game-concepts")){
			return translateGetAgentDetectGameConcepts(fieldsId);
		} else if(type.equals("agent-response-list-concepts-seen")){
			return translateGetAgentListDetectedGameConcepts(fieldsId);
		} else if(type.equals("agent-list-mobile-world-concepts-seen")){
			return translateGetAgentListDetectedMobileWorldConcepts(fieldsId);
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
		} else if(type.equals("task-execution-failure")){
			return "The " + taskHandle(fieldsId) + "task failed.";
         	// AM: produced by problem-space/action/failure-handling
		} else if(type.equals("maintenance-goal-achieved")){
			return "I have achieved the goal of " + taskHandle(fieldsId) + ".";
	    	} else if(type.equals("execution-failure")){
	    		return translateExecutionFailure(fieldsId);
	        } else if(type.equals("command-failure")){
                  	return translateCommandFailure(fieldsId);
	        } else if(type.equals("action-failure")){
         		return translateActionFailure(fieldsId);
	        } else if(type.equals("unsatisfied-goal")){
			return translateUnsatisfiedGoal(fieldsId);
	        } else if(type.equals("missing-object-failure")){
         		return translateMissingObjectFailure(fieldsId);
	        } else if(type.equals("invalid-argument-failure")){
         		return translateInvalidArgumentFailure(fieldsId);
         	// AM: End failures
	    	} else if(type.equals("cant-find-object")){
	    		return translateCantFindObject(fieldsId);
	    	} else if(type.equals("multiple-arguments")){
	    		return translateMultipleArguments(fieldsId);
	    	} else if(type.equals("start-leading-request")){
			return translateStartLeadingRequest(fieldsId);
	    	} else if(type.equals("unknown-word")){
			return translateUnknownWord(fieldsId);
	    	} else if(type.equals("unsatisfied-condition")){
			return translateUnsatisfiedCondition(fieldsId);
	    	} else if(type.equals("unknown-defined-word")){
			return translateUnknownDefinedWord(fieldsId);
                } else if(type.equals("learned-unknown-word")){
	 		return translateLearnedUnknownWord(fieldsId);
	        } else if(type.equals("transfer-concept")){
			return translateTransferConcept(fieldsId);
	        } else if(type.equals("already-know-concept")){
			return translateAlreadyKnowConcept(fieldsId);
                } else if(type.equals("learned-teacher-name")){
		    return translateLearnedTeacherName(fieldsId);
                } else if(type.equals("learned-game")){
		    return translateLearnedGame(fieldsId);
                } else if(type.equals("current-time")){
		    return translateCurrentTime(fieldsId);
                } else if(type.equals("reset-state")){
		    return translateResetState(fieldsId);
                } else if(type.equals("how-many-predicates")){
		    return translateHowManyPredicates(fieldsId);
                } else if(type.equals("how-many-concepts")){
		    return translateHowManyConcepts(fieldsId);
                } else if(type.equals("give-new-state")){
		    return translateGiveNewState(fieldsId);
                } 
		//conversational messages
		else if(type.equals("generic"))
		{
			return translateGeneric(fieldsId);
	        }
		
		return null;
	}
        
        public static String translateLearnedGame(Identifier fieldsId){
               String result = "I've learned ";
               String word = SoarUtil.getValueOfAttribute(fieldsId, "game");
  
    	       if (word != null)
    	       {
    		    word = word.replaceAll("\\d", "");
    		    result += word;
               }
               String type = SoarUtil.getValueOfAttribute(fieldsId, "type");
    	       if ((type != null) && type.equalsIgnoreCase("puzzle")) {
         		result += ". Should I try to solve the puzzle?";
               }
    	       else {
	    		result += ". Shall we play a game?";
               }

    	       return result;
        }

	public static String translateCurrentTime(Identifier fieldsId){
		String result = "The current time is ";
		Date now = new Date();
		
		String time = new SimpleDateFormat("h:mm a").format(now);
		result+= time;
		
		return result;	
	}
	
	public static String translateResetState(Identifier fieldsId){
		String result = "I couldn't detect the ";
		String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
		result+= word;
		result+= ". Please set it up again.";
		return result;
	}
	
// REPLACED WITH MORE GENERAL VERSION
//	public static String translateHowManyActions(Identifier fieldsId){
//    	String result = "How many available actions are there ";
//    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word1");
//    	
//    	if (word != null)
//    	{
//    		//word = word.replaceAll("\\d", "");
//    		result += word + " or ";
//    	}
//        String word2 = SoarUtil.getValueOfAttribute(fieldsId, "word2");
//    	
//    	if (word2 != null)
//    	{
//    		//word2 = word2.replaceAll("\\d", "");
//    		result += word2;
//    	}
//    	String word3 = SoarUtil.getValueOfAttribute(fieldsId, "word3");
//    	
//    	if (word3 != null)
//    	{
//    		//word3 = word3.replaceAll("\\d", "");
//    		result += " or " + word3;
//    	}
//    	result += "?";
//    	
//    	return result;
//    }
//	
	public static String translateHowManyConcepts(Identifier fieldsId){
    	String type = SoarUtil.getValueOfAttribute(fieldsId, "type");
    	
    	if (type != null)
    	{
    		type = type.replaceAll("\\d", "");
    	}
		else
		{
			type = "ERROR";
		}
		
		String result = "How many instances of this " + type + " are there, ";
    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word1");
    	
    	if (word != null)
    	{
    		//word = word.replaceAll("\\d", "");
    		result += word + " or ";
    	}
        String word2 = SoarUtil.getValueOfAttribute(fieldsId, "word2");
    	
    	if (word2 != null)
    	{
    		//word2 = word2.replaceAll("\\d", "");
    		result += word2;
    	}
    	
    	String word3 = SoarUtil.getValueOfAttribute(fieldsId, "word3");
    	
    	if (word3 != null)
    	{
    		//word2 = word2.replaceAll("\\d", "");
    		result += " or " + word3;
    	}
    	
    	result += "?";
    	
    	return result;
    }
	
	public static String translateHowManyPredicates(Identifier fieldsId){
    	String type = SoarUtil.getValueOfAttribute(fieldsId, "predicate");
    	
    	if (type != null)
    	{
    		type = type.replaceAll("\\d", "");
    	}
		else
		{
			type = "ERROR";
		}
		
		String result = "How many " + type + " objects are there, ";
    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word1");
    	
    	if (word != null)
    	{
    		//word = word.replaceAll("\\d", "");
    		result += word + " or ";
    	}
        String word2 = SoarUtil.getValueOfAttribute(fieldsId, "word2");
    	
    	if (word2 != null)
    	{
    		//word2 = word2.replaceAll("\\d", "");
    		result += word2;
    	}
    	result += "?";
    	
    	return result;
    }

	public static String translateGiveNewState(Identifier fieldsId){
    	String type = SoarUtil.getValueOfAttribute(fieldsId, "type");
    	
    	if (type != null)
    	{
    		type = type.replaceAll("\\d", "");
    	}
		else
		{
			type = "ERROR";
		}
		
		String result="I couldn't determine the correct interpretation in this state. Please setup another state containing the " + type + ".";
    	
    	return result;
    }
	
	public static String translateLearnedUnknownWord(Identifier fieldsId){
	    	String result = "Ok, I've learned the meaning of '";
	    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
	    	
	    	if (word != null)
	    	{
	    		word = word.replaceAll("\\d", "");
	    		result += word + "' for this context.";
	    	}
	    	return result;
        }
	
        public static String translateLearnedTeacherName(Identifier fieldsId){
		 
	    	String result = hiMsg.get(rand.nextInt(hiMsg.size())) + " ";
	    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
	    	if (word != null)
	    	{
	    		word = word.replaceAll("\\d", "");
	    		result += word;
	    	}
	    	result += "! My name is Rosie. I am an interactive task learning robot.";
	    	return result;
	}

        public static String translateUnsatisfiedCondition(Identifier fieldsId)
        {
        	Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
        	String conditionDescription = "";
    		HashMap<Integer, List<String>> object_descs = new HashMap<Integer, List<String>>();
    		object_descs = getObjectPredicateForGames(descSetId);
    		conditionDescription += String.join("and ",getConditionPredicateForGames(descSetId, object_descs));
    		conditionDescription = conditionDescription.substring(0,1).toUpperCase() + conditionDescription.substring(1) + ".";

        	return conditionDescription;
       }	 

       public static String translateUnknownWord(Identifier fieldsId){
	    	String result = "I don't know the concept ";
	    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
	    	if (word != null)
	    	{
	    		word = word.replaceAll("\\d", "");
	    		result += word;
	    	}
	    	return result;
	}
   	public static String translateUnknownDefinedWord(Identifier fieldsId){
    	String result = "Please describe the meaning of '";
    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
    	if (word != null)
    	{
    		word = word.replaceAll("\\d", "");
    		result += word;
    	}
    	result+= "' in this context.";
    	return result;
}
//	public static String translateUnknownDefinedWord(Identifier fieldsId){
//	    	String result = "I can't see any ";
//	    	String word = SoarUtil.getValueOfAttribute(fieldsId, "word");
//	    	if (word != null)
//	    	{
//	    		word = word.replaceAll("\\d", "");
//	    		result += word;
//	    	}
//	    	result+= " objects. Can you give a definition?";
//	    	return result;
//    }
	    	
        public static String translateGeneric(Identifier fieldsId){
    	        String result = null;
	    	String type = SoarUtil.getValueOfAttribute(fieldsId, "type");
    	
    		int size = 0;
	    	List<String> options = null;
    		if (type.equals("affirmative"))
    		{
    			options = affirmMsg;
    			size = affirmMsg.size();
    		}
    		else if (type.equals("negative"))
    		{
    			options = negateMsg;
    			size = negateMsg.size();
    		}
    		else if (type.equals("hello"))
    		{
    			options = hiMsg;
    			size = hiMsg.size();
    		}
	    	else if (type.equals("bye"))
    		{
    			options = byeMsg;
	    		size = byeMsg.size();
    		}
    	
	    	if (options == null)
    			return null;
	    	int randomInt = rand.nextInt(size);
    		return options.get(randomInt);
    	
        }
	
    public static String translateGetAgentDetectGameConcepts(Identifier fieldsId)  {
    	Identifier responseId = SoarUtil.getIdentifierOfAttribute(fieldsId, "response");
    	
    	String num_actions = SoarUtil.getValueOfAttribute(responseId, "count");
    	String conceptSeen = SoarUtil.getValueOfAttribute(responseId, "concept-seen");
    	String gtype = SoarUtil.getValueOfAttribute(responseId, "gtype");
    	
    	if (conceptSeen.equals("Yes") && gtype.equals("action"))
    	{
    		return "Yes. I see " + num_actions + " actions.";
    	}
    	// PR - add situation where you can detect a single action and respond about the same
    	else if (conceptSeen.equals("No"))// && (gtype.equals("goal") || gtype.equals("failure"))) // PR - THis won't work with multiple actions, since there might be too many that don't match,  
    	{
    		String reason = translateUnsatisfiedCondition(responseId);
    		return conceptSeen + ". " + reason;
    	}
    	else
    	{
    		return conceptSeen;
    	}
    }
    
    public static String translateGetAgentListDetectedGameConcepts(Identifier fieldsId)  {
    	Identifier responseId = SoarUtil.getIdentifierOfAttribute(fieldsId, "response");
    	String type = SoarUtil.getValueOfAttribute(responseId, "concept-type");
    	Identifier condetailsId = SoarUtil.getIdentifierOfAttribute(responseId, "concept-details");
    	int num_operators = Integer.parseInt(SoarUtil.getValueOfAttribute(responseId, "num-operators"));
    	String detectedConcepts="";
    	// PR - TODO right now only works with actions
    	if (num_operators < 5)    		
    	{
			detectedConcepts = "I see the following actions: ";
    		if(type.equals("action"))
    		{
	    		Identifier operatorsId = SoarUtil.getIdentifierOfAttribute(responseId, "concept-operators");
	    		// Counter for multiple operators
	    		int j = 0;
	    		WMElement operatorWME = operatorsId.FindByAttribute("operator1", j);
	    		while (operatorWME != null)
	    		{
	    			Identifier operatorId = operatorWME.ConvertToIdentifier();
	    			
	    			// Getting all objects referred in the action
	    			int num_parameters = Integer.parseInt(SoarUtil.getValueOfAttribute(operatorId, "num-parameters"));
	    			Identifier parametersId = SoarUtil.getIdentifierOfAttribute(operatorId, "parameters");
	    			HashMap<Integer, String> object_descs = new HashMap<Integer, String>();
	    			for(int z=1;z <= num_parameters;z++)
	    			{
	    				Identifier objId = SoarUtil.getIdentifierOfAttribute(parametersId, Integer.toString(z));
	    				String desc = generateObjectDescription(objId);
	                    String article = startsWithVowel(desc) ? "an ":"a ";
	                    object_descs.put(z, article+desc);    				
	    			}
	    			
	    			// Verb description
	    			Identifier verbId = SoarUtil.getIdentifierOfAttribute(operatorId, "verb");
					String verbName = SoarUtil.getValueOfAttribute(verbId, "verb-name").replace("1","");
					String verbPrep = SoarUtil.getValueOfAttribute(verbId, "verb-prep").replace("1","");
					if (verbPrep.equals("on"))
					{
						// Add "to" to "on" when it is associated with a verb
						verbPrep += "to";
					}
					
					// Retrieving multiple objects that need to be moved
					int k = 0;
					List<String> param1_list = new ArrayList<String>();
					WMElement param1_WME = verbId.FindByAttribute("1", k);
					while (param1_WME != null)
					{
						int param1Id = Integer.parseInt(param1_WME.GetValueAsString());
						param1_list.add(object_descs.get(param1Id));
						param1_WME = verbId.FindByAttribute("1", ++k);
					}
					
					int paramid2 = Integer.parseInt(SoarUtil.getValueOfAttribute(verbId, "2"));
					//PR - TODO: make this into a helper function that can be used across multiple functions
					if (param1_list.size() > 1)
					{
						String obj1Desc = String.join(", ",param1_list);
						int lastcomma = obj1Desc.lastIndexOf(',');
						obj1Desc = obj1Desc.substring(0,lastcomma) + " and" + obj1Desc.substring(lastcomma+1);
						detectedConcepts +=  verbName.substring(0, 1).toUpperCase() + verbName.substring(1) + " " + obj1Desc + " " + verbPrep 
								+ " " + object_descs.get(paramid2) + ". ";
					}
					else
					{
	                    String obj1Desc = param1_list.get(0);
	                    detectedConcepts += verbName.substring(0, 1).toUpperCase() + verbName.substring(1) + " " + obj1Desc 
	                    		+ " " + verbPrep + " " + object_descs.get(paramid2) + ". ";
					}
					
	    			// Counter for verbs
	    			operatorWME = operatorsId.FindByAttribute("operator1", ++j);
	    		}
    		}
    	}
    	else {
    		// Get number and name of concepts
	    	detectedConcepts = "I see ";
	    	List<String> concept_list = new ArrayList<String>();
	    	// Counter for individual concept WMEs
	    	int i = 0;
	    	WMElement conceptWME = condetailsId.FindByAttribute(type, i);
	    	while (conceptWME != null)
	    	{
	    		Identifier conceptId = conceptWME.ConvertToIdentifier();
	    		String name = SoarUtil.getValueOfAttribute(conceptId, "name");
	    		name = name.replaceAll("\\d", "");
	    		String count = SoarUtil.getValueOfAttribute(conceptId, "count");
	    		if(Integer.parseInt(count) == 1)
	    		{
	    			concept_list.add((count + " instance of " + name + " "));
	    		}
	    		else
	    		{
	    			concept_list.add((count + " instances of " + name + " ")); 
	    		}
	    		conceptWME = condetailsId.FindByAttribute(type, ++i);
	    	}
	    	
	    	detectedConcepts += String.join("and ",concept_list) + ".";
    	}
    	
    	return detectedConcepts;    		
    }
    
    public static String translateGetAgentListDetectedMobileWorldConcepts(Identifier fieldsId)  {
    	Identifier responseId = SoarUtil.getIdentifierOfAttribute(fieldsId, "response");
    	String type = SoarUtil.getValueOfAttribute(responseId, "concept-type");
    	String conceptSeen = SoarUtil.getValueOfAttribute(responseId, "concept-seen");
    	String detectedConceptsResponse = "";
    	
    	if (conceptSeen.equals("Yes"))
    	{
    		Identifier conceptDetailsId = SoarUtil.getIdentifierOfAttribute(responseId, "concept-details");
    		List<String> detectedConceptsList = new ArrayList<String>();
    		// Iterating through multiple concepts that Rosie can see
    		int i = 0;
    		WMElement conceptWME = conceptDetailsId.FindByAttribute(type, i);
    		
    		// PR - TODO this currently works only for actions
    		while (conceptWME != null)
    		{
    			Identifier conceptId = conceptWME.ConvertToIdentifier();
    			String verb = SoarUtil.getValueOfAttribute(conceptId, "action-handle");
    			verb = verb.replaceAll("\\d", "");
    			Identifier objId = SoarUtil.getIdentifierOfAttribute(conceptId, "object");
    			String objDesc = generateObjectDescription(objId);
    			detectedConceptsList.add(verb+" the " + objDesc);
    			conceptWME = conceptDetailsId.FindByAttribute(type, ++i);
    		}
    		
    		detectedConceptsResponse = "I see the following " + type + "s: ";
    		String detectedConcepts = String.join(", ",detectedConceptsList);
			int lastcomma = detectedConcepts.lastIndexOf(',');
			detectedConceptsResponse += detectedConcepts.substring(0,lastcomma) + " and" + detectedConcepts.substring(lastcomma+1);			
    	}
    	else
    	{
    		detectedConceptsResponse = "I do not see any " + type + "s.";
    	}
    	
    	return detectedConceptsResponse;
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
				actionDescription += String.join("and ",getConditionPredicateForGames(descSetId, object_descs));
				//if((actionDescription.length()-5) > 0)
				//{
				//	actionDescription = "If " + actionDescription.substring(0, actionDescription.length() - 5) + ", then ";
				//}
				actionDescription = "If " + actionDescription + ", then ";
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
				conceptDefinition += String.join("and ",getConditionPredicateForGames(descSetId, object_descs));
				//if ((conceptDefinition.length()-5) > 0)
				//{
				//	conceptDefinition = conceptDefinition.substring(0, conceptDefinition.length() - 5);
				//}
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
				goalDescription += String.join("and ",getConditionPredicateForGames(descSetId, object_descs)) + ".";
				
				//if ((goalDescription.length()-5) > 0)
				//{
				//	goalDescription = goalDescription.substring(0, goalDescription.length() - 5) + ".";
				//}
				
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
				failureDescription += String.join("and ",getConditionPredicateForGames(descSetId, object_descs));
				
				//if ((failureDescription.length()-5) > 0)
				//{
				//	failureDescription = failureDescription.substring(0, failureDescription.length() - 5);
				//}
				
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
	
	public static String translatePerceptionDescription(Identifier fieldsId) {
		Identifier descSetId = SoarUtil.getIdentifierOfAttribute(fieldsId, "descriptions");
		if(descSetId == null) {
			return "Something went wrong";
		}
		String description = "";
		
		// Verifying if object description was generated successfully
		String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
		if(generated.equals("yes"))	{	
			// Retrieving multiple predicates
			int i = 0;
			WMElement descWME = descSetId.FindByAttribute("description", i);
			while (descWME != null)
			{
				Identifier descId = descWME.ConvertToIdentifier();
				String prep = SoarUtil.getValueOfAttribute(descId, "prep").replace("1","");
				
				// Get object 2 identifier
				Identifier obj2Id = SoarUtil.getIdentifierOfAttribute(descId, "2");
				// Description of the related object in terms of size, shape and color
				String obj2Desc = generateObjectDescription(obj2Id);

				//PR - TODO: make this into a function that can be used across multiple functions
				// Retrieving multiple objects that are related to object 2 through prep
				int k = 0;
				List<String> param1_list = new ArrayList<String>();
				WMElement param1_WME = descId.FindByAttribute("1", k);
				while (param1_WME != null)
				{
					Identifier param1Id = param1_WME.ConvertToIdentifier();
					String objDesc1 = generateObjectDescription(param1Id);
                    String article = startsWithVowel(objDesc1) ? "an ":"a ";
					param1_list.add(article + objDesc1);
					param1_WME = descId.FindByAttribute("1", ++k);
				}
				
				//PR - TODO: make this into a helper function that can be used across multiple functions
				if (param1_list.size() > 1)
				{
					String obj1Desc = String.join(", ",param1_list);
					int lastcomma = obj1Desc.lastIndexOf(',');
					obj1Desc = obj1Desc.substring(0,1).toUpperCase() + obj1Desc.substring(1,lastcomma) + " and" + obj1Desc.substring(lastcomma+1);
					description += obj1Desc + " are " + prep + " the " + obj2Desc + ". ";
				}
				else
				{
                    String obj1Desc = param1_list.get(0);
                    obj1Desc = obj1Desc.substring(0,1).toUpperCase() + obj1Desc.substring(1);
					description += obj1Desc + " is " + prep + " the " + obj2Desc + ". ";
				}
				
				descWME = descSetId.FindByAttribute("description", ++i);
			}
			
		}
		else {
			description =  "Cannot describe what I see";
		}
		
		return description;
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
	
	public static String translateTransferConcept(Identifier fields){
		String concept = SoarUtil.getValueOfAttribute(fields, "concept-name");
		concept = concept.replaceAll("\\d", "");
		String result = "Does \'" + concept + "\' mean that ";
		result += getGoalState(fields);
		result += getGoalStateFunctions(fields);
		result += "?";
		return result;
		
    }
	public static String translateAlreadyKnowConcept(Identifier fields){
		String concept = SoarUtil.getValueOfAttribute(fields, "concept-name");
		String type = SoarUtil.getValueOfAttribute(fields, "type");
		concept = concept.replaceAll("\\d", "");
		String result = "Ok, I already know the "+ type + " " + concept + ".";
		
		return result;
		
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
	
	public static String getGoalStateFunctions(Identifier fields){
		Identifier relationships = SoarUtil.getIdentifierOfAttribute(fields, "functions");
		ArrayList<String> objects = new ArrayList<String>();
		String result = "";
		String definitive = SoarUtil.getValueOfAttribute(fields, "definitive");
		
	 
		for(int index = 0; index < relationships.GetNumberChildren(); index++)
		{
		    result+= " and ";

		    WMElement wme = relationships.GetChild(index);
		    
		    Identifier function = wme.ConvertToIdentifier();
		    
            String of1 = SoarUtil.getValueOfAttribute(function, "of1");
            String of2 = SoarUtil.getValueOfAttribute(function, "of2");
            of1 = of1.replaceAll("\\d", "");
            of2 = of2.replaceAll("\\d", "");
            
            //expect more than one, loop
            Identifier object1 = SoarUtil.getIdentifierOfAttribute(function, "1");
            Identifier object2 = SoarUtil.getIdentifierOfAttribute(function, "2");
            
            String objDescription1 = generateObjectDescriptionNew(object1);
            String objDescription2 = generateObjectDescriptionNew(object2);
            
	    	if (!objDescription1.equals("it"))
	    	{
	    		objDescription1 = "the " + objDescription1;
	        }
            if (!objDescription2.equals("it"))
            {
	    		objDescription2 = "the " + objDescription2;
            }
            	result += "the " + of1 + " of " + objDescription1 + " is " +  "the " + of2 + " of "+ objDescription2;
		}
		return result;
	}

	public static String getGoalState(Identifier fields){
		Identifier relationships = SoarUtil.getIdentifierOfAttribute(fields, "relationships");
		ArrayList<String> objects = new ArrayList<String>();
		String result = "";
		String definitive = SoarUtil.getValueOfAttribute(fields, "definitive");
		
	    boolean allDefinitive = false;
	    if ((definitive != null) && (definitive.equals("yes")))
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
           
            String negative = SoarUtil.getValueOfAttribute(relation, "negative");
            
            if ((negative != null) && (negative.equals("true")))
            	preposition = "not " + preposition;
            
            
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
            	if (!objDescription1.equals("it"))
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
            	}
            	if (!objDescription2.equals("it"))
            	{
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
		return parseObject(descSetId); 

	//	if(descSetId == null){
	//		return "An object";
	//	}
	//	
	//	// Verifying if object description was generated successfully
	//	String generated = SoarUtil.getValueOfAttribute(descSetId, "generated");
	//	if(generated == null || generated.equals("yes"))	{
	//		String desc="", returnDesc = "";
	//		
	//		if (descSetId.GetNumberChildren() == 2)
	//		{
	//			Identifier descId = SoarUtil.getIdentifierOfAttribute(descSetId, "object");
	//			desc = generateObjectDescription(descId); 
	//			if(desc.charAt(0) == 'a' || desc.charAt(0) == 'e' || desc.charAt(0) == 'i' || 
	//					desc.charAt(0) == 'o' || desc.charAt(0) == 'u'){
	//				return "An " + desc;
	//			} else {
	//				return "A " + desc;
	//			}
	//		}
	//		
	//		// Counter for individual description WMEs
	//		int i = 0;
	//		WMElement descWME = descSetId.FindByAttribute("object",i);
	//		
	//		// Retrieving multiple objects if they exist
	//		while (descWME != null)
	//		{
	//			Identifier descId = descWME.ConvertToIdentifier();
	//			desc = generateObjectDescription(descId); 
	//			if(desc.charAt(0) == 'a' || desc.charAt(0) == 'e' || desc.charAt(0) == 'i' || 
	//					desc.charAt(0) == 'o' || desc.charAt(0) == 'u'){
	//				desc = "An " + desc;
	//			} else {
	//				desc = "A " + desc;
	//			}
	//			
	//			returnDesc += desc + ", ";
	//			descWME = descSetId.FindByAttribute("object",++i);
	//		}
	//		
	//		returnDesc = returnDesc.substring(0,returnDesc.length()-2); // removing unnecessary comma
	//		// replacing last comma by and
	//		int lastcomma = returnDesc.lastIndexOf(',');
	//		returnDesc = returnDesc.substring(0, lastcomma) + " and" + returnDesc.substring(lastcomma+1);
	//		return "A" + returnDesc.substring(1).toLowerCase();			
	//	}
	//	else {
	//		return "Nothing";
	//	}	
	}
	
	public static boolean startsWithVowel(String adj){
		return (adj.startsWith(" a") || adj.startsWith(" e") || adj.startsWith(" i") || 
				adj.startsWith(" o") || adj.startsWith(" u") ||
				adj.startsWith("a") || adj.startsWith("e") || adj.startsWith("i") || 
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
			return "I can't find the object, can you help?";
		}
		return "I can't find " + parseObject(obj) + ", can you help?";
	}
	
	public static String translateMultipleArguments(Identifier fieldsId){
		Identifier obj = SoarUtil.getIdentifierOfAttribute(fieldsId, "argument-info");
		if(obj == null){
			return "Can you be more specific? Which object?";
		}
		return "Can you be more specific? Which " + worldObjectToString(obj) + "?";
		
	}

	public static String translateExecutionFailure(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");
		String command = SoarUtil.getValueOfAttribute(fieldsId, "command-name").replaceAll("\\d", "");

		if(action == null || command == null){
			return "I was not able to perform the action";
		}
		
		return "The " + command + " command failed while doing " + action;
	}	

	public static String translateUnsatisfiedGoal(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");

		if(action == null){
			return "I was not able to achieve the goal of the action";
		}
		
		return "I could not achieve the goal of " + action;
	}	
	

	public static String translateCommandFailure(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");
		String command = SoarUtil.getValueOfAttribute(fieldsId, "command-name").replaceAll("\\d", "");

		if(action == null || command == null){
			return "An outgoing command could not be interpreted";
		}
		
		return "The " + command + " command failed to be interpreted during " + action;
	}	
	
	public static String translateActionFailure(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");
		String subaction = SoarUtil.getValueOfAttribute(fieldsId, "action-name").replaceAll("\\d", "");

		if(action == null || subaction == null){
			return "I was not able to perform the action";
		}
		
		return "The action " + subaction + " failed while doing " + action;
	}	
	
	public static String translateMissingObjectFailure(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");
		Identifier objectId = SoarUtil.getIdentifierOfAttribute(fieldsId, "missing-object");
      String objectDesc = worldObjectToString(objectId);

		if(action == null || objectDesc == null){
         return "I lost the object I was using";
		}
		
		return "I lost " + objectDesc + " while doing " + action;
	}	
	
	public static String translateInvalidArgumentFailure(Identifier fieldsId){
		String action = SoarUtil.getValueOfAttribute(fieldsId, "action-handle").replaceAll("\\d", "");
		String subtype = SoarUtil.getValueOfAttribute(fieldsId, "subtype").replaceAll("\\d", "");

      if(subtype != null && subtype.equals("missing-property")){
         Identifier objectId = SoarUtil.getIdentifierOfAttribute(fieldsId, "object");
         if(objectId != null){
            String objectDesc = worldObjectToString(objectId);
		      String missingprop = SoarUtil.getValueOfAttribute(fieldsId, "missing-property").replaceAll("\\d", "");
            if(objectDesc != null && missingprop != null){
               return objectDesc + " is missing the " + missingprop + " property";
            }
         }
      } else if(subtype != null && subtype.equals("missing-argument")){
		  String argName = SoarUtil.getValueOfAttribute(fieldsId, "missing-argument");
		  if (action != null && argName != null){
			  return "The " + action + " action was missing a " + argName + " argument";
		  }
	  } else {
		   String argname = SoarUtil.getValueOfAttribute(fieldsId, "argument-name");
         if(action != null && argname != null){
            return "The " + argname + " argument of the " + action + " action was invalid";
         }
      }
		
      return "I got an invalid argument in an action";
	}	

/*	
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
//*/
	
	private static String translateNextTaskPrompt() {
        return "I'm ready for a new task";
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
		// PR - TODO consider adding article in here.
		String root = "object"; // PR - Commenting because now category seems to be correct. Verify after merge
		ArrayList<String> adjectives = new ArrayList<String>();
		Identifier predsId = SoarUtil.getIdentifierOfAttribute(descId, "predicates");
		
		Map<String, String> att_val = new HashMap<String, String>();
		for(int c = 0; c < predsId.GetNumberChildren(); c++){
			WMElement el = predsId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase();
			att_val.put(att, val);
		}
		
		if (SoarUtil.getValueOfAttribute(descId, "root-category") != null)
		{
			root = SoarUtil.getValueOfAttribute(descId, "root-category");
		}
		else if(att_val.containsKey("shape"))
		{
			root = att_val.get("shape");
		}
		else if (att_val.containsKey("category"))
		{
			root = att_val.get("category");
		}
		root = root.replaceAll("1", "");
		
		if (att_val.containsKey("size"))
		{
			adjectives.add(att_val.get("size"));
		}
		if(att_val.containsKey("color"))
		{
			adjectives.add(att_val.get("color"));
		}
		if(att_val.containsKey("modifier1"))
		{
			adjectives.add(att_val.get("modifier1"));
		}
		if(att_val.containsKey("name"))
		{
			adjectives.add(att_val.get("name"));
		}
		// PR - TODO: need to address other attributes such as x and y co-ordinates and such. Figure how to do this in a general way.
		String desc = "";
		for(String adj : adjectives){
			adj = adj.replace("1", "");
			desc += adj + " ";
		}
		
		desc = desc + root;
		return desc;
	}

	public static String join(List<String> strings, String sep){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : strings){
			if(s == null){ continue; }
			if(!first){
				sb.append(sep);
			}
			sb.append(s);
			first = false;
		}
		return sb.toString();
	}

	public static String parseObject(Identifier objId){
		ArrayList<String> words = new ArrayList<String>();
		Identifier predsId = SoarUtil.getIdentifierOfAttribute(objId, "predicates");

		String sentence = SoarUtil.getValueOfAttribute(predsId, "sentence");
		if(sentence != null){
			return sentence;
		}

		words.add(SoarUtil.getValueOfAttribute(predsId, "size"));
		words.add(SoarUtil.getValueOfAttribute(predsId, "color"));

		Set<String> mods = SoarUtil.getAllValuesOfAttribute(predsId, "modifier1");
		if(mods.size() > 0){
			String lowest = "zzz";
			for(String mod : mods){
				if(mod.toLowerCase().compareTo(lowest) < 0){
					lowest = mod;
				}
			}
			words.add(lowest);
		}
		words.add(SoarUtil.getValueOfAttribute(predsId, "shape"));

		String name = SoarUtil.getValueOfAttribute(predsId, "name");
		if(name == null){
			name = SoarUtil.getValueOfAttribute(objId, "root-category");
		} 
		if(name == null){
			name = SoarUtil.getValueOfAttribute(predsId, "category");
		} 
		words.add(name);

		String objDesc = join(words, " ");
		return objDesc.replaceAll("\\d", "");
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
		
		if (root.equals("it"))
			return root;
		
		//ArrayList<String> adjectives = (ArrayList<String>) SoarUtil.getAllValuesOfAttribute(descId, "value");
		ArrayList<String> adjectives = new ArrayList<String>();
		
		for(int c = 0; c < descId.GetNumberChildren(); c++){
			WMElement el = descId.GetChild(c);
			String att = el.GetAttribute().toLowerCase();
			String val = el.GetValueAsString().toLowerCase();
			val = val.replaceAll("\\d", "");//replace("1", "");
			if (val.isEmpty())
				continue;
			if(att.equals("type"))
				continue;
			if(att.equals("name")){
				root = val;
			} else if ((att.equals("shape")) || (val.equals("block")) || (val.equals("location")))
			{
				root = val;
			} else
			{
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
        String type="";
		
		while (!(attribute_value.equals("primitive") ||  attribute_value.equals("input-arg")))
		{
			description += SoarUtil.getValueOfAttribute(objId, "name").replaceAll("\\d+.*","") + " ";
			Identifier arg = SoarUtil.getIdentifierOfAttribute(objId, "args");
			Identifier arg1 = SoarUtil.getIdentifierOfAttribute(arg, "1");
			prev_attribute = attribute_value;
			attribute_value = SoarUtil.getValueOfAttribute(arg1, "attribute");
			type = SoarUtil.getValueOfAttribute(objId, "type");
			objId = arg1;
		}
		
		// PR - figure this object thing out, this may not be worth it in this case.
		// This is in case the "object" is directly referred in the statement, it must not be ignored. For e.g. in stack-block, a clear object is larger-than a clear block.
		//if(!prev_attribute.equals("category"))
		//{
		//	description += SoarUtil.getValueOfAttribute(objId, "name").replaceAll("\\d+.*","") + " ";
		//}

		if(attribute_value.equals("input-arg") || type.equals("concept")) // PR - except for husband/passenger, concepts tend to be adjectives
		{
			description += "object ";
		}
		
		if(set.equals("set"))
		{
			description = description.substring(0,description.length() - 1) + "s ";
		}
		
		return description;
	}
	
	
	public static List<String> getIndividualObjectPredicateForGame(Identifier objDescId)
	{
		String objectDescription = "";
		String article = "";
		String rtype = "";
		
		String negative = SoarUtil.getValueOfAttribute(objDescId, "negative");
		String satisfied = SoarUtil.getValueOfAttribute(objDescId, "satisfied");		
		if(satisfied != null)
		{
			negative = satisfied;
		}
		
		Identifier objId1 = SoarUtil.getIdentifierOfAttribute(objDescId, "1");
		
		// Based on if the rtype is set or single, auxiliaryVerb will be set as "are" or "is"
		String auxiliaryVerb = SoarUtil.getValueOfAttribute(objDescId, "aux-verb");
		if(auxiliaryVerb != null)
		{
			rtype = auxiliaryVerb.equals("are ")?"set":"single";
			objectDescription += getObjectDescriptionForGames(objId1);
		}
		else
		{
			// This is an object without a param-id
			rtype = SoarUtil.getValueOfAttribute(objDescId, "rtype");
			objectDescription += getObjectDescriptionForGames(objDescId);
		}
		// Adding preposition to the description			
		String prep = SoarUtil.getValueOfAttribute(objDescId, "prep");
		if (prep == null)
		{
			List<String> objDesc_values = Arrays.asList(objectDescription, auxiliaryVerb, rtype, "0");
			return objDesc_values;
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
		List<String> objDesc_values = Arrays.asList(objectDescription, auxiliaryVerb, rtype, "0");
		return objDesc_values;
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
		{		Identifier objDescId = objDescWME.ConvertToIdentifier();
				Integer param_id = Integer.parseInt(SoarUtil.getValueOfAttribute(objDescId, "param-id"));
				List<String> objDesc_values = getIndividualObjectPredicateForGame(objDescId);
				
				if (!object_descs.containsKey(param_id))
				{
					// PR - Hack, this should be able to be done using lambda expressions object_descs.values().stream().anymatch(l -> l.contains(objectDescription)) in java 1.8
					String objectDescription = objDesc_values.get(0);
					//ArrayList<String> object_descs_values = (object_descs.values()).stream().filter(p -> p.get(0)).collect(Collectors.toList())
					if(object_descs.values().contains(objectDescription))
					{
						objectDescription = "other " + objectDescription;
					}
					objDesc_values.set(0,objectDescription);
					//List<String> objDesc_values = Arrays.asList(objectDescription, auxiliaryVerb, rtype, "0");
					object_descs.put(param_id, objDesc_values);
				}
				objDescWME = descSetId.FindByAttribute("obj-desc", ++i);
		}
		
		return object_descs;
	}
	
	// Creates english statements based on the conditions and relations between objects specified as a part of conditions attribute in nlp-set in the games.
	// This function combines multiple object predicates together when necessary
	public static List<String> getConditionPredicateForGames(Identifier descSetId, HashMap<Integer, List<String>> object_descs)
	{
		List<String> descriptionList = new ArrayList<String>();
		// Counter for individual description WMEs
		int k = 0;
		WMElement conditionVarWME = descSetId.FindByAttribute("description", k);
		while(conditionVarWME != null)
		{
			String description = "";
			Identifier conditionId = conditionVarWME.ConvertToIdentifier();
			String negative = SoarUtil.getValueOfAttribute(conditionId, "negative");
			String satisfied = SoarUtil.getValueOfAttribute(conditionId, "satisfied");
			String article1 = "", article2="",article3="";
			Integer paramid1 = Integer.parseInt(SoarUtil.getValueOfAttribute(conditionId, "1"));
			List<String> objDesc1 = new ArrayList<String>();
			
			if(satisfied != null)
			{
				negative = satisfied;
			}
			
			// Retrieving object descriptions and their corresponding auxiliary verbs
			if (paramid1 != 0)
			{
				objDesc1 = object_descs.get(paramid1);
			}
			else
			{
				// This is for describing the unsatisfied object condition
				Identifier objDescId = SoarUtil.getIdentifierOfAttribute(conditionId, "obj-desc");
				objDesc1 = getIndividualObjectPredicateForGame(objDescId);
				if(objDesc1.get(2).equals("set"))
				{
					description += "I do not see all ";
				}
				else
				{
					description += "I do not see ";
				}
			}
			
			article1 = addArticleForObjectDescription(objDesc1);
			
			// When the condition is represented using only one param-id hence only one predicate is in the condition for e.g.  block on a clear location in one predicate retrieved in the object description
			String param2_string = SoarUtil.getValueOfAttribute(conditionId, "2");
			if(param2_string == null)
			{
				description += article1 + objDesc1.get(0);
				conditionVarWME = descSetId.FindByAttribute("description", ++k);
				descriptionList.add(description);
				//description += "and ";
				continue;
			}
			
            Integer paramid2;
            List<String> objDesc2 = new ArrayList<String>();
			if(param2_string.equals("id2")) // If this is prepositional predicate where only first arg has param-id
			{
				Identifier id2_string = SoarUtil.getIdentifierOfAttribute(conditionId, "id2");
                objDesc2 = getIndividualObjectPredicateForGame(id2_string);
			}
			else {
				paramid2 = Integer.parseInt(param2_string);
                objDesc2 = object_descs.get(paramid2);
            }
		
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
				descriptionList.add(description);
				//description += "and ";
				continue;
			}

			String name = SoarUtil.getValueOfAttribute(conditionId, "name");
								
			// When the condition involves the param-ids/values of two predicates being the same for e.g. the color of A is red/the color of A is the color of B
			if (name != null)
			{
				String equalcondition_article = SoarUtil.getValueOfAttribute(conditionId, "article");
				String aux_verb = "";
				if(negative.equals("true"))
				{
					aux_verb = "is not ";	
				}
				else
				{
					aux_verb = "is ";
				}
				
				if (prep.equals("number"))
				{
					description += equalcondition_article + name + " of " + article1 + objDesc1.get(0) + aux_verb + param2_string + " ";
				}
				else if (prep.equals("relation"))
				{
					String relation = SoarUtil.getValueOfAttribute(conditionId, "relation") + " than ";
					description += equalcondition_article + name + " of " + article1 + objDesc1.get(0) + aux_verb + relation + equalcondition_article + name + " of " + article2 + objDesc2.get(0);  	 			
				}
				else
				{
					description += equalcondition_article + name + " of " + article1 + objDesc1.get(0) + aux_verb + equalcondition_article + name + " of " + article2 + objDesc2.get(0);
				}
				
				conditionVarWME = descSetId.FindByAttribute("description", ++k);
                descriptionList.add(description);
				//description += "and ";
				continue;
			}
			
			prep = prep.replace("1","");
			if(prep.equals("adjacent"))
			{
				prep += " to";
			}
			if(negative != null && negative.equals("true"))
			{	
				prep = "not " + prep;
			}
			
			description += article1 + objDesc1.get(0) + objDesc1.get(1) + prep + " " + article2 + objDesc2.get(0);
			
			conditionVarWME = descSetId.FindByAttribute("description", ++k);
			descriptionList.add(description);
			//description += "and ";
		}
		
		return descriptionList;
	}

	public static String taskHandle(Identifier fieldsId){
		return SoarUtil.getValueOfAttribute(fieldsId, "task-handle").replaceAll("\\d", "");
	}


/*
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
  */
	
}
