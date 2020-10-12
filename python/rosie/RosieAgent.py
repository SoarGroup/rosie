import subprocess

from pysoarlib import SoarAgent, TimeConnector
from .LanguageConnector import LanguageConnector
from .ScriptConnector import ScriptConnector
from .InternalCommandHandler import InternalCommandHandler

class RosieAgent(SoarAgent):
    """ Wraps the standard pysoarlib SoarAgent with a few rosie-specific config settings and features

        source_config = [string]
            The file used to configure this agent (Rosie-specific configuration)

        reconfig_on_launch = true|false (default=false)
            If true, the agent will use the Rosie java configuration tool to re-generate the agent before continuing

        messages_file = [filename]
            A text file containing sentences to use as a script for Rosie (one sentence per line)

        use_script_connector = true|false (default=false)
            If true, adds a ScriptConnector which will automatically send messages to the agent
            and for internal worlds, handle commands to change the internal world

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
        self.add_connector("time", TimeConnector(self, include_ms=True, sim_clock=True, clock_step_ms=5000))

        # Create a language connector to handle messages to/from Rosie
        self.add_connector("language", LanguageConnector(self))

        if self.settings["domain"] == "internal":
            self.add_connector("commands", InternalCommandHandler(self, self.print_handler))

        if self.settings["domain"] == "internal" and self.use_script_connector:
            script_conn = ScriptConnector(self, self.print_handler)
            if self.find_help == "none":
                script_conn.set_find_helper(lambda m: "Unknown.")
            elif self.find_help == "script":
                script_conn.set_find_helper(lambda m: script_conn.advance_script())
            self.add_connector("script", script_conn)


    def _apply_settings(self):
        SoarAgent._apply_settings(self)

        # Rosie-specific settings
        self.source_config = self.settings.get("source_config", None)
        self.reconfig_on_launch = self.settings.get("reconfig_on_launch", "false").lower() == "true"

        self.messages_file = self.settings.get("messages_file", None)
        self.use_script_connector = self.settings.get("use_script_connector", "false").lower() == "true"
        self.find_help = self.settings.get("find_help", "manual")

    def _create_soar_agent(self):
        if self.source_config is not None and self.reconfig_on_launch:
            # Rerun the configuration tool and re-source the config file
            self.print_handler("RUNNING CONFIGURATOR: " + self.source_config)
            subprocess.check_output(['java', 'edu.umich.rosie.tools.config.RosieAgentConfigurator', self.source_config])
            self._read_config_file()

        SoarAgent._create_soar_agent(self)

