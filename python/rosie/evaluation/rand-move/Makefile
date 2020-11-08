AGENT_NAME=rand-move

ROSIE_CONFIG=agent/rosie.${AGENT_NAME}.config

all: world rand-move.script ${ROSIE_CONFIG}


# Launches both the simulator and the test chat panel
run_evaluation: run_mobile_sim run_rosie

run_mobile_sim: ${AGENT_NAME}.world
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world > mobile_sim_output.txt &

run_rosie: waypoint-map.soar internal-world.soar rand-move.script ${ROSIE_CONFIG} 
	python3 -m rosie.evaluation.${AGENT_NAME} ${AGENT_TYPE} > rosie_output.txt &


# agent/rosie.rand-move.config: Configures the rosie soar agent that will be sourced to run
${ROSIE_CONFIG}: agent.config smem-config.txt extra-smem-adds.soar rand-move.script
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# rand-move.script: Makes the script to be used during the evaluation -- given the number of moves to do
rand-move.script: generate_script.py
	python3 generate_script.py 100

#### Generating World/Map Files

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


#### Clean all files

clean:
	rm -f ${AGENT_NAME}.world
	rm -rf agent
	rm -rf __pycache__
	rm -f rand-move.script
	rm -f mobile_sim_output.txt
	rm -f rosie_output.txt
	rm -f waypoint-map.soar
	rm -f internal-world.soar
	rm -f ${AGENT_NAME}.log


