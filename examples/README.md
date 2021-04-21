# Rosie Examples

In this directory are several example Rosie agents that can run out of the box with a pre-loaded script.
They have been developed within a linux environment and have not been tested on other operating systems. 

**Requirements:**    
* Soar built with python3 SML library
* pysoarlib (https://github.com/amininger/pysoarlib) 
* MobileSim java simulator
* Both the $ROSIE_HOME and $MOBILE_SIM_HOME environment variables set
* Both pysoarlib and rosie/python/rosie are on the $PYTHONPATH

If the `source-rosie-env.sh` file has been sourced, then you can run these examples through the command:
```
$ run_rosie_example finding-objects
```

Each example also has a Makefile, so in the directory you can also run the example through `make run_example`


## finding-objects

This example shows different ways that the robot finds objects in a multi-room environment. 

```
$ run_rosie_example finding-objects
```

You can also run an internal test without needing the simulator through:
```
rosie/examples/finding-objects$ make run_test
```

## tourguide

This example involves teaching the agent to give a tour of the office, including
serving a drink and then leading the person to the lab and telling them about it. 
It also has the agent repeat the task as a test with no further instructions. 

```
$ run_rosie_example tourguide
```

## watercooler

This example involves teaching Rosie to fill a cup with water using a watercooler, 
where it does not have a pre-existing model of the button. 
It shows how the agent can utilize procedural steps (pressing the button)
within a goal-based task (the goal is that the cup contains water). 

Note: the agent may pause for some time while searching for the next action to take. 

```
$ run_rosie_example watercooler
```

You can also run an internal test without needing the simulator through:
```
rosie/examples/watercooler$ make run_test
```



