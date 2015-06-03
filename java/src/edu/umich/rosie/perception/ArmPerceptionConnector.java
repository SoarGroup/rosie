package edu.umich.rosie.perception;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import probcog.lcmtypes.*;
import april.util.PeriodicTasks;
import april.util.TimeUtil;
import edu.umich.insoar.world.PerceptualProperty;
import edu.umich.insoar.world.WorldModel;
import edu.umich.rosie.AgentConnector;
import edu.umich.rosie.SoarAgent;
import edu.umich.rosie.SoarUtil;
import edu.umich.rosie.gui.InSoar;

public class ArmPerceptionConnector extends AgentConnector implements LCMSubscriber {
	private static int SEND_TRAINING_FPS = 10;
	Timer sendTrainingTimer;
	
	// Time Information
	private Identifier timeId = null;
	private int stepNumber = 0;
	
	// Object being pointed to
	private int pointedId = -1;
	private int currentTimer = -10;
	private long prevTime = 0;
	private long totalTime = 0;

	
    private HashMap<training_label_t, Identifier> outstandingTraining;
    
    private LCM lcm;
    
    protected WorldModel world;
    private String classifiersFile;
    private String armStatus = "wait";
    
    public ArmPerceptionConnector(SoarAgent soarAgent, String classifiersFile)
    {
    	super(soarAgent);

    	this.classifiersFile = classifiersFile;
    	
    	outstandingTraining = new HashMap<training_label_t, Identifier>();
        
        world = new WorldModel(soarAgent);
        lcm = LCM.getSingleton();
        
        sendTrainingTimer = new Timer();
    }
    
    @Override
    public void connect(){
    	super.connect();

        sendTrainingTimer.schedule(new TimerTask(){
        	public void run(){
        		sendTrainingLabels();
        	}
        }, 1000, 1000/SEND_TRAINING_FPS);
        
        lcm.subscribe("OBSERVATIONS", this);
        lcm.subscribe("ROBOT_ACTION", this);
    }
    
    @Override
    public void disconnect(){
    	super.disconnect();
    	
    	sendTrainingTimer.cancel();
    	
        lcm.unsubscribe("OBSERVATIONS", this);
        lcm.unsubscribe("ROBOT_ACTION", this);
    }
    
    /*************************************************
     * handling training labels
     *************************************************/
    
    private void queueTrainingLabel(Integer objId, Integer cat, String label, Identifier id){
    	training_label_t newLabel = new training_label_t();
    	newLabel.utime = InSoar.GetSoarTime();
    	newLabel.id = objId;
    	newLabel.cat = new category_t();
    	newLabel.cat.cat = cat;
    	newLabel.label = label;
    	synchronized(outstandingTraining){
    		outstandingTraining.put(newLabel, id);
    	}
    }
    
    private void sendTrainingLabels(){
    	synchronized(outstandingTraining){
    		if(outstandingTraining.size() == 0){
    			return;
    		}
    		training_data_t data = new training_data_t();
    		data.utime = InSoar.GetSoarTime();
    		data.num_labels = outstandingTraining.size();
    		data.labels = new training_label_t[data.num_labels];
    		int i = 0;
    		for(training_label_t label : outstandingTraining.keySet()){
    			data.labels[i++] = label;
    		}
    		lcm.publish("TRAINING_DATA", data);
    	}
    }
    
    private void receiveAckTime(long time){
    	synchronized(outstandingTraining){
	    	Set<training_label_t> finishedLabels = new HashSet<training_label_t>();
	    	for(Map.Entry<training_label_t, Identifier> e : outstandingTraining.entrySet()){
	    		if(e.getKey().utime <= time){
	    			finishedLabels.add(e.getKey());
	    			e.getValue().CreateStringWME("status", "complete");
	    		}
	    	}
	    	for(training_label_t label : finishedLabels){
	    		outstandingTraining.remove(label);
	    	}
    	}
    }
    
    /*************************************************
     * runEventHandler
     * Runs every input phase to update the input link
     * with perceptual information (time, pointed obj) as well as
     * Send training labels to perception
     *************************************************/
    long time = 0;

	@Override
    public void onInputPhase(Identifier inputLink){
    	time = TimeUtil.utime();
    	
    	if(timeId == null){
            timeId = inputLink.CreateIdWME("time");
        }
        
        stepNumber++;
        
        SoarUtil.updateIntWME(timeId, "steps", stepNumber);
        SoarUtil.updateIntWME(timeId, "seconds", InSoar.GetSoarTime() / 1000000);
        
        // Update pointed object
        SoarUtil.updateIntWME(inputLink, "pointed-object", pointedId);
    	
    	if(currentTimer <= -10){
    		SoarUtil.updateStringWME(timeId, "timer-status", "waiting");
    	} else if(--currentTimer <= 0){
    		SoarUtil.updateStringWME(timeId, "timer-status", "expired");
    	} else {
    		SoarUtil.updateStringWME(timeId, "timer-status", "ticking");
    	}
    	
    	if(InSoar.DEBUG_TRACE){
    		System.out.println(String.format("%-20s : %d", "PERCEPTION CONNECTOR", (TimeUtil.utime() - time)/1000));
    	}
    }

    /*************************************************
     * outputEventHandlers
     *************************************************/

	@Override
	protected void onOutputEvent(String attName, Identifier id) {
		if(attName.equals("send-training-label")){
			processSendTrainingLabelCommand(id);
		} else if(attName.equals("modify-scene")){
			processModifySceneCommand(id);
		}
	}

    private void processSendTrainingLabelCommand(Identifier id){
    	Integer objId = Integer.parseInt(SoarUtil.getValueOfAttribute(id, "id", 
    			"Error (send-training-label): No ^id attribute"));
    	String label = SoarUtil.getValueOfAttribute(id, "label", 
    			"Error (send-training-label): No ^label attribute");
    	String propName = SoarUtil.getValueOfAttribute(id, "property-name", 
    			"Error (send-training-label): No ^property-name attribute");
    	Integer catNum = PerceptualProperty.getPropertyID(propName);
    	if(catNum == null){
    		id.CreateStringWME("status", "error");
    		System.err.println("ArmPerceptionConnector::processSendTrainingLabelCommand - bad category");
    		return;
    	}
    	
    	queueTrainingLabel(objId, catNum, label, id);
    }
    
    
    private void processModifySceneCommand(Identifier rootId){
    	String type = SoarUtil.getValueOfAttribute(rootId, "type", "Error: No ^type attribute");
    	if(type.equals("link")){
    		Set<String> sourceIds = SoarUtil.getAllValuesOfAttribute(rootId, "source-id");
    		if(sourceIds.size() == 0){
    			rootId.CreateStringWME("status", "error");
    			System.err.println("Error (link): No ^source-id attribute");
    		}
    		String destId = SoarUtil.getValueOfAttribute(rootId, "dest-id", "Error (link): No ^dest-id attribute");
    		if(destId.contains("bel-")){
    			destId = destId.substring(4);
    		}
    		world.linkObjects(sourceIds, destId);
    	} else {
    		rootId.CreateStringWME("status", "error");
    		System.err.println("ArmPerceptionConnector::processModifySceneCommand - bad type");
    		return;
    	}
    	rootId.CreateStringWME("status", "complete");
    }
    
    /*****************************************************
     * messageReceived
     * Gets a new observations_t message from LCM and sends it to the world
     ****************************************************/
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
    	if(channel.equals("OBSERVATIONS")){
            observations_t obs = null;
            try {
                obs = new observations_t(ins);
                pointedId = obs.click_id;
                world.newObservation(obs);
                receiveAckTime(obs.soar_utime);
            }
            catch (IOException e){
                e.printStackTrace();
                return;
            }
    	} else if(channel.equals("ROBOT_ACTION")){
    		try {
    			robot_action_t action = new robot_action_t(ins);
    			armStatus = action.action.toLowerCase();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public JMenu createMenu(){
    	JMenu perceptionMenu = new JMenu("Perception");
    	JMenuItem clearDataButton = new JMenuItem("Clear Classifier Data");
        clearDataButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "CLEAR_CLASSIFIERS";
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(clearDataButton);  
        
        JMenuItem loadDataButton = new JMenuItem("Load Classifier Data");
        loadDataButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "LOAD_CLASSIFIERS=" + classifiersFile;
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(loadDataButton);
        
        JMenuItem saveDataButton = new JMenuItem("Save Classifier Data");
        saveDataButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "SAVE_CLASSIFIERS=" + classifiersFile;
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(saveDataButton);
        
        JMenuItem loadDataFileButton = new JMenuItem("Load Data from File");
        loadDataFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		String filename = JOptionPane.showInputDialog(null, 
            			  "Enter the filename to load from",
            			  "Load Classifier Data From File",
            			  JOptionPane.QUESTION_MESSAGE);
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "LOAD_CLASSIFIERS=" + filename;
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(loadDataFileButton);
        
        JMenuItem saveDataFileButton = new JMenuItem("Save Data from File");
        saveDataFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		String filename = JOptionPane.showInputDialog(null, 
          			  "Enter the filename to save to",
          			  "Save Classifier Data To File",
          			  JOptionPane.QUESTION_MESSAGE);
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "SAVE_CLASSIFIERS=" + filename;
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(saveDataFileButton);
        
        return perceptionMenu;
    }

	@Override
	protected void onSoarInit() {
		timeId = null;
		outstandingTraining.clear();
	}
}
