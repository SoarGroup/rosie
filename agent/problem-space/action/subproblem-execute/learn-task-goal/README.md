# learn-task-goal

When the instructor tells the agent about its next goal, 
the agent will use the `learn-task-goal` operator and substate to 
create a general goal representation and add it to the TCN's goal graph. 

* `interpret-message` produces a new segment with the purpose of `learn-task-goal` and includes the parsed goal message
* `satisfy-purpose/task-event/handle-learn-task-goal` turns the parsed message into a 
grounded goal representation and adds it to the current task segment as `new-task-goal`
* `problem-space/action/subproblem-execute` will see this `new-task-goal` structure and propose this operator to add the goal to the TCN

**Operator Representation**
```
([o] ^name learn-task-goal
     ^task-goal [goal])
```
The task goal is a grounded representation of the goal and any conditions.

**State Representation**

```
([s] ^task-goal [goal]) # from superstate.operator
([s] ^task-operator [task-op]) # from superstate.task-operator
([s] ^task-concept-network [tcn]) # The TCN for the current task-operator [smem-retrievals.soar]
```

**1. generalize-task-argument**

If the goal came with any conditions, use the `generalize-task-argument` utility
(from `task-utils/generalize`) to create a generalized version 
of the conditions that can be added to the TCN. 

**2. generalize-task-goal**

Use the `task-utils/generalize/generalize-task-goal` operator to take the 
grounded goal representation and create a generalized version where arguments are either connected to existing slots in the TCN or a set of descriptive predicates. 

**3. add-node-to-goal-graph**

Once the goal is generalized, create a new goal node and add it to the TCN 
using `task-utils/add-node-to-goal-graph`. 
If the task-goal has the flag `^final-goal true` then we tell `add-node-to-goal-graph` to append a terminal goal node.

**4. complete-learn-task-goal**

Finally, once everything has been stored, remove the `new-task-goal` from the current task segment and 
report to the interaction-stack that we have performed a task event of type `learned-task-goal`. 
(Will pop the current segment). 


