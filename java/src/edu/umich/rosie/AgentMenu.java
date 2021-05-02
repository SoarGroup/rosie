package edu.umich.rosie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.umich.rosie.soar.SoarClient;

public class AgentMenu extends JMenu{
	private SoarClient client;
	public AgentMenu(SoarClient soarClient){
		super("Agent");
		
		this.client = soarClient;
    
		// Full Reset: Completely clears all memories and re-sources the agent
	    JMenuItem resetButton = new JMenuItem("Full Reset");
	    resetButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(client.isRunning()){
	    			JOptionPane.showMessageDialog(null, "The client must be stopped first");
	    		} else {
	    			client.resetAgent();
	    		}
	    	}
	    });
	    this.add(resetButton);        
	    
	    this.addSeparator();
	
	    // Backup: Creates a backup of all memories and chunks to the default location
	    JMenuItem backupButton = new JMenuItem("Backup");
	    backupButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(client.isRunning()){
	    			JOptionPane.showMessageDialog(null, "The client must be stopped first");
	    		} else {
	    			client.backup("default");
	    		}
	    	}
	    });
	    this.add(backupButton);  
	
	    // Restore: Restores all memories and chunks from the default location (last time Backup was pressed)
	    JMenuItem restoreButton = new JMenuItem("Restore");
	    restoreButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(client.isRunning()){
	    			JOptionPane.showMessageDialog(null, "The client must be stopped first");
	    		} else {
	    			client.restore("default");
	    		}
	    	}
	    });
	    this.add(restoreButton);  
	    
	    // Backup To File: Allows the user to specify the name to use when backing up
	    JMenuItem backupToFileButton = new JMenuItem("Backup To File");
	    backupToFileButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(client.isRunning()){
	    			JOptionPane.showMessageDialog(null, "The client must be stopped first");
	    		} else {
	        		String sessionName = JOptionPane.showInputDialog(null, 
	          			  "Enter the session name to backup",
	          			  "Backup To File",
	          			  JOptionPane.QUESTION_MESSAGE);
	        		if (sessionName != null && !(sessionName.equals(""))){
				    client.backup(sessionName);
				  }
				}
	    	}
	    });
	    this.add(backupToFileButton);  
	    
	    // Restore From File: Allows the user to specify the name to use when restoring
	    JMenuItem restoreFromFileButton = new JMenuItem("Restore From File");
	    restoreFromFileButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(client.isRunning()){
	    			JOptionPane.showMessageDialog(null, "The client must be stopped first");
	    		} else {
	        		String sessionName = JOptionPane.showInputDialog(null, 
	          			  "Enter the session name to restore",
	          			  "Restore from File",
	          			  JOptionPane.QUESTION_MESSAGE);
                                if (sessionName != null && !(sessionName.equals(""))){
				    client.restore(sessionName);
				 }
                               }
	    	}
	    });
	    this.add(restoreFromFileButton);  
	    
	    this.addSeparator();
	    
	    // Restore From File: Allows the user to specify the name to use when restoring
	    JMenuItem commandButton = new JMenuItem("Enter SML Command");
	    commandButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		String command = JOptionPane.showInputDialog(null, 
	        			  "Enter the command",
	        			  "SML Command",
	        			  JOptionPane.QUESTION_MESSAGE);
	  			System.out.println(client.sendCommand(command));
	    	}
	    });
	    this.add(commandButton);  
	}
}
