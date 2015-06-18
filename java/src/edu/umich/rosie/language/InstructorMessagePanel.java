package edu.umich.rosie.language;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import edu.umich.rosie.language.LanguageConnector.MessageType;

public class InstructorMessagePanel extends JPanel{
	
	private ChatPanel chat;
	private HashMap<String, JButton> messages;
	
	public InstructorMessagePanel(ChatPanel chat){
		this.chat = chat;
		this.messages = new HashMap<String, JButton>();

		initPanel();
	}
	
	public InstructorMessagePanel(ChatPanel chat, String[] messages){
		this.chat = chat;
		this.messages = new HashMap<String, JButton>();
		
		initPanel();
		
		for(String message : messages){
			addMessage(message);
		}
	}
	
	private void initPanel(){
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	public void addMessage(String message){
		if(!messages.containsKey(message)){
			JButton button = new JButton(message);
			button.setMinimumSize(new Dimension(600, 50));
			button.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					sendMessage(((JButton)arg0.getSource()).getText());
				}
			});
			this.add(button);
		}
	}
	
	public void removeMessage(String message){
		if(messages.containsKey(message)){
			this.remove(messages.get(message));
			messages.remove(message);
		}
	}
	
	private void sendMessage(String message){
		chat.getMessageLogger().sendMessage(message, MessageType.INSTRUCTOR_MESSAGE);
	}
}
