from tkinter import *
import tkinter.font

import sys
import os

from rosie.evaluation import EvaluationGUI

# ARG 1: Experiment Number from 1-3
e_num = 1
if len(sys.argv) >= 2 and sys.argv[1] in ['1', '2', '3']:
    e_num = int(sys.argv[1])

# Lookup $ROSIE_HOME
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)

agent_file = rosie_home + "/python/rosie/evaluation/formulations/agent/rosie.formulations.config"
script_file = rosie_home + "/python/rosie/evaluation/formulations/script" + str(e_num) + ".txt"

root = Tk()
eval_gui = EvaluationGUI(agent_file, agent_kwargs={ 'messages_file': script_file }, master=root)
root.protocol("WM_DELETE_WINDOW", eval_gui.on_exit)
root.mainloop()

