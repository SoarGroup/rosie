# attend-to-perception

Once a discrepancy is detected, the agent can use the attend-to-perception operator 
to discern why the discrepancy occurred and what to do about it. 
To enable these operators on a state, simply add `([s] ^problem-space.attend-to-perception yes)`
to that state. These operators may change the world on the top-state, but they 
do not usually GDS to re-enter the substate. 
The operator proposed will look like the following:

```
# Given a discrepancy perception-monitor.discrepancies.[type] [info]
([s] ^operator [o])
  ([o] ^name attend-to-perception
       ^discrepancy-type [type] # e.g. missing-object, different-waypoint
       ^discrepancy-info [info])
```

## new-perception-object

There is an object in perception that is not in belief

```
([o] ^name attend-to-perception
     ^discrepancy-type new-perception-object
     ^discrepancy-info [info])
  ([info] ^input-link-obj [il-obj])
```

(Note: most of this is done inside manage-world-state/add-object-to-world)

* **Misidentified Anchor**: 
The agent determines the new perception object matches a non-visible object
concluding that this new object is a new id for an existing object.
It uses `change-world-state/change-perception-id` to update the existing object's perception id to the new one.

* **Merged Anchor (End)**:
The agent determines the new perception object matches an object which had been segmented together with another (shared-input-link-obj), 
concluding that this must be over and perception segmented them separately again. 
It uses `change-world-state/change-perception-id` to change the missing object's perception-id
to the new one.

* **Fragmented Anchor**:
The agent determines the new perception object matches an existing object that already has a perception object,
concluding that the object was segmented into multiple pieces. 
It uses `change-world-state/merge-perception-objects` to tell perception to merge these objects together.

* If the object doesn't match an existing one, add it to the world 
(using `construct-world-object` and then `change-world-state/add-object-to-world`).



## different-object-predicate

The object-predicate predicate on the object doesn't match the input-link

```
([o] ^name attend-to-perception
     ^discrepancy-type different-object-predicate
     ^discrepancy-info [info])
  ([info] ^object-info [obj-info]
          ^property-handle [prop]
          ^predicate-handle [pred])
```

Add the given predicate to the world object and remove 
other predicates with the same property-handle.
(e.g. if we add ^color red then we would remove any other color predicates).

## grown-object

The volume of the perception object is significantly larger than the belief object

```
([o] ^name attend-to-perception
     ^discrepancy-type grown-object
     ^discrepancy-info [info])
  ([info] ^object-info [obj-info])
```

* **Merged Anchor**:
Using `intersect-command` and `overlap-command`, the agent
finds an object without a perception volume whose belief volume is contained inside the 
grown object's perception volume, concluding that they have been segmented together. 
It uses `change-world-state/change-perception-id` to change the missing object's perception-id
to the grown one, meaning that multiple belief objects will have a shared-input-link-obj (and thus ignore the discrepancy). 

* Otherwise: use `change-world-state/update-object-pose` to update the belief svs object.


## moved-object

The positions of the belief and perception objs are significantly different

```
([o] ^name attend-to-perception
     ^discrepancy-type moved-object
     ^discrepancy-info [info])
  ([info] ^object-info [obj-info])
```


* If occluded (determined via `occlusion-command`), then ignore the discrepancy.
* If not occluded, use `change-world-state/update-object-pose` to move the belief svs object.


## shrunken-object

The volume of the perception object is significantly smaller than the belief object

```
([o] ^name attend-to-perception
     ^discrepancy-type shrunken-object
     ^discrepancy-info [info])
  ([info] ^object-info [obj-info])
```

* **Fragmented Anchor**:
Using `intersect-command` and `overlap-command`, 
the agent finds a newly appeared perception object whose volume is contained inside the shrunken object's belief volume, 
concluding that the object was segmented into multiple pieces. 
It uses `change-world-state/merge-perception-objects` to tell perception to merge these objects together.

* If occluded (determined via `occlusion-command`), ignore the discrepancy.
* If not occluded, use `change-world-state/update-object-pose` to update the belief svs object's size.


## missing-object

There is a belief object that should be visible but is not

```
([o] ^name attend-to-perception
     ^discrepancy-type missing-object
     ^discrepancy-info [info])
  ([info] ^object-info [obj-info])
```

* **Merged Anchor**:
Using `intersect-command` and `overlap-command`, the agent
finds an object whose perception volume contains the missing object's belief volume, 
concluding that they have been segmented together. 
It uses `change-world-state/change-perception-id` to change the missing object's perception-id,
meaning that multiple belief objects will have a shared-input-link-obj (and thus ignore the discrepancy). 

* **Misidentified Anchor**: 
Using `intersect-command` and `overlap-command`, 
the agent finds a newly appeared perception object whose volume is contained inside the missing object's belief volume, 
concluding that the same object was given a different id. 
It uses `change-world-state/change-perception-id` to update the perception id to the new one.

* If occluded (determined via `occlusion-command`), ignore the discrepancy.
* If not occluded, use `change-world-state/delete-object` to delete the object



## different-object-status

The robot/arm status on the input-link is different than the world

```
([o] ^name attend-to-perception
     ^discrepancy-type different-robot-status
     ^discrepancy-info [info])
  ([info] ^robot-status [status])
    OR
  ([info] ^arm-status [arm-status])
```

Either update `world.robot.moving-status` or `world.robot.arm.moving-status`


## different-waypoint

The current-waypoint on the input-link doesn't match the world

```
([o] ^name attend-to-perception
     ^discrepancy-type different-waypoint
     ^discrepancy-info [info])
  ([info] ^waypoint-handle [wp-handle])
```

The agent does the following when it enters a new waypoint:
1. Extracts pose info form SVS for all objects with a belief volume (capturing it in epmem).
1. Removes all non task-related objects from the world and SVS. 
1. Retrieves the new location from smem and adds it to the world. 
1. Retrieves the episode of the last time it was in the new location. 
1. Adds all objects in that episode to the world, with their belief volumes (from the pose info). 
1. Updates the current-waypoint and current-location on the world.robot


## match-existing-object

A utility operator to see if a new object actually matches one that already exists in the world

```
# Matching belief objects
 ([o] ^name match-exising-object
      ^source-type belief
      ^source-obj [obj-info] # from object-monitor.object-info
 
# Matching perception objects
 ([o] ^name match-exising-object
      ^source-type perception
      ^source-obj [il-obj] # from the input-link.objects.object

# Will return either of these to the state on which the operator is proposed
 ([s] ^matches-existing-object none)
 ([s] ^matches-existing-object [obj-info])
```

**matches-overlapping-object**

1. The new perception object overlaps an existing object that doesn't have a perception object
1. The new perception object overlaps an existing object with a shared-input-link-obj
1. The new perception object is contained within an existing belief object (not a receptacle)

**matches-object-predicates**

1. A perception object can match any world object with no belief obj
1. A belief object can match any other object on predicates

Note: If both source and candidate have svs belief objects, it rejects the match

A predicate match occurs if the candidate has at least 1 matching predicate, no conflicting predicates, 
and more matching predicates will be preferred.
Predicate conflicts if the candidate has a different predicate for the same property

