package com.soartech.bolt.testing;

import java.io.File;
import java.io.IOException;

public class Settings {
	final static private Settings instance = new Settings();
	// final static private String scriptDirectory = "/scripts";

	private File sboltDirectory;
	
	private Object automatedLock = new Object();
	private boolean automated;
	
	private Object scriptRunningLock = new Object();
	private boolean scriptRunning;

	private Settings() {
		File dir;
		try {
			dir = new File(new File("").getCanonicalPath());
		} catch (IOException e) {
			dir = new File("");
		}
		File[] files = dir.listFiles();
		for(File f : files) {
			if(f.getName().equals("scripts"))
				dir = f;
		}
		sboltDirectory = dir;
		
		automated = false;
		scriptRunning = false;
	}

	public static Settings getInstance() {
		return instance;
	}

	public File getSboltDirectory() {
		return sboltDirectory;
	}

	public void setSboltDirectory(File sboltDirectory) {
		this.sboltDirectory = sboltDirectory;
	}

	public boolean isAutomated() {
		synchronized(automatedLock) {
			return automated;
		}
	}

	public void setAutomated(boolean automated) {
		synchronized(automatedLock) {
			this.automated = automated;
		}
	}

	public boolean isScriptRunning() {
		synchronized(scriptRunningLock) {
			return scriptRunning;
		}
	}

	public void setScriptRunning(boolean scriptRunning) {
		synchronized(scriptRunningLock) {
			this.scriptRunning = scriptRunning;
		}
	}
}
