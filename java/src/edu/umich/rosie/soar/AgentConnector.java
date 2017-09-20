package edu.umich.rosie.soar;

import java.util.HashSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;


import sml.*;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Kernel.AgentEventInterface;

public abstract class AgentConnector implements OutputEventInterface, RunEventInterface, AgentEventInterface {
    protected SoarAgent soarAgent;
    
    private String[] outputHandlerNames;
    private HashSet<Long> outputHandlerCallbackIds;
    private long inputPhaseCallbackId;
    private long initAgentCallbackId;
    
    private boolean connected;
	
	public AgentConnector(SoarAgent agent){
        this.soarAgent = agent;
        
        this.outputHandlerCallbackIds = new HashSet<Long>();
        this.outputHandlerNames = new String[]{};
        this.initAgentCallbackId = 0;

        this.connected = false;
	}
	
	protected void setOutputHandlerNames(String[] outputHandlerNames){
		this.outputHandlerNames = outputHandlerNames;
	}
	
	public void connect(){
		if(connected){
			return;
		}
		for(String name : outputHandlerNames){
			outputHandlerCallbackIds.add(soarAgent.getAgent().AddOutputHandler(name, this, null));
		}

		inputPhaseCallbackId = soarAgent.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
		
		initAgentCallbackId = soarAgent.getAgent().GetKernel().RegisterForAgentEvent(
				smlAgentEventId.smlEVENT_BEFORE_AGENT_REINITIALIZED, this, null);
		
		connected = true;
	}
	
	public void disconnect(){
		if(!connected){
			return;
		}
		
		for(Long callbackId : outputHandlerCallbackIds){
			soarAgent.getAgent().RemoveOutputHandler(callbackId);
		}
		outputHandlerCallbackIds.clear();

		soarAgent.getAgent().UnregisterForRunEvent(inputPhaseCallbackId);
		inputPhaseCallbackId = 0;
		
		soarAgent.getAgent().GetKernel().UnregisterForAgentEvent(initAgentCallbackId);
		initAgentCallbackId = 0;

    // Release all soar pointers
    onInitSoar();
		
		connected = false;
	}
	
	// Handling an input
    public void runEventHandler(int eventID, Object data, Agent agent, int phase){
    	if(eventID == smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE.swigValue()){
    		onInputPhase(agent.GetInputLink());
    	}
    }
    
    protected abstract void onInputPhase(Identifier inputLink);

    // Handling Output Events
    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
    	if(!wme.IsJustAdded() || !wme.IsIdentifier()){
    		return;
    	}
    	Identifier id = wme.ConvertToIdentifier();
    	onOutputEvent(attributeName, id);
    }
    
    protected abstract void onOutputEvent(String attName, Identifier id);

    // Handling an init-soar
	public void agentEventHandler(int agentEventId, Object data, String info) {
		onInitSoar();
	}
	
	protected abstract void onInitSoar();
	
	// Adding a Java Menu
	protected abstract void createMenu(JMenuBar menuBar);
}
