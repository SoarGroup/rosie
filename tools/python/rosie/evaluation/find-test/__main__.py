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

agent_file = rosie_home + "/tools/python/rosie/evaluation/find-test/agent/rosie.find-test.config"

root = Tk()
eval_gui = EvaluationGUI(agent_file, master=root)
root.protocol("WM_DELETE_WINDOW", eval_gui.on_exit)
root.mainloop()
