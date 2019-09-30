# Rosie Manual

## 1. Introduction


Test|Table
----|-----
Hello|World


## 2. Perception


## 3. Language Comprehension

## 4. Interaction

## 5. Executing Tasks

**Command Interpretation**

When the instructor gives a new command, the message is parsed and interpreted via ```interaction/interpret-message/interpret-command.soar```.
This will push a new segment onto the interaction stack which will look like:

```
([s] ^interaction.stack.segment.purpose [p]
([p] ^type execute-task
     ^parameters.command-message [msg]
	 ^satisfaction.task-event [e])
([e] ^type execute
     ^task-handle [task-handle])
```

The operator ```satisfy-purpose``` will be proposed on the top-state, and go into ```satisfy-purpose/task-event/handle-type-execute```. 

**



## 6. Task Learning

### 6.1 
