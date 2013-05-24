package com.soartech.bolt.testing;

import java.util.List;

import edu.umich.insoar.ChatFrame;
import edu.umich.insoar.InSoar;

public class ScriptRunner extends Thread {
	private Script script;
	private List<String> chatMessages;
	
	public ScriptRunner(Script script, List<String> chatMessages) {
		this.script = script;
		this.chatMessages = chatMessages;
	}

	@Override
	public void run() {
		if(script == null) {
    		ChatFrame.Singleton().addMessage("No script loaded!");
    		return;
    	}
    	if(!script.hasNextAction()) {
    		ChatFrame.Singleton().addMessage("Script finished.");
    		return;
    	}
    	if(ChatFrame.Singleton().isWaitingForMentor()) {
    		return;
    	}
    	
    	String observed;
    	if(chatMessages.size() > 0) {
    		observed = chatMessages.get(chatMessages.size()-1);
    	} else {
    		observed = "";
    	}
    	
    	Action next = script.getNextAction();
    	
    	if(next.getType() == ActionType.Mentor) {
    		ChatFrame.Singleton().setWaitingForMentor(true);
    		if(Settings.getInstance().isAutomated()) {
    			ChatFrame.Singleton().setWaitingForMentor(false);
    			ChatFrame.Singleton().addMessage(next.getAction(), ActionType.Mentor);
    	        ChatFrame.Singleton().sendSoarMessage(next.getAction());
    			Util.handleNextScriptAction(script, chatMessages);
    			return;
    		}
    		ChatFrame.Singleton().preSetMentorMessage(next.getAction());
    	}
    	if(next.getType() == ActionType.Agent) {
    		//check if response is correct
    		String expected = next.getAction();
    		if(!observed.contains("Agent:")) {
    			script.insertFirstAction(next);
    			return;
    		}
    		ChatFrame.Singleton().setWaiting(false);
    		if(!observed.contains(expected)) {
    			ChatFrame.Singleton().addMessage("- Error - Expected: "+expected, ActionType.Incorrect);
    			if(Settings.getInstance().isAutomated()) {
    				// AM: Changed so it refences the 
    				InSoar.Singleton().getSoarAgent().stop();
    				//ChatFrame.Singleton().stopAgent();
    				return;
    			}
    		} else {
    			ChatFrame.Singleton().addMessage("- Correct -", ActionType.Correct);
    		}
    		if(observed.contains("Please") && Settings.getInstance().isAutomated()) {
    			// wait 1 second for the simulator values to update
    			synchronized(this) {
    				try {
    					ChatFrame.Singleton().setWaiting(true);
						this.wait(1000);
						ChatFrame.Singleton().setWaiting(false);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    		}
    	}
    	if(next.getType() == ActionType.Comment) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.AgentAction) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.MentorAction) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.UiAction) {
    		String a = next.getAction();
			Util.executeUiAction(a);
			ChatFrame.Singleton().addMessage(a, ActionType.UiAction);
    	}
    	if(!script.actionRequiresMentorAttention(next)) {
    		Util.handleNextScriptAction(script, chatMessages);
    	} else {
    		ChatFrame.Singleton().setWaitingForScript(true);
    	}
	}
	
	public static void nextScriptAction(Script script, List<String> chatMessages) {
		if(script == null) {
    		ChatFrame.Singleton().addMessage("No script loaded!");
    		return;
    	}
    	if(!script.hasNextAction()) {
    		ChatFrame.Singleton().addMessage("Script finished.");
    		return;
    	}
    	if(ChatFrame.Singleton().isWaitingForMentor()) {
    		return;
    	}
    	
    	String observed;
    	if(chatMessages.size() > 0) {
    		observed = chatMessages.get(chatMessages.size()-1);
    	} else {
    		observed = "";
    	}
    	
    	Action next = script.getNextAction();
    	
    	if(next.getType() == ActionType.Mentor) {
    		ChatFrame.Singleton().setWaitingForMentor(true);
    		if(Settings.getInstance().isAutomated()) {
    			ChatFrame.Singleton().setWaitingForMentor(false);
    			ChatFrame.Singleton().addMessage(next.getAction(), ActionType.Mentor);
    	        ChatFrame.Singleton().sendSoarMessage(next.getAction());
    			Util.handleNextScriptAction(script, chatMessages);
    			return;
    		}
    		ChatFrame.Singleton().preSetMentorMessage(next.getAction());
    	}
    	if(next.getType() == ActionType.Agent) {
    		//check if response is correct
    		String expected = next.getAction();
    		if(!observed.contains("Agent:")) {
    			script.insertFirstAction(next);
    			return;
    		}
    		ChatFrame.Singleton().setWaiting(false);
    		if(!observed.contains(expected)) {
    			ChatFrame.Singleton().addMessage("- Error - Expected: "+expected, ActionType.Incorrect);
    			if(Settings.getInstance().isAutomated()) {
    				// AM: Changed so it refences the 
    				InSoar.Singleton().getSoarAgent().stop();
    				//ChatFrame.Singleton().stopAgent();
    				return;
    			}
    		} else {
    			ChatFrame.Singleton().addMessage("- Correct -", ActionType.Correct);
    		}
    	}
    	if(next.getType() == ActionType.Comment) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.AgentAction) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.MentorAction) {
    		ChatFrame.Singleton().addMessage(next.getAction(), next.getType());
    	}
    	if(next.getType() == ActionType.UiAction) {
    		String a = next.getAction();
			Util.executeUiAction(a);
			ChatFrame.Singleton().addMessage(a, ActionType.UiAction);
    	}
    	if(!script.actionRequiresMentorAttention(next)) {
    		Util.handleNextScriptAction(script, chatMessages);
    	} else {
    		ChatFrame.Singleton().setWaitingForScript(true);
    	}
	}
}
