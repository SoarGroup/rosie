import subprocess
import random

from pysoarlib import SoarAgent, TimeConnector
from rosie import CommandConnector, InternalCommandConnector
from rosie.events import InstructorMessageSent
from rosie.language import LanguageConnector, ScriptConnector

# Rule that elaborates an agent-param onto the top-state (0=attribute, 1=value)
agent_param_rule = \
"""sp {{top-state*elaborate*agent-params*{0}
   (state <s> ^superstate nil
              ^agent-params <p>)
-->
   (<p> ^{0} {1})
}}
"""

class RosieAgent(SoarAgent):
    """ Wraps the standard pysoarlib SoarAgent with a few rosie-specific config settings and features

        source_config = [string]
            The file used to configure this agent (Rosie-specific configuration)

        reconfig_on_launch = true|false (default=false)
            If true, the agent will use the Rosie java configuration tool to re-generate the agent before continuing

        domain = internal|magicbot|fetch|ai2thor|tabletop (default=internal)
            The domain that this agent is running in

        messages_file = [filename]
            A text file containing sentences to use as a script for Rosie (one sentence per line)

        use_script_connector = true|false (default=false)
            If true, adds a ScriptConnector which will automatically send messages to the agent

        use_command_connector = true|false (default=true)
            If true, will create a command connector to handle special instructor commands that change the world/agent

        custom_language_connector = true|false (default=false)
            If true, will rely on a subclass to create the language connector

        clock_step_ms = [int] (default=50)
            How many milliseconds the simulated clock on the input-link ticks every decision cycle

        agent_params = { }
            Specify att/val pairs to add to the ^agent-params wme in working memory

        find_help = manual|script|none|custom (default=manual)
            If use_script_connector = true, this tells it how to handle any find requests that come up
            none   = The answer will always be "Unknown"
            script = The script is assumed to have the answer
            manual = The instructor is expected to answer
            custom = It is assumed the handler will be added elsewhere

    """


    def __init__(self, print_handler=None, config_filename=None, **kwargs):
        SoarAgent.__init__(self, print_handler, config_filename, **kwargs)

        # Create a time connector to put timing information on the top state
        self.time_conn = TimeConnector(self, include_ms=True, sim_clock=True, 
                clock_step_ms=int(self.settings.get('clock_step_ms', 50)))
        self.add_connector("time", self.time_conn)

        # Create the default language connector
        if not self.custom_language_connector:
            self.add_connector("language", LanguageConnector(self, self.print_handler))

        # If use_script_connector = True, add a ScriptConnector
        #   This will automatically run through the messages and send the to the agent
        if self.use_script_connector:
            self.add_connector("script", ScriptConnector(self, self.find_help, self.print_handler))

        # Create the default command connector
        if self.use_command_connector:
            if self.domain == "internal":
                self.add_connector("commands", InternalCommandConnector(self, self.print_handler))
            else:
                self.add_connector("commands", CommandConnector(self, self.print_handler))

        # Map from an Event Class to callback methods that will handle the event
        self.event_handlers = { }

    # Will add a parameter to the top-state ^agent-params wme
    def add_agent_param(self, name, value):
        self.agent_params[name] = value
        self._source_agent_param(name, value)

    # Send a message to the rosie agent
    #   Either an english sentence, or a special command (see CommandConnector.py)
    def send_message(self, message):
        if message.startswith("!CMD") and self.has_connector("commands"):
            self.get_connector("commands").handle_command(message)
        else:
            self.get_connector("language").send_message(message)
        self.dispatch_event(InstructorMessageSent(message))
    
    ### Rosie Event Handlers
    # Connectors can listen for various events by registering handlers

    def add_event_handler(self, event_class, handler):
        """ Tells the agent to call the given handler when the given event occurs 
            Returns an id which can be used to remove the handler later """
        if event_class not in self.event_handlers:
            self.event_handlers[event_class] = []
        # NOTE: These ids are random, so there is a tiny chance that two could get the same id and cause problems
        rand_id = random.randint(0, 2**31)
        self.event_handlers[event_class].append((rand_id, handler))
        return rand_id

    def dispatch_event(self, event):
        """ Evoke all the added handlers for the given event """
        handlers = self.event_handlers.get(event.__class__, [])
        for handler in handlers:
            handler[1](event)

    def remove_event_handler(self, event_class, handler_id):
        """ Removes the handler with the given id (returned by add_event_handler) """
        if event_class not in self.event_handlers:
            return
        handler_index = next((i for i, h in enumerate(self.event_handlers[event_class]) if h[0] == handler_id), -1)
        if handler_index != -1:
            del self.event_handlers[event_class][handler_index]

    ################################# INTERNAL ###############################

    def _read_messages(self):
        self.messages = []
        try:
            if self.messages_file != None:
                with open(self.messages_file, 'r') as f:
                    lines = ( line.strip() for line in f.readlines() )
                    # Filter empty lines and commented lines
                    self.messages = list( line for line in lines if len(line) > 0 and line[0] != '#' )
        except:
            pass

    def _apply_settings(self):
        SoarAgent._apply_settings(self)

        # Rosie-specific settings
        self.source_config = self.settings.get("source_config", None)
        self.reconfig_on_launch = self._parse_bool_setting("reconfig_on_launch", False)

        self.messages_file = self.settings.get("messages_file", None)

        self.custom_language_connector = self._parse_bool_setting("custom_language_connector", False)
        self.use_script_connector = self._parse_bool_setting("use_script_connector", False)
        self.find_help = self.settings.get("find_help", "manual")

        self.use_command_connector = self._parse_bool_setting("use_command_connector", True)

        self.agent_params = self.settings.get("agent_params", {})

        self.domain = self.settings.get("domain", "internal")
        if self.domain == "mobilesim":
            self.domain = "magicbot"
        self.agent_params["domain"] = self.domain
        if self.domain == "internal":
            self.agent_params["simulate-perception"] = "true"


    def _create_soar_agent(self):
        if self.source_config is not None and self.reconfig_on_launch:
            # Rerun the configuration tool and re-source the config file
            self.print_handler("RosieAgent: Running RosieAgentConfigurator")
            output = subprocess.check_output(['java', 'edu.umich.rosie.tools.config.RosieAgentConfigurator', self.source_config])
            #for line in str(output).split('\\n'):
            #    self.print_handler(str(line))
            self._read_config_file()
        self._read_messages()

        super()._create_soar_agent()

    def _source_agent_param(self, name, value):
        self.execute_command(agent_param_rule.format(name, value))

    def _source_agent(self):
        super()._source_agent()
        for name, val in self.agent_params.items():
            self._source_agent_param(name, val)


