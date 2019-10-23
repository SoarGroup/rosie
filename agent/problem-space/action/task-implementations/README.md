# Task Implementations

Common files in task implementations:
* **propose-TASK-OP.soar**: Rules to propose the task during search
* **action-models.soar**: Rules that simulate the task and change the world state during search
* **elaborations.soar**: Substate elaborations, often copying task arguments from superoperator
* **send-TASK-command.soar**: Sends the actual command to the output link. 
Sometimes this has stricter conditions than the proposal rule preconditions, 
so the agent will do additional subtasks before it can send the command. 
For example, often with objects they must be within reach before sending the command, 
so the agent might do an approach subtask first. 
* **simulate-TASK-command.soar**: Rules for simulating the output command in the internal domain
* **propose-subtasks.soar**: If the task is a higher-level, it might be implemented through subtasks
* **complete-task.soar**: Anything else has to be done when the task finishes

Note that the task goal is usually created from the task concept network in ```init-smem/actions.soar```


### Approach

Have the robot drive up close to an object in order to interact with it. 

```
op_approach1(arg1:object)

  Pre:  visible1(arg1), not-reachable1(arg1)
  Goal: reachable1(arg1)
  Post: +reachable1(arg1), -not-reachable1(arg1)
```

**send-approach-command**
* Internal: Change the is-reachable1 predicate on the input link to reachable1
* Ai2Thor: Approach is a primitive action
* Cozmo: Approach is a primitive action
* Magicbot: Do custom calculations to extract-target-position and extract-object-position and first face, then drive to, the target's position

---

### Ask

Has the robot ask a question and then wait for a response from the instructor.

```
op_ask1(arg1:object)

  Goal: execute-command(ask-command)
  Model: +new_obj, +entity(new_obj), +answered1(new_obj), -answered1(other)
```

**send-ask-command**

All domains: Sends an outgoing-message to ask the question and pushes a new interaction segment to wait for the response. 
When it gets an answer, it adds it to the world if necessary, then marks it with modifier1=answered1 and completes.


### Close

Have the robot close an object, e.g. a door or a fridge

```
op_close1(arg1:object)

  Pre:  open2(arg1)
  Goal: closed2(arg1)
  Post: +closed2(arg1), -open2(arg1)
```

**send-close-command**, requires visible1(arg1) and reachable1(arg1)
* Internal: Change the predicate on the input link to closed2
* Ai2Thor: Close is a primitive action
* Tabletop: Close is a primitive action (set-state)
* Magicbot: Close is a primitive action (do-control-law set-state)


### Give

Have the robot give a grabbed object to a person

```
op_give1(arg1:object, arg2:partial-predicate=to1(per))

  Pre:  grabbed1(arg1), person(per)
  Goal: holding1(per, arg1)
  Post: +holding1(per, arg1), -grabbed1(arg1), +not-grabbed1(arg1)
```

**send-give-command**, requires visible1(per) and reachable1(per)
* Internal: Add the holding1(per, arg1) predicate and mark as not grabbed


### Go to location

Have the robot drive to another location in the world, e.g. the kitchen

```
op_go-to-location1(arg2:partial-predicate=to(loc))

  Pre:  location(loc), loc != current-location
  Goal: current-location(loc)
  Post: +current-location(loc), -current-location(old_loc) (if exists)
        if in1(obj, old_loc): -visible1(obj) +not-visible1(obj)
        if in1(obj, loc): -not-visible1(obj) +visible1(obj)
```

Subtask: go-to-waypoint1 (all domains)

### Go to waypoint

Have the robot drive to the given waypoint via its waypoint graph. 
(Note: this is only used internally to the robot)
It will do an a-star search to find the shortest waypoint path
using go-to-next-waypoint1 for edge traversal

```
op_go-to-location1(arg1:waypoint)

Goal: current-waypoint(arg1)
```

Subtask: go-to-next-waypoint1 (all domains)


### Go to next waypoint
Have the robot drive to an adjacent waypoint in the waypoint graph. 
(Note: this is only used internally to the robot). 
How it does this depends on the domain

Internal: just change the waypoint on the simulated input link


### Open

Have the robot open an object, e.g. a microwave or a cupboard

```
op_open1(arg1:object)

  Pre:  closed2(arg1)
  Goal: open2(arg1)
  Post: +open2(arg1), -closed2(arg1), 
        if receptacle1(arg1) and in1(obj, arg1) then +visible1(obj)
```

**send-open-command**, requires visible1(arg1) and reachable1(arg1)
* Internal: Change the predicate on the input link to open2
* Ai2Thor: Open is a primitive action
* Tabletop: Open is a primitive action (set-state)
* Magicbot: Open is a primitive action (do-control-law set-state)


### Pick Up

Have the robot pick up an object that is grabbable, e.g. a cup

```
op_pick-up1(arg1:object)

  Pre:  not-grabbed1(arg1), -grabbed1(any)
  Goal: grabbed1(arg1)
  Post: +grabbed1(arg1), -not-grabbed1(arg1), 
        remove any relations involving the object (e.g. on or right-of)
        will change the location of the belief object for the tabletop
```

**send-pick-up-command**, requires visible1(arg1) and reachable1(arg1), 
also if the object is inside a receptacle it must be open
* Internal: Change the holding-object on the simulated input-link and remove all relations
* Ai2Thor: Pickup is a primitive action
* Tabletop: Pickup is a primitive action (set-state)
* Magicbot: Pickup is a primitive action (do-control-law set-state)
* Cozmo: Pickup is a primitive action 


### Put Down

Have the robot put down an object it is holding, at a particular place

```
1: Put Down
op_put-down1(arg1:object) 

  Pre:  grabbed1(arg1)
  Goal: not-grabbed1(arg1)
  Post: -grabbed1(arg1), +not-grabbed1(arg1), in1(arg1, cur-loc)

2: Put Down on a surface
op_put-down1(arg1:object, arg2:partial-predicate=on1(dest))

  Pre:  grabbed1(arg1), surface1(dest)
  Goal: on1(arg1, dest)
  Post: -grabbed1(arg1), +not-grabbed1(arg1), on1(arg1, dest), in1(arg1, cur-loc)

3: Put Down in a receptacle
op_put-down1(arg1:object, arg2:partial-predicate=in1(dest))

  Pre:  grabbed1(arg1) and receptacle1(dest) and open2(dest)
        grabbed1(arg1) and receptacle1(dest) and !open2(dest) and !closed2(dest)
  Goal: in1(arg1, dest)
  Post: -grabbed1(arg1), +not-grabbed1(arg1), in1(arg1, dest), in1(arg1, cur-loc)
```

**send-pick-up-command**, requires visible1(arg1) and reachable1(arg1), 
also if the object is inside a receptacle it must be open
* Internal: Change the holding-object on the simulated input-link and remove all relations
* Ai2Thor: Pickup is a primitive action
* Tabletop: Pickup is a primitive action (set-state)
* Magicbot: Pickup is a primitive action (do-control-law set-state)
* Cozmo: Pickup is a primitive action 


### Remember

Remember one object as another, copies predicates from the second
onto the first. E.g. *Remember the current location as the starting location* 
will copy the starting predicate onto the current location. 

```
op_remember1(arg1:object, arg2:object)

No preconditions/goal, but does work during search
```


### Remove

Removes an object from the world, only used with an internal world

```
op_remove1(arg1:object)

  Goal: arg1 is not in the world
  Post: remove arg1
```

**send-remove-command**
* Internal: Removes the object from world.objects and any relations involving arg1


### Say

Speak a message, can be directed at a specific person

```
1: Say message without a target
op_say1(arg1:object) 

  Not used during planning/search
  Goal: execute-command(say-command)

2: Say message to a person
op_say1(arg1:object, arg2:partial-predicate=to1(per))

  Pre:  message(arg1), person(per), !heard(per, arg1)
  Goal: heard(per, arg1)
  Post: +heard(per, arg1)

```

**send-say-command**, requires visible1(per)

All domains: Will send an outgoing-message and mark the sentence as being heard by the person. 
Note that if the arg1 object has a ^sentence it will say that verbatim, otherwise it will describe the arg1 object. 


### Turn Off

Have the robot turn off an appliance, e.g. a microwave

```
op_turn-off1(arg1:object)

  Pre:  on2(arg1)
  Goal: on2(arg1)
  Post: +off2(arg1), -on2(arg1)
```

**send-turn-off-command**, requires visible1(arg1) and reachable1(arg1)
* Internal: Change the predicate on the input link to off2
* Ai2Thor: Turn Off is a primitive action
* Tabletop: Turn Off is a primitive action (set-state)
* Magicbot: Turn Off is a primitive action (do-control-law set-state)


### Turn On

Have the robot turn on an appliance, e.g. a light switch 

```
op_turn-on1(arg1:object)

  Pre:  off2(arg1)
  Goal: on2(arg1)
  Post: +on2(arg1), -off2(arg1)
```

**send-turn-on-command**, requires visible1(arg1) and reachable1(arg1), and closed2(arg1) if it has a door
* Internal: Change the predicate on the input link to on2
* Ai2Thor: Turn On is a primitive action
* Tabletop: Turn On is a primitive action (set-state)
* Magicbot: Turn On is a primitive action (do-control-law set-state)


### Write

Write a value onto an object, used for logic puzzles such as sudoku. 

```
op_write1(arg1:concept, arg2:partial-predicate=pred(dest))

Goal: arg1(dest) (the object in arg2 has the value arg1)
Post: +arg1(dest)

**send-write-command**
* Internal: Adds the predicate value=arg1 to the arg2 object
