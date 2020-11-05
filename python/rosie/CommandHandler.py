""" This is an Abstract Base Class that outlines how commands to Rosie are handled 

These are commands that are not sentences but can modify the simulator/internal world for example """
import sys
import time

from pysoarlib import WMInterface, AgentConnector

current_time_ms = lambda: int(round(time.time() * 1000))

class CommandHandler(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.callback = None
        self.command = None
        self.wait_time = 0

    def handle_command(self, command, callback=None):
        self.command = command
        self.callback = callback

    def on_input_phase(self, input_link):
        if self.command is not None:
            self._execute_command()
            self.command = None

        if self.wait_time > 0:
            if current_time_ms() > self.wait_time:
                self._evoke_callback("success")
                self.wait_time = 0

    def _evoke_callback(self, message):
        if self.callback is not None:
            self.callback(message)
            self.callback = None

    def _execute_command(self):
        args = self.command.split()
        if args[0] == '!CMD':
            args = args[1:]
        cmd_name = args[0].lower()

        # START
        if cmd_name == 'start':
            self.agent.start()
        # STOP
        elif cmd_name == 'stop':
            self.agent.stop()
        # WAIT <secs>
        #   will wait the given number of seconds before reporting success
        elif cmd_name == 'wait':
            self.wait_time = current_time_ms() + 1000*int(args[1])
        # CLI arg1 arg2 ...
        #   will execute a soar command (e.g. "CLI p s1 -d 2")
        elif cmd_name == 'cli':
            self.agent.execute_command(' '.join(args[1:]), print_res=True)
            self._evoke_callback("success")
        # IGNORE/SKIP
        #   will ignore what Rosie said
        elif cmd_name == 'ignore' or cmd_name == 'skip':
            pass

        ### CHANGING PREDICATES
        # SET-STATE <obj-h> <prop-h> <pred-h>
        elif cmd_name == 'set-state':
            self._handle_set_pred_command(args[1], args[2], args[3])
        # OPEN <obj-h> 
        elif cmd_name == 'open':
            self._handle_set_pred_command(args[1], 'is-open1', 'open2')
        # CLOSE <obj-h> 
        elif cmd_name == 'close':
            self._handle_set_pred_command(args[1], 'is-open1', 'not-open1')
        # ON <obj-h> 
        elif cmd_name == 'on':
            self._handle_set_pred_command(args[1], 'is-activated1', 'activated1')
        # OFF <obj-h> 
        elif cmd_name == 'off':
            self._handle_set_pred_command(args[1], 'is-activated1', 'not-activated1')

        ### MOVING OBJECTS
        # PLACE <obj-h> <rel> <dest-h>
        elif cmd_name == 'place':
            self._handle_place_command(args[1], args[2], args[3])
        # MOVE <obj-h> <wph>
        #    Move the given object to the given waypoint
        elif cmd_name == 'move':
            self._handle_move_command(args[1], args[2])
        # TELEPORT <obj-h> <x> <y> <z> <wph>?
        #    Move the given object to the given coordinate (with optional waypoint given)
        elif cmd_name == 'teleport':
            self._handle_teleport_command(args[1], float(args[2]), float(args[3]), float(args[4]), args[5] if len(args) > 5 else None)

        ### COMMAND NOT RECOGNIZED
        else:
            self._evoke_callback("failure")

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        pass

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        pass

    def _handle_move_command(self, obj_id, wp_handle):
        pass

    def _handle_teleport_command(self, obj_id, x, y, z, wp_handle):
        pass
