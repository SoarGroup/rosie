package com.soartech.bolt.script.ui.command;

import com.soartech.bolt.testing.Settings;

public class AutomateScript implements UiCommand {
	boolean automated;
	
	public AutomateScript(boolean isAutomated) {
		automated = isAutomated;
	}
	@Override
	public void execute() {
		Settings.getInstance().setAutomated(automated);
	}
}
