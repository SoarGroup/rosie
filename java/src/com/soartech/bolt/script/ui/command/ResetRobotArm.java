package com.soartech.bolt.script.ui.command;

import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;
import edu.umich.insoar.InSoar;

public class ResetRobotArm implements UiCommand {
	@Override
	public void execute() {
		robot_command_t command = new robot_command_t();
		command.utime = TimeUtil.utime();
		command.action = "RESET";
		command.dest = new double[6];
		InSoar.broadcastRobotCommand(command);
	}
}
