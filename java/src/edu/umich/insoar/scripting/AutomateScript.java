package edu.umich.insoar.scripting;

import edu.umich.insoar.testing.Settings;

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
