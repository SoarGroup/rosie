#!/usr/bin/env python3

from math import sin
from math import cos

#DOOR_DIST = 0.8 # Magicbot
DOOR_DIST = 0.6 # Cozmo

def create_waypoint_map(world_info, fout):
	nodes = {}
	edges = []

	for region in world_info.regions:
		node = Node(region)
		nodes[node.id_num] = node
	
	for edge in world_info.edges:
		edges.append(Edge(edge, True))
		edges.append(Edge(edge, False))

	fout.write("sp {topstate*elaborate*map\n")
	fout.write("   (state <s> ^superstate nil)\n")
	fout.write("-->\n")
	fout.write("   (<s> ^maps <maps>)\n")
	fout.write("   (<maps> ^map <building> <world>)\n")
	fout.write("\n")
	fout.write("   (<world> ^handle world-map ^waypoint <bwp01>)\n")
	fout.write("   (<bwp01> ^handle bwp01 ^x 0 ^y 0 ^map <world> ^sub-map <building>)\n")
	fout.write("\n")
	fout.write("   ### BUILDING ###\n")
	fout.write("\n")
	fout.write("   (<building> ^handle bmap1 ^super-waypoint <bwp01>")

	i = 0
	for node_id in sorted(nodes):
		if i % 5 == 0:
			fout.write("\n       ^waypoint ")
		fout.write(nodes[node_id].var + " ")
		i = i + 1

	fout.write(")\n")
	fout.write("\n")
	fout.write("   ### WAYPOINTS ###\n")
	fout.write("\n")

	for node_id in sorted(nodes):
		node = nodes[node_id]
		node.write(fout)
		for edge in edges:
			if edge.start == node.id_num:
				edge.write(nodes, fout)
		fout.write("\n")

	fout.write("}\n")

class Node:
	def __init__(self, region):
		self.x = str(region.x)
		self.y = str(region.y)

		self.id_num = region.tag_id
		self.id_str = region.soar_id

		self.name = "wp" + self.id_str
		self.var = "<" + self.name + ">"
		self.classification = region.label
	
	def write(self, out):
		out.write('  (%(var)s ^handle %(hand)s ^x %(x)s ^y %(y)s ^map <building>)\n' % \
				{ "var": self.var, "hand": self.name, "id_num": self.id_num, "x": self.x, "y": self.y })


class Edge:
	def __init__(self, edge, forward):
		if forward:
			self.start = edge.start_wp
			self.end = edge.end_wp
		else:
			self.start = edge.end_wp
			self.end = edge.start_wp
		self.has_door = edge.has_door
		if edge.has_door:
			dx = cos(edge.door_rot) * DOOR_DIST
			dy = sin(edge.door_rot) * DOOR_DIST
			if forward:
				self.door_sx = edge.door_x - dx
				self.door_sy = edge.door_y - dy
				self.door_ex = edge.door_x + dx
				self.door_ey = edge.door_y + dy
			else:
				self.door_sx = edge.door_x + dx
				self.door_sy = edge.door_y + dy
				self.door_ex = edge.door_x - dx
				self.door_ey = edge.door_y - dy
	
	def write(self, nodes, fout):
		start = nodes[self.start]
		end = nodes[self.end]
		self.var = "<e" + start.id_str + end.id_str + ">"

		fout.write('   (%(start)s ^edge %(edge)s)\n' % \
				{ "start": start.var, "edge": self.var })
		fout.write('    (%(edge)s ^start %(start)s ^end %(end)s \n' % \
				{ "edge": self.var, "start": start.var, "end": end.var })
		if self.has_door:
			fout.write('         ^doorway true ^door_sx %(sx)s ^door_sy %(sy)s ^door_ex %(ex)s ^door_ey %(ey)s)\n' % \
					{ "sx": self.door_sx, "sy": self.door_sy, "ex": self.door_ex, "ey": self.door_ey })
		else:
			fout.write('         ^doorway false)\n')
				
