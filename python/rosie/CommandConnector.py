""" This is the default connector that can handle special agent commands

These commands may be useful for scripting, change the world state, or printing information """
import sys
import time

from pysoarlib import WMInterface, AgentConnector
from rosie.events import CommandHandled

current_time_ms = lambda: int(round(time.time() * 1000))

class CommandConnector(AgentConnector):
    def __init__(self, client):
        AgentConnector.__init__(self, client)
        self.queued_command = None
        self.active_command = None
        self.wait_time = 0
        self.wait_steps = 0

        self.add_output_command("handled-command")

    def handle_command(self, command):
        self.queued_command = command

    def on_output_event(self, command_name, root_id):
        if command_name == "handled-command":
            self.client.dispatch_event(CommandHandled(root_id.GetChildString("command")))
            root_id.CreateStringWME("handled", "true")

    def on_input_phase(self, input_link):
        if self.queued_command is not None:
            self._execute_command(self.queued_command)
            self.queued_command = None

        if self.wait_time > 0:
            if current_time_ms() > self.wait_time:
                self.complete_command()
                self.wait_time = 0

        if self.wait_steps > 0:
            self.wait_steps -= 1
            if self.wait_steps == 0:
                self.complete_command()

    def complete_command(self):
        if self.active_command is not None:
            command = self.active_command
            self.active_command = None
            self.client.dispatch_event(CommandHandled(command))

    def _execute_command(self, command):
        self.active_command = command
        args = command.split()
        if args[0] == '!CMD':
            args = args[1:]
        cmd_name = args[0].lower()

        complete_now = False


        # START
        if cmd_name == 'start':
            self.client.start()
            complete_now = True
        # STOP
        elif cmd_name == 'stop':
            self.client.stop()
            complete_now = True
        # WAIT <secs>
        #   will wait the given number of seconds before reporting success
        elif cmd_name == 'wait':
            self.wait_time = current_time_ms() + 1000*int(args[1])
        # WAIT_DC <dcs>
        #   will wait the given number of cycles before reporting success
        elif cmd_name == 'wait_dc':
            self.wait_steps = int(args[1])
        # CLI arg1 arg2 ...
        #   will execute a soar command (e.g. "CLI p s1 -d 2")
        elif cmd_name == 'cli':
            self.client.execute_command(' '.join(args[1:]), print_res=True)
            complete_now = True
        # IGNORE/SKIP
        #   will ignore what Rosie said
        elif cmd_name == 'ignore' or cmd_name == 'skip':
            return

        # SET-TIME <H> <MIN>
        #   will set the input-link clock to the specific time
        elif cmd_name == 'set-time':
            self.client.get_connector('time').set_time(int(args[1]), int(args[2]))
            complete_now = True

        ### CHANGING PREDICATES
        # SET-STATE <obj-h> <prop-h> <pred-h>
        elif cmd_name == 'set-state':
            complete_now = self._handle_set_pred_command(args[1], args[2], args[3])
        # OPEN <obj-h> 
        elif cmd_name == 'open':
            complete_now = self._handle_set_pred_command(args[1], 'is-open1', 'open2')
        # CLOSE <obj-h> 
        elif cmd_name == 'close':
            complete_now = self._handle_set_pred_command(args[1], 'is-open1', 'not-open1')
        # ON <obj-h> 
        elif cmd_name == 'on':
            complete_now = self._handle_set_pred_command(args[1], 'is-activated1', 'activated1')
        # OFF <obj-h> 
        elif cmd_name == 'off':
            complete_now = self._handle_set_pred_command(args[1], 'is-activated1', 'not-activated1')

        ### MOVING OBJECTS
        # PLACE <obj-h> <rel> <dest-h>
        elif cmd_name == 'place':
            complete_now = self._handle_place_command(args[1], args[2], args[3])
        # MOVE <obj-h> <wph>
        #    Move the given object to the given waypoint
        elif cmd_name == 'move':
            complete_now = self._handle_move_command(args[1], args[2])
        # TELEPORT <obj-h> <x> <y> <z> <wph>?
        #    Move the given object to the given coordinate (with optional waypoint given)
        elif cmd_name == 'teleport':
            complete_now = self._handle_teleport_command(args[1], float(args[2]), float(args[3]), float(args[4]), args[5] if len(args) > 5 else None)

        ### COMMAND NOT RECOGNIZED
        else:
            complete_now = True

        if complete_now:
            self.complete_command()

    ### Change World Commands - Override to handle per domain
    # Each should return a boolean -- whether to immediately complete the command
    # (False would mean to wait before reporting it handled)

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        return True

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        return True

    def _handle_move_command(self, obj_id, wp_handle):
        return True

    def _handle_teleport_command(self, obj_id, x, y, z, wp_handle):
        return True


class InternalCommandConnector(CommandConnector):
    def __init__(self, client):
        CommandConnector.__init__(self, client)

    def _handle_set_pred_command(self, obj_id, prop_handle, pred_handle):
        self.client.get_connector("language").send_message(self.active_command)
        return False

    def _handle_place_command(self, obj_id, rel_handle, dest_id):
        self.client.get_connector("language").send_message(self.active_command)
        return False

    def _handle_move_command(self, obj_id, wp_handle):
        self.client.get_connector("language").send_message(self.active_command)
        return False

    def _handle_teleport_command(self, obj_id, x, y, z, wp_handle):
        if wp_handle is None:
            return True
        return self._handle_move_command(obj_id, wp_handle)

