AGENT_NAME=blending3

all: ${AGENT_NAME}.world agent/rosie.${AGENT_NAME}.config

# Launches both the simulator and the test chat panel
run_evaluation: run_mobile_sim run_rosie

run_mobile_sim: ${AGENT_NAME}.world
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world > mobile_sim_output.txt &

run_rosie: agent/rosie.${AGENT_NAME}.config
	python3 -m rosie.evaluation.${AGENT_NAME} > rosie_output.txt &

# agent/rosie.eval-${AGENT_NAME}.config: Configures the rosie soar agent that will be sourced to run
agent/rosie.${AGENT_NAME}.config: internal-world.soar waypoint-map.soar agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

world: ${AGENT_NAME}.world waypoint-map.soar internal-world.soar

# ${AGENT_NAME}.world: Creates the world file that the mobile simulator will use
${AGENT_NAME}.world: world.info
	python3 -m rosie.tools.mapmaker world.info --world --output-dir . --agent-name ${AGENT_NAME}

# The internal world used during simulation
internal-world.soar: world.info
	python3 -m rosie.tools.mapmaker world.info --internal --output-dir .

# The connected waypoint map
waypoint-map.soar: world.info
	python3 -m rosie.tools.mapmaker world.info --map --output-dir .

clean:
	rm -f ${AGENT_NAME}.world
	rm -rf agent
	rm -rf __pycache__
	rm -f mobile_sim_output.txt
	rm -f rosie_output.txt
	rm -f waypoint-map.soar
	rm -f internal-world.soar
	rm -f ${AGENT_NAME}.log


