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

    AGENT_NAME = "template"
    agent_dir = rosie_home + "/examples/" + AGENT_NAME 

    if "--test" in sys.argv:
        rosie_config = agent_dir + "/test-agent/rosie." + AGENT_NAME + ".config"
        run_test(rosie_config)
    else:
        rosie_config = agent_dir + "/agent/rosie." + AGENT_NAME + ".config"
        launch_gui(rosie_config)

def launch_gui(rosie_config):
    root = Tk()
    rosie_agent = MobileSimAgent(rosie_config)
    rosie_gui = RosieGUI(rosie_agent, master=root)
    rosie_gui.run()

def run_test(rosie_config):
    rosie_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0)
    rosie_agent.run_test('correct-output.txt')


main()
