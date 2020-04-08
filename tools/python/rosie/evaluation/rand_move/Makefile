all: agent/rosie.eval-rand-move.config

run_evaluation: agent/rosie.eval-rand-move.config
	java soargroup.mobilesim.MobileGUI -w rand-move.world &
	python -m rosie.evaluation.rand_move

# agent/rosie.eval-rand-move.config: Configures the rosie soar agent that will be sourced to run
agent/rosie.eval-rand-move.config: agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar rand-move.world rand-move.script
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# rand-move.world: Creates the world file that the mobile simulator will use
rand-move.world: world.info
	python3 -m rosie.tools.mapmaker world.info
	mv $(MOBILE_SIM_HOME)/worlds/world.world rand-move.world

# rand-move.script: Makes the script to be used during the evaluation 
rand-move.script: 
	python3 generate_script.py

clean:
	rm rand-move.script
	rm rand-move.world
	rm -rf agent


