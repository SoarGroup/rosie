# change-world-state

This operator can be proposed anywhere, with multiple changes being applied. 
All the changes will happen in 1 elaboration cycle, so it designed to be somewhat atomic.
(it will create SVS commands, but they will not take effect until next DC)

Below are the different structures than can be elaborated onto the operator 
You can mix and match as many as you want. 

```
(state [s] ^operator [o])
  ([o] ^name change-world-state

       # Will add the object to the world, SVS, and object-monitor
        ^add-object-to-world
          ^object [obj] # should be a valid world object representation (from construct-world-object)
          ^perception-id [perc-id] # optional - use to create a belief object in SVS

       # Will create a new belief object by copying the perception-obj
       ^create-belief-object
         ^belief-id [bel-id]
         ^perception-obj [perc-obj]

       # OR: create a belief obj using bounding box paramteters
       ^create-belief-object
         ^belief-id [bel-id]
         ^parameters
           ^position { ^x ^y ^z }
           ^rotation { ^x ^y ^z }
           ^scale { ^x ^y ^z }
  
       # Will change [obj-info] ^perception-id [id] to the given one
       ^change-perception-id
         ^object-info [obj-info]
         ^perception-id [new-id]
  
  
       # Will copy pose information from the perception-obj to the belief-obj
       ^update-object-pose [obj-info]
  
       # Will merge the given object-info's together into the core-object-info
       # (copy predicates over and update all references to be the core-object-info)
       ^merge-belief-objects
         ^core-object-info [core-obj-info]
         ^merging-object-info [obj_info1] [obj_info2] ...)
  
       # Will tell perception to merge the ids together to be the core-id
       ^merge-perception-objects
         ^core-perception-id [core-id]
         ^merging-perception-id [id1] [id2] ...)
  
       # Will delete the object from working memory and SVS
       #   (If the object is a task-object, will only delete from SVS)
       ^delete-object [obj-info]

  )
``` 
