""" Used by the Rosie Agent to print information about the tasks Rosie is performing """
import sys
import traceback

from string import digits
from rosie.tools import task_to_str
from pysoarlib import AgentConnector

class ActionStackConnector(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)

        self.add_output_command("started-task")
        self.add_output_command("completed-task")

        self.callbacks = []

    def register_task_change_callback(self, callback):
        self.callbacks.append(callback)

    def dispatch_task_change(self, change_info):
        for callback in self.callbacks:
            callback(change_info)

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
            self.dispatch_task_change(padding + "> " + task_to_str(task_id))
        except:
            self.print_handler("Error Parsing Started Task")
            self.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")

    def process_completed_task(self, root_id):
        try:
            seg_id = root_id.GetChildId("segment")
            padding = "  " * (seg_id.GetChildInt("depth") - 1)
            task_id = seg_id.GetChildId("task-operator")
            self.dispatch_task_change(padding + "< " + task_to_str(task_id))
        except:
            self.print_handler("Error Parsing Completed Task")
            self.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")
