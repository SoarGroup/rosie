""" Used to handle commands that modify the internal world """
import sys

from pysoarlib import WMInterface, AgentConnector
from .CommandHandler import CommandHandler

class InternalCommandHandler(CommandHandler):
    def __init__(self, agent, print_handler=None):
        CommandHandler.__init__(self, agent, print_handler)
        self.add_output_command("handled-world-change")
        self.cur_command_id = None
        self.command_num = 0

    def on_init_soar(self):
        super().on_init_soar()
        if self.cur_command_id is not None:
            self.cur_command_id.DestroyWME()
            self.cur_command_id = None

    def on_output_event(self, command_name, root_id):
        if command_name == "handled-world-change":
            self.send_next_message = True
            if self.cur_command_id is not None:
                self.cur_command_id.DestroyWME()
                self.cur_command_id = None
            root_id.CreateStringWME("status", "complete")
            if self.callback is not None:
                self.callback()
                self.callback = None

    def _handle_move_command(self, obj_id, x, y, z, wp_handle):
        input_link = self.agent.agent.GetInputLink()
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "move")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("waypoint-handle", wp_handle)

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        input_link = self.agent.agent.GetInputLink()
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "place")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("relation-handle", rel_handle)
        self.cur_command_id.CreateIntWME("destination-id", dest_id)

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        input_link = self.agent.agent.GetInputLink()
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "predicate")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("property-handle", prop_handle)
        self.cur_command_id.CreateStringWME("predicate-handle", pred_handle)

