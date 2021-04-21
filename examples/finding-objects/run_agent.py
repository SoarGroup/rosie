from tkinter import *
import tkinter.font

import sys
import os

from pysoarlib import AgentConnector
from rosie import RosieGUI
from rosie.testing import TestAgent
from mobilesim.rosie import MobileSimAgent

def main():
    # Lookup $ROSIE_HOME
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    AGENT_NAME = "finding-objects"
    agent_dir = rosie_home + "/examples/" + AGENT_NAME
    rosie_config = agent_dir + "/agent/rosie." + AGENT_NAME + ".config"

    if "--test" in sys.argv:
        run_test(rosie_config)
    else:
        launch_gui(rosie_config)

class FindObjConnector(AgentConnector):
    def __init__(self, agent, rosie_gui):
        AgentConnector.__init__(self, agent)
        self.add_output_command("find-result")
        self.rosie_gui = rosie_gui

    def on_output_event(self, command_name, root_id):
        if command_name == "find-result":
            obj_handle = root_id.GetChildString("object-handle")
            status = root_id.GetChildString("status")
            message = "# Find result for " + obj_handle + " = " + status
            self.rosie_gui.messages_list.add(message)

def launch_gui(rosie_config):
    root = Tk()
    rosie_agent = MobileSimAgent(rosie_config)
    rosie_gui = RosieGUI(rosie_agent, master=root)
    rosie_agent.add_connector("find_test", FindObjConnector(rosie_agent, rosie_gui))
    rosie_gui.run()

def run_test(rosie_config):
    rosie_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0)
    rosie_agent.add_agent_param("print-perception", "false")
    rosie_agent.add_agent_param("print-parsed-messages", "false")
    rosie_agent.run_test('correct-output.txt')

main()
