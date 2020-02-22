# action subproblem execute
 
This problem space is used when executing the current task in the world. 
When the robot gets a task command from the instructor, it creates a 
new task segment + operator and adds it to the task-stack on the top-state. 
The agent will then use the execute-task on the top-state to propose
the bottom task (highest level), and its decomposition will be handled
through a stack of substates. 

### State Representation

```
state [s] 
  ^name TASK_OP_NAME
  ^problem-space
    ^name action
    ^subproblem execute
    ^world.copy-type shallow   # Elaborated, will elaborate the world from the top-state
    ^attend-to-percepion yess  # Elaborated, will propose attend-to-perception operators

  ^task-stack           # from top-state.task-stack
  ^current-task-segment # The segment for this task on the task-stack
    ^task-operator      # A representaton of the current task 
    ^start-of-execution # A flag which is only present when the segment is first pushed
    ^task-objects       # a list of objects involved in the current task (arguments, goal, etc)
      ^object ...       

  ^parent-task-segment  # The segment of this task's parent task
  ^child-task-segment   # The segment of this task's child task

  ^task-operator        # Superstate operator, also on the current-task-segment
  ^world                # Copied from the top-state


```

## 1. Set up the state

1. The very first time the agent starts executing a task, it proposes `remove-start-of-execution-flag` 
to remove the flag from the current-task-segment. 
(The start-of-execution flag is used during epmem retrievals to get the episode at the start of the task) \
`remove-start-of-execution-flag.soar`

1. For tasks requiring the time the task started (e.g. if there is a duration predicate), it proposes 
`mark-start-time` to add a start-time to the current-task-segment. \
`mark-start-time.soar`

1. The agent retrieves the start goal from the TCN goal-graph and adds it to the task-operator as ^current-goal \
`retrieve-start-goal.soar`

1. If the goal has any implicit object arguments, add them to the world \
`add-object-to-world.soar`

## 2. Propose and execute subtasks

1. Subtask proposal rules are either included in the task-implementations, or learned chunks. 
These must propose an operator with a name, task-handle, and item-type task-operator.

1. If a subtask is selected, in the substate propose `copy-task-operator` to copy the operator onto task-stack as push-task-operator \
`copy-task-operator.soar` and `task-utils/copy`

1. Once the child task operator is copied, propose `push-task-segment` to push it onto the stack
`push-task-segment.soar` and `task-utils/push-task-segment`

1. Once there is a child-task-segment on the state, propose its task-operator and reject all other operators \
`propose-child-task.soar`

1. Once the child task has finished (there is a status on the child-task-segment), propose `pop-task-segment` to 
remove the child task from the stack \
`pop-task-segment.soar` and `task-utils/pop-task-segment`

1. If the current goal is satisfied, select the next one via the goal-graph \
`select-next-goal.soar`


## 3. Handle subtasks from instruction

If the agent has to ask the instructor for what to do next and they replied with a new task command, 
the agent has to add it to the TCN and learn a new proposal rule. 

1. If the agent cannot determine what to do next, it will enter a State No Change where where it will propose `initiate-interaction` with type get-next-goal. \
`Impasse__State-No-Change/initiate-interaction.soar`

1. If the task stack has a `^push-task-operator` with `^task-source instruction`, it will first propose `add-subtask-to-tcn` to 
add a generalized version to the TCN. When finished, it will add `^learn-subtask-proposal [subtask-handle]` to the current-task-segment. \
`add-subtask-to-tcn.soar` and `task-utils/add-subtask-to-tcn`

1. When the wme `^learn-subtask-proposal [sub-h]` is added to the current-task-segment, the agent will force a state no change
in order to learn the proposal rule. \
`Impasse__State-No-Change/learn-proposal-rule`

1. When this learning is complete, the agent will go  back to the normal process of pushing a new task segment and performing the subtask. 

## 4. Execution Failure

If the agent detects an execution failure, it will propose handle-execution-failure, which will go into a substate. 
(see `handle-execution-failure`)

## 5. Finishing a task

When the task is complete, either due to success or failure, use `complete-task` to exit the substate (puts a status on the task segment).






	
