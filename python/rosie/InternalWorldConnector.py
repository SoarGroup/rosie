""" Used in the Rosie project to send commands that modify the internal world """
import sys

from pysoarlib import WMInterface, AgentConnector

class InternalWorldConnector(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.add_output_command("handled-world-change")
        self.cur_command_id = None
        self.command_num = 0
        self.queued_commands = []

    def on_init_soar(self):
        if self.cur_command_id is None:
            self.cur_command_id.DestroyWME()
            self.cur_command_id = None

    def handle_script_command(self, command):
        self.queued_commands.append(command)

    def on_input_phase(self, input_link):
        if self.cur_command_id is None and len(self.queued_commands) > 0:
            self._send_next_command(input_link)

    def _send_next_command(self, input_link):
        command = self.queued_commands[0]
        self.queued_commands = self.queued_commands[1:]

        args = command.split()

        # TELEPORT <obj-h> <x> <y> <z> <wph>
        if args[0] == 'TELEPORT':
            self._send_move_command(input_link, int(args[1]), args[5])
        # PLACE <obj-h> <rel> <dest-h>
        elif args[0] == 'PLACE':
            self._send_place_command(input_link, int(args[1]), args[2], int(args[3]))
        # OPEN <obj-h> 
        elif args[0] == 'OPEN':
            self._send_set_predicate_command(input_link, int(args[1]), 'is-open1', 'open2')
        # CLOSE <obj-h> 
        elif args[0] == 'CLOSE':
            self._send_set_predicate_command(input_link, int(args[1]), 'is-open1', 'not-open1')

    def on_output_event(self, command_name, root_id):
        if command_name == "handled-world-change":
            self._process_handled_message(root_id)

    def _process_handled_message(self, root_id):
        if self.cur_command_id is not None:
            self.cur_command_id.DestroyWME()
            self.cur_command_id = None
        root_id.CreateStringWME("status", "complete")

        if len(self.queued_commands) > 0:
            self._send_next_command()

    def _send_move_command(self, input_link, obj_id, wp_handle):
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "move")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("waypoint-handle", wp_handle)

    def _send_place_command(self, input_link, obj_id, rel_handle, dest_id):
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "place")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("relation-handle", rel_handle)
        self.cur_command_id.CreateIntWME("destination-id", dest_id)

    def _send_set_predicate_command(self, input_link, obj_id, prop_handle, pred_handle):
        self.cur_command_id = input_link.CreateIdWME("change-world")
        self.command_num += 1
        self.cur_command_id.CreateIntWME("num", self.command_num)
        self.cur_command_id.CreateStringWME("type", "predicate")
        self.cur_command_id.CreateIntWME("object-id", obj_id)
        self.cur_command_id.CreateStringWME("property-handle", prop_handle)
        self.cur_command_id.CreateStringWME("predicate-handle", pred_handle)

