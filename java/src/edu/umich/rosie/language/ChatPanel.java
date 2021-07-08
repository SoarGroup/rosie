package edu.umich.rosie.language;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.text.*;

import edu.umich.rosie.language.LanguageConnector.MessageType;
import edu.umich.rosie.language.IMessagePasser.*;
import edu.umich.rosie.soar.SoarClient;

public class ChatPanel extends JPanel implements IMessagePasser.IMessageListener{
	// GUI COMPONENTS
	
	private StyledDocument chatDoc;
    // The document which messages are displayed to

    private JTextField chatField;
    // The text field the user can type messages to the agent in
    
    private JTextPane tPane;
    
    private JButton sendButton;
    // The button used to send a message to the soar agent
    
    
    // CHAT HISTORY

    private List<String> history = new ArrayList<String>();
    // A list of all messages typed into the chatField
    
    private int historyIndex = 0;
    // The current index into the history
    
    
    // OTHER
    private Object outputLock = new Object();
    
    private SoarClient soarClient;
    
    private IMessagePasser messageLogger;

	private BlockingQueue<RosieMessage> queuedMessages = new LinkedBlockingQueue<RosieMessage>();

    public ChatPanel(SoarClient client, JFrame parentFrame, IMessagePasser messageLogger) {
        this.soarClient = client;
        
        setupGUI(parentFrame);
		setupStyles();

		new ChatDocUpdater().execute();

		this.messageLogger = messageLogger;
        this.messageLogger.addMessageListener(this);
    }
    
    public IMessagePasser getMessageLogger(){
    	return messageLogger;
    }
    
    /**********************************************************
     * Public Interface for interacting with the chat frame
     * 
     * registerNewMessage(String message, MessageSource src)
     *   Use to add a new message to the chat text field
     * clearMessages()
     *   Remove all messages from the text field
     */
    
    @Override
    public void receiveMessage(RosieMessage message){
		try{
			queuedMessages.put(message);
		} catch (Exception e){ 
			System.err.println("Error adding message " + message.message + " to queuedMessages");
		}
    }
    
    
    public void clearMessages(){
    	chatField.setText("");
    	try {
			chatDoc.remove(0, chatDoc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	
    }

    
    /**************************************************
     * Code for setting up the Panel and its GUI elements
     */
    
    private void setupGUI(JFrame parentFrame){
        tPane = new JTextPane();
        tPane.setEditable(false);
        tPane.addKeyListener(ctrlListener);
        JScrollPane pane = new JScrollPane(tPane);
        DefaultCaret caret = (DefaultCaret)tPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		pane.setViewportView(tPane);
		chatDoc = (StyledDocument)tPane.getDocument();


		// Chat Field
        chatField = new JTextField();
        chatField.setFont(new Font("Serif",Font.PLAIN,18));
        chatField.addKeyListener(ctrlListener);
        
        // Send Button
        sendButton = new JButton("Send Message");
        sendButton.addKeyListener(ctrlListener);
        sendButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	sendButtonClicked();
            }
        });
		sendButton.setBackground(new Color(150, 255, 150));
		sendButton.setText("Send Message");

        JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chatField, sendButton);
        JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                pane2);

         pane1.setDividerLocation(350);//325
         pane2.setDividerLocation(100); //600
        
        this.setLayout(new BorderLayout());
        this.add(pane1, BorderLayout.CENTER);
        this.setSize(1200, 600);
        parentFrame.getRootPane().setDefaultButton(sendButton);
    }
    
    /*******************************
     * Setup Styles for how messages look
     */
    
    private void setupStyles() {
    	// defaultStyle - Base style used by others
    	Style defaultStyle = chatDoc.addStyle("DEFAULT", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        StyleConstants.setFontSize(defaultStyle, 24);
        StyleConstants.setFontFamily(defaultStyle, "SansSerif");
        StyleConstants.setLineSpacing(defaultStyle, 1f);

    	// agentStyle - Messages produced by the agent
    	Style agentStyle = chatDoc.addStyle(MessageType.AGENT_MESSAGE.toString(), defaultStyle);
        StyleConstants.setForeground(agentStyle, Color.BLACK);
        StyleConstants.setItalic(agentStyle, true);
        StyleConstants.setFontFamily(agentStyle, "Serif");
    	
    	// instructorStyle - Messages typed by the user
        Style instructorStyle = chatDoc.addStyle(MessageType.INSTRUCTOR_MESSAGE.toString(), defaultStyle);
        StyleConstants.setForeground(instructorStyle, Color.BLACK);
    }    

	/*********************************
	 * Setup Chat Document Updater
	 ********************************/

	private class ChatDocUpdater extends SwingWorker<Void, RosieMessage >{
		@Override
		public Void doInBackground(){
			List<RosieMessage> messages = new LinkedList<RosieMessage>();
			while(!Thread.interrupted()){
				RosieMessage nextMessage = queuedMessages.poll();
				if(nextMessage == null){
					// Got all messages
					publish(messages.toArray(new RosieMessage[messages.size()]));
					messages.clear();
				} else {
					messages.add(nextMessage);
				}
			}
			publish(messages.toArray(new RosieMessage[messages.size()]));
			return null;
		}

		@Override
		protected void process(List<RosieMessage> messages){
			for(RosieMessage message : messages){
				Style msgStyle = chatDoc.getStyle(message.type.toString());
				DateFormat dateFormat = new SimpleDateFormat("mm:ss:SSS");
				//int dc = soarClient.getAgent().GetDecisionCycleCounter();
				Date d = new Date();
				
				//String fullMessage = dc + " " + dateFormat.format(d) + " ";
				//String fullMessage = dateFormat.format(d) + " ";
				String fullMessage = "";
				switch(message.type){
				case INSTRUCTOR_MESSAGE:
					fullMessage += "Mentor: ";
					break;
				case AGENT_MESSAGE:
					fullMessage += "Agent: " ;
					break;
				default:
					// Don't handle other types
					return;
				}
				
				fullMessage += message.message + "\n";

				try{
					int origLength = chatDoc.getLength();
					chatDoc.insertString(origLength, fullMessage, msgStyle);
				} catch (BadLocationException e){
					// Should never encounter this
					System.err.println("Failed to add message to chat window");
				}
			}

			// AM: Will make it auto scroll to bottom
			int end = chatDoc.getLength();
			tPane.select(end, end);
		}
	}
    
    
    /*******************************************************
     * Handling Keyboard Events
     *   Up/Down - go up/down in history to repeat sentence
     */
    
    private KeyAdapter ctrlListener = new KeyAdapter(){
		public void keyPressed(KeyEvent arg0) {
			if(arg0.getKeyCode() == KeyEvent.VK_UP) {
				upPressed();
			} else if(arg0.getKeyCode() == KeyEvent.VK_DOWN){
				downPressed();
			} else if(arg0.getKeyCode() == KeyEvent.VK_CONTROL && soarClient != null){
				LanguageConnector lang = soarClient.getConnector(LanguageConnector.class);
				//lang.getSTT().startListening();
			}
		}
		public void keyReleased(KeyEvent arg0) {
			if(arg0.getKeyCode() == KeyEvent.VK_CONTROL && soarClient != null){
				LanguageConnector lang = soarClient.getConnector(LanguageConnector.class);
				//lang.getSTT().stopListening();
			}
			if(arg0.getKeyCode() == KeyEvent.VK_F1 && soarClient != null){
				if(soarClient.isRunning()){
					soarClient.stop();
				} else {
					soarClient.start();
				}
			}
		}
    }; 

    public void upPressed(){
		if(historyIndex > 0){
			historyIndex--;
		}
		if(history.size() > 0){
			chatField.setText(history.get(historyIndex));
		}
    }
    
    public void downPressed(){
		historyIndex++;
		if(historyIndex >= history.size()){
			historyIndex = history.size();
			chatField.setText("");
		} else {
			chatField.setText(history.get(historyIndex));
		}
    }
    public void updateChatHistory(String msg)
    {
    	history.add(msg);
    	historyIndex = history.size();
    	//messageLogger.sendMessage(msg, MessageType.INSTRUCTOR_MESSAGE);
    }
    /*****************************************
     * Event Handling for Send Button
     */

    private void sendButtonClicked(){
    	String msg = chatField.getText().trim();
    	if(msg.length() == 0){
    		return;
    	}
    	history.add(msg);
    	historyIndex = history.size();
    	messageLogger.sendMessage(msg, MessageType.INSTRUCTOR_MESSAGE);
		chatField.setText("");
		chatField.requestFocus();
    }
}
