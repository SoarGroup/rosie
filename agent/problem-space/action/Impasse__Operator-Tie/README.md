# Action Impasse Operator-Tie

These rules control the selection space provided by the default rules in 
order to evaluate tied task operators and select the one that will help achieve the task goal. 

**selection state** *(set up in problem-space.soar)*

Create a shallow copy of the superstate

**elaborations**

Don't evaluate force-operator-tie operators

### evaluate-operator

In the default rules, for each tied operator the agent will propose evaluate-operator

**evaluate-operator state** *(set up in problem-space.soar)*

Will create a deep copy of the superstate 
and simulate applying the operator to the world. 

```
([s] ^problem-space [ps])
([ps] ^propose-subtasks true
      ^execution-type internal
      ^default-state-copy no
      ^default-operator-copy custom
      ^world.copy-type deep
      ^subtasks.copy-type deep)
```

**elaborations**

Create a success evaluation if the desired goal is satisfied





