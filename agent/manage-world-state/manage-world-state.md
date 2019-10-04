# Constructing the World Representation

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

If the domain is internal and ```agent-params.simulate-perception true``` then 
create a simulated input-link on the perception-monitor and populate it 
with information from an internal-world file. 

```
manage-world-state/internal-worlds
```

This directory contains a collection of internal world models which are used to populate the simulated input-link.
To use one, add the line ```internal-world-file = world_file.soar``` to the agent config file. 


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
To use one, add the line ```waypoint-map-file = map_file.soar``` to the agent config file. 

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


## Perception Monitor

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

