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


### Remove

Removes an object from the world, only used with an internal world

```
op_remove1(arg1:object)

Goal: arg1 is not in the world
Post: remove arg1
```

**send-remove-command**
* Internal: Removes the object from world.objects and any relations involving arg1

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
op_write1(arg1:concept, arg2:partial-predicate)

Goal: arg1(arg2.obj2) (the object in arg2 has the value arg1)
Post: +arg1(arg2.obj2)

**send-write-command**
* Internal: Adds the predicate value=arg1 to the arg2 object
