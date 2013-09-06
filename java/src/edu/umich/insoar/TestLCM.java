package edu.umich.insoar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import probcog.lcmtypes.categorized_data_t;
import probcog.lcmtypes.category_t;
import probcog.lcmtypes.object_data_t;
import probcog.lcmtypes.observations_t;
import probcog.lcmtypes.set_state_command_t;

public class TestLCM extends JFrame implements LCMSubscriber{
	private JMenuBar menuBar;
	
	private JTextPane textPane;
	
	private JButton setState;
	
	private LCM lcm;
	
	public TestLCM() {
		super("Test LCM");
		
		lcm = LCM.getSingleton();
		lcm.subscribe("OBSERVATIONS", this);
		
		this.setSize(500, 300);
		
		textPane = new JTextPane();
		setState = new JButton("Set State");
		
		 setState.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e){
	        		Integer id = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter ID", "ID", JOptionPane.QUESTION_MESSAGE));
	        		String command = JOptionPane.showInputDialog(null, 
	            			  "What do you want to do?",
	            			  "Set State",
	            			  JOptionPane.QUESTION_MESSAGE);
	      			String[] keyVal = command.toLowerCase().split("=");
	      			setCommand(id, keyVal[0], keyVal[1]);
	        	}
	        });
		
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				textPane, setState);

		pane.setDividerLocation(250);

        this.add(pane);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestLCM testLCM = new TestLCM();
	}

	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
		try {
			if (channel.equals("OBSERVATIONS")) {
				observations_t obs = new observations_t(ins);
				StringBuilder sb = new StringBuilder();
				for(object_data_t od : obs.observations){
					String name = "(" + od.id + ")";
					for(categorized_data_t catdat : od.cat_dat){
						if(catdat.cat.cat == category_t.CAT_LOCATION){
							name += catdat.label[0];
						}
					}
					sb.append(name + ":\n");
					for(String kv : od.state_values){
						sb.append("  " + kv + "\n");
					}
				}
				textPane.setText(sb.toString());
	        }
        } catch (IOException ioex) {
            System.err.println("ERR: LCM channel -"+channel);
            ioex.printStackTrace();
        }
	}
	
	public void setCommand(int id, String stateName, String stateVal){
		set_state_command_t setState = new set_state_command_t();
		setState.obj_id = id;
		setState.state_name = stateName;
		setState.state_val = stateVal;
		lcm.publish("SET_STATE_COMMAND", setState);
	}
}
