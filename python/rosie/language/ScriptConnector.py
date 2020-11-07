""" This is a connector that will read the list of messages and send them one at a time to the agent """
import sys

from pysoarlib import WMInterface, AgentConnector

class ScriptConnector(AgentConnector):
    """ The ScriptConnector will automatically run through the list of messages on the RosieAgent
            and send each one to the agent through the input-link using the LanguageConnector """

    """ FindHelp: Different ways the script connector can respond if Rosie asks for help finding something
        none   # Respond to any find request with 'Unknown' (Rosie will have to figure it out)
        script # Assume the script will have the answer to the find request
        manual # Make the instructor answer the request manually
        custom # Some other code will handle the request (set the handler via set_find_helper)
    """

    def __init__(self, agent, find_help, print_handler=None):
        AgentConnector.__init__(self, agent, print_handler)
        self.callbacks = []
        self.send_next_message = False

        self.find_request_handler = None
        if find_help.lower() == "none":
            self.find_request_handler = lambda m: "Unknown."
        elif find_help.lower() == "script":
            self.find_request_handler = lambda m: self.advance_script()

    # The given helper method will be called if the agent asks for help
    #    It should be a method taking 1 argument: The find request
    def set_find_helper(self, find_helper):
        self.find_request_handler = find_helper
    
    # Add a callback method that accepts a single string argument
    # Whenever the next script message is sent to the agent, 
    #   the callback should take 2 arguments - the message [str] and index [int]
    def register_script_callback(self, callback):
        self.callbacks.append(callback)

    # On the next input phase, it will send the next scripted message to the agent
    def advance_script(self):
        self.send_next_message = True


    ################################### INTERNAL METHODS #######################################

    def connect(self):
        super().connect()
        self.script = list(self.agent.messages)
        self.script_index = 0
        self.agent.get_connector("language").register_message_callback(lambda msg: self._agent_message_callback(msg))

        if self.agent.has_connector("commands"):
            self.agent.get_connector("commands").register_command_callback(lambda res: self.advance_script())

    def on_init_soar(self):
        self.script_index = 0

    def on_input_phase(self, input_link):
        # If we have been told to send the next message, 
        #   advance the index and send the next one to the agent via the LanguageConnector
        if self.send_next_message:
            self.send_next_message = False
            if self.script_index >= len(self.script):
                return

            message = self.script[self.script_index]
            self.script_index += 1
            self.agent.send_message(message)

            for callback in self.callbacks:
                callback(message, self.script_index-1)

    def _agent_message_callback(self, msg):
        """ If we get a message from the agent, advance the current script """
        if msg.startswith('Ok'):
            # Ignore ok messages
            return
        elif "can't find" in msg:
            self._handle_find_request(msg)
        else:
            self.advance_script()

    def _handle_find_request(self, msg):
        answer = None
        if self.find_request_handler is not None:
            answer = self.find_request_handler(msg)
        if answer is not None:
            self.agent.send_message(answer)

