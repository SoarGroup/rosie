import math

pf = lambda n: ("%.2f" % n).rjust(6)
strip_digits = lambda s: ''.join(filter(lambda x: x.isalpha(), s))

class SimObject:
	sim_class = "soargroup.mobilesim.sim.RosieSimObject"
	NEXT_OBJ_ID = 1
	def __init__(self):
		self.obj_id = SimObject.NEXT_OBJ_ID
		SimObject.NEXT_OBJ_ID += 1

	# Instantiate the object by reading parameters from the reader
	# cat px py pz row sx sy sz r g b num_preds p1=v1 p2=v2 ...
	# (note that cat, pos, rot, scl can all be overriden by child class)
	# Should return self
	def read_info(self, reader, scale=1.0):
		if not hasattr(self, 'cat'):
			self.cat = reader.nextWord()
		self.read_transform(reader, scale)
		self.read_color(reader)
		self.read_predicates(reader)
		return self

	# Write the object information in a way that the java class will understand
	def write_info(self, writer):
		if not hasattr(self, 'desc'):
			self.desc = strip_digits(self.cat)
		writer.write("  # Object Description\n")
		writer.write("  " + self.desc + "\n")
		self.write_transform(writer)
		self.write_color(writer)
		self.write_predicates(writer)

	### READ/WRITE the transform (pos/rot/scale)

	def read_transform(self, reader, scale):
		if not hasattr(self, 'pos'):
			# 3 floats - xyz coordinate of center
			self.pos = [ float(reader.nextWord()) * scale for i in range(3) ]
		if not hasattr(self, 'rot'):
			# 1 float - yaw (rotation in xy plane in degrees)
			self.rot = float(reader.nextWord()) * math.pi / 180.0
		if not hasattr(self, 'scl'):
			# 3 floats - scale in xyz directions (compared to 1m unit cube)
			self.scl = [ float(reader.nextWord()) * scale for i in range(3) ]
		self.pos[2] += self.scl[2]/2 # Raise z by height/2 (assume z coord is bottom, not center)

	def write_transform(self, writer):
		writer.write("  # Object xyz\n")
		writer.write("  vec 3\n")
		writer.write("  %(x)s %(y)s %(z)s\n" % \
				{ "x": pf(self.pos[0]), "y": pf(self.pos[1]), "z": pf(self.pos[2]) })
		writer.write("  # Rotation (yaw)\n")
		writer.write("  %(yaw)s\n" % { "yaw": self.rot })
		writer.write("  # Scale xyz\n")
		writer.write("  vec 3\n")
		writer.write("  %(x)s %(y)s %(z)s\n" % \
				{ "x": pf(self.scl[0]), "y": pf(self.scl[1]), "z": pf(self.scl[2]) })

	### READ/WRITE object predicates
	
	def read_predicates(self, reader):
		# 1 int - number of predicates
		num_labels = int(reader.nextWord())
		# followed by N strings
		self.preds = { }
		for i in range(num_labels):
			pred = reader.nextWord()
			if '=' in pred:
				splitPred = pred.split('=')
				self.preds[splitPred[0]] = splitPred[1]
			else:
				self.preds[pred] = None
		if hasattr(self, 'cat') and 'category' not in self.preds:
			self.preds['category'] = self.cat
		if hasattr(self, 'name') and 'name' not in self.preds:
			self.preds['name'] = self.name

	def write_predicates(self, writer):
		writer.write("  # Properties\n")
		writer.write("  %d\n" % (len(self.preds), ))
		for prop, val in self.preds.items():
			if val is None:
				writer.write("    %s\n" % prop )
			else:
				writer.write("    %s=%s\n" % (prop, val) )

	### READ/WRITE color
	
	def read_color(self, reader):
		if not hasattr(self, 'rgb'):
			# 3 ints - RGB color value
			self.rgb = [ int(reader.nextWord()) for i in range(3) ]

	def write_color(self, writer):
		writer.write("  # Color rgb (int 0-255)\n")
		writer.write("  ivec 3\n")
		writer.write("    %(r)d %(g)d %(b)d\n" % \
				{ "r": self.rgb[0], "g": self.rgb[1], "b": self.rgb[2] })


class Person(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimPerson"

	def read_info(self, reader, scale=1.0):
		self.scl = [ 1.0, 1.0, 1.0 ]
		super().read_info(reader, scale)
		self.desc = strip_digits(self.preds.get('name'))
		return self

class Chair(SimObject):
	sim_class = "soargroup.mobilesim.sim.SimChair"
	cat = "chair1"

class Table(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimTable"
	cat = "table1"
	rgb = [ 94, 76, 28 ]

class Counter(SimObject):
	cat = "counter1"
	rgb = [ 250, 230, 140 ]

class Garbage(SimObject):
	cat = "garbage1"
	rgb = [ 100, 100, 100 ]

class Sink(SimObject):
	cat = "sink1"
	rgb = [ 150, 150, 150 ]

class Shelves(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimShelves"
	cat = "shelves1"
	rgb = [ 94, 76, 28 ]

	def read_info(self, reader, scale=1.0):
		super().read_info(reader, scale)
		if hasattr(self, "door"):
			self.preds["door2"] = "open2" if self.door == "open" else "closed2"
		return self

class Pantry(Shelves):  
	door = "open"
	cat = "pantry1"

class Cupboard(Shelves):  
	door = "closed"
	cat = "cupboard1"

class Fridge(Shelves):  
	sim_class = "soargroup.mobilesim.sim.SimFridge"
	door = "closed"
	cat = "fridge1"
	rgb = [ 200, 200, 200 ]

class Microwave(Shelves):  
	door = "closed"
	cat = "microwave1"
	rgb = [ 50, 50, 50 ]

class Drawer(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimDrawer"
	cat = "drawer1"
	rgb = [ 94, 76, 28 ]

	def read_info(self, reader, scale=1.0):
		super().read_info(reader, scale)
		self.preds["door2"] = "closed2"
		return self

class Desk(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimDesk"
	cat = "desk1"
	rgb = [ 94, 76, 28 ]

class LightSwitch(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimLightSwitch"
	desc = "LS"
	cat = "lightswitch1"
	rgb = [ 255, 255, 255 ]

	def read_info(self, reader, scale=1.0):
		# 1 word << on off >> - state of switch
		self.state = reader.nextWord()
		# 1 word wp02 - region to control
		self.region = reader.nextWord()
		super().read_info(reader, scale)
		self.preds["activation1"] = self.state + "2"
		return self

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.region + "\n")
