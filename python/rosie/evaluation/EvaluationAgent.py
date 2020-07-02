import sys
import lcm
import string

import Python_sml_ClientInterface as sml

from rosie import RosieAgent, LanguageConnector
from mobilesim.rosie import *

strip_digits = lambda s: s.translate(str.maketrans('', '', string.digits))

class EvaluationAgent(RosieAgent):
    def __init__(self, eval_gui, config_filename=None, verbose=True, **kwargs):
        RosieAgent.__init__(self, config_filename=config_filename, **kwargs)

        self.eval_gui = eval_gui

        self.lcm_conn = LCMConnector(self)
        self.add_connector("lcm", self.lcm_conn)

        self.actuation = MobileSimActuationConnector(self, self.lcm_conn.lcm)
        self.add_connector("actuation", self.actuation)

        self.perception = MobileSimPerceptionConnector(self, self.lcm_conn.lcm)
        self.add_connector("perception", self.perception)

        self.command_handler = AgentCommandConnector(self, self.lcm_conn.lcm)
        self.add_connector("agent_cmd", self.command_handler)

        self.get_connector("language").register_message_callback(lambda msg: self.handle_message(msg))

    def handle_message(self, msg):
        self.eval_gui.append_message(msg)
        if "new task" in msg:
            self.advance_script()
        elif "goal" in msg:
            self.advance_script()
        elif "can't find" in msg:
            self.handle_find_request(msg)

    def handle_find_request(self, msg):
        obj_cat = next(w for w in msg.split() if w[-1] == ',')
        obj_cat = obj_cat.replace(',', '')
        obj = self.perception.objects.get_object_by_cat(obj_cat)
        container = self.perception.objects.get_object_container(obj)
        response = "Unknown."
        if container is not None:
            response = "The {} is in the {}.".format(obj.get_property("category"), container.get_property("category"))
            response = strip_digits(response)
        self.eval_gui.send_message_to_agent(response)

    def advance_script(self):
        message = self.eval_gui.get_next_script_message()
        if message is None:
            return
        if message[0] == '!':
            self.eval_gui.append_message(message)
            self.handle_script_command(message[1:])
            self.advance_script()
        else:
            self.eval_gui.send_message_to_agent(message)

    def handle_script_command(self, message):
        args = message.split()
        # TELEPORT <obj-h> <x> <y> <z>
        if args[0] == 'TELEPORT':
            self.handle_teleport_command(args)
        # PLACE <obj-h> <rel> <dest-h>
        elif args[0] == 'PLACE':
            self.handle_place_command(args)

    def handle_teleport_command(self, args):
        cl_params = { 'object-id': int(args[1]), 'x': float(args[2]), 'y': float(args[3]), 'z': float(args[4]) }
        cond_test = ControlLawUtil.create_empty_condition_test("stabilized")
        control_law = ControlLawUtil.create_control_law("put-at-xyz", cl_params, cond_test)
        self.actuation.queue_command(control_law)

    def handle_place_command(self, args):
        cl_params = { 'object-id': int(args[1]), 'relation': args[2], 'destination-id': int(args[3]) }
        cond_test = ControlLawUtil.create_empty_condition_test("stabilized")
        control_law = ControlLawUtil.create_control_law("put-on-object", cl_params, cond_test)
        self.actuation.queue_command(control_law)


