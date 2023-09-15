package edu.umich.rosie.connectors;

import sml.*;
import sml.Agent.PrintEventInterface;
import edu.umich.rosie.soar.SoarClient;
import edu.umich.rosie.soar.FileWriterConnector;

public class LogfileWriter extends FileWriterConnector implements PrintEventInterface {
	private long printCallbackId = -1;

	public LogfileWriter(SoarClient agent, String filename){
		super(agent, filename);
	}

	@Override
	public void connect(){
        printCallbackId = soarClient.getAgent().RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, null);
		super.connect();
	}
	
	@Override
	public void disconnect(){
        if(printCallbackId != -1){
            soarClient.getAgent().UnregisterForPrintEvent(printCallbackId);
			printCallbackId = -1;
        }
		super.disconnect();
	}

    @Override
    public void printEventHandler(int eventID, Object data, Agent agent, String message) {
		writer.print(message);
    }
}
