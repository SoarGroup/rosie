package edu.umich.rosie.connectors;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import edu.umich.rosie.soar.AgentConnector;
import edu.umich.rosie.soar.SoarClient;
import edu.umich.rosie.soar.SoarUtil;
import edu.umich.rosie.language.AgentMessageParser;
import sml.Identifier;


public class ActionStackConnector extends AgentConnector {
	public interface TaskEventListener {
		void taskEventHandler(String taskInfo);
	}

	private HashSet<TaskEventListener> listeners;
	
	public ActionStackConnector(SoarClient client){
		super(client);
		
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

	private String join(List<String> strings, String sep){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : strings){
			if(s == null){
				continue;
			}
			sb.append(s);
			if(!first){
				sb.append(sep);
			}
			first = false;
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
		final String[] ARG_NAMES = new String[]{ "arg1", "arg2", "arg3", "when-clause", "start-clause", "end-clause" };
		String taskHandle = SoarUtil.getChildString(taskId, "task-handle");

		StringBuilder task = new StringBuilder();
		task.append(taskHandle + "(");

		boolean first = true;
		for(String argName : ARG_NAMES){
			Identifier argId = SoarUtil.getChildId(taskId, argName);
			if(argId == null){
				continue;
			}
			if(!first){ task.append(", "); }
			first = false;
			task.append(taskArgToString(argName, argId));
		}
		task.append(")");

		return task.toString();
	}

	private String taskArgToString(String argName, Identifier argId) {
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
		} else if (argType.equals("temporal-clause")){
			return argName.split("-")[0] + predSetToString(argId);
		}
		return "?";
	}

	private String predSetToString(Identifier argId){
		Long numPreds = SoarUtil.getChildInt(argId, "pred-count");
		ArrayList<String> parsedPreds = new ArrayList<String>();
		for(Integer i = 1; i <= numPreds; i += 1){
			Identifier predId = SoarUtil.getChildId(argId, i.toString());
			parsedPreds.add(predicateToString(predId));
		}
		return "{ " + join(parsedPreds, ", ") + " }";
	}

	private String predicateToString(Identifier predId){
		String predType = SoarUtil.getChildString(predId, "type");
		String predHandle = SoarUtil.getChildString(predId, "handle");
		if(predType.equals("unary")){
			String obj1_str = AgentMessageParser.parseObject(SoarUtil.getChildId(predId, "1"));
			return predHandle + "(" + obj1_str + ")";
		} else if(predType.equals("relation")){
			String obj1_str = AgentMessageParser.parseObject(SoarUtil.getChildId(predId, "1"));
			String obj2_str = AgentMessageParser.parseObject(SoarUtil.getChildId(predId, "2"));
			return predHandle + "(" + obj1_str + ", " + obj2_str + ")";
		} else if(predType.equals("duration")){
			String number = SoarUtil.getChildString(predId, "number");
			String unit = SoarUtil.getChildString(predId, "unit");
			return number + " " + unit;
		} else if(predType.equals("clocktime")){
			String hour = SoarUtil.getChildString(predId, "hour");
			String minute = SoarUtil.getChildString(predId, "minute");
			return hour + ":" + minute;
		} else {
			return "?";
		}
	}

}
