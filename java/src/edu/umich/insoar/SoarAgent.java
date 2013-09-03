package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import april.config.Config;
import april.config.ConfigFile;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Kernel;
import sml.smlRunEventId;
import edu.umich.insoar.world.WorldModel;

public class SoarAgent implements RunEventInterface{

    private Agent agent;
    
    private String agentSource = null;
    
    private String lgSoarSource = null;
    
    private String smemSource = null;
    
    private boolean isRunning = false;
    
    private boolean queueStop = false;
    
    private Kernel kernel;
    
    private WorldModel world = null;
   
    public String armConfig = null;
    
    public SoarAgent(String agentName, String agentSource, boolean headless){
    	kernel = Kernel.CreateKernelInNewThread();
        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);
        
		// Initialize Soar Agent
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
           throw new IllegalStateException("Kernel created null agent");
        }
        
    	System.out.println("Spawn Debugger: " + agent.SpawnDebugger(kernel.GetListenerPort()));
    	this.agentSource = agentSource;
    	this.smemSource = null;
    	this.lgSoarSource = null;
    	
        // Source the agent
        sourceAgent(true);

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
    }
	
	public SoarAgent(String agentName, Properties props, boolean useLG, boolean headless){
		kernel = Kernel.CreateKernelInNewThread();
        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);
        
		// Initialize Soar Agent
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
           throw new IllegalStateException("Kernel created null agent");
        }
        
        // Start Debugger (if desired)
        if (!headless) {
        	System.out.println("Spawn Debugger: " + agent.SpawnDebugger(kernel.GetListenerPort()));
        	// Requires the SOAR_HOME environment variable
        }
        
        // Get the various sources
        agentSource = props.getProperty("agent");
        smemSource = props.getProperty("smem-source");
        if(useLG){
        	lgSoarSource = props.getProperty("language-productions");
        } else {
        	lgSoarSource = null;
        }
        
        armConfig = props.getProperty("arm-config");
        System.out.println("Getting Arm Config: " + armConfig);
    	
        // Source the agent
        sourceAgent(true);

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);

	}
	
	public Agent getAgent(){
		return agent;
	}
	
	public boolean isRunning(){
		return isRunning;
	}
	
	public void setWorldModel(WorldModel worldModel){
		this.world = worldModel;
	}
	
	public String getArmConfig(){
		return armConfig;
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
    			sendCommand("run");
    			isRunning = false;
    		}
    	}
    	Thread agentThread = new Thread(new AgentThread());
    	agentThread.start();
	}
	
	public void stop(){
		queueStop = true;
	}
	
	public void kill(){
		agent.KillDebugger();
		//kernel.DestroyAgent(agent);
    	// SBW removed DestroyAgent call, it hangs in headless mode for some reason
    	// (even when the KillDebugger isn't there)
    	// I don't think there's any consequence to simply exiting instead.
	}


	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		if(queueStop){
			sendCommand("stop");
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
    	System.out.println("Re-initializing the agent");
    	System.out.println("  smem --init:  " + agent.ExecuteCommandLine("smem --init"));
    	System.out.println("  epmem --init: " + agent.ExecuteCommandLine("epmem --init"));
    	if(includeSmem && smemSource != null){
        	agent.ExecuteCommandLine("smem --set database memory");
        	agent.ExecuteCommandLine("epmem --set database memory");
    		String ret = agent.ExecuteCommandLine("source " + smemSource);
    		System.out.println(ret);
    	}
    	if(agentSource != null){
    		String ret = agent.ExecuteCommandLine("source " + agentSource);
    		System.out.println(ret);
    	}
    	if(lgSoarSource != null){
    		String ret = agent.ExecuteCommandLine("source " + lgSoarSource);
    		System.out.println(ret);
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
    	System.out.println("Completed Restore");
	}
	
	/*** 
     * Creates the Agent menu in the ChatFrame, which consists of 
     * functions to backup/restore/reset the agent
     */
	public JMenu createMenu(){
    	JMenu agentMenu = new JMenu("Agent");
        
    	// Full Reset: Completely clears all memories and re-sources the agent
        JMenuItem resetButton = new JMenuItem("Full Reset");
        resetButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			if(world != null){
        				world.reset();
        			}
        			if(ChatFrame.Singleton() != null){
            			ChatFrame.Singleton().clear();
        			}
        			sourceAgent(true);
        		}
        	}
        });
        agentMenu.add(resetButton);        
        
        agentMenu.addSeparator();

        // Backup: Creates a backup of all memories and chunks to the default location
        JMenuItem backupButton = new JMenuItem("Backup");
        backupButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			backup("default");
        		}
        	}
        });
        agentMenu.add(backupButton);  

        // Restore: Restores all memories and chunks from the default location (last time Backup was pressed)
        JMenuItem restoreButton = new JMenuItem("Restore");
        restoreButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
        			restore("default");
        		}
        	}
        });
        agentMenu.add(restoreButton);  
        
        // Backup To File: Allows the user to specify the name to use when backing up
        JMenuItem backupToFileButton = new JMenuItem("Backup To File");
        backupToFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
            		String sessionName = JOptionPane.showInputDialog(null, 
              			  "Enter the session name to backup",
              			  "Backup To File",
              			  JOptionPane.QUESTION_MESSAGE);
            		backup(sessionName);
        		}
        	}
        });
        agentMenu.add(backupToFileButton);  
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem restoreFromFileButton = new JMenuItem("Restore From File");
        restoreFromFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		} else {
            		String sessionName = JOptionPane.showInputDialog(null, 
              			  "Enter the session name to backup",
              			  "Backup To File",
              			  JOptionPane.QUESTION_MESSAGE);
            		restore(sessionName);
        		}
        	}
        });
        agentMenu.add(restoreFromFileButton);  
        
        agentMenu.addSeparator();
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem commandButton = new JMenuItem("Enter SML Command");
        commandButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		String command = JOptionPane.showInputDialog(null, 
            			  "Enter the command",
            			  "SML Command",
            			  JOptionPane.QUESTION_MESSAGE);
      			System.out.println(sendCommand(command));
        	}
        });
        agentMenu.add(commandButton);  
        
        return agentMenu;
    }
}
