from tkinter import *
import tkinter.font

import sys
import os

from rosie.evaluation import EvaluationGUI

# Lookup $ROSIE_HOME
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)

agent_file = rosie_home + "/python/rosie/evaluation/rand-move/agent/rosie.rand-move.config"

root = Tk()
eval_gui = EvaluationGUI(agent_file, master=root)
if "lesioned" in sys.argv:
    eval_gui.agent.execute_command("source lesioned-agent-rules.soar", print_res=True)
root.protocol("WM_DELETE_WINDOW", eval_gui.on_exit)
root.mainloop()

