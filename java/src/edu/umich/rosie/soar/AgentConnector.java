package edu.umich.rosie.soar;

import java.util.HashSet;

import sml.*;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.Kernel.AgentEventInterface;

public abstract class AgentConnector implements OutputEventInterface, RunEventInterface, AgentEventInterface {
    protected SoarClient soarClient;
    
    private String[] outputHandlerNames;
    private HashSet<Long> outputHandlerCallbackIds;
    private long inputPhaseCallbackId;
    private long initAgentCallbackId;
    
    private boolean connected;
	
	public AgentConnector(SoarClient client){
        this.soarClient = client;
        
        this.outputHandlerCallbackIds = new HashSet<Long>();
        this.outputHandlerNames = new String[]{};
        this.initAgentCallbackId = 0;

        this.connected = false;
	}

	/**********************************
	 * Methods to override
	 *********************************/
    
	// This method is called each input phase
    protected abstract void onInputPhase(Identifier inputLink);
    
	// This method is called when an output event happens, 
	// With (<output-link> ^attName <id>) given as input
	// (Need to use setOutputHandlerNames method to register for these events)
    protected abstract void onOutputEvent(String attName, Identifier id);
	
	// This method is called when init-soar is called, 
	// The agent MUST release any SML objects (Identifiers, Elements, etc.)
	protected abstract void onInitSoar();
	
	// This method registers for output events for each command name given in the array
	protected void setOutputHandlerNames(String[] outputHandlerNames){
		this.outputHandlerNames = outputHandlerNames;
	}
	
	public void connect(){
		if(connected){
			return;
		}
		for(String name : outputHandlerNames){
			outputHandlerCallbackIds.add(soarClient.getAgent().AddOutputHandler(name, this, null));
		}

		inputPhaseCallbackId = soarClient.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
		
		initAgentCallbackId = soarClient.getAgent().GetKernel().RegisterForAgentEvent(
				smlAgentEventId.smlEVENT_BEFORE_AGENT_REINITIALIZED, this, null);
		
		connected = true;
	}
	
	public void disconnect(){
		if(!connected){
			return;
		}
		
		for(Long callbackId : outputHandlerCallbackIds){
			soarClient.getAgent().RemoveOutputHandler(callbackId);
		}
		outputHandlerCallbackIds.clear();

		soarClient.getAgent().UnregisterForRunEvent(inputPhaseCallbackId);
		inputPhaseCallbackId = 0;
		
		soarClient.getAgent().GetKernel().UnregisterForAgentEvent(initAgentCallbackId);
		initAgentCallbackId = 0;

		connected = false;
	}
	
	// Handling an input
    public void runEventHandler(int eventID, Object data, Agent agent, int phase){
    	if(eventID == smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE.swigValue()){
    		onInputPhase(agent.GetInputLink());
    	}
    }

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

    // Handling an init-soar
	public void agentEventHandler(int agentEventId, Object data, String info) {
		onInitSoar();
	}
}
