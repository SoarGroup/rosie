package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import april.util.TimeUtil;
import edu.umich.insoar.world.PerceptualProperty;
import edu.umich.insoar.world.WMUtil;
import edu.umich.insoar.world.WorldModel;

public class PerceptionConnector implements OutputEventInterface, RunEventInterface, LCMSubscriber {
	private SoarAgent soarAgent;
	
	// Time Information
	private Identifier timeId = null;
	private int stepNumber = 0;
	private long startTime;
	
	// Object being pointed to
	private int pointedId = -1;
	
    private List<training_label_t> newLabels;
    
    private LCM lcm;
    
    protected WorldModel world;
    
    private String armStatus = "wait";
	
    public PerceptionConnector(SoarAgent soarAgent)
    {
    	this.soarAgent = soarAgent;
    	
    	startTime = TimeUtil.utime();
    	
    	
    	
        String[] outputHandlerStrings = { "send-training-label", "modify-scene" };

        for (String outputHandlerString : outputHandlerStrings)
        {
        	soarAgent.getAgent().AddOutputHandler(outputHandlerString, this, null);
        }
        
        soarAgent.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
        
        newLabels = new ArrayList<training_label_t>();
        
        world = new WorldModel(soarAgent);
        lcm = LCM.getSingleton();
        lcm.subscribe("OBSERVATIONS", this);
        lcm.subscribe("ROBOT_ACTION", this);
    }
    
    /*************************************************
     * runEventHandler
     * Runs every input phase to update the input link
     * with perceptual information (time, pointed obj) as well as
     * Send training labels to perception
     *************************************************/
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	
    	Identifier inputLinkId = agent.GetInputLink();
    	// Update time information on the input link
    	if(timeId == null){
            timeId = inputLinkId.CreateIdWME("time");
        }
        
        stepNumber++;
        WMUtil.updateIntWME(timeId, "steps", stepNumber);
        long curTime = TimeUtil.utime();
        WMUtil.updateIntWME(timeId, "seconds", (int)(curTime - startTime)/1000000);
        WMUtil.updateIntWME(timeId, "microseconds", (int)(curTime - startTime));
        
        // Update pointed object
        WMUtil.updateIntWME(inputLinkId, "pointed-object", pointedId);
        
        // Send new training labels to perception
        if(newLabels.size() > 0){
            training_data_t trainingData = new training_data_t();
        	trainingData.utime = TimeUtil.utime();
        	trainingData.num_labels = newLabels.size();
        	trainingData.labels = new training_label_t[newLabels.size()];
        	for(int i = 0; i < newLabels.size(); i++){
        		trainingData.labels[i] = newLabels.get(i);
        	}
        	lcm.publish("TRAINING_DATA", trainingData);
        	newLabels.clear();
    	}
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
    	
    	newLabel.cat = new category_t();
    	newLabel.cat.cat = catNum;
    	newLabel.id = objId;
    	newLabel.label = label;
    	
    	newLabels.add(newLabel);
    	id.CreateStringWME("status", "complete");
    }
    
    private void processModifySceneCommand(Identifier rootId){
    	String type = WMUtil.getValueOfAttribute(rootId, "type", "Error: No ^type attribute");
    	if(type.equals("delete")){
    		String deleteId = WMUtil.getValueOfAttribute(rootId, "id", "Error (delete): No ^id attribute");
    		world.removeObject(Integer.parseInt(deleteId));
    	} else if(type.equals("merge")){
    		String originalId = WMUtil.getValueOfAttribute(rootId, "original-id", "Error (merge): No ^original-id");
    		String copyId = WMUtil.getValueOfAttribute(rootId, "copy-id", "Error (merge): No ^copy-id");
    		world.mergeObject(Integer.parseInt(originalId), Integer.parseInt(copyId));
    	} else if(type.equals("move")){
    		String moveId = WMUtil.getValueOfAttribute(rootId, "id", "Error (move): No ^id attribute");
    		Identifier posId = WMUtil.getIdentifierOfAttribute(rootId, "pos", "Error (moidfy-scene.move): No pos");
    		double x = Double.parseDouble(WMUtil.getValueOfAttribute(
    				posId, "x", "Error (modify-scene.move): No ^x attribute"));
	        double y = Double.parseDouble(WMUtil.getValueOfAttribute(
	        		posId, "y", "Error (modify-scene.move): No ^y attribute"));
	        double z = Double.parseDouble(WMUtil.getValueOfAttribute(
	        		posId, "z", "Error (modify-scene.move): No ^z attribute"));
	        world.moveObject(Integer.parseInt(moveId), x, y, z);
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
                //if(armStatus.equals("wait")){
                    world.newObservation(obs);
                //}
                world.sendObservation();
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
        		robot_command_t cmd = new robot_command_t();
        		cmd.utime = TimeUtil.utime();
        		cmd.dest = new double[6];
        		cmd.updateDest = false;
        		cmd.action = "CLEAR";
                LCM.getSingleton().publish("ROBOT_COMMAND", cmd);
        	}
        });
        perceptionMenu.add(clearDataButton);  
        
        return perceptionMenu;
    }
}
