package edu.umich.rosie.connectors;

import sml.*;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soar.FileWriterConnector;
import edu.umich.rosie.connectors.ActionStackConnector;
import edu.umich.rosie.connectors.ActionStackConnector.TaskEventListener;
import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.IMessagePasser.IMessageListener;
import edu.umich.rosie.language.LanguageConnector;

public class TaskTestWriter extends FileWriterConnector
							implements TaskEventListener, IMessageListener {

	public TaskTestWriter(SoarAgent agent, String filename){
		super(agent, filename);

		// Ensure that the soar agent has an ActionStackConnector
		ActionStackConnector asConn = soarAgent.getConnector(ActionStackConnector.class);
		if(asConn == null){
			soarAgent.addConnector(new ActionStackConnector(soarAgent));
		}

		this.setOutputHandlerNames(new String[]{ "scripted-sentence" });
	}

	@Override
	public void connect(){
		soarAgent.getConnector(ActionStackConnector.class)
			.registerForTaskEvent(this);

		soarAgent.getConnector(LanguageConnector.class)
			.getMessagePasser()
			.addMessageListener(this);

		super.connect();
	}
	
	@Override
	public void disconnect(){
		
		soarAgent.getConnector(ActionStackConnector.class)
			.unregisterForTaskEvent(this);

		soarAgent.getConnector(LanguageConnector.class)
			.getMessagePasser()
			.removeMessageListener(this);

		super.disconnect();
	}

    @Override
    protected void onOutputEvent(String attName, Identifier id){
    	if (attName.equals("scripted-sentence")){
			String sentence = SoarUtil.getValueOfAttribute(id, "sentence");
			writer.print("I: \"" + sentence + "\"\n");
    	}
	}

	@Override
	public void taskEventHandler(String taskInfo){
		// Write the action stack information (when the agent begins/ends each task and subtask)
		writer.print(taskInfo + "\n");
	}	

	@Override
	public void receiveMessage(IMessagePasser.RosieMessage message){
		// Write each sentence that the agent says as output
		writer.write("R: \"" + message.message + "\"\n");
	}
}
