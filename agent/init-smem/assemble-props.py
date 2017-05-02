class Property:
    def __init__(self, items, commented):
        self.items = items
        self.sym = items[0]
        
        self.handle = None
        self.item_type = None
        self.prop = None

        for i in range(len(items)-1):
            if items[i] == "^handle":
                self.handle = items[i+1]
            elif items[i] == "^item-type":
                self.item_type = items[i+1]
            elif items[i] == "^property":
                self.prop = items[i+1]

        self.line = "(" + " ".join(items) + ")"


class Word:
    def __init__(self, items):
        self.items = items
        self.var = items[0]

        self.spelling = None
        self.ref = None
        for i in range(len(items)-1):
            if items[i] == "^spelling":
                self.spelling = items[i+1][1:-1]
            elif items[i] == "^referent" or items[i] == "^constraint":
                self.ref = items[i+1]

        self.var = "<" + self.spelling + ">"
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

f = open('new-properties.soar', 'w')

for prop in props:
    word = None
    f.write("### +" + prop.handle + "\n\n")
    for w in words:
        if w.ref == prop.sym:
            indent = " "*(3+2+len(w.var))
            f.write("   # word = '" + w.spelling + "'\n")
            f.write("   (" + w.var + " " + w.items[1] + " " + w.items[2] + "\n")
            for i in range(3, len(w.items), 2):
                f.write(indent + w.items[i] + " " + w.items[i+1] + "\n")
    f.write("   " + prop.line + "\n\n")

f.close()


