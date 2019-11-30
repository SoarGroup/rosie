# retrospective-learning

```
([s] ^operator [o])
([o] ^name retrospective-learning
     ^task-handle [h])  # the task to learn
```


**1. compute-start-episode-id**

The agent retrieves the episode where the task began, using the `start-of-execution` flag as the marker, 
and copies the episode id to the state as `start-episode-id`. 


