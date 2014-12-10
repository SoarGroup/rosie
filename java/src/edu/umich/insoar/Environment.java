package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import lcm.lcm.LCM;

import probcog.lcmtypes.perception_command_t;
import probcog.lcmtypes.robot_command_t;

import april.util.TimeUtil;

import com.soartech.bolt.script.ui.command.ResetEnvironmentState;

public class Environment {
	 private static MotorSystemConnector motor;
	 public Environment(MotorSystemConnector motorSystem) {
		 motor = motorSystem;
	}

	public static JMenu createMenu(){
	    	JMenu environmentMenu = new JMenu("Environment");
	    	JButton environmentResetButton  = new JButton("Reset State");
	    	environmentResetButton.addActionListener(new ActionListener(){
	    		public void actionPerformed(ActionEvent arg0){
	    			String input =  JOptionPane.showInputDialog("Input object id:");
	    			Integer id = Integer.parseInt(input);
	    			(new ResetEnvironmentState(id, motor.getHeldObject())).execute();
	    		}
	    	});
	    	environmentMenu.add(environmentResetButton);

	    	JButton worldResetButton  = new JButton("Reset World");
	    	worldResetButton.addActionListener(new ActionListener(){
	    		public void actionPerformed(ActionEvent arg0){
	    	    	robot_command_t command = new robot_command_t();
	    	        command.utime = TimeUtil.utime(); 
	    	        command.dest = new double[6];
	    	    	command.action = "RESET";
	    	    	LCM.getSingleton().publish("ROBOT_COMMAND", command);
	    	    	
	    	    	perception_command_t pcmd = new perception_command_t();
	    	    	pcmd.utime = InSoar.GetSoarTime();
	    	    	pcmd.command = "reset=time";
	    	    	LCM.getSingleton().publish("GUI_COMMAND", pcmd);    	    	
	    		}
	    	});
	    	environmentMenu.add(worldResetButton);

			return environmentMenu;
	 }
	
	public static Integer getHeldObject(){
		return motor.getHeldObject();
	}
}
