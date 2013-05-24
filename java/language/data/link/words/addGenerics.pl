#!/usr/bin/perl
#die "don't run this again unless you have reverted the dictionaries!";
$nGenerics = 20;

foreach $file (`ls | grep words`) {
	chomp $file;
	$wordType = $file;
	$wordType =~ s/words/generic-word/;
	print "processing $file\n";
	for ($i=0; $i<$nGenerics; $i++) {
		$cmd = "echo $wordType" . "[$i] >> $file";
#			print "$cmd\n";
		print `$cmd`;
	}
}
