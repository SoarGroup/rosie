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
	private Boolean stoveHeatState; // 0 is off; 1 is on
	private Boolean pantryDoorState; // 0 is closed; 1 is open
//	private Boolean stoveDoorState; // 0 is closed; 1 is open
//	private Boolean armState; // O is not-held; 1 is held
//	private Integer objectId;
	private Random random;
//	private Integer held;
//	private boolean objectCookedState;
	
	public ResetEnvironmentState(){//Integer id, Integer heldObject){
		//objectId = id;
		//held = heldObject;
		random = new Random();
	}
	
	@Override
	public void execute() {
		new ResetRobotArm().execute();
//		stoveHeatState = random.nextBoolean();
//		pantryDoorState = random.nextBoolean();
//		objectCookedState = random.nextBoolean();
//		armState = random.nextBoolean();
		
		pantryDoorState = false;
		stoveHeatState = false;
		
		// Object 1 - Pantry
		set_state_command_t commandPantryDoor = new set_state_command_t();
		commandPantryDoor.utime = TimeUtil.utime();
		commandPantryDoor.state_name = "door";
		commandPantryDoor.state_val = (pantryDoorState ? "open" : "closed");
		commandPantryDoor.obj_id = 1;
		LCM.getSingleton().publish("SET_STATE_COMMAND", commandPantryDoor);
		
		// Object 2 - Stove
		set_state_command_t commandStoveHeat = new set_state_command_t();
		commandStoveHeat.utime = TimeUtil.utime();
		commandStoveHeat.state_name = "heat";
		commandStoveHeat.state_val = (stoveHeatState ? "on" : "off");
		commandStoveHeat.obj_id = 2;
		LCM.getSingleton().publish("SET_STATE_COMMAND", commandStoveHeat);
		
		// Object 5: Steak - uncooked
		set_state_command_t commandObjectState1 = new set_state_command_t();
		commandObjectState1.utime = TimeUtil.utime();
		commandObjectState1.state_name = "cooked";
		commandObjectState1.state_val = "false";
		commandObjectState1.obj_id = 5;
		LCM.getSingleton().publish("SET_STATE_COMMAND", commandObjectState1);
		
		// Object 6: Steak - uncooked
		set_state_command_t commandObjectState2 = new set_state_command_t();
		commandObjectState2.utime = TimeUtil.utime();
		commandObjectState2.state_name = "cooked";
		commandObjectState2.state_val = "false";
		commandObjectState2.obj_id = 6;
		LCM.getSingleton().publish("SET_STATE_COMMAND", commandObjectState2);
		
//		set_state_command_t commandObjectState3 = new set_state_command_t();
//		commandObjectState3.utime = TimeUtil.utime();
//		commandObjectState3.state_name = "cooked";
//		commandObjectState3.state_val = "false";
//		commandObjectState3.obj_id = 7;
//		LCM.getSingleton().publish("SET_STATE_COMMAND", commandObjectState3);
//		
//		set_state_command_t commandObjectState4 = new set_state_command_t();
//		commandObjectState4.utime = TimeUtil.utime();
//		commandObjectState4.state_name = "cooked";
//		commandObjectState4.state_val = "false";
//		commandObjectState4.obj_id = 8;
//		LCM.getSingleton().publish("SET_STATE_COMMAND", commandObjectState4);
		
		//System.out.println("Held object is " + held);
		//System.out.println("Object id: "+ objectId +"; State vector: " + "stove-door|" +stoveDoorState + " " + "stove-heat|"+ stoveHeatState + " " + "pantry-door|" + pantryDoorState + " " + "arm-state|" + armState + "object-state|" + objectCookedState);
	}
}
