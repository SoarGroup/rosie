package edu.umich.rosie.language;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import edu.umich.rosie.language.LanguageConnector.MessageType;

@SuppressWarnings("serial")
public class InstructorMessagePanel extends JPanel{
	
	private ChatPanel chat;
	private HashMap<String, JButton> messages;
	private String messageFile;
	public InstructorMessagePanel(ChatPanel chat, Properties props){
		this.chat = chat;
		this.messages = new HashMap<String, JButton>();
		
		messageFile = props.getProperty("messages-file", null);
		if(messageFile != null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(messageFile));
				try {
					String line = br.readLine();
					while(line != null){
						addMessage(line);
						line = br.readLine();
					}
				} finally {
				    br.close();
				}
			} catch(IOException e){
				System.err.println("InstructorMessagePanel: Failed to read file " + messageFile);
			}
		}

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
	public void setNewMessageFile(String file){
		messageFile = file;
		this.removeAll();
		if(messageFile != null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(messageFile));
				try {
					String line = br.readLine();
					while(line != null){
						addMessage(line);
						line = br.readLine();
					}
				} finally {
				    br.close();
				}
			} catch(IOException e){
				System.err.println("InstructorMessagePanel: Failed to read file " + messageFile);
			}
		}

		initPanel();
	}
	
	private void initPanel(){
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	public void addMessage(String message){
		if(!messages.containsKey(message)){
			JButton button = new JButton(message);
			//button.setMinimumSize(new Dimension(600, 50));
			//button.setMaximumSize(new Dimension(400, 30));
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
