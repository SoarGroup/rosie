from tkinter import *
import tkinter.font

import sys
import os

from rosie.evaluation import EvaluationGUI

### Argument 1 is an integer representing the agent variation
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

print("Running agent variation " + str(var_num))

# Lookup $ROSIE_HOME
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)

agent_file = rosie_home + "/python/rosie/evaluation/blending2/agent/rosie.blending2.config"

root = Tk()
var_script = "var" + str(var_num) + "-script.txt"
eval_gui = EvaluationGUI(agent_file, agent_kwargs={ 'messages_file': var_script }, master=root)

var_rules = "var" + str(var_num) + "-rules.soar"
eval_gui.agent.execute_command("source " + var_rules, print_res=True)
root.protocol("WM_DELETE_WINDOW", eval_gui.on_exit)
root.mainloop()

