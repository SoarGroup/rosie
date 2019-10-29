import sys

from pysoarlib import SoarAgent, LanguageConnector

from ActionStackConnector import ActionStackConnector

class TestAgent(SoarAgent):
	def __init__(self, config_filename=None, **kwargs):
		SoarAgent.__init__(self, config_filename=config_filename, print_handler=lambda s: self.handle_agent_output(s), write_to_stdout=True)
		self.outfile = None

		self.connectors["language"] = LanguageConnector(self)
		self.connectors["language"].register_message_callback(lambda s: self.write_output("R: \"" + s + "\""))

		self.connectors["action_stack"] = ActionStackConnector(self)
		self.connectors["action_stack"].register_output_callback(lambda s: self.write_output(s))

	def handle_agent_output(self, message):
		print(message)
		if message.startswith("NEW-SENTENCE"):
			self.write_output("I: \"" + message.split(':')[1].strip() + "\"")


	def write_output(self, message):
		if self.outfile is not None:
			self.outfile.write(message + "\n")

	def run_test(self, correct_filename):
		self.connect()
		self.outfile = open("test-output.txt", 'w')
		self.execute_command("run")
		self.outfile.close()
		self.outfile = None
		self.kill()
	
