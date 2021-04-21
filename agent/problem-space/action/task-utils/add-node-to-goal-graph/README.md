## **Task Utility: add-node-to-goal-graph**

This utility operator is used to add subgoal nodes to a task's goal-graph. 
When it is finished, it will add `^add-node-result [node-handle]` to the 
add-node-to-goal-graph operator. 

See `add-node-to-goal-graph_source.soar` for details about the substate. 

**Operator Arguments** <br>
```
^type [[ start predicate-set subtask composite terminal ]] (REQUIRED)
# The type of node to create

^task-handle [task-h] (REQUIRED)
# The handle of the task to edit

^goal-predicates [pred-set] (REQUIRED for type=predicate-set)
# A goal predicate-set produced by task-utils/generalize-task-argument

^subtask-handle [sub-h] (REQUIRED for type=subtask)
# The handle of the subtask that corresponds to this subtask node

^goal-conditions [conds] 
# A predicate-set produced by task-utils/generalize-task-argument
#   which will determine conditions placed on the next pointer to the new node

^after [node-handle] OR ^before [node-handle]
# For all nodes except start nodes, you must specify either after or before
# This tells the code where to insert the new node into the goal graph

^final-goal true
# If present, will add a terminal node after the created one

^add-else-link true
# If present, and there are goal conditions, 
#   it will add an intermediate node following the goal
#   and create a default next link from the previous goal to the intermediate one
```

### Start Node

Every goal graph has 1 start node which every task execution should start at. 
It is pointed to by the task concept network. 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type start
     ^task-handle move1)
```

**Created Node**

```
# Note: it adds the node to the TCN root as ^goal-graph
([tcn] ^task-handle move1 ^goal-graph [start-node])
([start-node] ^handle move1start1 
              ^item-type start-goal)
```


### Predicate Node

A predicate node contains a set of state-based predicates where the goal 
is to achieve those predicates. This is a standard goal in Rosie. 
The predicate-set should be produced by the generalize task-util 
(already in a proper smem format). 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type predicate-set
     ^task-handle move1
     ^after move1start1
     ^goal-predicates [preds])
([preds] ^pred-count 1
         ^1 [p1])
([p1] ^type relation ^id [rel-slot] ^1 [arg1] ^2 [arg2])
# (These ids are slots connected to the rest of the TCN)
```

**Created Node**

```
([prev] ^handle move1start1 ^next [next])
([next] ^goal [pred-node])
([pred-node] ^handle move1goal1
             ^item-type task-goal
             ^pred-count 1
             ^1 [p1])
```

### Subtask Node

A subtask node contains 1 predicate with the purpose of executing the given subtask. 
When the given subtask has been completed, the subgoal is satisfied. 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type subtask
     ^task-handle move1
     ^after move1start1
     ^subtask-handle subtask27)
```

**Created Node**
```
([prev] ^handle move1start1 ^next [next])
([next] ^goal [sub-node])
([sub-node] ^handle move1goal1
            ^item-type task-goal
            ^pred-count 1
            ^1 [p1])
([p1] ^type subtask
      ^subtask-handle subtask27)
```

### Composite Node

A composite node will contain some number of subtasks where the goal is 
to execute all of them but the order doesn't matter (like a TODO list). 
The created node will be empty, other code should add individual predicates. 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type composite
     ^task-handle move1
     ^after move1start1)
```

**Created Node**
```
([prev] ^handle move1start1 ^next [next])
([next] ^goal [comp-node])
([comp-node] ^handle move1goal1
             ^item-type task-goal
             ^pred-count 0)
```

### Terminal Node

A terminal node indicates the task is complete when it is reached. 
There can be multiple terminal nodes in a goal graph. 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type terminal
     ^task-handle move1
     ^after move1goal1)
```

**Created Node**
```
([prev] ^handle move1goal1 ^next [next])
([next] ^goal [term-node])
([term-node] ^handle move1term1
             ^item-type terminal-goal)
```

## Other Examples

### Final Goal

If you add the flag `^final-goal true` to the task operator, it will add 
a terminal goal after the created one. 

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type subtask
     ^task-handle move1
     ^subtask-handle subtask27
     ^after move1start1
     ^final-goal true)
```

**Created Node**
```
([prev] ^handle move1start1 ^next [next])
([next] ^goal [sub-node])
([sub-node] ^handle move1goal1
            ^item-type task-goal
            ^pred-count 1
            ^1 [p1]
            ^next [next2])
([next2] ^goal [term-node])
([term-node] ^handle move1term1
             ^item-type terminal-goal)
```

### Goal Conditions

If goal-conditions are given on the task operator, 
they will be added to the next edge pointing to the newly created goal node. 
This says to only select this goal node if the conditions are met. 

**Example Operator**
```
# This corresponds to a conditional goal for store: 
# If the soda is a drink then the goal is that the soda is in the fridge.
([o] ^name add-node-to-goal-graph
     ^type predicate-set
     ^task-handle store1
     ^after store1start1
     ^goal-conditions [conds]
     ^goal-predicates [preds])
([conds] ^pred-count 1
         ^1 [c1])
([c1] ^type unary ^id [drink] ^1 [arg1])
([preds] ^pred-count 1
         ^1 [p1])
([p1] ^type relation ^id [rel] ^1 [arg1] ^2 [fridge])
```

**Created Node**
```
([prev] ^handle move1start1 ^next [next])
([next] ^conditions [conds]
        ^goal [pred-node])
([pred-node] ^handle move1goal1
             ^item-type task-goal
             ^pred-count 1
             ^1 [p1])
```

### Add Edge Link

If flag ^add-else-link true is given, we add an intermediate node after the created node
and connect a default next link from the previous node to the intermediate node.

**Example Operator**
```
([o] ^name add-node-to-goal-graph
     ^type predicate-set
     ^task-handle move1
     ^after move1start1
     ^goal-conditions [conds]
     ^goal-predicates [preds]
     ^add-else-link true)
```

**Created Node**

```
([prev] ^handle move1start1 ^next [next] ^next [else])
([next] ^conditions [conds]
        ^goal [pred-node])
([else] ^goal [int-node])

([pred-node] ^handle move1goal1
             ^next [next2]
             ^item-type task-goal
             ^pred-count 1
             ^1 [p1])
([next2] ^goal [int-node])

([int-node] ^handle move1int1
            ^item-type intermediate-goal)
```
