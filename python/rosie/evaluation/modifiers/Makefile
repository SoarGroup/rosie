AGENT_NAME=modifiers

ROSIE_CONFIG=agent/rosie.${AGENT_NAME}.config

all: world ${ROSIE_CONFIG}


# Launches both the simulator and the test chat panel
run_evaluation: run_mobile_sim run_rosie

run_mobile_sim: ${AGENT_NAME}.world
	mkdir -p output
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world > output/mobile_sim_output.txt &

run_rosie: waypoint-map.soar internal-world.soar ${ROSIE_CONFIG} 
	mkdir -p output
	python3 -m rosie.evaluation.${AGENT_NAME} > output/rosie_output.txt &

run_test: waypoint-map.soar internal-world.soar ${ROSIE_CONFIG} 
	mkdir -p output
	python3 -m rosie.evaluation.${AGENT_NAME} --test
	echo "########### DIFF ##########"
	diff output/test-output.txt correct-output.txt


# agent/rosie.modifiers.config: Configures the rosie soar agent that will be sourced to run
${ROSIE_CONFIG}: agent.config smem-config.txt extra-smem-adds.soar
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config


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
	rm -rf agent
	rm -rf output
	rm -rf __pycache__
	rm -f ${AGENT_NAME}.world
	rm -f waypoint-map.soar
	rm -f internal-world.soar



