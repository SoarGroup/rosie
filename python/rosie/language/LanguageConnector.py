""" Used in the Rosie project to send and receive messages from the agent

Adds English sentences as a linked list of words on input-link.language
Handles send-message commands on the output link
"""
import sys

from string import digits
from pysoarlib import WMInterface, AgentConnector
from .RosieMessageParser import RosieMessageParser
from rosie.events import AgentMessageSent, InstructorMessageSent

class Message(WMInterface):
    """ Represents a single sentence that can be added to working memory as a linked list """
    def __init__(self, message, num):
        """ message:string - a single natural language sentence
            num:int - a number indicating the id of the message """
        WMInterface.__init__(self)
        self.message = message.strip()
        self.num = num

        self.message_id = None

    def _add_to_wm_impl(self, parent_id):
        self.message_id = parent_id.CreateIdWME("sentence")
        self.message_id.CreateIntWME("sentence-number", self.num)
        self.message_id.CreateStringWME("complete-sentence", self.message)
        self.message_id.CreateStringWME("spelling", "*")

        # Get the punctutation character, if exists, and remove it
        punct = '.'
        if self.message[-1] in ".!?":
            punct = self.message[-1]
            self.message = self.message[:-1]

        self.message = self.message.replace(',', ' ,')

        # If there is a quote in the sentence, adds it as a single unit (not broken into words)
        # (In place of the quote, puts a _XXX_ placeholder)
        self.message = self.message.replace('|', '"')
        quote = None
        begin_quote = self.message.find('"')
        end_quote = self.message.find('"', begin_quote+1)
        if begin_quote != -1 and end_quote != -1:
            quote = self.message[begin_quote+1:end_quote]
            self.message = self.message[:begin_quote] + "_XXX_" + self.message[end_quote+1:]

        # Create a linked list of words with next pointers
        next_id = self.message_id.CreateIdWME("next")
        words = self.message.split()
        for word in words:
            if len(word) == 0:
                continue
            if word == "_XXX_":
                # Add the quote as a whole
                word = quote
                next_id.CreateStringWME("quoted", "true")
            if ':' in word:
                # Special handling for times (assuming HH:MM)
                next_id.CreateStringWME("clocktime", "true")
                next_id.CreateIntWME("hour", int(word.split(':')[0]))
                next_id.CreateIntWME("minute", int(word.split(':')[1]))

            next_id.CreateStringWME("spelling", word.lower())
            next_id = next_id.CreateIdWME("next")
        
        # Add punctuation at the end
        next_id.CreateStringWME("spelling", str(punct))
        next_id.CreateStringWME("next", "nil")

    def _remove_from_wm_impl(self):
        self.message_id.DestroyWME()
        self.message_id = None

class LanguageConnector(AgentConnector):
    """ Will handle natural language input and output to a soar agent
        For input - will add sentences onto the input link as a linked list
        For output - will take a message type and generate a natural language sentence """
    def __init__(self, client):
        AgentConnector.__init__(self, client)
        self.add_output_command("send-message")
        self.add_output_command("scripted-sentence")

        self.current_message = None
        self.next_message_id = 1
        self.language_id = None

        self.messages_to_remove = set()

    def on_init_soar(self):
        if self.current_message != None:
            self.current_message.remove_from_wm()
            self.current_message = None
        if self.language_id != None:
            self.language_id.DestroyWME()
            self.language_id = None

    def send_message(self, message):
        if self.current_message != None:
            self.messages_to_remove.add(self.current_message)
        self.current_message = Message(message, self.next_message_id)
        self.next_message_id += 1

    def on_input_phase(self, input_link):
        if self.language_id == None:
            self.language_id = input_link.CreateIdWME("language")

        if self.current_message != None and not self.current_message.is_added():
            self.current_message.add_to_wm(self.language_id)

        for msg in self.messages_to_remove:
            msg.remove_from_wm()
        if len(self.messages_to_remove) > 0:
            self.messages_to_remove = set()

    def on_output_event(self, command_name, root_id):
        if command_name == "send-message":
            self.process_output_link_message(root_id)
        elif command_name == "scripted-sentence":
            self.process_scripted_sentence(root_id)

    def process_output_link_message(self, root_id):
        message_type = root_id.FindByAttribute("type", 0).GetValueAsString()
        if not message_type:
            root_id.CreateStringWME("status", "error")
            root_id.CreateStringWME("error-info", "send-message has no type")
            self.client.print_handler("LanguageConnector: Error - send-message has no type")
            return

        message = RosieMessageParser.parse_message(root_id, message_type)
        self.client.dispatch_event(AgentMessageSent(message))
        root_id.CreateStringWME("status", "complete")

    def process_scripted_sentence(self, root_id):
        sentence = root_id.GetChildString("sentence")
        self.client.dispatch_event(InstructorMessageSent(sentence))
        root_id.CreateStringWME("handled", "true")

