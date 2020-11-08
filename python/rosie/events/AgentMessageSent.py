
class AgentMessageSent:
    """ Triggered when a message is sent from the agent to the instructor
        message:str - the message that was sent """
    def __init__(self, message):
        self.message = message

    def __str__(self):
        return "AgentMessageSent: " + self.message 

