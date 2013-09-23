package com.soartech.bolt.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.JFileChooser;

import com.soartech.bolt.script.ui.command.PointAtObject;

import edu.umich.insoar.ChatFrame;

public class Util {
	
	public static void saveFile(File f, List<String> history) {
		try {
			Writer output = new BufferedWriter(new FileWriter(f));
			for(String str : history) {
				output.write(str+"\n");
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveFileBechtelFormat(File f, List<String> history) {
		try {
			Writer output = new BufferedWriter(new FileWriter(f));
			output.write("#!BechtelFormat\n");
			for(String str : history) {
				String[] res = str.split(":");
				if(res.length > 1 && res[0] != null && res[1] != null) {
					String charString;
					try {
						charString = ScriptDataMap.getInstance().getChar(res[0]+":").toString();
						output.write(charString+" "+res[1].trim()+"\n");
					} catch (UiCommandNotFoundException e) {
						System.out.println("Ignoring line: "+str);
						//ignore unrecognized lines
					}
				}
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Script loadScript() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(Settings.getInstance().getSboltDirectory());
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return ParseScript.parse(chooser.getSelectedFile());
		}
		return null;
	}
	
	public static void saveScript(List<String> chatMessages) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(Settings.getInstance().getSboltDirectory());
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Util.saveFileBechtelFormat(chooser.getSelectedFile(), chatMessages);
		}
	}
	
    public static void handleNextScriptAction(Script script, List<String> chatMessages) {
    	if(!Settings.getInstance().isScriptRunning()) {
    		return;
    	}
    	if(Settings.getInstance().isAutomated()) {
    		new ScriptRunner(script, chatMessages).start();
    	} else {
    		ScriptRunner.nextScriptAction(script, chatMessages);
    	}
    	if(script != null && script.peekType() == ActionType.Agent && Settings.getInstance().isAutomated()) {
    		ChatFrame.Singleton().setWaiting(true);
        }
    }
    
    public static void executeUiAction(String action) {
    	try {
    		if(action.contains("select")){
    			Integer id = Integer.parseInt(action.split(" ")[1]);
    			(new PointAtObject(id)).execute();
    		} else {
    			ScriptDataMap.getInstance().getUiCommand(action).execute();
    		}
		} catch (UiCommandNotFoundException e) {
			e.printStackTrace();
		}
    }
}
