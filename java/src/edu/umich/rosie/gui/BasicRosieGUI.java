package edu.umich.rosie.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import edu.umich.rosie.AgentMenu;
import edu.umich.rosie.language.ChatPanel;
import edu.umich.rosie.language.InstructorMessagePanel;
import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soarobjects.Time;

public class BasicRosieGUI extends JFrame
{
	private SoarAgent soarAgent;

	private JButton startStopButton;
	
	
	
	private LanguageConnector language;

    public BasicRosieGUI(Properties props)
    {
		super("Rosie Chat");
		
    	this.setSize(1000, 450);
    	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		soarAgent.kill();
        	}
     	});
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    	soarAgent = new SoarAgent(props);
    	
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	language = new LanguageConnector(soarAgent, props, internalPasser);
    	soarAgent.setLanguageConnector(language);

    	ChatPanel chat = new ChatPanel(soarAgent, this, internalPasser);
    	this.add(chat);
    	
    	add(new InstructorMessagePanel(chat, props));

    	setupMenu();
    	
    	soarAgent.createAgent();
    	
    	this.setVisible(true);
    	soarAgent.start();
    }

    private void setupMenu(){
    	JMenuBar menuBar = new JMenuBar();

    	startStopButton = new JButton("START");
        startStopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(soarAgent.isRunning()){
					startStopButton.setText("START");
					soarAgent.stop();
				} else {
					startStopButton.setText("STOP");
					soarAgent.start();
				}
			}
        });
        
        
        menuBar.add(startStopButton);

    	menuBar.add(new AgentMenu(soarAgent));
    	
    	language.createMenu(menuBar);

    	this.setJMenuBar(menuBar);
    }


    public static void main(String[] args) {
    	if(args.length == 0){
    		System.err.println("BasicRosieGUI: Need path to config file as argument");
    		System.exit(1);
    	}

        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(args[0]));
		} catch (IOException e) {
			System.out.println("File not found: " + args[0]);
			e.printStackTrace();
			return;
		}
        
        new BasicRosieGUI(props);
    }
}
