package edu.umich.insoar;

import sml.Agent;
import sml.Kernel;
import sml.smlRunEventId;
import sml.Agent.RunEventInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;

import edu.umich.insoar.world.SVSConnector;

public class SoarAgent implements RunEventInterface{

    private Agent agent;
    
    private InteractionStack stack;
    // Used to display a visual representation of the current state of the interaction stack
    
    private String agentSource = null;
    
    private String lgSoarSource = null;
    
    private String smemSource = null;
    
    private boolean isRunning = false;
    
    private boolean queueStop = false;
	
	public SoarAgent(Kernel kernel, String agentName, Properties props, boolean useLG){
        stack = new InteractionStack();
        
        // Get the various sources
        agentSource = props.getProperty("agent");
        smemSource = props.getProperty("smem-source");
        if(useLG){
        	lgSoarSource = props.getProperty("language-productions");
        } else {
        	lgSoarSource = null;
        }
    	
		// Initialize Soar Agent
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
           throw new IllegalStateException("Kernel created null agent");
        }
        sourceAgent(true);

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);

	}
	
	public Agent getAgent(){
		return agent;
	}
    
    public InteractionStack getStack(){
    	return stack;
    }
	
	public boolean isRunning(){
		return isRunning;
	}

	/**
     * Spawns a new thread that invokes a run command on the agent
     */
	public void start(){
		if(isRunning){
			return;
		}
    	class AgentThread implements Runnable{
    		public void run(){
    	    	isRunning = true;
    			InSoar.Singleton().getSoarAgent().sendCommand("run");
    			isRunning = false;
    		}
    	}
    	Thread agentThread = new Thread(new AgentThread());
    	agentThread.start();
	}
	
	public void stop(){
		queueStop = true;
	}


	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		if(queueStop){
			InSoar.Singleton().getSoarAgent().sendCommand("stop");
			queueStop = false;
		}
	}
	
	public void commitChanges(){
		if(agent.IsCommitRequired()){
			agent.Commit();
		}
	}
	
	public String sendCommand(String command){
		return agent.ExecuteCommandLine(command);
	}
	
	/* IMPORTANT: Do not call if the agent is running */
	public void sourceAgent(boolean includeSmem){
		stack.clear();
    	System.out.println("Re-initializing the agent");
    	System.out.println("  smem --init:  " + agent.ExecuteCommandLine("smem --init"));
    	System.out.println("  epmem --init: " + agent.ExecuteCommandLine("epmem --init"));
    	if(includeSmem && smemSource != null){
        	agent.ExecuteCommandLine("smem --set database memory");
        	agent.ExecuteCommandLine("epmem --set database memory");
    		agent.LoadProductions(smemSource);
    		System.out.println("  source " + smemSource);
    	}
    	if(agentSource != null){
    		agent.LoadProductions(agentSource);
    		System.out.println("  source " + agentSource);
    	}
    	if(lgSoarSource != null){
    		agent.LoadProductions(lgSoarSource);
    		System.out.println("  source " + lgSoarSource);
    	}
    	System.out.println("Agent re-initialized");
	}

	/* IMPORTANT: Do not call if the agent is running */
	public void backup(String sessionName){
    	System.out.println("Performing backup: " + sessionName);
    	System.out.println("  epmem: " + agent.ExecuteCommandLine(String.format("epmem --backup saved-agents/%s_epmem.db", sessionName)));
    	System.out.println("  smem: " + agent.ExecuteCommandLine(String.format("smem --backup saved-agents/%s_smem.db", sessionName)));
    	System.out.println("  chunks: " + agent.ExecuteCommandLine(String.format("command-to-file saved-agents/%s_chunks.soar pc -f", sessionName)));
    	System.out.println("Completed Backup");
	}
	
	/* IMPORTANT: Do not call if the agent is running */
	public void restore(String sessionName){
    	System.out.println("Restoring agent: " + sessionName);
    	ChatFrame.Singleton().clear();
    	sourceAgent(false);
    	agent.ExecuteCommandLine("smem --set database file");
    	agent.ExecuteCommandLine("epmem --set database file");
    	System.out.println("  epmem:" + agent.ExecuteCommandLine(String.format("epmem --set path saved-agents/%s_epmem.db", sessionName)));
    	System.out.println("  smem:" + agent.ExecuteCommandLine(String.format("smem --set path saved-agents/%s_smem.db", sessionName)));
    	agent.LoadProductions(String.format("saved-agents/%s_chunks.soar", sessionName));
    	System.out.println("  source: " + String.format("saved-agents/%s_chunks.soar", sessionName));
    	SVSConnector.Singleton().reset();
    	System.out.println("Completed Restore");
	}
	
	/*** 
     * Creates the Agent menu in the ChatFrame, which consists of 
     * functions to backup/restore/reset the agent
     */
	public static void createAgentMenu(JMenuBar menuBar){
    	JMenu agentMenu = new JMenu("Agent");
        
    	// Full Reset: Completely clears all memories and re-sources the agent
        JMenuItem resetButton = new JMenuItem("Full Reset");
        resetButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		if(agent.isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			ChatFrame.Singleton().clear();
        			agent.sourceAgent(true);
        	    	SVSConnector.Singleton().reset();
        		}
        	}
        });
        agentMenu.add(resetButton);        
        
        agentMenu.addSeparator();

        // Backup: Creates a backup of all memories and chunks to the default location
        JMenuItem backupButton = new JMenuItem("Backup");
        backupButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		if(agent.isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			agent.backup("default");
        		}
        	}
        });
        agentMenu.add(backupButton);  

        // Restore: Restores all memories and chunks from the default location (last time Backup was pressed)
        JMenuItem restoreButton = new JMenuItem("Restore");
        restoreButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		if(agent.isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			agent.restore("default");
        		}
        	}
        });
        agentMenu.add(restoreButton);  
        
        // Backup To File: Allows the user to specify the name to use when backing up
        JMenuItem backupToFileButton = new JMenuItem("Backup To File");
        backupToFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		if(agent.isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
            		String sessionName = JOptionPane.showInputDialog(null, 
              			  "Enter the session name to backup",
              			  "Backup To File",
              			  JOptionPane.QUESTION_MESSAGE);
            		agent.backup(sessionName);
        		}
        	}
        });
        agentMenu.add(backupToFileButton);  
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem restoreFromFileButton = new JMenuItem("Restore From File");
        restoreFromFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		if(agent.isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
            		String sessionName = JOptionPane.showInputDialog(null, 
              			  "Enter the session name to backup",
              			  "Backup To File",
              			  JOptionPane.QUESTION_MESSAGE);
            		agent.restore(sessionName);
        		}
        	}
        });
        agentMenu.add(restoreFromFileButton);  
        
        agentMenu.addSeparator();

        // Interaction Stack: Shows a representation of the current interaction stack
        JMenuItem stackButton = new JMenuItem("Interaction Stack");
        stackButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
				agent.getStack().showFrame();
			}
        });
        agentMenu.add(stackButton);
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem commandButton = new JMenuItem("Enter SML Command");
        commandButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		SoarAgent agent = InSoar.Singleton().getSoarAgent();
        		String command = JOptionPane.showInputDialog(null, 
            			  "Enter the command",
            			  "SML Command",
            			  JOptionPane.QUESTION_MESSAGE);
      			System.out.println(agent.sendCommand(command));
        	}
        });
        agentMenu.add(commandButton);  
        
        JMenuItem clearDataButton = new JMenuItem("Clear Classifier Data");
        clearDataButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		robot_command_t cmd = new robot_command_t();
        		cmd.utime = TimeUtil.utime();
        		cmd.dest = new double[6];
        		cmd.updateDest = false;
        		cmd.action = "CLEAR";
        		InSoar.broadcastRobotCommand(cmd);
        	}
        });
        agentMenu.add(clearDataButton);  

        menuBar.add(agentMenu);
    }
}
