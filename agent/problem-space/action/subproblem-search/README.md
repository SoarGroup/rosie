# subproblem-search

Special rules that control the selection search problem-space while searching for the next subtask. 

**problem-space**

Copy the `search-phase` and `search-phase-info` from the superstate.problem-space

**preferences**

Make sure that any command operators are selected, as they often will satisfy the goal. 

Don't propose any subtasks with the same name as the parent task

**control-search-phase**

Rules that will constrain the search by rejecting certain operators based on search-phase-info

```
([s] ^problem-space [ps])
([ps] ^search-phase-info [info])
([info] ^require-all-objects-match [depth]
#       Require that all objects on a subtask also appear on the current task or goal

        ^require-matching-object [depth])
#       Require that at least one object on a subtas also appears on the current task/goal

# Note: here the depth is number of levels above the bottom
# (e.g., if max-depth is 5, and require-all-objects-match 2, 
#  then this won't apply on depths 1-3, but will on 4-5)
```

## Operator Tie

When the operators tie, the agent will enter the selection space and use iterative deepening to evaluate operators and find one the agent should perform next. 

(See `problem-space/action/Impasse__Operator-Tie`)

**init-current-evalution-depth**

Specify the max depth of the iterative deepening. Either uses `problem-space.search-phase-info.max-depth` or `agent-params.subtask-search-depth`. 

**achieved-max-depth**

If this operator fires, create a flag `subtask-search-failed true` on the superstate. 

**return-successful-operator**

If an operator has a successful evaluation on the top selection state, copy it to the superstate as `successful-operator`. 
