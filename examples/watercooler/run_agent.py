from tkinter import *
import tkinter.font

import sys
import os

from rosie import RosieGUI
from rosie.testing import TestAgent
from mobilesim.rosie import MobileSimAgent

def main():
    # Lookup $ROSIE_HOME
    rosie_home = ""
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    AGENT_NAME = "watercooler"
    agent_dir = rosie_home + "/examples/" + AGENT_NAME
    rosie_config = agent_dir + "/agent/rosie." + AGENT_NAME + ".config"

    if "--test" in sys.argv:
        run_test(rosie_config)
    else:
        launch_gui(rosie_config, var_num)

def launch_gui(rosie_config):
    root = Tk()
    rosie_agent = MobileSimAgent(rosie_config)
    rosie_agent.messages.append("!CMD cli pc -f")
    rosie_gui = RosieGUI(rosie_agent, master=root)
    rosie_gui.run()

def run_test(rosie_config):
    rosie_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0)
    rosie_agent.run_test('correct-output.txt')


main()

