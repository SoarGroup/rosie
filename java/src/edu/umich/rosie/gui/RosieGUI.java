package edu.umich.rosie.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import edu.umich.rosie.AgentMenu;
import edu.umich.rosie.SoarAgent;
import edu.umich.rosie.actuation.ArmActuationConnector;
import edu.umich.rosie.actuation.EnvironmentMenu;
import edu.umich.rosie.language.ChatPanel;
import edu.umich.rosie.language.LanguageConnector;
import edu.umich.rosie.perception.ArmPerceptionConnector;
import april.util.GetOpt;
import april.util.StringUtil;

public class RosieGUI extends JFrame
{
	private SoarAgent soarAgent;

	private JButton startStopButton;
	
	private ArmPerceptionConnector perception;
	private ArmActuationConnector actuation;
	private LanguageConnector language;

    public RosieGUI(Properties props)
    {
		super("Rosie Chat");
		
    	this.setSize(800, 450);
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		soarAgent.kill();
        	}
     	});
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    	soarAgent = new SoarAgent(props);
    	
    	actuation = new ArmActuationConnector(soarAgent, props);
    	soarAgent.setActuationConnector(actuation);
    	
    	perception = new ArmPerceptionConnector(soarAgent, props);
    	soarAgent.setPerceptionConnector(perception);
    	
    	language = new LanguageConnector(soarAgent, props);
    	soarAgent.setLanguageConnector(language);

    	ChatPanel chat = new ChatPanel(soarAgent, this);
    	this.add(chat);

    	setupMenu();

    	((LanguageConnector)soarAgent.getLanguageConnector()).setChat(chat);
//    	this.add(new CommandPanel(soarAgent));
    	
    	soarAgent.createAgent();
    	
    	this.setVisible(true);
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
    	menuBar.add(EnvironmentMenu.createMenu());
    	
    	language.createMenu(menuBar);
    	perception.createMenu(menuBar);
    	actuation.createMenu(menuBar);

    	this.setJMenuBar(menuBar);
    }


    public static void main(String[] args) {

        GetOpt opts = new GetOpt();

        opts.addBoolean('h', "help", false, "Show this help screen");
        opts.addBoolean('d', "debug", true, "Show the soar debugger");

        if (!opts.parse(args)) {
            System.err.println("ERR: Error parsing args - "+opts.getReason());
            System.exit(1);
        }
        if (opts.getBoolean("help")) {
            opts.doHelp();
            System.exit(0);
        }

        String configFile = StringUtil.replaceEnvironmentVariables("$ROSIE_CONFIG");
        if(configFile.equals("")){
          System.err.println("ERR: No $ROSIE_CONFIG environment variable set");
          System.exit(1);
        }
        
        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(configFile));
		} catch (IOException e) {
			System.out.println("File not found: " + configFile);
			e.printStackTrace();
			return;
		}

        new RosieGUI(props);
    }
}
