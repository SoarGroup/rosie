
class InstructorMessageSent:
    """ Triggered when a message is sent from the instructor to Rosie via the input link  
        message:str - the message that was sent """
    def __init__(self, message):
        self.message = message

    def __str__(self):
        return "InstructorMessageSent: " + self.message 

