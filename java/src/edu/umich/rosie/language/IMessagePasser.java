package edu.umich.rosie.language;

import edu.umich.rosie.language.LanguageConnector.MessageType;

public interface IMessagePasser {
	public interface IMessageListener{
		public void receiveMessage(RosieMessage message);
	}
	
	public class RosieMessage{
		public int id;
		public MessageType type;
		public String message;
		public RosieMessage(int id, MessageType type, String message){
			this.id = id;
			this.type = type;
			this.message = message;
		}
	}
	
	void addMessageListener(IMessageListener listener);
	
	void removeMessageListener(IMessageListener listener);
	
	void sendMessage(String msg, MessageType type);
}
