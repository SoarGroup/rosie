# top-subtask-search-state

```
# Parent Operator:
([s] ^operator [o])
([o] ^top-subtask-search-state true
     ^copy-of-state [state])
     # NOTE: no name on the operator!
```

When doing a subtask search, this is the top search state which is a copy of 
a task substate but where the agent can simulate actions and do an iterative deepening search. 

```
# State Representation
([s] ^top-subtask-search-state true
     ^copy-of-state [state]          # from superstate.operator
     ^name [name]                    # from copy-of-state.task-operator.name
     ^task-operator [op]             # deep-copied from copy-of-state.task-operator 
                                         via task-utils/copy-task-operator
     ^desired [des]                  # deep-copied from copy-of-state.desired 
                                         via task-utils/copy-task-goal
     ^problem-space
       ^name action
       ^subproblem search
       ^propose-subtasks true
       ^execution-type internal
       ^learning
         ^type exploration
       ^world
         ^copy-type deep
         ^source [world]             # from copy-of-state.world
)
```

### force-operator-tie

On this state, we propose all known subtasks (via problem-space.propose-subtasks true), 
and two operators called force-operator-tie to make sure that a tie occurs. 
The the agent will perform iterative-deepening search to find a successful operator
(see `Impasse__Operator-Tie` and `subproblem-search/Impasse__Operator-Tie`)

Will result in either:
* `([s] ^successful-operator [o])`
* `([s] ^subtask-search-failed true)`


### copy-task-operator

If there is a `successful-operator`, create a copy called `copied-successful-operator` (via task-utils/copy-task-operator)

### return-successful-operator

Once the successful-operator is copied, copy it to the superstate

### return-search-failure

If there is a `subtask-search-failed true` flag, copy it to the superstate
