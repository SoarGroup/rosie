package edu.umich.rosie.language.Message;

import java.io.Serializable;

import edu.umich.rosie.language.IMessagePasser.RosieMessage;
import edu.umich.rosie.language.LanguageConnector.MessageType;

public class RosieMessageSerializable implements Serializable
{
	private static final long serialVersionUID = 8983177521839071138L;

	public int id;
	public MessageType type;
	public String message;

	public RosieMessageSerializable(RosieMessage message)
	{
		this.id = message.id;
		this.type = message.type;
		this.message = message.message;
	}

	public RosieMessage toRosieMessage()
	{
		return new RosieMessage(id, type, message);
	}
}
