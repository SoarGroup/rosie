""" This is a connector that will read the list of messages and send them one at a time to the agent """
import sys

from pysoarlib import WMInterface, AgentConnector

class ScriptConnector(AgentConnector):
    def __init__(self, agent, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.find_request_handler = None
        self.callbacks = []
        self.send_next_message = False

    # The given helper method will be called if the agent asks for help
    #    It should be a method taking 1 argument: The find request
    def set_find_helper(self, find_helper):
        self.find_request_handler = find_helper
    
    # Add a callback method that accepts a single string argument
    # Whenever the next script message is sent to the agent, 
    #   the callback is invoked with the message as the argument
    def register_script_callback(self, callback):
        self.callbacks.append(callback)

    def connect(self):
        super().connect()
        self.read_script()
        self.agent.get_connector("language").register_message_callback(lambda msg: self.handle_message(msg))

    def on_init_soar(self):
        self.script_index = 0

    def on_input_phase(self, input_link):
        if self.send_next_message:
            self.send_next_message = False
            if self.script_index >= len(self.script):
                return

            message = self.script[self.script_index]
            self.script_index += 1
            if message[0] == '!':
                self.agent.get_connector("commands").handle_command(message[1:], 
                        lambda res: self.advance_script())
            else:
                self.agent.get_connector("language").send_message(message)

            for callback in self.callbacks:
                callback(message)

    def read_script(self):
        lines = []
        if self.agent.messages_file != None:
            with open(self.agent.messages_file, 'r') as f:
                lines = ( line.strip() for line in f.readlines() )
                # Filter empty lines and commented lines
                lines = ( line for line in lines if len(line) > 0 and line[0] != '#' )

        self.script_index = 0
        self.script = list(lines)

    def handle_message(self, msg):
        # Ignore ok messages
        if msg.startswith('Ok'):
            return
        if "can't find" in msg:
            answer = None
            if self.find_request_handler is not None:
                answer = self.find_request_handler(msg)
            self.print_handler("CANT FIND " + str(answer))
            if answer is not None:
                self.agent.get_connector("language").send_message(answer)
                for callback in self.callbacks:
                    callback(answer)
        else:
            self.advance_script()

    def advance_script(self):
        self.send_next_message = True

