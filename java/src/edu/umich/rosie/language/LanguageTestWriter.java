package edu.umich.rosie.language;

import sml.*;
import sml.Agent.PrintEventInterface;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.FileWriterConnector;

public class LanguageTestWriter extends FileWriterConnector implements PrintEventInterface {
	private long printCallbackId = -1;

	public LanguageTestWriter(SoarAgent agent, String filename){
		super(agent, filename);
	}

	@Override
	public void connect(){
        printCallbackId = soarAgent.getAgent().RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, null);
		super.connect();
	}
	
	@Override
	public void disconnect(){
        if(printCallbackId != -1){
            soarAgent.getAgent().UnregisterForPrintEvent(printCallbackId);
			printCallbackId = -1;
        }
		super.disconnect();
	}

    @Override
    public void printEventHandler(int eventID, Object data, Agent agent, String message) {
    	final String preamble = "Interpreting Instructor Message:";
    	if (message.startsWith(preamble)) {
    		//	Strip off the preamble
    		String rest = message.substring(preamble.length() + 2);
    		//	This may get called twice with the same message data
    		//	If so, the second time the sentence is not given,
    		//	just the parser output message.
    		//	In such a case, print nothing.
    		if (!rest.startsWith("(")) {
        		writer.print(rest);
        		writer.println();
    		}
    	}
    }
}
