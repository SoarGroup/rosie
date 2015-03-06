package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

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
	    			//String input =  JOptionPane.showInputDialog("Input object id:");
	    			//Integer id = Integer.parseInt(input);
	    			//(new ResetEnvironmentState(id, motor.getHeldObject())).execute();
	    			(new ResetEnvironmentState()).execute();
	    		}
	    	});
	    	environmentMenu.add(environmentResetButton);
			return environmentMenu;
	 }
	
	public static Integer getHeldObject(){
		return motor.getHeldObject();
	}
}
