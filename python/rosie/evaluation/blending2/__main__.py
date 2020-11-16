from tkinter import *
import tkinter.font

import sys
import os

from rosie import RosieGUI
from rosie.testing import TestAgent
from mobilesim.rosie import MobileSimAgent

def launch_gui(rosie_config, var_num):
    """ var_num is an integer from 1-3 representing the agent variation to use """

    root = Tk()
    var_script = "variations/var" + str(var_num) + "-script.txt"
    eval_agent = MobileSimAgent(rosie_config, messages_file=var_script)
    eval_agent.messages.append("!CMD cli pc -f")
    eval_gui = RosieGUI(eval_agent, master=root)

    # Source rules specific to the agent variation
    var_rules = "variations/var" + str(var_num) + "-rules.soar"
    eval_agent.execute_command("source " + var_rules, print_res=True)
    
    eval_gui.run()

# Runs on the normal agent (variation 2)
def run_test(rosie_config):
    eval_agent = TestAgent(config_filename=rosie_config, write_to_stdout=True, source_output="summary",
            task_test_output_filename='output/test-output.txt', watch_level=0, 
            messages_file="variations/var2-script.txt")

    # Source rules specific to the agent variation
    eval_agent.execute_command("source variations/var2-rules.soar", print_res=True)

    eval_agent.run_test('correct-output.txt')

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

    rosie_config = rosie_home + "/python/rosie/evaluation/blending2/agent/rosie.blending2.config"

    if "--test" in sys.argv:
        run_test(rosie_config)
    else:
        launch_gui(rosie_config, var_num)

if __name__ == "__main__":
    main()

