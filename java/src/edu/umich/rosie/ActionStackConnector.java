package edu.umich.rosie;

import java.util.ArrayList;

import javax.swing.JMenuBar;

import edu.umich.rosie.language.IMessagePasser.RosieMessage;
import edu.umich.rosie.soar.AgentConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.soarobjects.Message;
import sml.Identifier;
import sml.WMElement;


public class ActionStackConnector extends AgentConnector {
	public interface OutputCallback {
		void dispatch(String message);
	}

	private ArrayList<OutputCallback> callbacks;
	
	public ActionStackConnector(SoarAgent agent){
		super(agent);
		
        this.setOutputHandlerNames(new String[]{ "started-task", "completed-task" });
		this.callbacks = new ArrayList<OutputCallback>();
	}

	public void register(OutputCallback callback){
		this.callbacks.add(callback);
	}
	
	@Override
	protected void onInitSoar(){ }

	@Override
	protected void createMenu(JMenuBar menuBar){ }

	@Override
    protected void onInputPhase(Identifier inputLink){ }

	private void dispatchOutput(String message){
		for(OutputCallback callback : this.callbacks){
			callback.dispatch(message);
		}
	}

    @Override
    protected void onOutputEvent(String attName, Identifier id){
    	if (attName.equals("started-task")){
    		processStartedTask(id);
    	}
    	if (attName.equals("completed-task")){
    		processCompletedTask(id);
    	}
    }

	private String padding(int n){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n; i += 1){
			sb.append("  ");
		}
		return sb.toString();
	}

	private void processStartedTask(Identifier rootId) {
		Identifier segId = SoarUtil.getChildId(rootId, "segment");
		int depth = SoarUtil.getChildInt(segId, "depth").intValue();
		Identifier taskId = SoarUtil.getChildId(segId, "task-operator");
		dispatchOutput(padding(depth-1) + "> " + taskToString(taskId));
		rootId.CreateStringWME("handled", "true");
	}

	private void processCompletedTask(Identifier rootId) {
		Identifier segId = SoarUtil.getChildId(rootId, "segment");
		int depth = SoarUtil.getChildInt(segId, "depth").intValue();
		Identifier taskId = SoarUtil.getChildId(segId, "task-operator");
		dispatchOutput(padding(depth-1) + "< " + taskToString(taskId));
		rootId.CreateStringWME("handled", "true");
	}

	private String taskToString(Identifier taskId) {
		String taskHandle = SoarUtil.getChildString(taskId, "task-handle");
		Identifier arg1id = SoarUtil.getChildId(taskId, "arg1");
		Identifier arg2id = SoarUtil.getChildId(taskId, "arg2");

		String task = taskHandle + "(";
		if (arg1id != null){
			task += taskArgToString(arg1id);
		}
		if (arg2id != null){
			if (arg1id != null){
				task += ", ";
			}
			task += taskArgToString(arg2id);
		}
		task += ")";

		return task;
	}

	private String taskArgToString(Identifier argId) {
		String argType = SoarUtil.getChildString(argId, "arg-type");
		if (argType.equals("object")) {
			return objArgToString(SoarUtil.getChildId(argId, "id"));
		} else if (argType.equals("partial-predicate")) {
			String handle_str = SoarUtil.getChildString(argId, "handle");
			String obj2_str = objArgToString(SoarUtil.getChildId(argId, "2"));
			return handle_str + "(" + obj2_str + ")";
		} else if (argType.equals("waypoint")) {
			Identifier wp_id = SoarUtil.getChildId(argId, "id");
			return SoarUtil.getChildString(wp_id, "handle");
		} else if (argType.equals("concept")) {
			return SoarUtil.getChildString(argId, "handle");
		}
		return "?";
	}

	private String objArgToString(Identifier objId) {
		Identifier preds_id = SoarUtil.getChildId(objId, "predicates");


		String[] words = new String[4];
		words[0] = SoarUtil.getChildString(preds_id, "size");
		words[1] = SoarUtil.getChildString(preds_id, "color");
		words[2] = SoarUtil.getChildString(objId, "root-category");
		words[3] = SoarUtil.getChildString(preds_id, "name");

		String s = "";
		for(String w : words){
			if(w != null){
				if(s.length() > 0){
					s += " ";
				}
				s += w;
			}
		}

		return s.replaceAll("\\d", "");
	}
}
