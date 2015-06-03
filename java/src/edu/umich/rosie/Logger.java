package edu.umich.rosie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import sml.Agent;
import sml.Agent.PrintEventInterface;
import sml.smlPrintEventId;

public class Logger implements PrintEventInterface {
	private PrintWriter soarLogWriter;
	private PrintWriter interactionLogWriter;

	private long logNumber;

	public Logger(SoarAgent soarAgent, String enableLog) throws IOException {
		if (enableLog != null && !enableLog.equals("false")) {
			if (enableLog.equals("true")) {
				logNumber = System.currentTimeMillis()/1000 % 1000;
				interactionLogWriter = new PrintWriter(new File(
						"logs/exp/interaction-log-" + logNumber + ".log"));
				soarLogWriter = new PrintWriter(new File("logs/exp/soar-log-"
						+ logNumber + ".log"));
			} else if (enableLog.equals("human")) {
				String id = getInformation();
				int trial = 0;
				String fileName;
				File folder = new File("logs/hri/");
				File[] listOfFiles = folder.listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					fileName = listOfFiles[i].getCanonicalPath();
					if (fileName.contains(id)) {
						String string = fileName.replaceAll("[^0-9]", "");
						int number = Integer.parseInt(string);
						if (number > trial)
							trial = number;
					}
				}
				interactionLogWriter = new PrintWriter(new File(
						"logs/hri/interaction-log-" + id + "-" + (trial + 1)
								+ ".log"));
				soarLogWriter = new PrintWriter(new File("logs/hri/soar-log-"
						+ id + "-" + (trial + 1) + ".log"));
			}
			soarAgent.getAgent().RegisterForPrintEvent(
					smlPrintEventId.smlEVENT_PRINT, this, this);
		}

	}

	private String getInformation() {
		String id = JOptionPane
				.showInputDialog("Provide an identification string.");
		return id;
	}

	@Override
	public void printEventHandler(int eventID, Object data, Agent agent,
			String message) {
		if (soarLogWriter != null){
			synchronized (soarLogWriter) {
				soarLogWriter.println(message);
				}
		}
	}

	public void writeInteractionLog(String message) {
		if (interactionLogWriter != null) {
			synchronized (interactionLogWriter){
				interactionLogWriter.println(message);
			}
		}
	}

	public void close() {
		if (interactionLogWriter != null)
			interactionLogWriter.close();
		if (interactionLogWriter != null)
			soarLogWriter.close();
	}
}