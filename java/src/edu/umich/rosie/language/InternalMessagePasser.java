package edu.umich.rosie.language;

import java.util.HashSet;
import edu.umich.rosie.language.LanguageConnector.MessageType;

public class InternalMessagePasser implements IMessagePasser{
	private int nextMessageId = 1;
	
	private HashSet<IMessageListener> listeners;
	
	public InternalMessagePasser(){
		listeners = new HashSet<IMessageListener>();
	}

	public void addMessageListener(IMessageListener listener){
		synchronized(listeners){
			listeners.add(listener);
		}
	}
	
	public void removeMessageListener(IMessageListener listener){
		synchronized(listeners){
			listeners.remove(listener);
		}
	}
	
	public void sendMessage(String msg, MessageType type){
		RosieMessage message = new RosieMessage(nextMessageId++, type, msg);
		notifyListeners(message);
	}
	
	private void notifyListeners(RosieMessage message){
		synchronized(listeners){
			for(IMessageListener listener : listeners){
				listener.receiveMessage(message);
			}
		}
	}
}

