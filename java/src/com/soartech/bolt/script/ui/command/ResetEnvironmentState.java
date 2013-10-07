package com.soartech.bolt.script.ui.command;

import java.io.IOException;
import java.util.Random;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import april.util.TimeUtil;
import probcog.lcmtypes.*;
import edu.umich.insoar.world.WorldModel;


public class ResetEnvironmentState implements UiCommand {
	private Boolean stoveDoorState; // 0 is closed; 1 is open
	private Boolean stoveHeatState; // 0 is off; 1 is on
	private Boolean armState; // O is not-held; 1 is held
	private Boolean pantryDoorState; // 0 is closed; 1 is open
	private Integer objectId;
	private Random random;
	private Integer held;
	
	public ResetEnvironmentState(Integer id, Integer heldObject){
		objectId = id;
		held = heldObject;
		random = new Random();
	}
	
	@Override
	public void execute() {
		
		new ResetRobotArm().execute();
		
		stoveDoorState = random.nextBoolean();	
		stoveHeatState = random.nextBoolean();
		pantryDoorState = random.nextBoolean();
		armState = random.nextBoolean();
		
		set_state_command_t commandStoveDoor = new set_state_command_t();
		if (stoveDoorState){
			commandStoveDoor.utime = TimeUtil.utime();
			commandStoveDoor.state_name = "door";
			commandStoveDoor.state_val = "open";
			commandStoveDoor.obj_id = 4;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandStoveDoor);
		}
		else {
			commandStoveDoor.utime = TimeUtil.utime();
			commandStoveDoor.state_name = "door";
			commandStoveDoor.state_val = "closed";
			commandStoveDoor.obj_id = 4;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandStoveDoor);
		}
		
		set_state_command_t commandStoveHeat = new set_state_command_t();
		if (stoveHeatState){
			commandStoveHeat.utime = TimeUtil.utime();
			commandStoveHeat.state_name = "heat";
			commandStoveHeat.state_val = "on";
			commandStoveHeat.obj_id = 4;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandStoveHeat);
		}
		else {
			commandStoveHeat.utime = TimeUtil.utime();
			commandStoveHeat.state_name = "heat";
			commandStoveHeat.state_val = "off";
			commandStoveHeat.obj_id = 4;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandStoveHeat);
		}
		
		set_state_command_t commandPantryDoor = new set_state_command_t();
		if (pantryDoorState){
			commandPantryDoor.utime = TimeUtil.utime();
			commandPantryDoor.state_name = "door";
			commandPantryDoor.state_val = "open";
			commandPantryDoor.obj_id = 1;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandPantryDoor);
		}
		else {
			commandPantryDoor.utime = TimeUtil.utime();
			commandPantryDoor.state_name = "door";
			commandPantryDoor.state_val = "closed";
			commandPantryDoor.obj_id = 1;
			LCM.getSingleton().publish("SET_STATE_COMMAND", commandPantryDoor);
		}
		
		System.out.println("Held object is " + held);
		Random locRand = new Random();
		if (armState){
			if(held != objectId){
//				if (held != -1){
//					robot_command_t commandHeldObject = new robot_command_t();
//					commandHeldObject.utime = TimeUtil.utime();
//					commandHeldObject.action = "DROP";
//					commandHeldObject.dest = new double[]{(-0.15 + (locRand.nextDouble()*(0.3))), (-0.15 + (locRand.nextDouble()*(0.3))), 0.0, 0, 0, 0};
//					LCM.getSingleton().publish("ROBOT_COMMAND", commandHeldObject);
//				}
				robot_command_t commandObject = new robot_command_t();
				commandObject.utime = TimeUtil.utime();
				commandObject.action = String.format("GRAB=%d", objectId);
				commandObject.dest = new double[6];
				LCM.getSingleton().publish("ROBOT_COMMAND", commandObject);
			}
		}
		else {
			if(held != -1){
				robot_command_t commandObject = new robot_command_t();
				commandObject.utime = TimeUtil.utime();
				commandObject.action = "DROP";
				commandObject.dest = new double[]{(-0.15 + (locRand.nextDouble()*(0.3))), (-0.5 + (locRand.nextDouble()*(0.3))), 0.0, 0, 0, 0};
				LCM.getSingleton().publish("ROBOT_COMMAND", commandObject);
			}
		}
		
		System.out.println("Object id: "+ objectId +"; State vector: " + "stove-door|" +stoveDoorState + " " + "stove-heat|"+ stoveHeatState + " " + "pantry-door|" + pantryDoorState + " " + "arm-state|" + armState);
	}
}
