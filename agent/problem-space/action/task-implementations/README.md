# Task Implementations

### Approach

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


### Turn Off

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
