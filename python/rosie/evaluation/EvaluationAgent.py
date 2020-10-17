import sys
import lcm
import string
import time

import Python_sml_ClientInterface as sml

from mobilesim.rosie import *

strip_digits = lambda s: s.translate(str.maketrans('', '', string.digits))
current_time_ms = lambda: int(round(time.time() * 1000))

class EvaluationAgent(MobileSimAgent):
    def __init__(self, eval_gui, config_filename=None, **kwargs):
        MobileSimAgent.__init__(self, config_filename=config_filename, use_script_connector=True, **kwargs)
        self.eval_gui = eval_gui

        self.get_connector("language").register_message_callback(lambda msg: self.handle_message(msg))

    def connect(self):
        super().connect()
        script_conn = self.get_connector("script")
        script_conn.register_script_callback(lambda msg: self.script_callback(msg))
        self.eval_gui.set_script(script_conn.script)

    def script_callback(self, msg):
        self.eval_gui.append_message(msg, True)
        self.eval_gui.advance_script()

    def handle_message(self, msg):
        self.eval_gui.append_message(msg)

