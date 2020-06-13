import subprocess

from pysoarlib import SoarAgent
from .LanguageConnector import LanguageConnector

class RosieAgent(SoarAgent):
    """ Wraps the standard pysoarlib SoarAgent with a few rosie-specific config settings and features

        source_config = [string]
            The file used to configure this agent (Rosie-specific configuration)

        reconfig_on_launch = true|false (default=false)
            If true, the agent will use the Rosie java configuration tool to re-generate the agent before continuing

        messages_file = [filename]
            A text file containing sentences to use as a script for Rosie (one sentence per line)

    """

    def __init__(self, print_handler=None, config_filename=None, **kwargs):
        SoarAgent.__init__(self, print_handler, config_filename, **kwargs)

        # Create a language connector to handle messages to/from Rosie
        self.connectors["language"] = LanguageConnector(self)

    def _apply_settings(self):
        SoarAgent._apply_settings(self)

        # Rosie-specific settings
        self.source_config = self.settings.get("source_config", None)
        self.reconfig_on_launch = self.settings.get("reconfig_on_launch", "false").lower() == "true"

        self.messages_file = self.settings.get("messages_file", None)

    def _create_soar_agent(self):
        if self.source_config is not None and self.reconfig_on_launch:
            # Rerun the configuration tool and re-source the config file
            self.print_handler("RUNNING CONFIGURATOR: " + self.source_config)
            subprocess.check_output(['java', 'edu.umich.rosie.tools.config.RosieAgentConfigurator', self.source_config])
            self._read_config_file()

        SoarAgent._create_soar_agent(self)

