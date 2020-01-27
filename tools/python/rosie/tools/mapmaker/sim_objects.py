
pf = lambda n: ("%.2f" % n).rjust(6)
strip_digits = lambda s: ''.join(filter(lambda x: x.isalpha(), s))

class SimObject:
	sim_class = "soargroup.mobilesim.sim.RosieSimObject"
	NEXT_OBJ_ID = 1
	def __init__(self):
		self.obj_id = SimObject.NEXT_OBJ_ID
		SimObject.NEXT_OBJ_ID += 1

	# Instantiate the object by reading parameters from the reader
	# Should return self
	def read_info(self, reader, scale=1.0):
		return self

	# Write the object information in a way that the java class will understand
	def write_info(self, writer):
		pass

	### READ/WRITE the transform (pos/rot/scale)

	def read_transform(self, reader, scale):
		if not hasattr(self, 'pos'):
			# 3 floats - xyz coordinate of center
			self.pos = [ float(reader.nextWord()) * scale for i in range(3) ]
		if not hasattr(self, 'rot'):
			# 1 float - yaw (rotation in xy plane)
			self.rot = float(reader.nextWord())
		if not hasattr(self, 'scl'):
			# 3 floats - scale in xyz directions (compared to 1m unit cube)
			self.scl = [ float(reader.nextWord()) * scale for i in range(3) ]

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
		# followed by 2N strings, of predicate_category predicate_name
		self.preds = { }
		for i in range(num_labels):
			self.preds[reader.nextWord()] = reader.nextWord()

	def write_predicates(self, writer):
		writer.write("  # Properties\n")
		writer.write("  %d\n" % (len(self.preds), ))
		for cat, pred in self.preds.items():
			writer.write("    %s=%s\n" % (cat, pred) )

	### READ/WRITE color
	
	def read_color(self, reader):
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
		self.read_transform(reader, scale)
		self.read_color(reader)
		# 1 string - category
		self.cat = reader.nextWord()
		# 1 string - name
		self.name = reader.nextWord()
		self.preds = { 'category': self.cat, 'name': self.name }
		return self

	def write_info(self, writer):
		writer.write("  # Object Description\n")
		writer.write("  " + strip_digits(self.name) + "\n")
		self.write_transform(writer)
		self.write_predicates(writer)
		self.write_color(writer)


class BoxObject(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimBoxObject"
	def read_info(self, reader, scale=1.0):
		if not hasattr(self, 'cat'):
			self.cat = reader.nextWord()
		if not hasattr(self, 'desc'):
			self.desc = strip_digits(self.cat)

		self.read_transform(reader, scale)
		self.pos[2] += self.scl[2]/2 # Raise z by height/2 to center it

		if not hasattr(self, 'rgb'):
			self.read_color(reader)

		self.read_predicates(reader)
		self.preds['category'] = self.cat
		return self

	def write_info(self, writer):
		writer.write("  # Object Description\n")
		writer.write("  " + self.desc + "\n")
		self.write_transform(writer)
		self.write_predicates(writer)
		self.write_color(writer)


class Receptacle(BoxObject):  
	sim_class = "soargroup.mobilesim.sim.SimReceptacle"

class Surface(BoxObject):  
	sim_class = "soargroup.mobilesim.sim.SimSurface"

class Table(Surface):  
	sim_class = "soargroup.mobilesim.sim.SimTable"
	def __init__(self):
		super().__init__()
		self.cat = "table1"
		self.rgb = [ 94, 76, 28 ]

class Counter(Surface):
	def __init__(self):
		super().__init__()
		self.cat = "counter1"
		self.rgb = [ 250, 230, 140 ]

class Garbage(Receptacle):
	def __init__(self):
		super().__init__()
		self.cat = "garbage1"
		self.rgb = [ 100, 100, 100 ]

class Sink(Receptacle):
	def __init__(self):
		super().__init__()
		self.cat = "sink1"
		self.rgb = [ 150, 150, 150 ]

class Shelves(Surface):  
	sim_class = "soargroup.mobilesim.sim.SimShelves"
	def __init__(self):
		super().__init__()
		self.cat = "shelves1"
		self.door = "none"
		self.rgb = [ 94, 76, 28 ]

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.door + "\n")

class Pantry(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "open"
		self.cat = "pantry1"

class Cupboard(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "closed"
		self.cat = "cupboard1"

class Drawer(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "closed"
		self.cat = "drawer1"

class Fridge(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "closed"
		self.cat = "fridge1"
		self.rgb = [ 200, 200, 200 ]

class Microwave(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "closed"
		self.cat = "microwave1"
		self.rgb = [ 50, 50, 50 ]

class Drawer(Receptacle):  
	sim_class = "soargroup.mobilesim.sim.SimDrawer"
	def __init__(self):
		super().__init__()
		self.cat = "drawer1"
		self.door = "closed"
		self.rgb = [ 94, 76, 28 ]

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.door + "\n")

class Desk(Surface):  
	sim_class = "soargroup.mobilesim.sim.SimDesk"
	def __init__(self):
		super().__init__()
		self.cat = "desk1"
		self.door = "closed"
		self.rgb = [ 94, 76, 28 ]

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.door + "\n")

class LightSwitch(SimObject):  
	sim_class = "soargroup.mobilesim.sim.SimLightSwitch"
	def __init__(self):
		super().__init__()
		self.desc = "LS"
		self.cat = "lightswitch1"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.state = reader.nextWord()
		self.region = reader.nextWord()
		return self

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.state + "\n")
		writer.write(self.region + "\n")
