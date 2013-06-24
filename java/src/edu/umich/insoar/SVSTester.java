package edu.umich.insoar;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

import edu.umich.insoar.world.WMUtil;

import sml.Identifier;
import sml.WMElement;
import sml.Agent.OutputEventInterface;

public class SVSTester extends JFrame implements OutputEventInterface{
	private JMenuBar menuBar;
	
	private SoarAgent soarAgent;
	
	private PerceptionConnector perception;
	
	private double occlusion = .5;
	
	private JLabel occlusionLabel;
	
	public SVSTester() {
		super("Test SVS");
		soarAgent = new SoarAgent("svs-tester", "agent/perception/test-svs.soar", false);
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
