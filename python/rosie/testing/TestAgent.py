import sys

from rosie import RosieAgent, ActionStackConnector
from rosie.events import *
from pysoarlib import AgentConnector

class TestAgent(RosieAgent):
    def __init__(self, **kwargs):
        RosieAgent.__init__(self, **kwargs)
        self.outfile = None
        self.filename = self.settings["task_test_output_filename"]

        # Listen for messages and write/print them
        self.add_event_handler(AgentMessageSent, lambda e: self.write_output("R: \"" + e.message + "\""))
        self.add_event_handler(InstructorMessageSent, lambda e: self.write_output("I: \"" + e.message + "\""))

        if self.write_to_stdout:
            self.add_event_handler(AgentMessageSent, lambda e: self.print_handler("R: \"" + e.message + "\""))
            self.add_event_handler(InstructorMessageSent, lambda e: self.print_handler("I: \"" + e.message + "\""))

        # Listen for start/end of a task and write it to the file
        self.add_connector("action_stack", ActionStackConnector(self))

        self.add_event_handler(TaskStarted, lambda e: 
                self.write_output("  " * (e.depth-1) + "> " + e.task_desc))
        self.add_event_handler(TaskCompleted, lambda e: 
                self.write_output("  " * (e.depth-1) + "< " + e.task_desc))

        # Listen for any agent writes that start with @TEST
        self.add_print_event_handler(lambda msg: self.print_callback(msg))

    def write_output(self, message):
        if self.outfile is not None:
            self.outfile.write(message + "\n")

    def run_test(self, correct_filename):
        self.connect()
        self.outfile = open(self.filename, 'w')
        self.execute_command("run")
        self.outfile.close()
        self.outfile = None
        self.kill()

    def print_callback(self, message):
        message = message.strip()
        if message.startswith("@TEST: "):
            self.write_output(message[7:])


