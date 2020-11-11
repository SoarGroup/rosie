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

* [ Pick Up ](#pickup)
* [ Put Down ](#putdown)
* [ Open ](#open)
* [ Close ](#close)
* [ Turn On ](#turnon)
* [ Turn Off ](#turnoff)

<!-- ===================================================================================== -->
<!-- ================================== PHYSICAL ========================================= -->
<!-- ===================================================================================== -->

# Physical Tasks
Tasks intended to change something in the external environment

<!-- ==================================  PICK UP ========================================= -->
<a name="pickup"></a>

## Pick Up
Have the robot pick up an object that is grabbable, e.g. a cup
> *Pick up the fork.*

```
([o] ^name op_pick-up1 
     ^task-handle pick-up1
     ^arg1 [type:object])  # The object to pick up
```

*Proposal:* <br>
`arm.holding-object(false) & grabbable1(arg1) & not-grabbed1(arg1) & confirmed1(arg1)` <br>

*Goal:* <br>
`grabbed1(arg1)`

*Action Model:* <br>
`+grabbed1(arg1), -not-grabbed1(arg1), +holding-object(true), -holding-object(false)` <br>
`-relation(arg1, any), -relation(any, arg1)` <br>
if `in(arg1, rec) & receptacle(rec) & openable(rec)` then `open(rec)` must also exist <br>

*Execute:* <br>
requires `visible1(arg1) & reachable1(arg1) & not-grabbed1(arg1)`
* Internal: Change the held object on the input link
* Ai2Thor: Pickup is a primitive action
* Tabletop: Pickup is a primitive action 
* Magicbot: Pickup is a primitive action 


<!-- ==================================  PUT DOWN ========================================= -->
<a name="putdown"></a>

## Put Down
Have the robot put down an object it is holding, at a particular place
> *Put down the block. Put the can into the pantry. Put the fork onto the table.*

```
([o] ^name op_put-down1 
     ^task-handle op_put-down1
     ^arg1 [type:object])  # The object to put down
     ^arg2 [type:partial-predicate]) # Where to put it (optional)
```

*Proposal:* <br>
`grabbed1(arg1) -> no arg2` <br>
`grabbed1(arg1) & confirmed1(dest) & surface1(dest) -> arg2=on1(dest)` <br>
`grabbed1(arg1) & confirmed1(dest) & receptacle(dest) -> arg2=in1(dest)` <br>
`grabbed1(arg1) & confirmed1(dest) & receptacle(dest) & openable(dest) & open2(dest) -> arg2=in1(dest)` 

*Goal:* <br>
`no arg2 -> not-grabbed1(arg1)` <br>
`has arg2 -> arg2.relation(arg1, arg2.obj2)`

*Action Model:* <br>
`+arg2.relation(arg1, arg2.obj2), +in1(arg1, cur-loc) `<br>
`-grabbed1(arg1), +not-grabbed(arg1), -holding-object(true), +holding-object(false)` 

*Execute:* <br>
requires `visible1(dest) and reachable1(dest)`
* Internal: Remove the holding-object on the simulated input-link and add the arg2 relation
* Ai2Thor: Put down is a primitive action
* Tabletop: Put down is a primitive action
* Magicbot: Put down is a primitive action
* Cozmo: Put down is a primitive action 


<!-- ================================== OPEN ========================================= -->
<a name="open"></a>

## Open

Have the robot open an object, e.g. a door or a fridge
> *Open the pantry.*

```
([o] ^name op_open1 
     ^task-handle open1
     ^arg1 [type:object])  # The object to open
```

*Proposal:* <br>
`arm.holding-object(false) & not-open1(arg1) & confirmed1(arg1)`

*Goal:* <br>
`open2(arg1)`

*Action Model:* <br>
`+open2(arg1), -not-open1(arg1)`

*Execute:* <br>
requires `visible1(arg1) & reachable1(arg1)`
* Internal: Change the predicate on the input link to open2
* Ai2Thor: Open is a primitive action
* Tabletop: Open is a primitive action (set-state)
* Magicbot: Open is a primitive action (do-control-law set-state)


<!-- ================================== CLOSE ========================================= -->
<a name="close"></a>

## Close

Have the robot close an object, e.g. a door or a fridge
> *Close the oven.*


```
([o] ^name op_close1 
     ^task-handle close1
     ^arg1 [type:object])  # The object to close
```

*Proposal:* <br>
`arm.holding-object(false) & open2(arg1) & confirmed1(arg1)`

*Goal:* <br>
`not-open1(arg1)`

*Action Model:* <br>
`+not-open1(arg1), -open2(arg1)`

*Execute:* <br>
requires `visible1(arg1) & reachable1(arg1)`
* Internal: Change the predicate on the input link to not-open1
* Ai2Thor: Close is a primitive action
* Tabletop: Close is a primitive action (set-state)
* Magicbot: Close is a primitive action (do-control-law set-state)


<!-- ================================== TURN ON ========================================= -->
<a name="turnon"></a>

## Turn On

Have the robot turn off an object
> *Turn off the lamp.*

```
([o] ^name op_turn-on1 
     ^task-handle turn-on1
     ^arg1 [type:object])  # The object to turn on
```

*Proposal:* <br>
`arm.holding-object(false) & not-activated1(arg1) & confirmed1(arg1)`

*Goal:* <br>
`activated1(arg1)`

*Action Model:* <br>
`+activated1(arg1), -not-activated1(arg1)`

*Execute:* <br>
requires `visible1(arg1) & reachable1(arg1)`
* Internal: Change the predicate on the input link to activated1
* Ai2Thor: Turn-on is a primitive action
* Tabletop: Turn-on is a primitive action (set-state)
* Magicbot: Turn-on is a primitive action (do-control-law set-state)


<!-- ================================== TURN OFF ========================================= -->
<a name="turnoff"></a>

## Turn Off

Have the robot turn off an object, e.g. a light
> *Turn off the stove.*

```
([o] ^name op_turn-off1 
     ^task-handle turn-off1
     ^arg1 [type:object])  # The object to turn off
```

*Proposal:* <br>
`arm.holding-object(false) & activated1(arg1) & confirmed1(arg1)`

*Goal:* <br>
`not-activated1(arg1)`

*Action Model:* <br>
`+not-activated1(arg1), -activated1(arg1)`

*Execute:* <br>
requires `visible1(arg1) & reachable1(arg1)`
* Internal: Change the predicate on the input link to not-activated1
* Ai2Thor: Turn-off is a primitive action
* Tabletop: Turn-off is a primitive action (set-state)
* Magicbot: Turn-off is a primitive action (do-control-law set-state)


<!-- ================================== GIVE ========================================= -->
<a name="give"></a>

## Give

Have the robot give a held object to a person
> *Give the soda to Greg.*

```
([o] ^name op_give1 
     ^task-handle give1
     ^arg1 [type:object])  # The object to give
     ^arg2 [type:partial-predicate]) # to a person; arg2 = to1(per)
```

*Proposal:* <br>
`grabbed1(arg1) & person(per) & confirmed1(per)`

*Goal:* <br>
`holding1(per, arg1)`

*Action Model:* <br>
`+holding1(per, arg1), -grabbed1(arg1), +not-grabbed1(arg1),` <br>
`-holding-object(true), +holding-object(false)`

*Execute:* <br>
requires `visible1(per) & reachable1(per)`
* Internal: Add the holding1(per, arg1) predicate and mark as not grabbed
* Magicbot: Give is a primitive command


<!-- ================================== PRESS ========================================= -->
<a name="press"></a>

## Press

Have the robot press a button
> *Press the blue button.*

```
([o] ^name op_press1 
     ^task-handle press1
     ^arg1 [type:object])  # The object to press
```

*Proposal:* <br>
`pressable1(arg1) & confirmed1(arg1)`

*Goal:* <br>
`execute-command(press)`

*Execute:* <br>
requires `visible1(obj) & reachable1(obj)`
* Internal: 
* Magicbot: Press is a primitive command


<!-- ================================== REMOVE ========================================= -->
<a name="remove"></a>

## Remove

Removes an object from the world, only used with an internal world and game learning. 

```
([o] ^name op_remove1
     ^task-handle remove1
     ^arg1 [type:object])  # The object to remove
```

*Goal:* `execute-command(remove)`
* Internal: Removes the object from world.objects and any relations involving arg1


<!-- ================================== WRITE ========================================= -->
<a name="write"></a>

## Write

Write a value onto an object, used for logic puzzles such as sudoku. 

```
([o] ^name op_write1 
     ^task-handle write1
     ^arg1 [type:concept])  # The thing to write (e.g. number)
     ^arg2 [type:partial-predicate]) # where to write (on1(dest))
```

*Goal:* `execute-command(write)`
* Internal: Adds the predicate value=arg1 to the arg2 object


<!-- ===================================================================================== -->
<!-- ================================== MOVEMENT ========================================= -->
<!-- ===================================================================================== -->

# Movement

Primary purpose is to move the robot in some way. 


<!-- ================================== APPROACH ========================================= -->
<a name="approach"></a>

## Approach

Have the robot drive up close to an object in order to interact with it. 
> *Approach the table.*

```
([o] ^name op_approach1 
     ^task-handle approach1
     ^arg1 [type:object])  # The object to approach
```

*Proposal:* <br>
`visible1(arg1) & not-reachable1(arg1)`

*Goal:* <br>
`reachable(arg1)`

*Action Model:* <br>
`+reachable1(arg1), -not-reachable1(arg1)`

*Execute:*
* Internal: Set `reachable1 is-reachable1` on the input link, make all others `not-reachable1`
* Ai2Thor: Approach is a primitive action
* Cozmo: Approach is a primitive action
* Magicbot: Calculuate a target point to drive to (in front of the object), then use subtasks `drive-xy(target-pt) and face(arg1)`


<!-- ================================== DRIVE ========================================= -->
<a name="drive"></a>

## Drive

Have the robot drive forward, different environments use this in different ways. 
> *Drive forward. Drive through the door. Drive until you see the kitchen.*

No innate proposal rules, action models, or goals

* Internal: can say 'Drive through the door', only real valid use
* Ai2thor: go forward 1 step
* Magicbot: start driving forward, should specify until or through clause
* Cozmo: Go forward 300 mm



<!-- ================================== TURN ========================================= -->
<a name="turn"></a>

## Turn

Have the robot turn a given direction
> *Turn right. Turn around.*

```
([o] ^name op_turn1 
     ^task-handle turn1
     ^arg1 [type:concept])     # Must be a relative direction (left/right/around)
```

*Goal:* `execute-command(turn)`
* Magicbot: Turn the specified amount/direction


<!-- ================================== FACE ========================================= -->
<a name="face"></a>

## Face

Have the robot turn towards an object or coordinate
> *Face the apple.*

```
([o] ^name op_face1 
     ^task-handle face1
     # One of the following:
     ^arg1 [type:object]      # Face an object
     ^arg1 [type:coordinate]) # Face a xy coordinate
```
No proposal rule or action model

*Goal:* `execute-command(face)`
* Magicbot, Cozmo: Calculate amount to turn (yaw), then propose `send-face-command(yaw)`


<!-- ================================ GO TO LOC ======================================== -->
<a name="gotoloc"></a>

## Go to Location

Have the robot drive to another location in the world
> *Go to the kitchen*

```
([o] ^name op_go-to-location1 
     ^task-handle go-to-location1
     ^arg2 [type:partial-predicate]) # to a location; arg2 = to1(loc)
```

*Proposal:* <br>
`location(loc) & confirmed1(loc) & !current-location(loc)`

*Goal:* <br>
`current-location(loc)`

*Action Model:* <br>
`+current-location(loc), -current-location(old)`

*Execute:* (Internal, Cozmo, Magicbot)
* `go-to-waypoint1(wp)` for the waypoint associated with the location


<!-- ================================== GO TO WP ========================================= -->
<a name="gotowp"></a>

## Go to Waypoint
Have the robot drive to the given waypoint via its waypoint graph. <br>
(Internal use only, cannot speak a command)

```
([o] ^name op_go-to-waypoint1 
     ^task-handle go-to-waypoint1
     ^arg1 [type:waypoint]) # the goal waypoint
```

*Goal:* <br>
`current-waypoint(arg1)`

*Execute:* (Internal, Cozmo, Magicbot) <br>
It will do an a-star search to find the shortest waypoint path
using `go-to-next-waypoint1` for edge traversal


<!-- =============================== GO TO NEXT WP ======================================= -->
<a name="gotonextwp"></a>

## Go to next waypoint
Have the robot drive to an adjacent waypoint in the waypoint graph. <br>
(Note: this is only used internally to the robot). 

```
([o] ^name op_go-to-next-waypoint1 
     ^task-handle go-to-next-waypoint1
     ^arg1 [type:waypoint]) # the goal waypoint
```

*Execute:*
* Internal: just change the waypoint on the simulated input link
* Magicbot: issue a series of `go-to-xy` subtasks to follow the waypoint edge
* Cozmo: issue a series of `go-to-xy` subtasks to follow the waypoint edge


<!-- ================================== GO TO XY ========================================= -->
<a name="gotoxy"></a>

## Go to xy
Have the robot drive to the given coordinate (Magicbot, Cozmo). <br>
(Note: this is only used internally to the robot). 

```
([o] ^name op_go-to-xy1 
     ^task-handle go-to-xy1
     ^arg1 [type:coordinate]) # the goal xy coordinate
```

Will issue a `face-command`, then do a `go-to-xy-command`


<!-- ================================== LIFT ========================================= -->
<a name="lift"></a>

## Lift
Have the cozmo robot raise/lower the forklift
> *Lift up. Lift down.*

```
([o] ^name op_lift1 
     ^task-handle lift1
     ^arg1 [type:concept])     # should be up or down
```

No proposal rule or action model

*Goal:* `execute-command(lift)`
* Cozmo: Primitive action to move-lift either up or down


<!-- ================================== ORIENT ========================================= -->
<a name="orient"></a>

## Orient
Have the robot face a cardinal direction (north, south, east, west)
> *Orient north.*

```
([o] ^name op_orient1 
     ^task-handle orient1
     ^arg1 [type:concept])  # direction to face (NSEW)
```

No proposal rule or action model

*Goal:* `execute-command(orient)`
* Magicbot: send an orient command to the robot


<!-- ===================================================================================== -->
<!-- ================================== PERCEPTION ======================================= -->
<!-- ===================================================================================== -->

# Perception

Primary purpose is to change the sensory info or changing the perceptual status of an object.


<!-- ================================== LOOK ========================================= -->
<a name="look"></a>

## Look
Have the robot look up/down
> *Look up. Look down.*

```
([o] ^name op_look1 
     ^task-handle look1
     ^arg1 [type:concept])     # should be up or down
```
No proposal rule or action model

*Goal:* `execute-command(look)`
* Ai2Thor: Primitive action to Look-Up or Look-Down
* Cozmo: Primitive action to move-head either up or down


<!-- ================================== VIEW ========================================= -->
<a name="view"></a>

## View

Tries to move so that the given object is in view (has to be confirmed)
> *View the book.*

```
([o] ^name op_view1 
     ^task-handle view1
     ^arg1 [type:object])  # The object to view
```

*Proposal:* <br>
`object(arg1) or person(arg1) & confirmed1(arg1) & not-visible1(arg1)`

*Goal:* <br>
`visible1(arg1)`

*Action Model:* <br>
`+visible1(arg1), -not-visible1(arg1)`

*Execute:* (Magicbot, Cozmo)
* `op_face1(arg1)` try facing the object
* `op_open1(rec)` if inside a closed receptacle


<!-- ================================== FIND ========================================= -->
<a name="find"></a>

## Find

Tries to find an unconfirmed object (in the world, but no belief object)
> *Find a stapler. Find the garbage*

```
([o] ^name op_find1 
     ^task-handle find1
     ^arg1 [type:object])  # The object to find
```

*Proposal:* <br>
`object(arg1) or person(arg1) & not-confirmed1(arg1)`

*Goal:* <br>
`confirmed1(arg1)`

*Action Model:* <br>
`+confirmed1(arg1), -not-confirmed(arg1), +visible1(arg1), -not-visible1(arg1)`

*Execute:* (Internal, Magicbot, Cozmo)
* `op_go-to-location(loc)` if the object is in a known location
* `op_scan1(end(visible1(arg1)))` if we haven't tried that yet (not internal)
* `get-find-help` Ask the instructor for help
* `op_explore1(end(visible1(arg1)))` search the entire environment


<!-- ================================== SCAN ========================================= -->
<a name="scan"></a>

## Scan
Turn in place one full revolution, looking around the room
> *Scan the room. Scan until you see the book.*

```
([o] ^name op_scan1
     ^task-handle scan1
     ^end-clause [type:temporal-clause]) # Optionally specify end of scan
```

*Goal:* `execute-command(scan)`
* Magicbot: Do a full turn, stop if end clause is satisfied
* Cozmo: Do a full turn, stop if end clause is satisfied


<!-- ================================== EXPLORE ========================================= -->
<a name="explore"></a>

## Explore

Have the robot drive to each room and scan it, usually trying to find an object
> *Explore until you see the garbage.*

```
([o] ^name op_explore1 
     ^task-handle explore1
     ^end-clause [type:temporal-clause]) # usually present
```

No proposal rule or action model

*Goal:* end clause is met, or all waypoints were scanned

*Execute:* (Internal, Magicbot, Cozmo)
* If it has not scanned the current wp, propose `op_scan1()`
* If it has scanned the current wp, propose `op_go-to-waypoint(wp)` for the closest unvisited wp



<!-- ===================================================================================== -->
<!-- ================================ COMMUNICATION ====================================== -->
<!-- ===================================================================================== -->

# Communication

<!-- ================================== SAY ========================================= -->
<a name="say"></a>

## Say
Speak a message, can be directed at a specific person
> *Say "Hello". Say the message to Bob.*

```
([o] ^name op_say1
     ^task-handle say1
     ^arg1 [type:object])  # The message/object to say
     ^arg2 [type:partial-predicate]) # to a person (optional); arg2 = to1(per)
```

*Proposal:* <br>
`message(arg1), sentence(arg1, s), person(per), visible1(per), !heard2(per, arg1)`

*Goal:* <br>
`if arg2 -> heard2(per, arg1)` <br>
`no arg2 -> execute-command(say)`

*Action Model:* <br>
`+heard2(per, arg1)`

*Execute:* (All domains) <br>
requires `visible1(per)`
* Send an outgoing-message saying the sentence
* Mark the sentence as being heard by the person


<!-- ================================== DESCRIBE ========================================= -->
<a name="describe"></a>

## Describe
Describe an object in the world, can be directed at a specific person. 
> *Say the grabbed object. Say the emergency location to Bob.*

```
([o] ^name op_describe1
     ^task-handle describe1
     ^arg1 [type:object])  # The object to describe
     ^arg2 [type:partial-predicate]) # to a person (optional); arg2 = to1(per)
```

*Proposal:* <br>
`object(arg1), person(per), visible1(per), !heard2(per, arg1)`

*Goal:* <br>
`if arg2 -> heard2(per, arg1)` <br>
`no arg2 -> execute-command(describe)`

*Action Model:* <br>
`+heard2(per, arg1)`

*Execute:* (All domains) <br>
requires `visible1(per)`
* Send an outgoing-message describing the object
* Mark the object as being heard by the person


<!-- ================================== ASK ========================================= -->
<a name="ask"></a>

## Ask
Has the robot ask a question and then wait for a response from the instructor.
> *Ask "What time is the meeting?"*


```
([o] ^name op_ask1
     ^task-handle ask1
     ^arg1 [type:object]) # a message object with a sentence to ask
```
No proposal rule

*Goal:* <br>
`execute-command(ask)`

*Action Model:* <br>
`+new_obj, +entity(new_obj), +answered1(new_obj), -answered1(other)`

*Execute:*
* All Domains: 
Sends an outgoing-message to ask the question and pushes a new interaction segment to wait for the response. 
When it gets an answer, it adds the answered object to the world if necessary, then marks it with `modifier1=answered1` and completes.


<!-- ===================================================================================== -->
<!-- ==================================== MENTAL ========================================= -->
<!-- ===================================================================================== -->

# Mental

<!-- ================================== LABEL ========================================= -->
<a name="label"></a>

## Label
Label one object as another, merges the two referred objects together
E.g. *Label the current location as the starting location* 

```
([o] ^name op_xxx 
     ^task-handle give1
     ^arg1 [type:object])  # The object to give
     ^arg2 [type:partial-predicate]) # to a person; arg2 = to1(per)
```
```
op_remember1(arg1:object, arg2:object)

No preconditions/goal, but does work during search
```

