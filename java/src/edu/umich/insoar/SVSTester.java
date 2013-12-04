package edu.umich.insoar;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

import edu.umich.insoar.world.SVSCommands;
import edu.umich.insoar.world.WMUtil;

import sml.Agent;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;

public class SVSTester extends JFrame implements OutputEventInterface, RunEventInterface{
	private JMenuBar menuBar;
	
	private SoarAgent soarAgent;
	
	private PerceptionConnector perception;
	
	private double val = .5;
	
	private JLabel label;
	
	private int stepNo = 0;
	
	public SVSTester() {
		super("Test SVS");
		soarAgent = new SoarAgent("svs-tester", "agent/test_svs_copy.soar", false);
		soarAgent.getAgent().AddOutputHandler("report-val", this, null);
		
		perception = new PerceptionConnector(soarAgent);
		soarAgent.setWorldModel(perception.world);
		
		this.menuBar = new JMenuBar();
		
		this.setJMenuBar(menuBar);
		
		menuBar.add(soarAgent.createMenu());
		
		addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
		
		soarAgent.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
		
		label = new JLabel("Filter Value: " + val);
		this.add(label);
		
		this.setSize(200, 200);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SVSTester tester = new SVSTester();
	}
	
	public void exit(){
		soarAgent.kill();
	}
	
	float rotX = 0;
	
	public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
		StringBuilder svsCommands = new StringBuilder();
//		if(stepNo == 0){
//			svsCommands.append("a 1 object world p 0 0 0 r 0 0 0 s 1 1 1 v " + SVSCommands.bboxVertices() + "\n");
//			svsCommands.append("a 2 object world p 3 -2 0 r 0 0 0 s 1 1 1 v " + SVSCommands.bboxVertices() + "\n");
//			svsCommands.append("a 3 object world p 3 0 0 r 0 0 0 s 1 1 1 v " + SVSCommands.bboxVertices() + "\n");
//			svsCommands.append("a eyeball object world p 5 4 0 b 0\n");
//			svsCommands.append(SVSCommands.addProperty("1", "color", "blue"));
//			svsCommands.append(SVSCommands.addProperty("2", "color", "red"));
//			svsCommands.append(SVSCommands.addProperty("3", "color", "red"));
//		} else if(stepNo % 3 == 0){
//			float y = stepNo / 3.0f * .5f;
//			svsCommands.append("c 2 p 3 " + (y-2) + " 0\n");
//			svsCommands.append("c 3 p 3 " + y  + " 0\n");
//		}
		if(stepNo == 0){
			System.out.println("CREATE");
			svsCommands.append("a obj1 object world p 0 0 0 r 0 0 0 s 1 1 1 v " + SVSCommands.bboxVertices() + "\n");
			svsCommands.append("a obj2 object world p .95 .95 .95 r 0 0 0 s 1 1 1 v " + SVSCommands.bboxVertices() + "\n");
			svsCommands.append(SVSCommands.addProperty("obj1", "object-source", "perception"));
			svsCommands.append(SVSCommands.addProperty("obj2", "object-source", "perception"));
		}
		agent.SendSVSInput(svsCommands.toString());
		
		stepNo++;
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
            if (wme.GetAttribute().equals("report-val")) {
            	String valueStr = WMUtil.getValueOfAttribute(id, "value");
            	val = Double.parseDouble(valueStr);
            	id.CreateStringWME("status", "success");
            	label.setText("Filter Value: " + val);
            } 
            soarAgent.commitChanges();
        } catch (IllegalStateException e){
        	System.out.println(e.getMessage());
        }
	}
}
