package edu.umich.insoar;

import java.awt.*;
import java.util.Stack;
import javax.swing.*;

public class InteractionStack extends JFrame{
	
	private Stack<JButton> stack;
	private JPanel panel;
	
	public InteractionStack(){
		super("Interaction Stack");
		
		stack = new Stack<JButton>();
		
		panel = new JPanel(new GridLayout(0, 1));
        JScrollPane pane = new JScrollPane(panel);
		this.add(pane);
		
        this.setSize(200, 50);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        hideFrame();
        
        pushSegment("<------ Bottom ------>", "none");
	}
	
	public void clear(){
		while(stack.size() > 1){
			popSegment();
		}
	}
	
	public void showFrame()
    {
        this.setVisible(true);
    }

    public void hideFrame()
    {
        this.setVisible(false);
    }
    
    public void pushSegment(String type, String originator){
    	JButton button = new JButton(type);
    	if(originator.equals("none")){
    		button.setBackground(new Color(255, 255, 255));
    	} else if(originator.equals("agent")){
    		button.setBackground(new Color(200, 255, 255));
    	} else {
    		button.setBackground(new Color(255, 255, 200));
    	}
    	button.setPreferredSize(new Dimension(200, 30));
    	stack.push(button);
    	panel.add(button);
    	this.pack();
    }
    
    public void popSegment(){
    	if(stack.size() > 1){
    		panel.remove(stack.pop());
    		this.pack();
    	}
    }
}
