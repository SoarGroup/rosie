package edu.umich.rosie.connectors;

import java.util.HashSet;

import edu.umich.rosie.soar.AgentConnector;
import edu.umich.rosie.soar.SoarAgent;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.language.AgentMessageParser;
import sml.Identifier;


public class ActionStackConnector extends AgentConnector {
	public interface TaskEventListener {
		void taskEventHandler(String taskInfo);
	}

	private HashSet<TaskEventListener> listeners;
	
	public ActionStackConnector(SoarAgent agent){
		super(agent);
		
        this.setOutputHandlerNames(new String[]{ "started-task", "completed-task" });
		this.listeners = new HashSet<TaskEventListener>();
	}

	public void registerForTaskEvent(TaskEventListener listener){
		this.listeners.add(listener);
	}

	public void unregisterForTaskEvent(TaskEventListener listener){
		this.listeners.remove(listener);
	}

	@Override
    protected void onInitSoar() { }

	@Override
    protected void onInputPhase(Identifier inputLink){ }

	private void notifyListeners(String taskInfo){
		for(TaskEventListener listener : this.listeners){
			listener.taskEventHandler(taskInfo);
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
		notifyListeners(padding(depth-1) + "> " + taskToString(taskId));
		rootId.CreateStringWME("handled", "true");
	}

	private void processCompletedTask(Identifier rootId) {
		Identifier segId = SoarUtil.getChildId(rootId, "segment");
		int depth = SoarUtil.getChildInt(segId, "depth").intValue();
		Identifier taskId = SoarUtil.getChildId(segId, "task-operator");
		notifyListeners(padding(depth-1) + "< " + taskToString(taskId));
		rootId.CreateStringWME("handled", "true");
	}

	private String taskToString(Identifier taskId) {
		String taskHandle = SoarUtil.getChildString(taskId, "task-handle");
		Identifier arg1id = SoarUtil.getChildId(taskId, "arg1");
		Identifier arg2id = SoarUtil.getChildId(taskId, "arg2");
		Identifier arg3id = SoarUtil.getChildId(taskId, "arg3");

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
		if (arg3id != null){
			task += ", " + taskArgToString(arg3id);
		}
		task += ")";

		return task;
	}

	private String taskArgToString(Identifier argId) {
		String argType = SoarUtil.getChildString(argId, "arg-type");
		if (argType.equals("object")) {
			return AgentMessageParser.parseObject(SoarUtil.getChildId(argId, "id"));
		} else if (argType.equals("partial-predicate")) {
			String handle_str = SoarUtil.getChildString(argId, "handle");
			String obj2_str = AgentMessageParser.parseObject(SoarUtil.getChildId(argId, "2"));
			return handle_str + "(" + obj2_str + ")";
		} else if (argType.equals("waypoint")) {
			Identifier wp_id = SoarUtil.getChildId(argId, "id");
			return SoarUtil.getChildString(wp_id, "handle");
		} else if (argType.equals("concept")) {
			return SoarUtil.getChildString(argId, "handle");
		} else if (argType.equals("measure")){
			return SoarUtil.getChildString(argId, "number") + " " + 
					SoarUtil.getChildString(argId, "unit");
		}
		return "?";
	}
}
