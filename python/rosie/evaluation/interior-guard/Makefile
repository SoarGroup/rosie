AGENT_NAME=interior-guard

EVAL_DIR=${ROSIE_HOME}/python/rosie/evaluation/${AGENT_NAME}

ROSIE_CONFIG=agent/rosie.${AGENT_NAME}.config
SIM_WORLD=${AGENT_NAME}.world

all: ${ROSIE_CONFIG}

# Launches both the simulator and the test chat panel
run_evaluation: ${ROSIE_CONFIG}
	make run_mobile_sim
	make run_rosie

run_mobile_sim: ${SIM_WORLD}
	java soargroup.mobilesim.MobileGUI -w ${SIM_WORLD} > mobile_sim_output.txt &

run_rosie: ${ROSIE_CONFIG}
	python3 -m rosie.evaluation.${AGENT_NAME} 
#> rosie_output.txt &

# ${ROSIE_CONFIG}: Configures the rosie soar agent that will be sourced to run
${ROSIE_CONFIG}: ${SIM_WORLD} agent.config smem-config.txt extra-smem-adds.soar extra-soar-rules.soar script.txt
	@echo "########## Configuring the agent ###########"
	java edu.umich.rosie.tools.config.RosieAgentConfigurator agent.config

# ${SIM_WORLD}: Creates the world file that the mobile simulator will use
${SIM_WORLD}: world.info
	@echo "########## Making ${SIM_WORLD} ###########"
	python3 -m rosie.tools.mapmaker world.info --world --output-dir ${EVAL_DIR} --agent-name ${AGENT_NAME}
	@echo "########## Making internal-world.soar ###########"
	python3 -m rosie.tools.mapmaker world.info --internal --agent-name eval_${AGENT_NAME}
	@echo "########## Making waypoint-map.soar ###########"
	python3 -m rosie.tools.mapmaker world.info --map --agent-name eval_${AGENT_NAME}

clean:
	rm -f ${SIM_WORLD}
	rm -rf agent
	rm -rf __pycache__
	rm -f mobile_sim_output.txt
	rm -f rosie_output.txt
	rm -f ${AGENT_NAME}.log


