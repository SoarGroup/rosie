# learn-new-subtask

When the agent has a new subtask to perform (either from instruction or from search), 
it will need to add the subtask to the set of subtasks on the TCN and possibly create a new procedural goal in the goal graph. 

**Option 1:**
* `interpret-message` produces a new segment with the purpose of `execute-task` 
and includes the parsed task structure
* `satisfy-purpose/task-event/handle-execute-task` turns the parsed message into a 
grounded task operator representation and adds it to the current task segment as `new-subtask`
* `problem-space/action/subproblem-execute` will see this `new-subtask` structure and propose this operator to add the subtask to the TCN

**Option 2:**
* The agent hits a SNC impasse while executing a task and tries to `seach-for-next-subtask`, which succeeds
* `search-for-next-subtask/return-successful-operator` copies the successful operator to the current task segment as `new-subtask`
* `problem-space/action/subproblem-execute` will see this `new-subtask` structure and propose this operator to add the subtask to the TCN

**Operator Representation**
```
([o] ^name learn-new-subtask
     ^subtask-operator [sub-op])
```
The subtask operator is a grounded task-operator representation of the subtask.

**State Representation**

```
([s] ^subtask-operator [sub-op]) # from superstate.operator
([s] ^task-operator [task-op])   # from superstate.task-operator
([s] ^task-goal [goal])          # from superstate.desired
([s] ^task-concept-network [tcn]) # The TCN for the current task-operator [smem-retrievals.soar]
([s] ^current-goal-id [gid]) # The root LTI of the current task's goal [smem-retrievals.soar]
([s] ^subtask-tcn [sub-tcn])     # The TCN for the subtask [smem-retrievals.soar]
([s] ^subtask-handle [sub-h]) # handle that identifies this subtask within the parent task 

([s] ^no-matching-operators true) # Flag created if the new subtask does not match any superstate operators

([s] ^generalized-subtask [gen-sub]) # Result of generalize-task-operator

```

**1. match-subtask-operator**

First, see if the new subtask matches one that is already learned/proposed
by using `task-utils/match-task-operator` to compare the subtask-operator against
all the proposed subtasks in the superstate. 

**2. generalize-task-operator**

If no matching operators are found, then this is the first time this subtask was used in the parent task. 
First, the agent will create a generalized version of the subtask-operator using `task-utils/generalize`, 
which it can store in the parent's TCN.

**3. add-subtask-to-tcn**

Once the subtask is generalized, it is added to the parent's TCN under `procedural.subtasks`. 
The subtask is also given a unique subtask handle and is marked with `learned-proposal no`. 

**4. add-node-to-goal-graph**

Regardless of this being a new subtask, if there is not a current goal the agent is working towards, 
it will create a new procedural goal node to be added to the goal graph after the current one. 
(See `task-utils/add-node-to-goal-graph`)

**5. store-subtask**

If anything from smem was added/changed, the agent will store the changes to smem. 

**6. complete-learn-new-subtask**

Once everything else is finished, the agent will remove the `new-subtask` operator from the current task segment
and add a flag `prefer-subtask` on the current task segment. 



