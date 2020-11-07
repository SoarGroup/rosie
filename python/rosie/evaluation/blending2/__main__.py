from tkinter import *
import tkinter.font

import sys
import os

from rosie import RosieGUI
from rosie.evaluation import EvaluationAgent

def launch_gui(var_num, rosie_home):
    """ var_num is an integer from 1-3 representing the agent variation to use """

    agent_file = rosie_home + "/python/rosie/evaluation/blending2/agent/rosie.blending2.config"

    root = Tk()
    var_script = "var" + str(var_num) + "-script.txt"
    eval_agent = EvaluationAgent(agent_file, messages_file=var_script)
    eval_gui = RosieGUI(eval_agent, master=root)

    # Source rules specific to the agent variation
    var_rules = "var" + str(var_num) + "-rules.soar"
    eval_agent.execute_command("source " + var_rules, print_res=True)
    
    eval_gui.run()

def main():
    # 1 argument - the agent variation number (1-3)
    var_num = 1
    if len(sys.argv) == 1:
        print("rosie.evaluation.blending2 <vnum>")
        print("   vnum - the agent variation, either 1, 2, or 3")
    else:
        try:
            arg1 = int(sys.argv[1])
            if arg1 >= 1 and arg1 <= 3:
                var_num = arg1
        except:
            pass

    # Lookup $ROSIE_HOME
    rosie_home = ""
    if "ROSIE_HOME" in os.environ:
        rosie_home = os.environ["ROSIE_HOME"]
    else:
        print("ERROR: Requires ROSIE_HOME environment variable set")
        return

    launch_gui(var_num, rosie_home)

if __name__ == "__main__":
    main()

