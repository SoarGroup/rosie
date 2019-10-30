#!/usr/bin/env python3

import sys

def pNum(n):
	return ("%.2f" % n).rjust(6)

def create_world(world_info, writer):
	writeRobot(world_info.robot, writer)

	for obj in world_info.objects:
		writer.write("\"" + obj.sim_class() + "\"\n")
		writer.write("{\n")
		obj.write_info(writer)
		writer.write("}\n")

	for wall in world_info.walls:
		writeWall(wall, writer)

	for door in world_info.doors:
		writeDoor(door, writer)

	for region in world_info.regions:
		writeRegion(region, writer)


###### ROBOT ######

def writeRobot(robot_info, writer):
	writer.write("\"soargroup.mobilesim.sim.SimRobot\"\n")
	writer.write("{\n")
	writer.write("  # Robot ID\n")
	writer.write("  6\n")
	writer.write("  # XYZRPY Truth\n")
	writer.write("  vec 6\n")
	writer.write("  %(x)s %(y)s %(z)s %(r)s %(p)s %(yaw)s\n" % \
			{ "x": pNum(robot_info.x), "y": pNum(robot_info.y), "z": pNum(0.1), \
			  "r": pNum(0.0), "p": pNum(0.0), "yaw": pNum(0.0) })
	writer.write("  # XYZRPY Odometry\n")
	writer.write("  vec 6\n")
	writer.write("  %(x)s %(y)s %(z)s %(r)s %(p)s %(yaw)s\n" % \
			{ "x": pNum(0.0), "y": pNum(0.0), "z": pNum(0.0), \
			  "r": pNum(0.0), "p": pNum(0.0), "yaw": pNum(0.0) })
	writer.write("}\n")


##### WALLS ######

def writeWall(wall_info, writer):
	writer.write("\"soargroup.mobilesim.sim.SimRoomWall\"\n")
	writer.write("{\n")
	writer.write("  # End Point 1\n")
	writer.write("  vec 2\n")
	writer.write("  %(x1)s %(y1)s\n" % \
			{ "x1": pNum(wall_info.x1), "y1": pNum(wall_info.y1) })
	writer.write("  # End Point 2\n")
	writer.write("  vec 2\n")
	writer.write("  %(x2)s %(y2)s\n" % \
			{ "x2": pNum(wall_info.x2), "y2": pNum(wall_info.y2) })
	writer.write("}\n")

##### REGIONS #####

def writeRegion(region, writer):
	writer.write("\"soargroup.mobilesim.sim.SimRegion\"\n")
	writer.write("{\n")
	writer.write("  # Region ID\n")
	writer.write("  " + str(region.tag_id) + "\n")
	writer.write("  # Tag Position\n")
	writer.write("  vec 6\n")
	writer.write("  %(x)s %(y)s %(z)s %(roll)s %(pitch)s %(yaw)s\n" % \
			{ "x": pNum(region.x), "y": pNum(region.y), "z": pNum(0.01), \
			  "roll": pNum(0), "pitch": pNum(0), "yaw": pNum(region.rot) })
	writer.write("  # Width (dx) and Length (dy)\n")
	writer.write("  " + str(region.width) + "\n")
	writer.write("  " + str(region.length) + "\n")
	writer.write("}\n")

##### DOOR #####

def writeDoor(door_info, writer):
	writer.write("\"soargroup.mobilesim.sim.SimDoor\"\n")
	writer.write("{\n")
	writer.write("  # Door xyzrpy\n")
	writer.write("  vec 6\n")
	writer.write("  %(x)s %(y)s %(z)s %(roll)s %(pitch)s %(yaw)s\n" % \
			{ "x": pNum(door_info.x), "y": pNum(door_info.y), "z": pNum(0), \
			  "roll": pNum(0), "pitch": pNum(0), "yaw": pNum(door_info.yaw) })
	writer.write("  # Connected Waypoints \n")
	writer.write("  " + str(door_info.wp1) + "\n")
	writer.write("  " + str(door_info.wp2) + "\n")
	writer.write("  # Open or closed\n")
	writer.write("  " + ("open" if door_info.open else "closed") + "\n")
	writer.write("}\n")

