#!/usr/bin/perl

# jar files needed in classpath:
#   lgsoar.jar (/opt/bolt/stbolt/lgsoar/lgsoar.jar)
#		SoarJavaDebugger.jar (/opt/bolt/soar/out/SoarJavaDebugger.jar)
#   sml.jar    (/opt/bolt/soar/out/java/sml.jar)

$cmd = "java  com.soartech.bolt.SoarRunner --source soarcode/load-standalone.soar";

# need to preserve quoted arguments
foreach $arg (@ARGV) {
	if ($arg =~ / /) {
		$cmd .= " \"$arg\"";
	}
	else {
		$cmd .= " $arg";
	}
}

exec($cmd);
