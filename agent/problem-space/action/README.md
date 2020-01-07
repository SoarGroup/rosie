# action problem-space

## problem-space

```
([s] ^problem-space [ps])
([ps] ^name action)

# subproblem indicates the specific variation of the action problem-space, to focus on different things
# These are mutually exclusive
([ps] ^subproblem execute # used when actually executing a task in the real world
      ^subproblem search  # tries to find the next subtask to perform to satisfy the goal
      ^subproblem waypoint-search # Used for navigation to do A* search for a path to the goal waypoint
      ^subproblem retrospective) # Used after performing a task to learn policy knowledge

# execution-type indicates whether any tasks should be executed via the robot system (external)
#     or simulated on a local copy of the world (internal)
([ps] ^execution-type [[ internal external ]])
```

### Default Rules/Elaborations

```
# task-operator: often elaborated from superstate.operator
#   This is the canonical representation of the current task
([s] ^task-operator [op])
```

  
