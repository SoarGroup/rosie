import sys

from pysoarlib import SoarAgent, LanguageConnector, AgentConnector

from .ActionStackConnector import ActionStackConnector

class TestAgent(SoarAgent):
    def __init__(self, config_filename=None, **kwargs):
        SoarAgent.__init__(self, config_filename=config_filename, write_to_stdout=True)
        self.outfile = None
        self.filename = self.settings["task_test_output_filename"]

        self.connectors["language"] = LanguageConnector(self)
        self.connectors["language"].register_message_callback(lambda s: self.write_output("R: \"" + s + "\""))

        self.connectors["action_stack"] = ActionStackConnector(self)
        self.connectors["action_stack"].register_output_callback(lambda s: self.write_output(s))

        self.add_print_event_handler(lambda msg: self.print_callback(msg))


    def write_output(self, message):
        if self.outfile is not None:
            self.outfile.write(message + "\n")

    def run_test(self, correct_filename):
        self.connect()
        self.agent.AddOutputHandler("scripted-sentence", TestAgent._output_event_handler, self)
        self.outfile = open(self.filename, 'w')
        self.execute_command("run")
        self.outfile.close()
        self.outfile = None
        self.kill()

    def print_callback(self, message):
        if message.startswith("@TEST: "):
            self.write_output(message[7:])
    
    def _output_event_handler(self, agent_name, att_name, wme):
        sentence = wme.ConvertToIdentifier().GetChildString("sentence")
        print(sentence)
        self.write_output("I: \"" + sentence + "\"")


