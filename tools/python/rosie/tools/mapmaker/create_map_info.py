#!/usr/bin/env python3

import sys

def pNum(n):
	return ("%.2f" % n).rjust(6)

def create_map_info(world_info, fout):
	fout.write("robot {:f} {:f}\n".format(world_info.robot.x, world_info.robot.y))
	for region in world_info.regions:
		writeRegion(region, fout)

##### REGIONS #####

def writeRegion(region, fout):
	fout.write("wp" + str(region.soar_id))
	fout.write(" " + str(region.x))
	fout.write(" " + str(region.y))
	fout.write(" " + str(region.rot))
	fout.write(" " + str(region.width))
	fout.write(" " + str(region.length))
	fout.write(" " + str(region.label))
	fout.write("\n")

