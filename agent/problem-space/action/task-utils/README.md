# task-utils

These are general-purpose operators that are used during task execution and learning. 
Most involve task-operators, goals, or the task-stack. 



## construct

Rules for taking a message structure from the parser (usually a `command`) 
and constructing a proper `task-operator` representation. 

If the `update-task-concept-network` flag is true, it
will also initialze the TCN for new tasks, and add task objects to the 
world that aren't already there, and update existing ones.

**Proposal**
```
([s] ^operator [o] +)
([o] ^name construct-task-operator
     ^command-message [msg] # produced by the parser
     ^update-task-concept-network [bool]) 
```

**Application**
```
# Will add the following to the state
([s] ^task-operator [task-op])
```




**copy**

**generalize**

**instantiate**

**
