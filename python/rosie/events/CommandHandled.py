
class CommandHandled:
    """ Triggered when Rosie handles a command
        (special commands beginning with !CMD)
        command:str - the command that was completed """
    def __init__(self, command):
        self.command = command

    def __str__(self):
        return "CommandHandled: " + self.command 

