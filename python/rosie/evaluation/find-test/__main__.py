from tkinter import *
import tkinter.font

import sys
import os

from rosie.evaluation import EvaluationGUI, EvaluationAgent
from pysoarlib import AgentConnector

# Lookup $ROSIE_HOME
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)

agent_file = rosie_home + "/python/rosie/evaluation/find-test/agent/rosie.find-test.config"

class FindTestConnector(AgentConnector):
    def __init__(self, agent, result_handler):
        AgentConnector.__init__(self, agent)
        self.result_handler = result_handler
        self.add_output_command("find-result")

    def on_output_event(self, command_name, root_id):
        if command_name == "find-result":
            obj_handle = root_id.GetChildString("object-handle")
            status = root_id.GetChildString("status")
            self.result_handler(obj_handle, status)

class FindEvaluationAgent(EvaluationAgent):
    def handle_find_request(self, msg):
        # Disable default behavior and use the script to answer a find request instead
        self.advance_script()

class FindEvaluationGUI(EvaluationGUI):
    def init_soar_agent(self, config_file):
        self.agent = FindEvaluationAgent(self, config_filename=config_file)
        # Add a printout showing the result of each task
        self.agent.add_connector("find_test", FindTestConnector(self.agent, 
            lambda obj, res: self.append_message("# Find result for " + obj + " = " + res)))



root = Tk()
eval_gui = FindEvaluationGUI(agent_file, master=root)
root.protocol("WM_DELETE_WINDOW", eval_gui.on_exit)
root.mainloop()
