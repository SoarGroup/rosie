# detect-discrepancies

These rules compare perception and belief and if a discrepancy is detected, 
they will create a structure on the perception-monitor which notes the discrepancy. 

All of these structures are created on:
```
(top-state [s] ^perception-monitor.discrepancies [discs])
```

**new-perception-object**

There is an object in perception that is not in belief

```
([discs] ^new-perception-object [disc])
  ([disc] ^input-link-obj [il-obj])
```

**new-belief-object**

There is an object in belief that is not in perception

```
([discs] ^new-belief-object [disc])
  ([disc] ^object-info [obj-info])
```

**different-object-predicate**

The object-predicate predicate on the object doesn't match the input-link

```
([discs] ^different-object-predicate [disc])
  ([disc] ^object-info [obj-info]
          ^property-handle [prop]
          ^predicate-handle [pred])
```

**different-object-status**

One of the status predicates on the object-info doesn't match the world object

```
([discs] ^different-object-predicate [disc])
  ([disc] ^object-info [obj-info] 
          ^property-handle [status-prop]
          ^predicate-handle [status-pred])
```

**grown-object**

The volume of the perception object is significantly larger than the belief object

```
([discs] ^grown-object [disc])
  ([disc] ^object-info [obj-info])
```

**moved-object**

The positions of the belief and perception objs are significantly different

```
([discs] ^moved-object [disc])
  ([disc] ^object-info [obj-info])
```

**shrunken-object**

The volume of the perception object is significantly smaller than the belief object

```
([discs] ^shrunken-object [disc])
  ([disc] ^object-info [obj-info])
```

**missing-object**

There is a belief object that should be visible but is not

```
([discs] ^missing-object [disc])
  ([disc] ^object-info [obj-info])
```

**different-robot-status**

The robot/arm status on the input-link is different than the world

```
([discs] ^different-robot-status [disc])
  ([disc] ^robot-status [status])
    OR
  ([disc] ^arm-status [arm-status])
```

**different-waypoint**

The current-waypoint on the input-link doesn't match the world

```
([discs] ^different-waypoint [disc])
  ([disc] ^waypoint-handle [wp-handle])
```


