# `construct-task-operator`

This operator is responsible for taking a command message produced by the parser and 
turning it into the proper task-operator representation used in the rest of Rosie. 
It can also initialize and update the task-concept-network for that task 
using the arguments in the command, if the update-tcn flag is true. 

**Operator Representation**
```
([o] ^name construct-task-operator
     ^command-message [msg] # A command message produced by the parser
     ^result-name [str]     # The name to use when creating the result on the state
     ^update-tcn [bool])    # If true, will create/update the TCN with the arguments
```

**State Elaborations**
```
([s] ^command-message [msg]       # from superstate.operator
     ^result-name [str]           # from superstate.operator
     ^update-tcn [bool]           # from superstate.operator
     ^task-concept-network [tcn]  # Retrieved from smem
    
     ^uncreated-args true         # Present if 1+ arguments still need to be created
     ^uncopied-args true          # Present if 1+ arguments have not been copied 
                                  #  onto the task operator
```

**Result** <br>
Will construct the task operator and copy it to the state as `([s] ^[result-name] [task-op])`

### Substate Operators

**1. smem-query** `(retrieve-tcn.soar)`

Retrieve the task-concept-network for the handle of the given action on the message

**2. create-operator-base** `(create-operator-base.soar)`

Will create the root structure of a task-operator, with the handle and name but no arguments.  

**3. construct-task-argument** `(construct-task-argument.soar)`

For each argument on the command message, it will elaborate 
a construct-task-argument structure onto the state which will 
cause operators to be proposed to construct the argument (see below). 

Note that this file has a lot of special case rules to handle peculiarities of the parser 
and translating from the parser structures to ones that the construct-task-argument operators can understand.

**4. add-arguments-to-task-operator** `(add-arguments-to-task-operator.soar)`

Once all the arguments have been constructed, this operator copies them onto the task-operator

**5. update-tcn** `(update-tcn directory)`

If the flag update-tcn is true, this will propose an operator which will 
use the argument structure in the created task-operator to initialize or update the TCN for that task. 
This is especially important to learn the argument structure for a new task, and will create the procedural and initial goal
representations and store them. 

**6. complete-construct-task-operator** `(complete-construct-task-operator.soar)

Once everything is finished, this will copy the constructed task-operator to the superstate, 
using the ^result-name parameter as the attribute.  





# `construct-task-goal`

This operator is responsible for taking a goal description message produced by the parser and 
turning it into the proper task goal representation used in the rest of Rosie. 

**Operator Representation**
```
([o] ^name construct-task-goal
     ^goal-message [msg]    # A goal message produced by the parser
     ^result-name [str]     # The name to use when creating the result on the state
     ^conditions [conds])   # Any conditions mentioned by the instructor (optional)
```

**State Elaborations**
```
([s] ^goal-message [msg]   # from superstate.operator
     ^conditions [conds]   # from superstate.operator
     ^result-name [str]    # from superstate.operator

     ^goal-clause [cl]     # Elaborate individual goal clauses from the goal-message
    
     ^uncreated-args true         # Present if 1+ arguments still need to be created
     ^uncopied-args true          # Present if 1+ arguments have not been copied 
                                  #  onto the task operator
```

**Result** <br>
Will construct the task operator and copy it to the state as `([s] ^[result-name] [task-op])`

### Substate Operators

**1. create-goal-base** `(create-goal-base.soar)`

Creates the initial empty goal with 0 predicates

**2. construct-task-argument** `(construct-task-argument.soar)`

For each goal clause or condition, this will use the operators in construct-task-argument
to create the proper representation of each one, especially making sure that
any object arguments match those in the world. 

**3. add-goal-predicate** `(add-goal-predicatecondition.soar)`

Once a goal predicatecondition is constructured, add it to the task goal 
and increment the pred-count. 

**4. add-goal-condition** `(add-goal-condition.soar)`

Once a goal condition is constructured, add it to the task goal

**5. complete-construct-task-goal** `(complete-construct-task-goal.soar)`

Once everything has been added to the task goal, copy it to the superstate using result-name as the attribute. 





# `construct-task-argument`
An operator is proposed for each `construct-task-argument` structure elaborated onto the state. 
They will construct the proper argument representation for each different type of argument, 
e.g. object, concept, clause. 

These operators refer to the following structures on the state: 
```
([s] ^construct-task-argument [cta])
([cta] ^arg-type [arg-type] # e.g. object, concept, clause
       ^arg-name [arg-name] # what to name the argument when its added to the task-operator
       ^source [src]        # The argument on the command-message to refer to 
       ^abstract true       # Flag copied from the TCN if present
                            # It means object shouldn't be grounded

       ^created-arg [arg])  # The result created by construct-task-argument
```

**objects**

* `select-object-argument` - If there are multiple objects matching a given argument, select one
* `add-object-to-world` - If the object is not abstract, make sure it is in the world
* `create-abstract-object` - If the object is abstract, create an ungrounded representation for it
* `construct-task-argument` - Finally, create the object argument + copy any reference-info

