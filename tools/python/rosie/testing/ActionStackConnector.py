""" Used by the Rosie Agent to print information about the tasks Rosie is performing """
import sys
import traceback

from string import digits
from pysoarlib import AgentConnector


def task_to_string(task_id):
    task_handle = task_id.GetChildString("task-handle")
    arg1_id = task_id.GetChildId("arg1")
    arg2_id = task_id.GetChildId("arg2")

    task = task_handle + "("
    if arg1_id != None:
        task += task_arg_to_string(arg1_id)
    if arg2_id != None:
        if arg1_id != None:
            task += ", "
        task += task_arg_to_string(arg2_id)
    task += ")"

    return task

def task_arg_to_string(arg_id):
    arg_type = arg_id.GetChildString("arg-type")
    if arg_type == "object":
        return obj_arg_to_string(arg_id.GetChildId("id"))
    elif arg_type == "partial-predicate":
        handle_str = arg_id.GetChildString("handle")
        obj2_str = obj_arg_to_string(arg_id.GetChildId("2"))
        return "%s(%s)" % ( handle_str, obj2_str )
    elif arg_type == "waypoint":
        wp_id = arg_id.GetChildId("id")
        return wp_id.GetChildString("handle")
    elif arg_type == "concept":
        return arg_id.GetChildString("handle")
    return "?"

def obj_arg_to_string(obj_id):
    preds_id = obj_id.GetChildId("predicates")
    words = []
    words.append(preds_id.GetChildString('size'))
    words.append(preds_id.GetChildString('color'))
    words.append(obj_id.GetChildString('root-category'))
    words.append(preds_id.GetChildString('name'))
    obj_desc = ' '.join(w for w in words if w is not None)

    return obj_desc.translate(str.maketrans('', '', digits))

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
