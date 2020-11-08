
class TaskCompleted:
    """ Triggered when Rosie completes a task
        task_desc - a description of the task
        depth - How far down in a task decomposition this is (top task = 1) """
    def __init__(self, task_desc, depth):
        self.task_desc = task_desc
        self.depth = depth

    def __str__(self):
        return "TaskCompleted: " + self.task_desc

