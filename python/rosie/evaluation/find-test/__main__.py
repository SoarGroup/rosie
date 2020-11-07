from tkinter import *
import tkinter.font

import sys
import os

from pysoarlib import AgentConnector
from rosie import RosieGUI
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


def launch_gui(rosie_home):
    AGENT_NAME = "find-test"

    agent_file = rosie_home + "/python/rosie/evaluation/" + AGENT_NAME + "/agent/rosie." + AGENT_NAME + ".config"

    root = Tk()
    eval_agent = MobileSimAgent(agent_file)
    eval_gui = RosieGUI(eval_agent, master=root)
    eval_agent.add_connector("find_test", FindTestConnector(eval_agent, eval_gui))
    eval_gui.run()

def main():
    # Lookup $ROSIE_HOME
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    launch_gui(rosie_home)

if __name__ == "__main__":
    main()
