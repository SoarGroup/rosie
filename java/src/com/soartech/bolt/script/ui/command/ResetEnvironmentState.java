package com.soartech.bolt.script.ui.command;

import java.util.Random;

import lcm.lcm.LCM;
import april.util.TimeUtil;
import probcog.lcmtypes.perception_command_t;
import edu.umich.insoar.world.WorldModel;


public class ResetEnvironmentState implements UiCommand {
	private Boolean stoveDoorState; // 0 is open; 1 is closed
	private Boolean stoveHeatState; // 0 is on; 1 is off
	private Boolean armState; // O is held; 1 is not-held
	private Boolean pantryDoorState; // 0 is open; 1 is closed
	private Integer objectId;
	private Random random;
	
	public ResetEnvironmentState(Integer id){
		objectId = id;
		random = new Random();
	}
	
	@Override
	public void execute() {
		stoveDoorState = random.nextBoolean();	
		stoveHeatState = random.nextBoolean();
		pantryDoorState = random.nextBoolean();
		armState = random.nextBoolean();
		
		perception_command_t commandStoveDoor = new perception_command_t();
		if (stoveDoorState){
			commandStoveDoor.utime = TimeUtil.utime();
			commandStoveDoor.command = "open=" + 1;
			LCM.getSingleton().publish("GUI_COMMAND", commandStoveDoor);
		}
		else {
			commandStoveDoor.utime = TimeUtil.utime();
			commandStoveDoor.command = "close=" + 1;
			LCM.getSingleton().publish("GUI_COMMAND", commandStoveDoor);
		}
		
		System.out.println("State vector: " + stoveDoorState + " " + stoveHeatState + " " + pantryDoorState + " " + armState);
	}

}
