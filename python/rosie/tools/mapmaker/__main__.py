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
from rosie.tools.mapmaker.create_internal_world import create_internal_world

#################### CHECK ARGUMENTS ##############################

if len(sys.argv) <= 1:
    print("python3 -m rosie.tools.mapmaker [map_info_file] [opts..]")
    print("  map_file_file is a specification of the world")
    print("")
    print("Will generate a set of world/map files for a rosie environment")
    print("  (By default it will create all 3, unless 1+ are specified)")
    print("  -w|--world    Will generate the mobilesim world files")
    print("  -m|--map      Will generate waypoint maps")
    print("  -i|--internal Will generate an internal world for rosie")
    print("")
    print("  --output-dir <dir>  Will create the files in the given directory instead of the default")
    print("  --agent-name <name>  Specifies the name to use when creating files instead of matching the map file name")
    print("")
    print("Example: python3 -m rosie.tools.mapmaker simple_office.info")
    print("  Output File: $MOBILE_SIM_HOME/worlds/simple_office.world")
    print("  Output File: $ROSIE_HOME/agent/manage-world-state/waypoint-maps/simple_office.soar")
    print("  Output File: $ROSIE_HOME/agent/manage-world-state/internal-worlds/simple_office.soar")
    print("")
    print("Example: python3 -m rosie.tools.mapmaker simple_office.info --output_dir world_files")
    print("  Output File: world_files/simple_office.world")
    print("  Output File: world_files/waypoint-map.soar")
    print("  Output File: world_files/internal-world.soar")
    sys.exit(0)


# Arg 1: The file containing the world and map specifications [REQUIRED]

map_info = sys.argv[1]
world_stem = map_info.split("/")[-1].split(".")[0]

# Options:
make_all      = len(sys.argv) == 2
make_world    = make_all or "-w" in sys.argv or "--world" in sys.argv
make_map      = make_all or "-m" in sys.argv or "--map" in sys.argv
make_internal = make_all or "-i" in sys.argv or "--internal" in sys.argv
output_dir    = next((sys.argv[i+1] for i in range(len(sys.argv)-1) if sys.argv[i] == "--output-dir"), None)
agent_name    = next((sys.argv[i+1] for i in range(len(sys.argv)-1) if sys.argv[i] == "--agent-name"), world_stem)

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

# First try locally
print("Parsing info file: " + map_info)
try:
    world_info = parse_info_file(world_stem, map_info)
except Exception as e:
    print(e)
    sys.exit(0)

print("Success!\n")



##################### SIMULATOR WORLD ########################
# Writing the world file defining the simulated world 
#   $MOBILE_SIM_HOME/worlds/map_name.world
if make_world and mobile_sim_home is not None:
    if output_dir is None:
        world_filename = mobile_sim_home + "/worlds/" + agent_name + ".world"
    else:
        world_filename = output_dir + "/" + agent_name + ".world"

    print("Writing world file: " + world_filename)
    with tempfile.TemporaryFile(mode='w+t') as tf:
        create_world(world_info, tf)
        save_temp_file(tf, world_filename)
    print("Success!\n")


##################### ROSIE WAYPOINT MAP ########################
# Writing the waypoint map used by rosie to navigate
#   $ROSIE_HOME/agent/manage-world-state/waypoint-maps/map_name.soar
if output_dir is None:
    wpmap_filename = rosie_home + "/agent/manage-world-state/waypoint-maps/" + agent_name + ".soar"
else:
    wpmap_filename = output_dir + "/waypoint-map.soar"

if make_map:
    print("Writing waypoint-map file: " + wpmap_filename)
    with tempfile.TemporaryFile(mode='w+t') as tf:
        create_waypoint_map(world_info, tf)
        save_temp_file(tf, wpmap_filename)
    print("Success!\n")


##################### ROSIE INTERNAL WORLD ########################
# Writing the internal soar model of the world used without the simulator
#   $ROSIE_HOME/agent/manage-world-state/internal-worlds/map_name.soar
if output_dir is None:
    internal_filename = rosie_home + "/agent/manage-world-state/internal-worlds/" + agent_name + ".soar"
else:
    internal_filename = output_dir + "/internal-world.soar"

if make_internal:
    print("Writing internal world soar file: " + internal_filename)
    with tempfile.TemporaryFile(mode='w+t') as tf:
        create_internal_world(world_info, tf)
        save_temp_file(tf, internal_filename)
    print("Success!\n")


