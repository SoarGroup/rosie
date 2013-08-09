package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JMenu;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import probcog.lcmtypes.robot_action_t;
import probcog.lcmtypes.robot_command_t;
import april.util.TimeUtil;

import com.soartech.bolt.script.ui.command.ResetRobotArm;

import edu.umich.insoar.world.Pose;
import edu.umich.insoar.world.WMUtil;

public class MotorSystemConnector   implements OutputEventInterface, RunEventInterface, LCMSubscriber{
	private SoarAgent agent;
    private Identifier inputLinkId;
	private Identifier selfId;

	private Pose pose;
	
	private robot_action_t curStatus = null;
	private robot_action_t prevStatus = null;
	// Last received information about the arm
	
	private boolean gotUpdate = false;
	
    private LCM lcm;

    public MotorSystemConnector(SoarAgent agent){
    	this.agent = agent;
    	pose = new Pose();
    	
    	// Setup LCM events
        lcm = LCM.getSingleton();
        lcm.subscribe("ROBOT_ACTION", this);

        // Setup Input Link Events
        inputLinkId = agent.getAgent().GetInputLink();
        agent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
        
        // Setup Output Link Events
        String[] outputHandlerStrings = { "pick-up", "put-down", "point", "set-state", "home"};
        for (String outputHandlerString : outputHandlerStrings)
        {
        	agent.getAgent().AddOutputHandler(outputHandlerString, this, null);
        }
    }
    
    @Override
    public synchronized void messageReceived(LCM lcm, String channel, LCMDataInputStream ins){
    	if(channel.equals("ROBOT_ACTION")){
    		try {
    			robot_action_t action = new robot_action_t(ins);
				newRobotStatus(action);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public void newRobotStatus(robot_action_t status){
    	prevStatus = curStatus;
    	curStatus = status;
    	gotUpdate = true;
    }
    
    public String getStatus(){
    	if(curStatus == null){
    		return "wait";
    	} else {
    		return curStatus.action.toLowerCase();
    	}
    }

	// Happens during an input phase
	public synchronized void runEventHandler(int eventID, Object data, Agent agent, int phase){
		if(selfId == null){
			initIL();
		} else if(gotUpdate){
			updateIL();
			gotUpdate = false;
		}
		this.agent.commitChanges();
	}
    
    private void initIL(){
    	selfId = inputLinkId.CreateIdWME("self");
    	selfId.CreateStringWME("action", "wait");
    	selfId.CreateStringWME("prev-action", "wait");
    	selfId.CreateStringWME("holding-obj", "false");
    	selfId.CreateIntWME("grabbed-object", -1);
    	pose.updateWithArray(new double[]{0, 0, 0, 0, 0, 0});
    	pose.updateInputLink(selfId);
    }
    
    private void updateIL(){
    	if(prevStatus == null){
    		return;
    	}
    	WMUtil.updateStringWME(selfId, "action", curStatus.action.toLowerCase());
    	WMUtil.updateStringWME(selfId, "prev-action", prevStatus.action.toLowerCase());
    	WMUtil.updateStringWME(selfId, "holding-obj", (curStatus.obj_id != -1 ? "true" : "false"));
    	WMUtil.updateIntWME(selfId, "grabbed-object", curStatus.obj_id);
    	pose.updateWithArray(curStatus.xyz);
    	pose.updateInputLink(selfId);
    }
    

    @Override
    public synchronized void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme) {
		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
        {
            return;
        }
		Identifier id = wme.ConvertToIdentifier();
        System.out.println(wme.GetAttribute());
            
        try{
            if (wme.GetAttribute().equals("set-state")) {
                processSetCommand(id);
            } 
            else if (wme.GetAttribute().equals("pick-up")) {
                processPickUpCommand(id);
            } 
            else if (wme.GetAttribute().equals("put-down")) {
                processPutDownCommand(id);
            } 
            else if (wme.GetAttribute().equals("point")) {
                processPointCommand(id);
            } 
            else if(wme.GetAttribute().equals("home")){
            	processHomeCommand(id);
            }
            agent.commitChanges();
        } catch (IllegalStateException e){
        	System.out.println(e.getMessage());
        }
	}
    
    /**
     * Takes a pick-up command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command. Expects pick-up
     * ^object-id [int]
     */
    private void processPickUpCommand(Identifier pickUpId)
    {
        String objectIdStr = WMUtil.getValueOfAttribute(pickUpId,
                "object-id", "pick-up does not have an ^object-id attribute");
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = String.format("GRAB=%d", Integer.parseInt(objectIdStr));
        command.dest = new double[6];
        sendArmCommand(command);
        pickUpId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a put-down command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command Expects put-down
     * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
     */
    private void processPutDownCommand(Identifier putDownId)
    {
        Identifier locationId = WMUtil.getIdentifierOfAttribute(
                putDownId, "location",
                "Error (put-down): No ^location identifier");
        double x = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "x", "Error (put-down): No ^location.x attribute"));
        double y = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "y", "Error (put-down): No ^location.y attribute"));
        double z = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "z", "Error (put-down): No ^location.z attribute"));
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = "DROP";
        command.dest = new double[]{x, y, z, 0, 0, 0};
        sendArmCommand(command);
        putDownId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a set-state command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processSetCommand(Identifier id)
    {
        String objId = WMUtil.getValueOfAttribute(id, "id",
                "Error (set-state): No ^id attribute");
        String name = WMUtil.getValueOfAttribute(id,
                "name", "Error (set-state): No ^name attribute");
        String value = WMUtil.getValueOfAttribute(id, "value",
                "Error (set-state): No ^value attribute");

        String action = String.format("ID=%s,%s=%s", objId, name, value);
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = action;
        command.dest = new double[6];
        sendArmCommand(command);
        id.CreateStringWME("status", "complete");
    }

    private void processPointCommand(Identifier pointId)
    {
    	Integer id = Integer.parseInt(WMUtil.getValueOfAttribute(pointId, "id"));
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[]{0, 0, 0, 0, 0, 0};
    	command.action = "POINT=" + id;
        sendArmCommand(command);
        pointId.CreateStringWME("status", "complete");
    }
    
    private void processHomeCommand(Identifier id){
    	robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[6];
    	command.action = "HOME";
        sendArmCommand(command);
        id.CreateStringWME("status", "complete");
    }
    
    private void sendArmCommand(robot_command_t command){
        lcm.publish("ROBOT_COMMAND", command);
    }
    
    public JMenu createMenu(){
    	JMenu actionMenu = new JMenu("Action");
    	JButton armResetButton  = new JButton("Reset Arm");
        armResetButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				new ResetRobotArm().execute();
			}
        });
        actionMenu.add(armResetButton);
        
        return actionMenu;
    }
}
