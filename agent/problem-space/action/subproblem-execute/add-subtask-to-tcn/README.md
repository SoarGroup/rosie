# add-subtask-to-tcn 

Takes a subtask operator representation and creates a generalized version that
is stored in the parent task's TCN. 

##### Operator #####

(<o> ^name add-subtask-to-tcn
     ^task-concept-network <tcn>  - The task-concept-network for the parent task
     ^task-operator  - The task-operator for the parent task
     ^subtask        - The task-operator representation of the subtask


##### State Rep #####

task-concept-network - TCN for the parent task, elaborated from superoperator
task-operator - task-operator for the parent task, elaborated from superoperator
subtask       - task-operator for the subtask, elaborated from superoperator
subtask-tcn   - TCN for the subtask, retrieved through an smem-query
subtask-structure - The generalized rep to be stored in smem

argument-info
  arg-name <arg-name> - name of the argument (from the subtask.<arg-name>)
  arg-type << object concept predicate until-clause >>
  argument <arg>
  generalized <gen>  - link to the corresponding generalize-task-argument on the state

generalize-task-argument - helper structure used to generalize arguments against task-operator
  (See action/task-utils/generalize-task-operator)


##### Results ######

Adds the new subtask onto the parent task's TCN and stores it in semantic memory

##### Operators #####

create-subtask-structure - Creates the initial subtask-structure and copies basic info
generalize-task-argument - Generalizes each argument (see action/task-utils/generalize-task-operator)
add-subtask-goal         - If the current goal is satisfied, add a next goal with the goal of performing this subtask
store-task-subtask       - Adds the subtask onto the parent task's TCN and stores it in smem


