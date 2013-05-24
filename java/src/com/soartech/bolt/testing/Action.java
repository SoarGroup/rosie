package com.soartech.bolt.testing;

public class Action {
	
	private ActionType type;
	private String action;
	
	public Action(ActionType type, String action) {
		this.type = type;
		this.action = action;
	}
	
	public ActionType getType() {
		return type;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
