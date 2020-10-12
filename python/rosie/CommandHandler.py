""" This is an Abstract Base Class that outlines how commands to Rosie are handled 

These are commands that are not sentences but can modify the simulator/internal world for example """
import sys

from pysoarlib import WMInterface, AgentConnector

class CommandHandler(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.callback = None

    def handle_command(self, command, callback=None):
        self.callback = callback
        args = command.split()

        # TELEPORT <obj-h> <x> <y> <z> <wph>
        #    Move the given object to the given waypoint
        if args[0] == 'TELEPORT':
            self._handle_move_command(int(args[1]), float(args[2]), float(args[3]), float(args[4]), args[5])
        # PLACE <obj-h> <rel> <dest-h>
        elif args[0] == 'PLACE':
            self._handle_place_command(int(args[1]), args[2], int(args[3]))
        # OPEN <obj-h> 
        elif args[0] == 'OPEN':
            self._handle_set_pred_command(int(args[1]), 'is-open1', 'open2')
        # CLOSE <obj-h> 
        elif args[0] == 'CLOSE':
            self._handle_set_pred_command(int(args[1]), 'is-open1', 'not-open1')

    def _handle_move_command(self, obj_id, x, y, z, wp_handle):
        pass

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        pass

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        pass
