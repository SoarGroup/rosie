#!/usr/bin/perl

@sentences = (
	"What is the color of this?",
	"This is a red block.",
	"This is a yellow block.",
	"This is an orange block.",
	"What shape is this?",
	"This is a square block.",
	"This is a triangular block.",
	"Point at the square block.",
	"Point at the square red block.",
	"Point at the yellow one.",
	"Point at the triangular one.",
	"Describe the blocks.",
	"What is to the left of the red block?",
	"The orange block is to the left of the red block",
	"The orange block is on the table.",
	"Where is the orange block?",
	"Where is the red block?",
	"Move the yellow block to the pantry.",
	"Pick up the yellow block.",
	"Put the yellow block on the pantry.",
	"You moved the yellow block to the pantry.",
	"Where is the yellow block.",
	"Move the red block to the sink.",
	"No.",
	"Red is a color.",
	"The triangle is to the left of the yellow square.",
	"Move the blue square to the pantry.",
	"The goal is a red square on the stove.",
	"The goal is an empty gripper.",
	"What color is this?",
	"Pick up the square on the right of the triangle.",
	"The color of this is red.",
	"Move the red block by the circle to the area near the kitchen.",
);


for ($i=0; $i<=$#sentences; $i++) {
	$sentence = $sentences[$i];

	$outMessage = `./runMessageInterpretation.pl "$sentence" | ./extractMessage.pl`;

	chomp $outMessage;
	
	print "[\"$sentence\",\n\"$outMessage\"],\n";
}
