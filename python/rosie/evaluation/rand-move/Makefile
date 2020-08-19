all: agent/rosie.rand-move.config

script: rand-move.script 

world: rand-move.world

run_mobile_sim: rand-move.world
	java soargroup.mobilesim.MobileGUI -w rand-move.world

run_evaluation: agent/rosie.rand-move.config
	java soargroup.mobilesim.MobileGUI -w rand-move.world &
	python3 -m rosie.evaluation.rand-move

# agent/rosie.rand-move.config: Configures the rosie soar agent that will be sourced to run
agent/rosie.rand-move.config: agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar rand-move.world rand-move.script
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# rand-move.world: Creates the world file that the mobile simulator will use
rand-move.world: world.info
	python3 -m rosie.tools.mapmaker world.info --world
	mv $(MOBILE_SIM_HOME)/worlds/world.world rand-move.world

# rand-move.script: Makes the script to be used during the evaluation -- given the number of moves to do
rand-move.script: generate_script.py
	python3 generate_script.py 100

clean:
	rm -f rand-move.script
	rm -f rand-move.world
	rm -rf agent
	rm -rf __pycache__


