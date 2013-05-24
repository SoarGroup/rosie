#!/usr/bin/perl

if ($ARGV[0] =~ /--simple/) {
	$simple = 1;
}
else {
	$simple = 0;
}

# sentences structure:
# sentence, correct structure 

@sentences = (
["What is the color of this?",
"relation-question 
  question-word what
  relation 
    p1 
      object o1
        specifier this
    p2 
      object o2
        specifier DEF
        word UNKNOWN
    word color-of"],
["This is a red block.",
"object-message 
  object o1
    specifier this
    word red
    word block"],
["This is red and a block.",
"object-message 
  object o1
    specifier this
    word block
    word red"],
["This is a yellow block.",
"object-message 
  object o1
    specifier this
    word yellow
    word block"],
["This is an orange block.",
"object-message 
  object o1
    specifier this
    word orange
    word block"],
["What shape is this?",
"object-question 
  object o1
    specifier this
    word shape
    word UNKNOWN
  question-word what"],
["This is a square block.",
"object-message 
  object o1
    specifier this
    word square
    word block"],
["This is a triangular block.",
"object-message 
  object o1
    specifier this
    word triangular
    word block"],
["Point at the square block.",
"verb-command 
  verb 
    preposition 
      object o1
        specifier DEF
        word square
        word block
      word at
    word point"],
["Point at the square red block.",
"verb-command 
  verb 
    preposition 
      object o1
        specifier DEF
        word red
        word square
        word block
      word at
    word point"],
["Point at the yellow one.",
"verb-command 
  verb 
    preposition 
      object o1
        specifier DEF
        word yellow
        word one
      word at
    word point"],
["Point at the triangular one.",
"verb-command 
  verb 
    preposition 
      object o1
        specifier DEF
        word triangular
        word one
      word at
    word point"],
["Describe the blocks.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word blocks
    word describe"],
["What is to the left of the red block?",
"relation-question 
  question-word what
  relation 
    p1 
      object o1
        specifier none
        word UNKNOWN
    p2 
      object o2
        specifier DEF
        word red
        word block
    word left-of"],
["The orange block is to the left of the red block",
"object-message 
  object o1
    specifier DEF
    word block
    word orange
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word red
        word block
    word left-of"],
["The orange block is on the table.",
"object-message 
  object o1
    specifier DEF
    word block
    word orange
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word table
    word on"],
["Where is the orange block?",
"object-question 
  object o1
    specifier DEF
    word orange
    word block
    word UNKNOWN
  question-word where"],
["Where is the red block?",
"object-question 
  object o1
    specifier DEF
    word red
    word block
    word UNKNOWN
  question-word where"],
["Move the yellow block to the pantry.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word yellow
    preposition 
      object o3
        specifier DEF
        word pantry
      word to
    word move"],
["Pick up the yellow block.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word yellow
    word pick"],
["Put the yellow block on the pantry.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word yellow
    preposition 
      object o3
        specifier DEF
        word pantry
      word on
    word put"],
["You moved the yellow block to the pantry.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word yellow
    preposition 
      object o3
        specifier DEF
        word pantry
      word to
    word moved"],
["Where is the yellow block.",
"object-question 
  object o1
    specifier DEF
    word yellow
    word block
    word UNKNOWN
  question-word where"],
["Move the red block to the sink.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word red
    preposition 
      object o3
        specifier DEF
        word sink
      word to
    word move"],
["No.",
"single-word-response 
  response no"],
["Red is a color.",
"object-message 
  object o1
    specifier INDEF
    word color
    word red"],
["The triangle is to the left of the yellow square.",
"object-message 
  object o1
    specifier DEF
    word triangle
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word yellow
        word square
    word left-of"],
["Move the blue square to the pantry.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word square
        word blue
    preposition 
      object o3
        specifier DEF
        word pantry
      word to
    word move"],
["The goal is a red square on the stove.",
"goal-relation-message 
  relation 
    p1 
      object o1
        specifier DEF
        word red
        word square
    p2 
      object o2
        specifier DEF
        word stove
    word on"],
["The goal is an empty stove.",
"goal-object-message 
  object o1
    specifier DEF
    word empty
    word stove"],
["What color is this?",
"object-question 
  object o1
    specifier this
    word color
    word UNKNOWN
  question-word what"],
["Pick up the square on the right of the triangle.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word square
      relation 
        p1 
          object o2
        p2 
          object o3
            specifier DEF
            word triangle
        word right-of
    word pick"],
["The color of this is red.",
"object-message 
  object o1
    specifier this
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word red
    word color-of"],
["Move the red block by the circle to the area near the kitchen.",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word block
        word red
      relation 
        p1 
          object o2
        p2 
          object o3
            specifier DEF
            word circle
        word by
    preposition 
      object o4
        specifier DEF
        word area
      relation 
        p1 
          object o4
        p2 
          object o5
            specifier DEF
            word kitchen
        word near
      word to
    word move"],
["This one.",
"object-message 
  object o1
    specifier this
    word one"],
["The square to the right.",
	"object-message 
  object o1
    specifier DEF
    word square
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word right
    word to"],
["The circle on the left.",
	"object-message 
  object o1
    specifier DEF
    word circle
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word left
    word on"],
["Is this triangle blue?", 
"object-is-question 
  object o1
    questioned blue
    specifier this
    word blue
    word triangle"],
["Is the triangle to the left of the square?", 
"object-is-question 
  object o1
    specifier DEF
    word triangle
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word square
    questioned true
    word left-of"],
["Is the triangle on the table blue?", 
"object-is-question 
  object o1
    questioned blue
    specifier DEF
    word blue
    word triangle
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word table
    word on"],
["Describe the relationship between the red triangle and the yellow square",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word relationship
      relation 
        p1 
          object o2
        p2 
          object o3
            specifier DEF
            word yellow
            word square
        word between
      relation 
        p1 
          object o2
        p2 
          object o4
            specifier DEF
            word red
            word triangle
        word between
    word describe",
"verb-command 
  verb 
    direct-object o1
      object o2
        specifier DEF
        word relationship
      relation 
        p1 
          object o2
        p2 
          object o3
            specifier DEF
            word red
            word triangle
        word between
      relation 
        p1 
          object o2
        p2 
          object o4
            specifier DEF
            word yellow
            word square
        word between
    word describe"],
["context_which_question The red square to the right of the circle.", 
"object-message 
  object o1
    specifier DEF
    word square
    word red
  relation 
    p1 
      object o1
    p2 
      object o2
        specifier DEF
        word circle
    word right-of"],
["The pan should be on the stove and the stove should be on.",
"goal-relation-message 
  relation 
    p1 
      object o1
        specifier DEF
        word pan
    p2 
      object o2
        specifier DEF
        word stove
    word on
goal-object-message 
  object o3
    specifier DEF
    word stove
    word on"]
);

$passCount = 0;
$failCount = 0;

for ($i=0; $i<=$#sentences; $i++) {
	$sentence = $sentences[$i][0];
	$correctMessage = $sentences[$i][1];
	@struct = @{$sentences[$i]};
	if ($#struct > 1) {
		$alternateCorrectMessage = $sentences[$i][2];
	}
	else {
		$alternateCorrectMessage = "NO_ALTERNATE";
	}

	$outMessage = `./runMessageInterpretation.pl "$sentence" | ./extractMessage.pl`;

	chomp $outMessage;
	
	if (($outMessage eq $correctMessage or $outMessage eq $alternateCorrectMessage) and $outMessage ne "") {
		if ($simple == 0) {
			print "test sentence: $sentence\n";
			print "PASSED:\n$correctMessage\n";
		}
		else {
			print "$sentence PASSED\n";
		}
		$passCount++;
	}
	else {
		if ($simple == 0) {
			print "test sentence: $sentence\n";
			print "FAILED:\n";	
			print "expected \n$correctMessage\n";
			print "got \n$outMessage\n";
		}
		else {
			print "$sentence FAILED\n";
		}

		$failCount++;
	}
}

$total = $passCount + $failCount;

print "passed $passCount of $total tests.\n";
