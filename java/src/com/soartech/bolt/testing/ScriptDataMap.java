package com.soartech.bolt.testing;

import com.soartech.bolt.script.ui.command.AutomateScript;
import com.soartech.bolt.script.ui.command.ClearClassifierData;
import com.soartech.bolt.script.ui.command.PointAtObject;
import com.soartech.bolt.script.ui.command.ResetRobotArm;
import com.soartech.bolt.script.ui.command.UiCommand;

public class ScriptDataMap {
	private static final ScriptDataMap instance = new ScriptDataMap();
	private BijectiveMap<ActionType, Character> charMap = new BijectiveMap<ActionType, Character>();
	private BijectiveMap<ActionType, String> stringMap = new BijectiveMap<ActionType, String>();
	private BijectiveMap<String, UiCommand> uiCommandMap = new BijectiveMap<String, UiCommand>();
	
	public static ScriptDataMap getInstance() {
		return instance;
	}
	
	private ScriptDataMap() {
		add(ActionType.Comment, "Comment:", '#');
		add(ActionType.AgentAction, "AgentAction:", '{');
		add(ActionType.MentorAction, "MentorAction:", '}');
		add(ActionType.Agent, "Agent:", '<');
		add(ActionType.Mentor, "Mentor:", '>');
		add(ActionType.UiAction, "UiAction:", '@');
		
		addUiCommand("automated true", new AutomateScript(true));
		addUiCommand("automated false", new AutomateScript(false));
		addUiCommand("arm reset", new ResetRobotArm());
		addUiCommand("point square", new PointAtObject(0));
		addUiCommand("point circle", new PointAtObject(1));
		addUiCommand("point tee", new PointAtObject(2));
		addUiCommand("point triangle", new PointAtObject(3));
		addUiCommand("classifier clear", new ClearClassifierData());
	}
	
	public void add(ActionType type, String s, char c) {
		charMap.add(type, new Character(c));
		stringMap.add(type, s);
	}
	
	public void addUiCommand(String commandString, UiCommand command) {
		uiCommandMap.add(commandString, command);
	}
	
	private ActionType checkNull(ActionType type) {
		if(type == null) {
			return type = ActionType.Invalid;
		} else {
			return type;
		}
	}
	
	public UiCommand getUiCommand(String commandString) throws UiCommandNotFoundException {
		UiCommand res = uiCommandMap.getLeft(commandString);
		if(res != null)
			return res;
		throw new UiCommandNotFoundException("Could not find UiCommand for: "+commandString);
	}
	
	public Character getChar(String startString) throws UiCommandNotFoundException {
		Character res = charMap.getLeft(stringMap.getRight(startString));
		if(res != null)
			return res;
		throw new UiCommandNotFoundException("Could not find UiCommand for: "+startString);
	}
	
	public Character getChar(ActionType type) throws UiCommandNotFoundException {
		Character res = charMap.getLeft(type);
		if(res != null)
			return res;
		throw new UiCommandNotFoundException("Could not find character for: "+type);
	}
	
	/**
	 * Get the string associated with an ActionType.
	 * @param type
	 * @return The string associated with an ActionType,
	 * or a string containing 4 spaces if no string is associated.
	 */
	public String getString(ActionType type) {
		String r = stringMap.getLeft(type);
		if(r != null) {
			return r;
		}
		return "   ";
	}
	
	/**
	 * Get the ActionType associated with a starting string.
	 * For example, ActionType.Mentor would return <code>Mentor:</code>
	 * @param startString
	 * @return The associated ActionType, or ActionType.Invalid if there
	 * is not one.
	 */
	public ActionType getType(String startString) {
		return checkNull(stringMap.getRight(startString));
		
	}
	
	public ActionType getType(char c) {
		return checkNull(charMap.getRight(new Character(c)));
	}
}
