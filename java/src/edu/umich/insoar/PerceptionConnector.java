package edu.umich.insoar;

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
import javax.swing.JMenuItem;

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
import edu.umich.insoar.world.WMUtil;
import edu.umich.insoar.world.WorldModel;

public class PerceptionConnector implements OutputEventInterface, RunEventInterface, LCMSubscriber {
	private SoarAgent soarAgent;
	
	private static int SEND_TRAINING_FPS = 10;
	Timer sendTrainingTimer;
	
	// Time Information
	private Identifier timeId = null;
	private int stepNumber = 0;
	
	// Object being pointed to
	private int pointedId = -1;
	
    private HashMap<training_label_t, Identifier> outstandingTraining;
    
    private LCM lcm;
    
    protected WorldModel world;
    
    private String armStatus = "wait";
    
    
    
    public PerceptionConnector(SoarAgent soarAgent)
    {
    	this.soarAgent = soarAgent;
    	
    	outstandingTraining = new HashMap<training_label_t, Identifier>();
    	
        String[] outputHandlerStrings = { "send-training-label", "modify-scene" };

        for (String outputHandlerString : outputHandlerStrings)
        {
        	soarAgent.getAgent().AddOutputHandler(outputHandlerString, this, null);
        }
        
       	soarAgent.getAgent().RegisterForRunEvent(
               smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
        
        
        world = new WorldModel(soarAgent);
        lcm = LCM.getSingleton();
        lcm.subscribe("OBSERVATIONS", this);
        lcm.subscribe("ROBOT_ACTION", this);
        
        sendTrainingTimer = new Timer();
        sendTrainingTimer.schedule(new TimerTask(){
        	public void run(){
        		sendTrainingLabels();
        	}
        }, 1000, 1000/SEND_TRAINING_FPS);
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

    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	smlRunEventId runEventId = smlRunEventId.swigToEnum(eventID);
    	if(runEventId == smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE){
    		time = TimeUtil.utime();
    		updateInputLink(agent);
    		if(InSoar.DEBUG_TRACE){
    			System.out.println(String.format("%-20s : %d", "PERCEPTION CONNECTOR", (TimeUtil.utime() - time)/1000));
    		}
    	}
    }
    
    public void updateInputLink(Agent agent){
    	Identifier inputLinkId = agent.GetInputLink();
    	// Update time information on the input link
    	
    	if(timeId == null){
            timeId = inputLinkId.CreateIdWME("time");
        }
        
        stepNumber++;
        
        WMUtil.updateIntWME(timeId, "steps", stepNumber);
        WMUtil.updateIntWME(timeId, "seconds", InSoar.GetSoarTime() / 1000000);
        
        // Update pointed object
        WMUtil.updateIntWME(inputLinkId, "pointed-object", pointedId);
    }

    /*************************************************
     * outputEventHandler
     * Runs when the agent puts the appropriate command on the output link
     * send-training-label: sends a new training example to perception
     *************************************************/
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
    	synchronized(this){
    		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
            {
                return;
            }
    		Identifier id = wme.ConvertToIdentifier();
            

            try{
            	if(wme.GetAttribute().equals("send-training-label"))
	            {
	            	processSendTrainingLabelCommand(id);
	                System.out.println(wme.GetAttribute());
	            } else if(wme.GetAttribute().equals("modify-scene")){
	            	processModifySceneCommand(id);
	            }
	            soarAgent.commitChanges();
            } catch (IllegalStateException e){
            	System.out.println(e.getMessage());
            }
    	}
    }
    

    private void processSendTrainingLabelCommand(Identifier id){
    	Integer objId = Integer.parseInt(WMUtil.getValueOfAttribute(id, "id", 
    			"Error (send-training-label): No ^id attribute"));
    	String label = WMUtil.getValueOfAttribute(id, "label", 
    			"Error (send-training-label): No ^label attribute");
    	String propName = WMUtil.getValueOfAttribute(id, "property-name", 
    			"Error (send-training-label): No ^property-name attribute");
    	training_label_t newLabel = new training_label_t();
    	Integer catNum = PerceptualProperty.getPropertyID(propName);
    	if(catNum == null){
    		return;
    	}
    	
    	
    	queueTrainingLabel(objId, catNum, label, id);
    }
    
    private void processModifySceneCommand(Identifier rootId){
    	String type = WMUtil.getValueOfAttribute(rootId, "type", "Error: No ^type attribute");
    	if(type.equals("delete")){
    		String deleteId = WMUtil.getValueOfAttribute(rootId, "id", "Error (delete): No ^id attribute");
    		//world.removeObject(Integer.parseInt(deleteId));
    	} else if(type.equals("merge")){
    		String originalId = WMUtil.getValueOfAttribute(rootId, "original-id", "Error (merge): No ^original-id");
    		String copyId = WMUtil.getValueOfAttribute(rootId, "copy-id", "Error (merge): No ^copy-id");
    		//world.mergeObject(Integer.parseInt(originalId), Integer.parseInt(copyId));
    	} else if(type.equals("move")){
    		String moveId = WMUtil.getValueOfAttribute(rootId, "id", "Error (move): No ^id attribute");
    		Identifier posId = WMUtil.getIdentifierOfAttribute(rootId, "pos", "Error (moidfy-scene.move): No pos");
    		double x = Double.parseDouble(WMUtil.getValueOfAttribute(
    				posId, "x", "Error (modify-scene.move): No ^x attribute"));
	        double y = Double.parseDouble(WMUtil.getValueOfAttribute(
	        		posId, "y", "Error (modify-scene.move): No ^y attribute"));
	        double z = Double.parseDouble(WMUtil.getValueOfAttribute(
	        		posId, "z", "Error (modify-scene.move): No ^z attribute"));
	        //world.moveObject(Integer.parseInt(moveId), x, y, z);
    	} else if(type.equals("confirm")){
    		String objId = WMUtil.getValueOfAttribute(rootId, "id", "Error (confirm): No ^id attribute");
    		//world.confirmObject(Integer.parseInt(objId));
    	} else if(type.equals("link")){
    		Set<String> sourceIds = WMUtil.getAllValuesOfAttribute(rootId, "source-id");
    		if(sourceIds.size() == 0){
    			rootId.CreateStringWME("status", "error");
    			System.err.println("Error (link): No ^source-id attribute");
    		}
    		String destId = WMUtil.getValueOfAttribute(rootId, "dest-id", "Error (link): No ^dest-id attribute");
    		if(destId.contains("bel-")){
    			destId = destId.substring(4);
    		}
    		world.linkObjects(sourceIds, destId);
    		
    	} else {
    		rootId.CreateStringWME("status", "error");
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
        		cmd.command = "LOAD_CLASSIFIERS";
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(loadDataButton);
        
        JMenuItem saveDataButton = new JMenuItem("Save Classifier Data");
        saveDataButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		perception_command_t cmd = new perception_command_t();
        		cmd.utime = InSoar.GetSoarTime();
        		cmd.command = "SAVE_CLASSIFIERS";
                LCM.getSingleton().publish("PERCEPTION_COMMAND", cmd);
        	}
        });
        
        perceptionMenu.add(saveDataButton);
        
        return perceptionMenu;
    }
}
