import sys
import lcm
import string

import Python_sml_ClientInterface as sml

from pysoarlib import SoarAgent, LanguageConnector
from mobilesim.rosie import *

strip_digits = lambda s: s.translate(str.maketrans('', '', string.digits))

class EvaluationAgent(SoarAgent):
    def __init__(self, eval_gui, config_filename=None, **kwargs):
        SoarAgent.__init__(self, config_filename=config_filename, verbose=False, **kwargs)

        self.eval_gui = eval_gui

        self.lcm_conn = LCMConnector(self)
        self.connectors["lcm"] = self.lcm_conn

        self.language = LanguageConnector(self)
        self.language.register_message_callback(lambda msg: self.handle_message(msg))
        self.connectors["language"] = self.language

        self.actuation = MobileSimActuationConnector(self, self.lcm_conn.lcm)
        self.connectors["actuation"] = self.actuation

        self.perception = MobileSimPerceptionConnector(self, self.lcm_conn.lcm)
        self.connectors["perception"] = self.perception

        self.command_handler = AgentCommandConnector(self, self.lcm_conn.lcm)
        self.connectors["agent_cmd"] = self.command_handler

    def handle_message(self, msg):
        self.eval_gui.append_message(msg)
        if "new task" in msg:
            self.advance_script()
        elif "goal" in msg:
            self.advance_script()
        elif "can't find" in msg:
            self.handle_find_request(msg)

    def handle_find_request(self, msg):
        obj_cat = msg.split()[4]
        if obj_cat[-1] == ',':
            obj_cat = obj_cat[:-1]
        obj = self.perception.objects.get_object_by_cat(obj_cat)
        if obj is None:
            return
        container = self.perception.objects.get_object_container(obj)
        if container is None:
            return
        response = "The {} is in the {}.".format(obj.get_property("category"), container.get_property("category"))
        response = strip_digits(response)
        self.eval_gui.send_message_to_agent(response)

    def advance_script(self):
        message = self.eval_gui.get_next_script_message()
        if message[0] == '!':
            self.eval_gui.append_message(message)
            self.handle_script_command(message[1:])
            self.advance_script()
        else:
            self.eval_gui.send_message_to_agent(message)

    def handle_script_command(self, message):
        args = message.split()
        if args[0] == 'TELEPORT':
            self.handle_teleport_command(args)

    def handle_teleport_command(self, args):
        cl_params = { 'object-id': int(args[1]), 'destination': int(args[2]) }
        cond_test = ControlLawUtil.create_empty_condition_test("stabilized")
        control_law = ControlLawUtil.create_control_law("teleport-object", cl_params, cond_test)
        self.actuation.send_command(control_law)


