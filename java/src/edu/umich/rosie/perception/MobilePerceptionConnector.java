package edu.umich.rosie.perception;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import edu.umich.rosie.AgentConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.SoarUtil;
import april.lcmtypes.pose_t;
import april.util.TimeUtil;
import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
//import probcog.commands.CommandCoordinator;
//import probcog.commands.CommandCoordinator.Status;
//import probcog.lcmtypes.classification_list_t;
////import probcog.lcmtypes.control_law_list_t;
//import probcog.lcmtypes.classification_t;
//import probcog.lcmtypes.control_law_status_list_t;
//import probcog.lcmtypes.control_law_status_t;
//import probcog.lcmtypes.control_law_t;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;

public class MobilePerceptionConnector extends AgentConnector implements LCMSubscriber{

//	private Object lock = new Object();
//
	private boolean gotUpdate = false;
//	private classification_list_t curClassifications = null;
//	private Identifier waypointId = null;
//	private int curWaypoint = -1;
//
//    private LCM lcm;
//    
    private boolean gotPose = false;
    private pose_t pose = null;

    public MobilePerceptionConnector(SoarAgent agent){
    	super(agent);

//
//    	// Setup LCM events
//        lcm = LCM.getSingleton();
//        lcm.subscribe("CLASSIFICATIONS.*", this);
//        lcm.subscribe("POSE_TRUTH.*", this);
//
//        // Setup Input Link Events
//        agent.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);

    }
//
//    @Override
//    public synchronized void messageReceived(LCM lcm, String channel, LCMDataInputStream ins){
//		try {
//			if(channel.startsWith("CLASSIFICATIONS")){
//				curClassifications = new classification_list_t(ins);
//				gotUpdate = true;
//			} else if (channel.startsWith("POSE_TRUTH")){
//				pose = new pose_t(ins);
//				gotPose = true;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }

	// Happens during an input phase
	protected void onInputPhase(Identifier inputLink){
		if (gotPose){
			SoarUtil.updateFloatWME(inputLink, "x", pose.pos[0]);
			SoarUtil.updateFloatWME(inputLink, "y", pose.pos[1]);
			gotPose = false;
		}
		if (gotUpdate){
			//updateInputLink(inputLink);
			gotUpdate = false;
		}
	}

	@Override
	public void messageReceived(LCM arg0, String arg1, LCMDataInputStream arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onOutputEvent(String attName, Identifier id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onInitSoar() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createMenu(JMenuBar menuBar) {}

//    private void updateInputLink(Identifier inputLink){
//    	int closestWaypoint = -1;
//    	double closestDistance = Double.MAX_VALUE;
//    	for (classification_t c : curClassifications.classifications){
//    		if (c.range < closestDistance){
//    			closestWaypoint = c.id;
//    			closestDistance = c.range;
//    		}
//    	}
//   		if (closestWaypoint != curWaypoint && waypointId != null){
//   			waypointId.DestroyWME();
//   			waypointId = null;
//   		}
//   		curWaypoint = closestWaypoint;
//   		if (curWaypoint == -1){
//   			return;
//   		}
//   		if (waypointId == null){
//    		waypointId = inputLink.CreateIdWME("current-waypoint");
//   		}
//   		for (classification_t c : curClassifications.classifications){
//   			if (c.id == closestWaypoint){
//   				if (c.name.startsWith("wp")){
//   					SoarUtil.updateStringWME(waypointId, "id", c.name);
//   				} else {
//   					SoarUtil.updateStringWME(waypointId, "classification", c.name);
//   				}
//   			}
//   		}
//    }
}
