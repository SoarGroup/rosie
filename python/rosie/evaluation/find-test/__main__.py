from tkinter import *
import tkinter.font

import sys
import os

from pysoarlib import AgentConnector
from rosie import RosieGUI
from rosie.testing import TestAgent
from mobilesim.rosie import MobileSimAgent


class FindTestConnector(AgentConnector):
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
    eval_agent = MobileSimAgent(rosie_config)
    eval_gui = RosieGUI(eval_agent, master=root)
    eval_agent.add_connector("find_test", FindTestConnector(eval_agent, eval_gui))
    eval_gui.run()

def run_test(rosie_config):
    eval_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0)
    eval_agent.add_agent_param("print-perception", "false")
    eval_agent.add_agent_param("print-parsed-messages", "false")
    eval_agent.run_test('correct-output.txt')

def main():
    # Lookup $ROSIE_HOME
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    AGENT_NAME = "find-test"
    rosie_config = rosie_home + "/python/rosie/evaluation/" + AGENT_NAME + "/agent/rosie." + AGENT_NAME + ".config"

    if "--test" in sys.argv:
        run_test(rosie_config)
    else:
        launch_gui(rosie_config)

if __name__ == "__main__":
    main()
