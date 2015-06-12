package edu.umich.rosie.language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;

import april.util.TimeUtil;

import edu.umich.rosie.language.LanguageConnector.MessageType;
import edu.umich.rosie.lcmtypes.interaction_message_t;
import edu.umich.rosie.lcmtypes.interaction_messages_t;

public class MessageLogger implements LCMSubscriber{
	public interface IMessageListener{
		public void receiveMessage(interaction_message_t message);
	}
	
	public int SEND_MESSAGE_FPS = 20;
	public int TTL = 5;
	
	private String loggerName;
	
	private int nextMessageId = 1;
	
	private HashMap<String, Integer> processedIds;
	
	private HashSet<IMessageListener> listeners;
	
	private Queue<interaction_message_t> messages;
	
	private SendMessagesThread sendThread;
	
	public MessageLogger(String loggerName){
		this.loggerName = loggerName;
		
		processedIds = new HashMap<String, Integer>();
		listeners = new HashSet<IMessageListener>();
		messages = new LinkedList<interaction_message_t>();
		
		sendThread = new SendMessagesThread();
		sendThread.start();

		LCM.getSingleton().subscribe("INTERACTION_MESSAGE.*", this);
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
		interaction_message_t message = new interaction_message_t();
		message.utime = TimeUtil.utime();
		message.message_type = type.toString();
		message.message = msg;
		synchronized(messages){
			message.message_id = nextMessageId++;
			messages.add(message);
		}
	}
	
	private void notifyListeners(interaction_message_t message){
		synchronized(listeners){
			for(IMessageListener listener : listeners){
				listener.receiveMessage(message);
			}
		}
	}
	
	class SendMessagesThread extends Thread{
		private boolean killThread = false;
		
		public SendMessagesThread(){}

		public void run(){
			while(!killThread) {
				synchronized(messages){
					// Keep removing messages until you reach one that hasn't expired
					while(!messages.isEmpty()){
						long addedTime = messages.peek().utime;
						if((TimeUtil.utime() - addedTime) < TTL * 1000000){
							break;
						}
						messages.poll();
					}

					interaction_messages_t messageList = new interaction_messages_t();
					messageList.utime = TimeUtil.utime();
					messageList.source = loggerName;
					messageList.num_messages = messages.size();
					messageList.messages = new interaction_message_t[messageList.num_messages];
					int i = 0;
					for(interaction_message_t message : messages){
						messageList.messages[i++] = message;
					}
					LCM.getSingleton().publish("INTERACTION_MESSAGE_" + loggerName.toUpperCase() + "_TX", messageList);
				}

				TimeUtil.sleep(1000/SEND_MESSAGE_FPS);
			}
		}
		
		public void stopThread(){
			killThread = true;
		}
	}


	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
		if(channel.startsWith("INTERACTION_MESSAGE")){
			try{
				interaction_messages_t newMessages = new interaction_messages_t(ins);
				handleNewMessages(newMessages);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private void handleNewMessages(interaction_messages_t newMessages){
		synchronized(processedIds){
			if(!processedIds.containsKey(newMessages.source)){
				processedIds.put(newMessages.source, -1);
			}
			int processedId = processedIds.get(newMessages.source);
			for(interaction_message_t message : newMessages.messages){
				if(message.message_id > processedId){
					notifyListeners(message);
					processedId = message.message_id;
				}
			}
			processedIds.put(newMessages.source, processedId);
		}
	}
}
