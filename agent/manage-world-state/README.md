# Constructing the World Representation

```
rosie/agent/manage-world-state
```

The agent must deliberately handle perceptual updates and 
change the world representation. This is done in four steps: 

1. **Monitoring Perception**:
The agent has two representations of the world, there is the
one coming in from perception on the input-link and SVS, 
and the other is its belief representation of the world
on the top-state and SVS. (Note that an object may have two 
versions of itself in SVS.) 

1. **Detecting Discrepancies**:
The agent is continually comparing these
two representations using elaborations and SVS filters.
If a discrepancy is detected, it will create a structure
noting it on the perception-monitor. 

1. **Attending to a Discrepancy**:
Once a discrepancy is detected, the agent can propose the
attend-to-perception operator to analyze the discrepancy
and determine its cause. The result can be either
updating the belief world representation, or 
attributing it to noise/errors and ignoring it. 

1. **Updating the World**: 
If a discrepancy was determined be caused by an actual 
change in the environment, the agent can use the 
chnage-world-state operator to modify its belief world representation.


## Input Link Representation

This is what information should be on the input-link. 
Each object on the input-link should also be accompanied by 
an object in SVS with an id matching the object-handle
and with the tag object_source=perception

```
(top-state.io.input-link [il])
  ([il] ^self [self]
        ^objects [objs]
        ^time [time]
        
   ([self] ^pose.{ ^x ^y ^z ^roll ^pitch ^yaw } # in meters and radians
           ^current-waypoint [wp-handle] or none
           ^moving-status [[ moving stopped ]]
           ^arm [arm])
      ([arm] ^moving-status [[ moving stopped ]]
             ^holding-object [obj-id] or none)

   ([objs] ^object [obj1] [obj2] ...)
     ([obj1] ^object-handle [obj1-handle]
             ^property [o1prop1] [o1prop2] ...)
       ([o1prop1] ^property-handle [prop1-handle]
                  ^values [p1vals])
         ([p1vals] ^[pred1] [p1conf] ^pred2 [p2conf] ...) 

   ([time] ^seconds [secs] # real-time elapsed since agent started 
           ^steps [steps] 
           ^clock [clock])
     ([clock] ^hour [hour] # 0-23
              ^minute [min] # 0-59
              ^second [sec] # 0-59
              ^elapsed-seconds [secs] # clock time elapsed since agent started

```

### Simulating the Input Link 

```
manage-world-state/simulate-perception
```

If the domain is internal and `agent-params.simulate-perception true` then 
create a simulated input-link on the perception-monitor and populate it 
with information from an internal-world file. 

```
manage-world-state/internal-worlds
```

This directory contains a collection of internal world models which are used to populate the simulated input-link.
To use one, add the line `internal-world-file = world_file.soar` to the agent config file. 


## World Representation

``` 
manage-world-state/initialized-world.soar
manage-world-state/elaborate-world.soar
```

The representation of the world on the top state. 

```
(state [s] ^world [w])
  ([w] ^objects [objs]
       ^predicates [preds]
       ^robot [rob])

    ([objs] ^object [obj1] [obj2] ...)
      ([obj1] ^handle [obj1-handle]
              ^item-type object
              ^root-category [cat]
              ^predicates [obj1-preds])
         ([obj1-preds] ^[prop1] [pred1] ^[prop2] [pred2] ...)

    ([preds] ^predicate [pred1] [pred2] ...)
       ([pred1] ^handle [p1-handle]
                ^instance [p1i1] [p1i2] ...)
         ([p1i1] ^1 [obj_i1] ^2 [obj_i2])

    ([rob] ^handle rosie
           ^predicates [rob-preds]
           ^arm [arm]
           ^moving-status [[ wait stopped moving ]] ) # if the robot is moving
      ([rob-preds] ^name rosie)
      ([arm] ^state [[ internal external ]] # if the arm is real or imagined
             ^moving-status [[ wait stopped moving ]] # what the arm is doing


```

### construct-world-object

```
manage-world-state/construct-world-object.soar
```

This utility operator can be used to construct an object in the proper world reprensentation
from a variety of sources, including smem, epmem, TCN, and interaction. 
It assumes that there is some root category information, and it will do an smem 
query to retrieve category information. In doing so, it will add all 
super-categories to the object, and affordances. 
(e.g. if the root category is table, it will also add predicates category=furniture 
and category=object, plus affordance=surface)


```
([s] ^operator [o])
([o] ^name construct-world-object
     ^source-type [[ perception smem tcn-obj-slot imagined ]]
     ^source-obj [obj]
     ^result-destination [dest]  # OPTIONAL
     ^result-name [res-name])    # OPTIONAL

Will create the object and copy it to ([dest] ^[res-name] [world-obj])

If no result info is given, defaults as 
([s] ^constructed-world-object [cons])
  ([cons] ^source [src-obj] ^result [world-obj])
```

## Waypoint Maps 

```
manage-world-state/waypoint-maps
```

This directory contains a collection of waypoint maps which are used during navigation to reach other locations. 
To use one, add the line `waypoint-map-file = map_file.soar` to the agent config file. 

**Waypoint Map Representation**

The waypoint map is on the top state and contains a list of all waypoints, 
plus edges between them. If two waypoints are connected, there should be 
an edge in both directions. Edges are labeled with either doorway=true or doorway=false. 
If doorway=false, then if the agent is traversing the edge it will drive straight
to the end waypoint's coordinate. If doorway=true, it will drive to (door_sx, door_sy), then
(door_ex, door_ey), then finally the end waypoint's coordinate. 

```
(top-state [s] ^maps [maps])
  ([maps] ^map [map])
    ([map] ^handle [map-handle] 
           ^waypoint [wp1] [wp2] ...) # List of all waypoints

      ([wp1] ^handle [wp1-handle] ^x [x1] ^y [y1] ^map [map] ^edge [wp1e1] [wp1e2] ... )
        ([wp1e1] ^start [wp1] ^end [wp2] ^doorway false)
        ([wp1e2] ^start [wp1] ^end [wp3] ^doorway true 
           ^door_sx [sx] ^door_sy [sy] ^door_ex [ex] ^door_ey [ey]) 
      
      ([wp2] ^handle [wp2-handle] ^x [x2] ^y [y2] ^map [map] ^edge [wp2e1])
        ([wp2e1] ^start [wp2] ^end [wp1] ^doorway false)  
```



## 1. Monitoring Perception

```
manage-world-state/perception-monitor
```

These rules are responsible for analyzing perception (input-link and SVS) and 
storing intermediate work for comparing the world and perception. 
It includes extracing predicates using SVS (such as in or right-of)
and unifying all that is known about an object from both belief and perception.

```
(top-state [s] ^perception-monitor [perc])
  ([perc] ^input-link [il]     # Either real (top-state.io) or simulated
          ^object-monitor [obj-mon]
          ^predicate-monitor [pred-mon]
          ^discrepancies [discs]
          # In some domains, calculate which objects intersect the view volume
          ^robot-view-filter [view]  
          # In some domains, calculate how far way objects are from the robot
          ^obj-distance-filter [dist] )
```

**Object Monitor**

This is just a collection of ^object-info wmes, each corresponding to a known object in the world. 
Each object-info contains all the unified information about that object, including links to 
the input-link, SVS, and world. 

```
(top-state.perception-monitor.object-monitor [obj-mon])
  ([obj-mon] ^object-info [obj-info]) # one per object
    ([obj-info] 
      ^object-handle [obj-handle] # REQUIRED

      # References to the perceptual version of the object
      ^perception-id [perc-id] 
      ^perception-obj [perc-obj] # From SVS, if it exists
      ^input-link-obj [il-obj]   # From input-link.objects, if it exists
      
      # References to the belief version of the object
      ^belief-id [bel-id]
      ^belief-obj [bel-obj] # From SVS, if it exists
      ^world-obj [world-obj] # From world.objects, should usually exist
      
      # Properties of the object from the input-link
      ^properties [props]
        ^[prop1-handle] [pred1-handle]
        ^[prop2-handle] [pred2-handle]

      # Status of the object (see object-monitor/elaborate-object-status.soar)
      ^status [status]
        # Whether the agent should be able to see the object (ignoring occlusion)
        ^in-view [[ true false ]] 

        # True if the agent determined the object is fully/partially occluded
        ^is-occluded [[ true false ]]

        # True if this object shares an input-link-obj with another object
        #   (They are segmented together)
        ^shared-input-link-obj true

        # How far away the object is from the robot
        ^distance [meters]

        # Whether the object is visible (on the input-link)
        ^is-visible1 [[ visible1 not-visible1 ]]
        
        # Whether the object is reachable (the arm can grab it)
        ^is-reachable [[ reachable1 not-reachable1 ]]

        # Whether the object is being held by the arm
        ^is-grabbed [[ grabbed1 not-grabbed1 ]]
      
    )
```

**Predicate Monitor**

Responsible for extracting relations on the top-state by retrieving
the relation info from SMEM and use it to construct svs filters 
that will compute objects satisfying the given relation. 
For example, for the ontop relation it checks to see if 
objects are aligned in the x and y axes but one object is greater 
than another along the z axis.

To extract a certain predicate, elaborate the following onto the predicate-monitor. 
(It will automatically populate the world with the results)

```
(top-state ^perception-monitor.predicate-monitor [pred-mon])
  ([pred-mon] ^predicate-info [pred-info])
    ([pred-info] ^predicate-handle [pred-handle])
```

## 2. Detecting Discrepancies 

```
manage-world-state/detect-discrepancies
```

These will create a structure on the perception-monitor.discrepancies, such as:
```
(top-state ^perception-monitor.discrepancies [discs])
  ([discs] ^new-perception-object [npo])
    ([npo] ^input-link-obj [il-obj])
```

1. **new-perception-object**: There is an object in perception that is not in belief
1. **new-belief-object**: There is a newly added world object that needs to be checked
1. **missing-object**: There is an object that is in-view, not-occluded, and not-visible

1. **moved-object**: Theposition of the perception object in SVS is significantly different than the position of the belief object. 
1. **grown-object**: The perception volume of an object is significantly larger than the belief volume 
1. **shrunken-object**: The perception volume of an object is significantly smaller than the belief volume

1. **different-object-predicate**: The object has a predicate in perception which is not in belief. 
1. **different-object-status**: The object has a status predicate which does not match the belief predicate (is-visible1, is-reachable1, is-grabbed1). 

1. **different-robot-status**: The input-link status of the robot/arm does not match the world.robot
1. **different-waypoint**: The input-link current-waypoint does not match the world.robot

(See `detect-discrepancies/README.md` for more information)


## 3. Attending to a Discrepancy

```
manage-world-state/attend-to-perception
```

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

Details about how the agent handles each discrepancy type is in `attend-to-perception/README.md`. 
It might change the world representation via the `change-world-state` operator
or it could choose to ignore the discrepancy: 

* **grown-object**: ignored if it detects it is segmented together with another object (shared-input-link-obj)
* **shrunken-object**: ignored if it determines the object is partially occluded
* **moved-object**: ignored if it determines the object is partially occluded
* **different-object-predicate**: ignored if it determines the object is partially occluded
* **new-perception-object**: ignores it until a set time has passed (e.g. 1 second) and it is still there


## 4. Updating the World

```
manage-world-state/change-world-state
```

If at some point the agent determines it needs to modify the world, it should
use the operator `change-world-state` to do so. This can be proposed
anywhere/anytime and should be elaborated with the following structures:

1. **add-object-to-world**: Add a new object to the world, SVS, and perception-monitor
1. **create-belief-object**: Create a new belief object in SVS, either a copy of a perception object
or a new bounding box with given parameters (pos/rot/scale). 
1. **update-object-pose**: Change the belief object's pose to match that of perception (pos/rot/scale)
1. **change-perception-id**: Change the perception-id on an object-info
1. **merge-belief-objects**: Take two or more belief objects and merge them into one.
1. **merge-perception-objects**: Tell perception the list of perception objects are all one (merge together)
1. **delete-object**: Deletes the object from the world, object-monitor, and SVS

(See `change-world-state/README.md` for more information)

   
