all: agent/rosie.eval-find-test.config

world: find-test.world

# Launches both the simulator and the test chat panel
run-test: agent/rosie.eval-find-test.config
	java soargroup.mobilesim.MobileGUI -w find-test.world &
	python3 -m rosie.evaluation.find-test

# Launches the simuator
run-sim: find-test.world
	java soargroup.mobilesim.MobileGUI -w find-test.world &

# agent/rosie.eval-find-test.config: Configures the rosie soar agent that will be sourced to run
agent/rosie.eval-find-test.config: agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar find-test.world find-test.script
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# find-test.world: Creates the world file that the mobile simulator will use
find-test.world: world.info
	python3 -m rosie.tools.mapmaker world.info --world
	mv $(MOBILE_SIM_HOME)/worlds/world.world find-test.world

clean:
	rm -rf agent
	rm -rf __pycache__


