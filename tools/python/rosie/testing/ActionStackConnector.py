""" Used by the Rosie Agent to print information about the tasks Rosie is performing """
import sys
import traceback

from string import digits
from pysoarlib import AgentConnector, RosieMessageParser


def task_to_string(task_id):
    task_handle = task_id.GetChildString("task-handle")

    # Get the root identifier of each argument
    arg_ids = ( task_id.GetChildId(arg_name) for arg_name in ("arg1", "arg2", "arg3", "when-clause") )
    # Convert each non-null argument to a string
    parsed_args = ( task_arg_to_string(arg_id) for arg_id in arg_ids if arg_id is not None)

    return task_handle + "(" + ", ".join(parsed_args) + ")"

def task_arg_to_string(arg_id):
    arg_type = arg_id.GetChildString("arg-type")
    if arg_type == "object":
        return RosieMessageParser.parse_obj(arg_id.GetChildId("id"))
    elif arg_type == "partial-predicate":
        handle_str = arg_id.GetChildString("handle")
        obj2_str = RosieMessageParser.parse_obj(arg_id.GetChildId("2"))
        return "%s(%s)" % ( handle_str, obj2_str )
    elif arg_type == "waypoint":
        wp_id = arg_id.GetChildId("id")
        return wp_id.GetChildString("handle")
    elif arg_type == "concept":
        return arg_id.GetChildString("handle")
    elif arg_type == "measure":
        return arg_id.GetChildString("number") + " " + arg_id.GetChildString("unit")
    elif arg_type == "when-clause":
        return "when" + pred_set_to_string(arg_id)
    return "?"

def pred_set_to_string(arg_id):
    num_preds = arg_id.GetChildInt("pred-count")
    parsed_preds = []
    for i in range(num_preds):
        pred = arg_id.GetChildId(str(i+1))
        parsed_preds.append(predicate_to_string(pred))
    return "{ " + ", ".join(parsed_preds) + " }"

def predicate_to_string(pred_id):
    pred_type = pred_id.GetChildString("type")
    pred_handle = pred_id.GetChildString("handle")
    if pred_type == "unary":
        obj1_str = RosieMessageParser.parse_obj(pred_id.GetChildId("1"))
        return "%s(%s)" % (pred_handle, obj1_str)
    elif pred_type == "relation":
        obj1_str = RosieMessageParser.parse_obj(pred_id.GetChildId("1"))
        obj2_str = RosieMessageParser.parse_obj(pred_id.GetChildId("2"))
        return "%s(%s, %s)" % (pred_handle, obj1_str, obj2_str)
    else:
        return "?"

class ActionStackConnector(AgentConnector):
    def __init__(self, agent):
        AgentConnector.__init__(self, agent)

        self.add_output_command("started-task")
        self.add_output_command("completed-task")

        self.callbacks = []

    def register_output_callback(self, callback):
        self.callbacks.append(callback)

    def dispatch_output(self, message):
        for callback in self.callbacks:
            callback(message)

    def on_init_soar(self):
        pass

    def on_input_phase(self, input_link):
        pass

    def on_output_event(self, command_name, root_id):
        if command_name == "started-task":
            self.process_started_task(root_id)
        elif command_name == "completed-task":
            self.process_completed_task(root_id)

    def process_started_task(self, root_id):
        try:
            seg_id = root_id.GetChildId("segment")
            padding = "  " * (seg_id.GetChildInt("depth") - 1)
            task_id = seg_id.GetChildId("task-operator")
            self.dispatch_output(padding + "> " + task_to_string(task_id))
        except:
            self.print_handler("Error Parsing Started Task")
            self.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")

    def process_completed_task(self, root_id):
        try:
            seg_id = root_id.GetChildId("segment")
            padding = "  " * (seg_id.GetChildInt("depth") - 1)
            task_id = seg_id.GetChildId("task-operator")
            self.dispatch_output(padding + "< " + task_to_string(task_id))
        except:
            self.print_handler("Error Parsing Completed Task")
            self.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")
