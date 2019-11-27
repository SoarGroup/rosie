
pf = lambda n: ("%.2f" % n).rjust(6)

class SimObject:
	NEXT_OBJ_ID = 1
	def __init__(self):
		self.obj_id = SimObject.NEXT_OBJ_ID
		SimObject.NEXT_OBJ_ID += 1
		self.vals = [0, 0, 0, 0, 0, 0, 0, 0, 0]
		self.cats = []
		self.labels = []
		self.desc = ""
	
	def sim_class(self):
		return "soargroup.mobilesim.sim.RosieSimObject"

	def read_transform(self, reader, scale):
		# 3 floats - xyz coordinate of center
		for i in range(0, 3):
			self.vals[i] = float(reader.nextWord()) * scale
		# 1 float - yaw (rotation in xy plane)
		self.vals[5] = float(reader.nextWord())
		# 3 floats - scale in xyz directions (compared to 1m unit cube)
		for i in range(6, 9):
			self.vals[i] = float(reader.nextWord()) * scale


	def read_info(self, reader, scale=1.0):
		# 1 string - short object description
		self.desc = reader.nextWord()
		# 7 floats - transform
		self.read_transform(reader, scale)
		# 1 int - number of properties
		num_labels = int(reader.nextWord())
		# followed by 2N strings, of property_category property_value
		self.cats = []
		self.labels = []
		for i in range(num_labels):
			self.cats.append(reader.nextWord())
			self.labels.append(reader.nextWord())
		return self

	def write_info(self, writer):
		writer.write("  # Object Description\n")
		writer.write("  " + str(self.desc) + "\n")
		writer.write("  # Object xyz\n")
		writer.write("  vec 3\n")
		writer.write("  %(x)s %(y)s %(z)s\n" % \
				{ "x": pf(self.vals[0]), "y": pf(self.vals[1]), "z": pf(self.vals[2]) })
		writer.write("  # Rotation (yaw)\n")
		writer.write("  %(yaw)s\n" % { "yaw": self.vals[5] })
		writer.write("  # Scale xyz\n")
		writer.write("  vec 3\n")
		writer.write("  %(x)s %(y)s %(z)s\n" % \
				{ "x": pf(self.vals[6]), "y": pf(self.vals[7]), "z": pf(self.vals[8]) })
		writer.write("  # Properties\n")
		writer.write("  %d\n" % (len(self.cats), ))
		for i in range(len(self.cats)):
			writer.write("    %s=%s\n" % (self.cats[i], self.labels[i]))

class Person(SimObject):  
	def __init__(self):
		super().__init__()
		self.name = ""
		self.cat = ""
		self.rgb = [ 0, 0, 0 ]

	def sim_class(self):
		return "soargroup.mobilesim.sim.SimPerson"

	def read_info(self, reader, scale=1.0):
		# 1 string - short object description
		self.desc = reader.nextWord()
		# 3 floats - xyz coordinate of center
		for i in range(0, 3):
			self.vals[i] = float(reader.nextWord()) * scale
		# 1 float - yaw (rotation in xy plane)
		self.vals[5] = float(reader.nextWord())
		# 1 string - category
		self.cat = reader.nextWord()
		# 1 string - name
		self.name = reader.nextWord()
		self.cats = [ "category", "name" ]
		self.labels = [ self.cat, self.name ]
		# 3 ints - RGB color value
		self.rgb = []
		self.rgb.append(int(reader.nextWord()))
		self.rgb.append(int(reader.nextWord()))
		self.rgb.append(int(reader.nextWord()))
		return self

	def write_info(self, writer):
		writer.write("  # Object Description\n")
		writer.write("  " + str(self.desc) + "\n")
		writer.write("  # Object xyz\n")
		writer.write("  vec 3\n")
		writer.write("  %(x)s %(y)s %(z)s\n" % \
				{ "x": pf(self.vals[0]), "y": pf(self.vals[1]), "z": pf(self.vals[2]) })
		writer.write("  # Rotation (yaw)\n")
		writer.write("  %(yaw)s\n" % { "yaw": self.vals[5] })
		writer.write("  # Category, Name\n")
		writer.write("  %s\n" % self.cat)
		writer.write("  %s\n" % self.name)
		writer.write("  # Color rgb (int 0-255)\n")
		writer.write("  ivec 3\n")
		writer.write("    %(r)d %(g)d %(b)d\n" % \
				{ "r": self.rgb[0], "g": self.rgb[1], "b": self.rgb[2] })


class BoxObject(SimObject):  
	def __init__(self):
		super().__init__()
		self.rgb = [ 0, 0, 0 ]

	def sim_class(self):
		return "soargroup.mobilesim.sim.SimBoxObject"

	def read_info(self, reader, scale=1.0):
		super().read_info(reader, scale)
		# 3 ints - RGB color value
		self.rgb = []
		self.rgb.append(int(reader.nextWord()))
		self.rgb.append(int(reader.nextWord()))
		self.rgb.append(int(reader.nextWord()))
		return self

	def write_info(self, writer):
		super().write_info(writer)
		writer.write("  # Color rgb (int 0-255)\n")
		writer.write("  ivec 3\n")
		writer.write("    %(r)d %(g)d %(b)d\n" % \
				{ "r": self.rgb[0], "g": self.rgb[1], "b": self.rgb[2] })

class Surface(BoxObject):  
	def sim_class(self):
		return "soargroup.mobilesim.sim.SimSurface"

class Receptacle(BoxObject):  
	def sim_class(self):
		return "soargroup.mobilesim.sim.SimReceptacle"

class Table(Surface):  
	def sim_class(self):
		return "soargroup.mobilesim.sim.SimTable"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Table"
		self.cats = [ 'category' ]
		self.labels = [ 'table1' ]
		self.rgb = [ 94, 76, 28 ]
		return self

class Counter(Surface):
	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Counter"
		self.cats = [ 'category' ]
		self.labels = [ 'counter1' ]
		self.rgb = [ 250, 230, 140 ]
		return self

class Garbage(Receptacle):
	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Garbage"
		self.cats = [ 'category' ]
		self.labels = [ 'garbage1' ]
		self.rgb = [ 100, 100, 100 ]
		return self

class Sink(Receptacle):
	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Sink"
		self.cats = [ 'category' ]
		self.labels = [ 'sink1' ]
		self.rgb = [ 150, 150, 150 ]
		return self

class Shelves(Surface):  
	def __init__(self):
		super().__init__()
		self.door = "none"

	def sim_class(self):
		return "soargroup.mobilesim.sim.SimShelves"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Shelves"
		self.cats = [ 'category' ]
		self.labels = [ 'shelves1' ]
		self.rgb = [ 94, 76, 28 ]
		return self

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.door + "\n")

class Pantry(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "open"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Pantry"
		self.cats = [ 'category' ]
		self.labels = [ 'pantry1' ]
		self.rgb = [ 94, 76, 28 ]
		return self

class Fridge(Shelves):  
	def __init__(self):
		super().__init__()
		self.door = "closed"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.desc = "Fridge"
		self.cats = [ 'category' ]
		self.labels = [ 'fridge1' ]
		self.rgb = [ 200, 200, 200 ]
		return self

class LightSwitch(SimObject):  
	def __init__(self):
		super().__init__()
		self.state = ""
		self.region = ""

	def sim_class(self):
		return "soargroup.mobilesim.sim.SimLightSwitch"

	def read_info(self, reader, scale=1.0):
		self.read_transform(reader, scale)
		self.state = reader.nextWord()
		self.region = reader.nextWord()
		self.desc = "LS"
		self.cats = [ 'category' ]
		self.labels = [ 'lightswitch1' ]
		return self

	def write_info(self, writer):
		super().write_info(writer)
		writer.write(self.state + "\n")
		writer.write(self.region + "\n")
