AGENT_NAME=serve

all: agent/rosie.${AGENT_NAME}.config

world: ${AGENT_NAME}.world

# Launches both the simulator and the test chat panel
run_evaluation: agent/rosie.${AGENT_NAME}.config
	make run_mobile_sim
	make run_rosie

run_mobile_sim: ${AGENT_NAME}.world
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world > mobile_sim_output.txt &

run_rosie: agent/rosie.${AGENT_NAME}.config
	python3 -m rosie.evaluation.${AGENT_NAME} > rosie_output.txt &

# agent/rosie.eval-${AGENT_NAME}.config: Configures the rosie soar agent that will be sourced to run
agent/rosie.${AGENT_NAME}.config: agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar ${AGENT_NAME}.world script.txt
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# ${AGENT_NAME}.world: Creates the world file that the mobile simulator will use
${AGENT_NAME}.world: world.info
	python3 -m rosie.tools.mapmaker world.info --world
	mv $(MOBILE_SIM_HOME)/worlds/world.world ${AGENT_NAME}.world

clean:
	rm -f ${AGENT_NAME}.world
	rm -rf agent
	rm -rf __pycache__
	rm -f mobile_sim_output.txt
	rm -f rosie_output.txt
	rm -f ${AGENT_NAME}.log


