from tkinter import *
import tkinter.font

import sys
import os

from pysoarlib import AgentConnector
from rosie import RosieGUI
from rosie.testing import TestAgent
from mobilesim.rosie import MobileSimAgent


def launch_gui(rosie_config):
    root = Tk()
    eval_agent = MobileSimAgent(rosie_config)
    eval_agent.messages.append("!CMD cli pc -f")
    eval_gui = RosieGUI(eval_agent, master=root)
    eval_gui.run()

def run_test(rosie_config):
    eval_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0)
    eval_agent.run_test('correct-output.txt')

def main():
    # Lookup $ROSIE_HOME
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    AGENT_NAME = "blending3"
    rosie_config = rosie_home + "/python/rosie/evaluation/" + AGENT_NAME + "/agent/rosie." + AGENT_NAME + ".config"

    if "--test" in sys.argv:
        run_test(rosie_config)
    else:
        launch_gui(rosie_config)

if __name__ == "__main__":
    main()
