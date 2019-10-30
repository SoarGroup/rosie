class FileReader:
	def __init__(self, file):
		self.f = file
		self.splitline = None
		self.lineIndex = 0
		self.lineNum = -1

	def skipWords(self, num):
		for i in range(num):
			self.nextWord()

	def nextWord(self):
		while True:
			if self.splitline != None and self.lineIndex < len(self.splitline):
				self.lineIndex = self.lineIndex + 1
				return self.splitline[self.lineIndex - 1]
			line = self.nextLine()
			if line == None:
				self.splitline = None
				return None
			else:
				self.splitline = line.split()
				self.lineIndex = 0

	def nextLine(self):
		# Read file until the end is reached or a non-empty line
		while True:
			line = self.f.readline()
			self.lineNum += 1
			if len(line) == 0:
				return None
			if line.startswith("#"):
				continue
			if len(line.split()) > 0:
				return line
