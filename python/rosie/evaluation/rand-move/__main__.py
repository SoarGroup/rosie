import sys
import os

from tkinter import Tk

from rosie import RosieGUI
from mobilesim.rosie import MobileSimAgent

def launch_gui(rosie_config, lesioned):
    root = Tk()
    eval_agent = MobileSimAgent(rosie_config)
    eval_gui = RosieGUI(eval_agent, master=root)
    if "lesioned" in sys.argv:
        eval_agent.execute_command("source lesioned-agent-rules.soar", print_res=True)
    eval_gui.run()

def main():
    # Running with argument lesioned will disable learning in innate tasks
    lesioned = ("lesioned" in sys.argv)

    # Lookup $ROSIE_HOME
    rosie_home = ""
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    AGENT_NAME = "rand-move"
    rosie_config = rosie_home + "/python/rosie/evaluation/" + AGENT_NAME + "/agent/rosie." + AGENT_NAME + ".config"

    launch_gui(rosie_config, lesioned)

if __name__ == "__main__":
    main()
