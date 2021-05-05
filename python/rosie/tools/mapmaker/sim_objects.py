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
    # !!! Should return self !!!
    def read_info(self, reader, scale=1.0):
        if not hasattr(self, 'cat'):
            self.cat = reader.nextWord()
        self.read_transform(reader, scale)
        self.read_color(reader)
        self.read_predicates(reader)
        self.handle = self.cat + "_" + str(self.obj_id)
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

    ### READ/WRITE the transform (3 floats for pos, 1 float for rotation, 3 floats for scale)

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

    ### READ/WRITE object predicates (an integer N followed by N words)
    
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
        if hasattr(self, 'def_props'):
            for prop in self.def_props:
                self.preds[prop] = None
        # temp_id is an optional identifier used to have different objects in the world file
        #   refer to each other. It is not exposed to Rosie at all 
        self.temp_id = self.preds.get('temp_id', None)

    def write_predicates(self, writer):
        writer.write("  # Properties\n")
        writer.write("  %d\n" % (len(self.preds), ))
        for prop, val in self.preds.items():
            if val is None:
                writer.write("    %s\n" % prop )
            else:
                writer.write("    %s=%s\n" % (prop, val) )

    ### READ/WRITE color (3 int values)
    
    def read_color(self, reader):
        if not hasattr(self, 'rgb'):
            # 3 ints - RGB color value
            self.rgb = [ int(reader.nextWord()) for i in range(3) ]

    def write_color(self, writer):
        writer.write("  # Color rgb (int 0-255)\n")
        writer.write("  ivec 3\n")
        writer.write("    %(r)d %(g)d %(b)d\n" % \
                { "r": self.rgb[0], "g": self.rgb[1], "b": self.rgb[2] })



# This is not a Rosie Object, will not be perceived
class BoxObject(SimObject):
    sim_class = "soargroup.mobilesim.sim.BoxObject"
    def read_info(self, reader, scale=1.0):
        self.read_transform(reader, scale)
        self.read_color(reader)
        return self
    def write_info(self, writer):
        self.write_transform(writer)
        self.write_color(writer)

class Person(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimPerson"

    def read_info(self, reader, scale=1.0):
        self.scl = [ 1.0, 1.0, 1.0 ]
        super().read_info(reader, scale)
        if 'name' in self.preds:
            self.desc = strip_digits(self.preds.get('name'))
        else:
            self.desc = strip_digits(self.preds.get('category'))
        return self

class Mug(SimObject):
    sim_class = "soargroup.mobilesim.sim.SimCup"
    cat = "mug1"
    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        self.preds["contents1"] = "empty1"
        return self

class Cup(SimObject):
    sim_class = "soargroup.mobilesim.sim.SimCup"
    cat = "cup1"
    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        self.preds["contents1"] = "empty1"
        return self

class Chair(SimObject):
    cat = "chair1"
    def_props = ('custom-model', )

class Table(SimObject):  
    cat = "table1"
    rgb = [ 94, 76, 28 ]
    def_props = ('custom-model', 'surface')

class Counter(SimObject):
    cat = "counter1"
    rgb = [ 250, 230, 140 ]
    def_props = ('box', 'surface')

class Sink(SimObject):
    cat = "sink1"
    rgb = [ 150, 150, 150 ]
    def_props = ('openbox', 'receptacle', 'drain')

class Garbage(SimObject):
    cat = "garbage1"
    def_props = ('openbox', 'receptacle')

class Stove(SimObject):
    sim_class = "soargroup.mobilesim.sim.SimStove"
    cat = "stove1"
    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        self.preds["is-activated1"] = "not-activated1"
        return self

class Shelves(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimShelves"
    cat = "shelves1"
    rgb = [ 94, 76, 28 ]

    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        if hasattr(self, "door"):
            self.preds["is-open1"] = "open2" if self.door == "open" else "not-open1"
        return self

class Pantry(Shelves):  
    door = "open"
    cat = "pantry1"

class Cupboard(Shelves):  
    door = "not-open1"
    cat = "cupboard1"

class Fridge(Shelves):  
    sim_class = "soargroup.mobilesim.sim.SimFridge"
    door = "not-open1"
    cat = "fridge1"
    rgb = [ 200, 200, 200 ]

class Microwave(Shelves):  
    door = "not-open1"
    cat = "microwave1"
    rgb = [ 50, 50, 50 ]

class Kettle(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimKettle"
    cat = "kettle1"
    def_props = ('grabbable', )
    rgb = [ 150, 150, 150 ]

class Cooler(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimCooler"
    cat = "cooler1"
    rgb = [ 0, 0, 0 ]

    def read_info(self, reader, scale=1.0):
        # 1 word - either auto or manual
        self.auto = reader.nextWord()
        super().read_info(reader, scale)
        return self

    def write_info(self, writer):
        super().write_info(writer)
        writer.write("    " + self.auto + "\n")


class Drawer(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimDrawer"
    cat = "drawer1"
    rgb = [ 94, 76, 28 ]

    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        self.preds["is-open1"] = "not-open1"
        return self

class Desk(SimObject):  
    cat = "desk1"
    rgb = [ 94, 76, 28 ]
    def_props = ('custom-model', 'surface')

class LightSwitch(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimLightSwitch"
    desc = "LS"
    cat = "lightswitch1"
    rgb = [ 255, 255, 255 ]

    def read_info(self, reader, scale=1.0):
        # 1 word << on off >> - state of switch
        self.state = reader.nextWord().lower()
        # 1 word wp02 - region to control
        self.region = reader.nextWord()
        super().read_info(reader, scale)
        self.preds["is-activated1"] = ("activated1" if self.state == "on" else "not-activated1")
        return self

    def write_info(self, writer):
        super().write_info(writer)
        writer.write("    " + self.region + "\n")

class Button(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimButton"
    cat = "button1"
    desc = "btn"
    def read_info(self, reader, scale=1.0):
        # 1 word - the target to trigger when pressed (they must have a temp_id defined)
        self.target = reader.nextWord()
        super().read_info(reader, scale)
        return self

    def write_info(self, writer):
        super().write_info(writer)
        writer.write("    " + self.target + "\n")

    def write_extra_soar_info(self, writer, world_info):
        # Try to find a target object with a matching temp_id
        target = next((obj for obj in world_info.objects if obj.temp_id == self.target), None) 
        if target is not None:
            writer.write("   (<obj{:d}> ^target <obj{:d}>)\n".format(self.obj_id, target.obj_id))



class Alarm(SimObject):
    sim_class = "soargroup.mobilesim.sim.SimAlarm"
    desc = "Alarm"
    cat = "alarm1"
    rgb = [ 255, 0, 0 ]

    def read_info(self, reader, scale=1.0):
        super().read_info(reader, scale)
        self.preds["is-activated1"] = "not-activated1"
        return self


class Door(SimObject):  
    sim_class = "soargroup.mobilesim.sim.SimDoor"

    def __init__(self, edge, regions, scale=1.0):
        SimObject.__init__(self)
        self.cat = "door1"
        self.handle = "door1_" + str(self.obj_id)
        self.pos = [ edge.door_x, edge.door_y, 1.0*scale ]
        self.rot = edge.door_rot
        self.scl = [ 0.1*scale, edge.door_wid, 2.0*scale ]
        self.rgb = [ 94, 76, 28 ]
        self.reg1 = next(reg.handle for reg in regions if reg.tag_num == edge.start_wp)
        self.reg2 = next(reg.handle for reg in regions if reg.tag_num == edge.end_wp)
        self.state = edge.door_state
        self.preds = { "category": self.cat }
        self.preds["is-open1"] = ("open2" if self.state == "open" else "not-open1")
        edge.set_door(self)

    def write_info(self, writer):
        super().write_info(writer)
        writer.write("    " + self.reg1 + "\n")
        writer.write("    " + self.reg2 + "\n")
