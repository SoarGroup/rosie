package edu.umich.rosie.gui;

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
import edu.umich.rosie.soar.SoarClient;
import edu.umich.rosie.connectors.ActionStackConnector;

@SuppressWarnings("serial")
public class BasicRosieGUI extends JFrame
{
	private SoarClient soarClient;

	private JButton startStopButton;
	
	private LanguageConnector language;

    public BasicRosieGUI(Properties props)
    {
		super("Rosie Chat");
		
    	this.setSize(1000, 450);
    	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		soarClient.kill();
        	}
     	});
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    	soarClient = new SoarClient(props);
    	
    	InternalMessagePasser internalPasser = new InternalMessagePasser();
    	
    	language = new LanguageConnector(soarClient, props, internalPasser);
    	soarClient.addConnector(language);

		// print-action-stack = true
		// Create an ActionStackConnector and print any task events to the console
		if(props.getProperty("print-action-stack", "false").equals("true")){
			ActionStackConnector asConn = new ActionStackConnector(soarClient);
			soarClient.addConnector(asConn);
			asConn.registerForTaskEvent(new ActionStackConnector.TaskEventListener(){
				public void taskEventHandler(String taskInfo){
					System.out.println(taskInfo);
				}
			});
		}

    	ChatPanel chat = new ChatPanel(soarClient, this, internalPasser);
    	this.add(chat);
    	
    	add(new InstructorMessagePanel(chat, props));

    	setupMenu();
    	
    	soarClient.createAgent();
    	
    	this.setVisible(true);
    }

    private void setupMenu(){
    	JMenuBar menuBar = new JMenuBar();

    	startStopButton = new JButton("START");
        startStopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(soarClient.isRunning()){
					startStopButton.setText("START");
					soarClient.stop();
				} else {
					startStopButton.setText("STOP");
					soarClient.start();
				}
			}
        });
        
        
        menuBar.add(startStopButton);

    	menuBar.add(new AgentMenu(soarClient));
    	
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
