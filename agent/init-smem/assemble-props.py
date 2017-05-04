class Property:
    def __init__(self, items, commented):
        self.items = items
        self.sym = items[0]
        
        self.handle = None
        self.item_type = None
        self.prop = None
        self.commented = commented
        self.added = False

        for i in range(len(items)-1):
            if items[i] == "^handle":
                self.handle = items[i+1]
            elif items[i] == "^item-type":
                self.item_type = items[i+1]
            elif items[i] == "^property":
                self.prop = items[i+1]

        if self.prop != None:
            if self.handle[0] >= '0' and self.handle[0] <= '9':
                self.items[0] = "<num" + self.handle + ">"
            else:
                self.items[0] = "<" + self.handle + ">"

        self.line = "(" + " ".join(items) + ")"

    def __str__(self):
        return self.handle


class Word:
    def __init__(self, items):
        self.items = items
        self.var = items[0]
        self.added = False

        self.spelling = None
        self.ref = None
        for i in range(len(items)-1):
            if items[i] == "^spelling":
                self.spelling = items[i+1][1:-1]
            elif items[i] == "^referent" or items[i] == "^constraint":
                self.ref = items[i+1]

        self.var = "<word-" + self.spelling + ">"
        self.var = self.var.replace("'", "")
        self.items[0] = self.var
        self.line = "(" + " ".join(self.items) + ")"


props = []

f = open('properties.soar', 'r')

for line in f:
    line = line[:-1]
    if line.find("(") == -1:
        continue

    # Strip parentheses
    line = line.replace("(", "")
    line = line.replace(")", "")
    if len(line) == 0:
        continue

    # Check if line is commented
    commented = (line[0] == '#')
    line = line.replace("#", "").strip()
    if len(line) == 0 or line[0] != '@':
        continue

    props.append(Property(line.split(), commented))

f.close()

words = []

f = open('combined-words.soar', 'r')

content = ""
for line in f:
    line = line[:-1].strip()
    if len(line) == 0:
        if content != "":
            content = content.replace("(", "")
            content = content.replace(")", "")
            words.append(Word(content.split()))
            content = ""
    else:
        content += " " + line

f.close()

order = ["name"
,"first-name"
,"last-name"
,"possessive"
,"gender"
,"color"
,"shape"
,"size"
,"weight"
,"temperature"
,"activation1"
,"spatial-shape"
,"door1"
,"location2"
,"meat1"
,"game1"
,"action1"
,"number"
,"unit"
,"cardinal-direction1"
,"relative-direction1"
,"property"
,"property3"
,"property1"
,"modifier1"
,"category"]

cats = {}
for prop in props:
    if prop.prop == None and prop.item_type == "property":
        cats[prop.sym] = prop

groups = {}
for prop in props:
    if prop.sym in cats:
        continue
    cat = cats[prop.prop].handle
    if cat in groups:
        groups[cat].append(prop)
    else:
        groups[cat] = [ prop ]


f = open('new-properties.soar', 'w')

print([ c.handle for c in cats.values()])

for cat_h in order:
    for cat in cats.values():
        if cat.handle == cat_h:
            cat.added = True
            f.write("#"*60 + "\n")
            f.write("#"*15 + " CATEGORY = " + "{:17s}".format(cat.handle) + " " + "#"*15 + "\n\n")
            f.write("### !" + cat.handle + "\n")
            f.write("smem --add {\n")
            f.write("   " + cat.line + "\n}\n\n")

    if cat_h not in groups:
        continue

    for prop in groups[cat_h]:
        prop.added = True
        f.write("### +" + prop.handle + "\n")
        linked_words = []
        for w in words:
            if w.ref == prop.sym:
                linked_words.append(w)

        if len(linked_words) == 0 and prop.commented:
            f.write("#")
        f.write("smem --add {\n")
        for w in linked_words:
            w.added = True
            indent = " "*(3+2+len(w.var))
            f.write("   (" + w.var + " " + w.items[1] + " " + w.items[2])
            for i in range(3, len(w.items), 2):
                if w.items[i] == "^referent" or w.items[i] == "^constraint":
                    f.write("\n" + indent + w.items[i] + " " + prop.items[0])
                else:
                    f.write("\n" + indent + w.items[i] + " " + w.items[i+1])
            f.write(")\n")
        f.write(("#" if prop.commented else "") + "   " + prop.line + "\n")
        if len(linked_words) == 0 and prop.commented:
            f.write("#")
        f.write("}\n\n")
        

f.close()

for prop in props:
    if not prop.added:
        print(prop.handle)




