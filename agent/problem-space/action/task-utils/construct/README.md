# `construct-task-operator`

State elaborations:
```
([s] ^command-message [msg]              # from superstate.operator
     ^update-task-concept-network [bool] # from superstate.operator
     ^task-concept-network [tcn]         # Retrieved from smem
```

**1. smem-query** `(retrieve-tcn.soar)`

Retrieve the task-concept-network for the handle of the given action on the message

**2. initialize-tcn**

For a new task, initialize the TCN by adding item-type, procedural id, and op_name

**3. create-operator-base**

Create the base task-operator structure w/o arguments 

```

   ^task-operator
      ^item-type task-operator
      ^task-handle <h>
      ^name <op-name>
```

### 4. [propose-construct-task-argument.soar + substate]
#      Construct each argument 
#       Take the argument from the command message 
#       and create the appropriate structure on the task-operator
   operator construct-task-argument
     ^arg-name <arg-name> # The name to use when adding to the task-operator
     ^arg-type <arg-type> # Type of the argument << concept object predicate until-clause >>
     ^message-arg <marg>  # The corresponding argument on the command-message

### 5. [mark-argument-missing.soar] 
#      If a required argument is missing on the command-message  
#      (The argument is marked ^required true on the task-concept-network)
#      Then mark it missing on the task-operator (Add ^missing-argument <arg-name>)
   operator mark-argument-missing
      ^arg-name <arg-name>

### 6. [store-smem-concepts.soar]
#      Store any changes to smem structures 
#      (If there exist any ^to-store identifiers on the state)
   operator store-smem-concepts

### 7. [create-new-task-segment.soar]
#      Once the task-operator is constructed, create a new-task-segment
   operator create-new-task-segment

### 8. [push-task-segment.soar]
#      Once the new-task-segment is created, push it onto the task-stack
#      (See problem-space/action/task-utils/push-task-segment)
#      Will add ^pushed <seg> to the state when finished
   operator push-task-segment

### 9. [complete-construct-task-operator.soar]
#      Once the new-task-segment is pushed, 
#        mark the message as pushed to exit the substate
   operator complete-construct-task-operator

      
#################################################################
####################### construct-task-argument #################

Takes an argument from a command-message from the parser, 
  and constructs an argument representation that 
  will be copied onto the superstate.task-operator 

(<s> ^operator <o> +)
(<o> ^name construct-task-argument
     ^arg-name <arg-name> # The name to use when adding to the task-operator
     ^arg-type <arg-type> # Type of the argument << concept object predicate until-clause >>
     ^message-arg <marg>) # The corresponding argument on the command-message

# arg-type concept
(<o> ^handle <concept-handle>)

# arg-type object
(<o> ^object <obj>)

# arg-type partial-predicate
(<o> ^handle <pred-handle>
     ^2 <obj2>)

# arg-type after-clause/until-clause
(<o> ^predicate <pred>)
(<pred> ^type << state relation >>
        ^handle <pred-handle>
        ^1 <obj1>
        ^2 <obj2>) # only used for relations
(<pred> ^type duration # after 3 minutes
        ^number <n>
        ^unit << minutes seconds >>)
(<pred> ^type clocktime # after 9:00
        ^hour <hour>
        ^minute <min>)


0. Elaborations
   ^task-concept-network (from superstate)
   ^command-message (from superstate)

1. add-object-to-world
   Make sure any objects are represented on the top-state world (adding if not present)
   (Implemented in manage-world-state/add-object-to-world)

2. create-task-argument
   Create the actual task-argument representation

3. add-object-reference-info
   If the command-message has information about how the object was referred to 
     copy that info onto the argument

4. add-argument-to-tcn
   If this is the first time seeing an argument, and it is not in the task-concept-nework,
     add it to the task-concept-network.procedural link

5. store-smem-concepts
   If we did add-argument-to-tcn, we have to store the changes

6. complete-construct-task-argument
   Finish by copying the argument onto the superstate.task-operator

