#!/usr/bin/env python3

import sys

def create_internal_world(world_info, fout):
	# Find the first region that contains the robot's point
	start_loc = next( (region.handle for region in world_info.regions 
		if region.contains_point(world_info.robot.x, world_info.robot.y)), "wp01")

	# Write the start of the rule
	fout.write("### Autogenerated world file from create_internal_world.py\n")
	fout.write("### Used to test actions in a simulated world\n")
	fout.write("\n")
	fout.write("sp {top-state*domain*internal*elaborate*internal-world*" + world_info.name + "\n")
	fout.write("   (state <s> ^superstate nil\n")
	fout.write("			  ^agent-params.simulate-perception true)\n")
	fout.write("-->\n")
	fout.write("   (<s> ^internal-world <w>)\n")
	fout.write("   (<w> ^starting-location " + start_loc + "\n")
	fout.write("		^objects <objs>)\n")
	fout.write("\n")

	name_counts = {}

	i = 0

	# Write the list of objects
	for obj in world_info.objects:
		i += 1
		obj_id = "<obj" + str(i) + ">"
		preds_id = "<obj" + str(i) + "-preds>"

		preds = dict( i for i in obj.preds.items() if i[1] is not None)

		# Create a unique handle for the object
		handle = preds['category']
		
		if handle not in name_counts:
			name_counts[handle] = 0
		name_counts[handle] += 1
		handle += "_" + str(name_counts[handle])

		# Determine the object's location
		obj_loc = next( (region for region in world_info.regions 
			if region.contains_point(obj.pos[0], obj.pos[1])), None)

		if obj_loc is None:
			print("ERROR No location for: " + handle)
			continue

		fout.write("   (<objs> ^object {:s})\n".format(obj_id))
		fout.write("   ({:s} ^handle {:s} ^waypoint {:s} ^predicates {:s})\n".format(obj_id, handle, obj_loc.handle, preds_id))
		fout.write("   ({:s} {:s})\n".format(preds_id, " ".join( "^{:s} {:s}".format(cat, pred) for cat, pred in preds.items() )))
		
		fout.write("\n")
	# End of writing objects

	fout.write("}\n")


