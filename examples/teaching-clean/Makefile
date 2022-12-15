AGENT_NAME=cleaner

ROSIE_CONFIG=agent/rosie-client.config

#### Running Various Build Scripts

all: world agent 

agent: ${ROSIE_CONFIG}

world: ${AGENT_NAME}.world waypoint-map.soar internal-world.soar


#### Running Different Versions

run_example: run_mobile_sim run_rosie

run_mobile_sim: ${AGENT_NAME}.world
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world -f &

run_rosie: waypoint-map.soar internal-world.soar ${ROSIE_CONFIG} 
	python3 -m mobilesim.rosie.MobileRosieGUI ${ROSIE_CONFIG} --hide-script &


#### Configuring Rosie Agents

# agent/rosie.template.config: Configures the rosie soar agent that will be sourced to run
${ROSIE_CONFIG}: rosie.config smem-config.txt extra-smem-adds.soar
	java edu.umich.rosie.tools.config.RosieAgentConfigurator rosie.config


#### Generating World/Map Files

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
	rm -rf __pycache__
	rm -rf *.log
	rm -f ${AGENT_NAME}.world
	rm -f waypoint-map.soar
	rm -f internal-world.soar




