import sys
import os

import tempfile
# write the given TemporaryFile to disk using the given filename
def save_temp_file(temp_file, filename):
    with open(filename, 'w') as f:
        temp_file.seek(0)
        f.write(temp_file.read())

from rosie.tools.mapmaker.parse_info_file import parse_info_file

from rosie.tools.mapmaker.create_world import create_world
from rosie.tools.mapmaker.create_waypoint_map import create_waypoint_map
from rosie.tools.mapmaker.create_map_info import create_map_info
from rosie.tools.mapmaker.create_internal_world import create_internal_world

#################### CHECK ARGUMENTS ##############################

if len(sys.argv) <= 1:
    print("python3 -m rosie.tools.mapmaker [map_info_file]")
    print("  map_file_file is a specification of the world to create in $ROSIE_HOME/tools/map_info")
    print("Will generate a set of world/map files for a rosie environment")
    print("")
    print("Example: python3 -m rosie.tools.mapmaker simple_office.info")
    print("  Input File : $ROSIE_HOME/tools/map_info/simple_office.info")
    print("")
    print("  Output File: $MOBILE_SIM_HOME/worlds/simple_office.world")
    print("  Output File: $MOBILE_SIM_HOME/worlds/maps/simple_office.map")
    print("  Output File: $ROSIE_HOME/agent/manage-world-state/waypoint-maps/simple_office.soar")
    print("  Output File: $ROSIE_HOME/agent/manage-world-state/internal-worlds/simple_office.soar")
    sys.exit(0)


# Arg 1: The file containing the world and map specifications [REQUIRED]

map_info = sys.argv[1]
world_stem = map_info.split("/")[-1].split(".")[0]


#################### CHECK ENVIRONMENT VARIABLES #######################

# Lookup $MOBILE_SIM_HOME environment variable [OPTIONAL]
mobile_sim_home = None
if "MOBILE_SIM_HOME" in os.environ:
    mobile_sim_home = os.environ["MOBILE_SIM_HOME"]

# Lookup $ROSIE_HOME environment variable [REQUIRED]
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)


#################### PARSE THE MAP_INFO FILE #######################

# Read the file
info_filename = rosie_home + "/tools/map_info/" + map_info

# First try locally
print("Parsing info file: " + map_info)
try:
    world_info = parse_info_file(world_stem, map_info)
except Exception as e:
    world_info = None

if world_info is None:
    print("Parsing info file: " + info_filename)
    try:
        world_info = parse_info_file(world_stem, info_filename)
    except Exception as e:
        print(e)
        sys.exit(0)
print("Success!\n")



##################### SIMULATOR WORLD ########################
# Writing the world file defining the simulated world 
#   $MOBILE_SIM_HOME/worlds/map_name.world
if mobile_sim_home is not None:
    world_filename = mobile_sim_home + "/worlds/" + world_stem + ".world"

    print("Writing world file: " + world_filename)
    with tempfile.TemporaryFile(mode='w+t') as tf:
        create_world(world_info, tf)
        save_temp_file(tf, world_filename)
    print("Success!\n")


########################### MAP INFO FILE ###########################
# Writing a file containing info about the map and its regions
#   to be used when determining the current waypoint of the agent 
#   given its position
#   $MOBILE_SIM_HOME/worlds/maps/map_name.map
if mobile_sim_home is not None:
    map_filename = mobile_sim_home + "/worlds/maps/" + world_stem + ".map"

    print("Writing map info file: " + map_filename)
    with tempfile.TemporaryFile(mode='w+t') as tf:
        create_map_info(world_info, tf)
        save_temp_file(tf, map_filename)
    print("Success!\n")


##################### ROSIE WAYPOINT MAP ########################
# Writing the waypoint map used by rosie to navigate
#   $ROSIE_HOME/agent/manage-world-state/waypoint-maps/map_name.soar
wpmap_filename = rosie_home + "/agent/manage-world-state/waypoint-maps/" + world_stem + ".soar"

print("Writing waypoint-map file: " + wpmap_filename)
with tempfile.TemporaryFile(mode='w+t') as tf:
    create_waypoint_map(world_info, tf)
    save_temp_file(tf, wpmap_filename)
print("Success!\n")



##################### ROSIE INTERNAL WORLD ########################
# Writing the internal soar model of the world used without the simulator
#   $ROSIE_HOME/agent/manage-world-state/internal-worlds/map_name.soar
internal_filename = rosie_home + "/agent/manage-world-state/internal-worlds/" + world_stem + ".soar"

print("Writing internal world soar file: " + internal_filename)
with tempfile.TemporaryFile(mode='w+t') as tf:
    create_internal_world(world_info, tf)
    save_temp_file(tf, internal_filename)
print("Success!\n")


