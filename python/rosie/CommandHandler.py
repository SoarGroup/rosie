""" This is an Abstract Base Class that outlines how commands to Rosie are handled 

These are commands that are not sentences but can modify the simulator/internal world for example """
import sys

from pysoarlib import WMInterface, AgentConnector

class CommandHandler(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.callback = None
        self.command = None
        self.wait_dcs = -1

    def handle_command(self, command, callback=None):
        self.command = command
        self.callback = callback

    def on_input_phase(self, input_link):
        if self.command is not None:
            self._execute_command()
            self.command = None
            self.callback = None

        if self.wait_dcs >= 0:
            self.wait_dcs -= 1
            if self.wait_dcs == 0 and self.callback is not None:
                self.callback("success")


    def _execute_command(self):
        args = self.command.split()
        cmd_name = args[0].upper()
        if cmd_name[0] == '!':
            # Strip ! off front if present
            cmd_name = cmd_name[1:]

        # START
        if cmd_name == 'START':
            self.agent.start()
        # STOP
        elif cmd_name == 'STOP':
            self.agent.stop()
        # WAIT <dcs>
        #   will wait the given number of decision cycles before reporting success
        elif cmd_name == 'WAIT':
            self.wait_dcs = int(args[1])
        # CLI arg1 arg2 ...
        #   will execute a soar command (e.g. "CLI p s1 -d 2")
        elif cmd_name == 'CLI':
            self.agent.execute_command(' '.join(args[1:]), print_res=True)
            if self.callback is not None:
                self.callback("success")
                self.callback = None

        ### CHANGING PREDICATES
        # SET-STATE <obj-h> <prop-h> <pred-h>
        elif cmd_name == 'SET-STATE':
            self._handle_set_pred_command(args[1], args[2], args[3])
        # OPEN <obj-h> 
        elif cmd_name == 'OPEN':
            self._handle_set_pred_command(args[1], 'is-open1', 'open2')
        # CLOSE <obj-h> 
        elif cmd_name == 'CLOSE':
            self._handle_set_pred_command(args[1], 'is-open1', 'not-open1')
        # ON <obj-h> 
        elif cmd_name == 'ON':
            self._handle_set_pred_command(args[1], 'is-activated1', 'activated1')
        # OFF <obj-h> 
        elif cmd_name == 'OFF':
            self._handle_set_pred_command(args[1], 'is-activated1', 'not-activated1')

        ### MOVING OBJECTS
        # PLACE <obj-h> <rel> <dest-h>
        elif cmd_name == 'PLACE':
            self._handle_place_command(args[1], args[2], args[3])
        # MOVE <obj-h> <wph>
        #    Move the given object to the given waypoint
        elif cmd_name == 'MOVE':
            self._handle_move_command(args[1], args[2])
        # TELEPORT <obj-h> <x> <y> <z> <wph>?
        #    Move the given object to the given coordinate (with optional waypoint given)
        elif cmd_name == 'TELEPORT':
            self._handle_move_command(args[1], float(args[2]), float(args[3]), float(args[4]), args[5] if len(args) > 5 else None)

        ### COMMAND NOT RECOGNIZED
        elif self.callback is not None:
            self.callback("failure")

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        pass

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        pass

    def _handle_move_command(self, obj_id, wp_handle):
        pass

    def _handle_teleport_command(self, obj_id, x, y, z, wp_handle):
        pass
