# task-state-no-change

Occurs when the agent encounters a state-no-change inside an action problem-space
and this SNC was not intentionally caused to learn some task-util rules `([s] ^learning-task-utils-rule true)`

```
(state [s] ^name task-state-no-change
           ^task-operator [op] # from superstate.task-operator
           ^desired [des]      # from superstate.desired
           ^world [w]          # from superstate.world
           ^task-concept-network [tcn] # retrieved from smem (retrieve-tcn.soar)
```

### handle-missing-desired
This operator is proposed if there is no desired on the state, and will do one of the following:
* learn a `select-next-goal` rule to advance the current goal
* learn a desired elaboration rule to create a desired structure on the state
* initiate-interaction `get-next-goal` to ask the instructor for a goal/next subtask

### learn-subtask-proposal
This operator is proposed if the TCN has a subtask with a `learned-proposal no` flag, indicating
it still needs to learn a proposal rule for that subtask. 

### search-for-next-subtask
If neither of the above resolve the SNC, it proposed an operator to search for the 
next action to take to satisfy the current goal. 




### initiate-interaction

Current goal is satisfied -> Check for next goal 
  -> Look for a next goal, learn a select-next-goal rule
  -> Person said 'you are done', create a terminal goal node
  -> None of the above, ask the person for the next goal

Current goal is not satisfied -> Check if there is an unlearned proposal rule
  -> If unlearned proposal, learn the rule
  -> Otherwise, Search for the next task

There is no current goal -> Try to learn an elaboration rule
  If this fails -> Ask for the next action

