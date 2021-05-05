package edu.umich.rosie.connectors;

import sml.*;
import sml.Agent.PrintEventInterface;
import edu.umich.rosie.soar.SoarClient;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soar.FileWriterConnector;
import edu.umich.rosie.connectors.ActionStackConnector;
import edu.umich.rosie.connectors.ActionStackConnector.TaskEventListener;
import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.IMessagePasser.IMessageListener;
import edu.umich.rosie.language.LanguageConnector;

public class TaskTestWriter extends FileWriterConnector
							implements TaskEventListener, IMessageListener, PrintEventInterface {
	private long printCallbackId = -1;

	public TaskTestWriter(SoarClient client, String filename){
		super(client, filename);

		// Ensure that the soar client has an ActionStackConnector
		ActionStackConnector asConn = soarClient.getConnector(ActionStackConnector.class);
		if(asConn == null){
			soarClient.addConnector(new ActionStackConnector(soarClient));
		}

		this.setOutputHandlerNames(new String[]{ "scripted-sentence" });
	}

	@Override
	public void connect(){
		soarClient.getConnector(ActionStackConnector.class)
			.registerForTaskEvent(this);

		soarClient.getConnector(LanguageConnector.class)
			.getMessagePasser()
			.addMessageListener(this);

        printCallbackId = soarClient.getAgent().RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, null);

		super.connect();
	}
	
	@Override
	public void disconnect(){
		
		soarClient.getConnector(ActionStackConnector.class)
			.unregisterForTaskEvent(this);

		soarClient.getConnector(LanguageConnector.class)
			.getMessagePasser()
			.removeMessageListener(this);

        if(printCallbackId != -1){
            soarClient.getAgent().UnregisterForPrintEvent(printCallbackId);
			printCallbackId = -1;
        }

		super.disconnect();
	}

    @Override
    protected void onOutputEvent(String attName, Identifier id){
    	if (attName.equals("scripted-sentence")){
			String sentence = SoarUtil.getValueOfAttribute(id, "sentence");
			writer.print("I: \"" + sentence + "\"\n");
    	}
		id.CreateStringWME("handled", "true");
	}

	@Override
	public void taskEventHandler(String taskInfo){
		// Write the action stack information (when the agent begins/ends each task and subtask)
		writer.print(taskInfo + "\n");
	}	

	@Override
	public void receiveMessage(IMessagePasser.RosieMessage message){
		// Write each sentence that the agent says as output
		writer.print("R: \"" + message.message + "\"\n");
	}

    @Override
    public void printEventHandler(int eventID, Object data, Agent agent, String message) {
		message = message.trim();
		if(message.indexOf("@TEST:") == 0){
			writer.print(message.substring(7) + "\n");
		}
    }
}
