import sys
import os

from tkinter import Tk

from rosie import RosieGUI
from mobilesim.rosie import MobileSimAgent

def launch_gui(rosie_home):
    AGENT_NAME = "blending3"

    agent_file = rosie_home + "/python/rosie/evaluation/" + AGENT_NAME + "/agent/rosie." + AGENT_NAME + ".config"

    root = Tk()
    eval_agent = MobileSimAgent(agent_file)
    eval_gui = RosieGUI(eval_agent, master=root)
    eval_gui.run()

def main():
    # Lookup $ROSIE_HOME
    rosie_home = ""
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    launch_gui(rosie_home)

if __name__ == "__main__":
    main()
