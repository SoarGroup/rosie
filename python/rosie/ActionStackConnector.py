""" Used by the Rosie Agent to print information about the tasks Rosie is performing """
import sys
import traceback

from string import digits
from rosie.events import TaskStarted, TaskCompleted
from rosie.tools import task_to_str
from pysoarlib import AgentConnector

class ActionStackConnector(AgentConnector):
    def __init__(self, client):
        AgentConnector.__init__(self, client)
        self.client.add_agent_param("report-tasks-to-output-link", "true")
        self.add_output_command("started-task")
        self.add_output_command("completed-task")

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
            depth = seg_id.GetChildInt("depth")
            task_id = seg_id.GetChildId("task-operator")
            self.client.dispatch_event(TaskStarted(task_to_str(task_id), depth))
        except:
            self.client.print_handler("Error Parsing Starteded Task")
            self.client.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")

    def process_completed_task(self, root_id):
        try:
            seg_id = root_id.GetChildId("segment")
            depth = seg_id.GetChildInt("depth")
            task_id = seg_id.GetChildId("task-operator")
            self.client.dispatch_event(TaskCompleted(task_to_str(task_id), depth))
        except:
            self.client.print_handler("Error Parsing Completed Task")
            self.client.print_handler(traceback.format_exc())
        root_id.CreateStringWME("handled", "true")
