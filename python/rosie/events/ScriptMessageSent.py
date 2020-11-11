
class ScriptMessageSent:
    """ Triggered when a message is sent using the ScriptConnector
        message:str - the message that was sent
        index:int - the index into the script (agent.messages) """
    def __init__(self, message, index):
        self.message = message
        self.index = index

    def __str__(self):
        return "ScriptMessageSent: " + self.message 

