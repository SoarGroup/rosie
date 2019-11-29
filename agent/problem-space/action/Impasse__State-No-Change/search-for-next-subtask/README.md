# search-for-next-subtask

This substate will try to find the subtask it should perform next to help satisfy the goal. 

It sets up a copy of the substate with the SNC that forms the top of a subtask search. 

### propose top-subtask-search-state

This is an anonymous operator that will lead to a substate, where it will create
a copy of the SNC substate and perform search. (There is no name on the operator so 
the substate can use the same name as the copied state). 

(See `subproblem-search/top-subtask-search-state` for implementation)

### return-successful-operator

If the search succeeded, return the next subtask operator to the task-stack, 
which will push it and cause a proposal rule to be learned

### increment-search-phase

There is an option to do multiple phases of search, with different search settings
It starts at phase 1, and if it failed, it increments the search-phase

For example, it may do the first phase only searching subtasks that directly involve task arguments, 
then in later phases it expands the set of subtasks it is considering. 
This is an attempt to make search more efficient. 

### initiate-interaction

If all phases of the search failed, it uses initiate-interaction `get-next-subaction` to ask
the instructor for what to do next. 

