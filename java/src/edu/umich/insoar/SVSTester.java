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
	
	private double occlusion = .5;
	
	private JLabel occlusionLabel;
	
	private int stepNo = 0;
	
	public SVSTester() {
		super("Test SVS");
		soarAgent = new SoarAgent("svs-tester", "agent/empty_agent.soar", false);
		soarAgent.getAgent().AddOutputHandler("occlusion", this, null);
		
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
		
		occlusionLabel = new JLabel("Occlusion: " + occlusion);
		this.add(occlusionLabel);
		
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
		if(stepNo == 0){
			svsCommands.append("a 1 object world p 0 0 0 r 0 0 0 s 1 5 1 ");
			svsCommands.append("v " + SVSCommands.bboxVertices() + "\n");
		} else {
			rotX += .1;
			svsCommands.append("p 1 c rx " + rotX + "\n");
		}
		agent.SendSVSInput(svsCommands.toString());
		System.out.println(rotX);
		
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
            if (wme.GetAttribute().equals("occlusion")) {
            	String valueStr = WMUtil.getValueOfAttribute(id, "value");
            	occlusion = Double.parseDouble(valueStr);
            	id.CreateStringWME("status", "success");
                occlusionLabel.setText("Occlusion: " + occlusion);
            } 
            soarAgent.commitChanges();
        } catch (IllegalStateException e){
        	System.out.println(e.getMessage());
        }
	}
}
