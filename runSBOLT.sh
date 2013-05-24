# required environment variables for this script to work:
# (paths for the SoarTech VM image in parens)
# $SOAR_HOME must be the Soar build directory (/opt/bolt/soar/out)
# $LD_LIBRARY_PATH must include the same
# $ABOLT_HOME must be the abolt root directory (/opt/bolt/abolt)
# $CLASSPATH must include all of these:
#   sbolt.jar  (/opt/bolt/stbolt/sbolt.jar)
#   abolt.jar  (/opt/bolt/abolt/java/abolt.jar)
#   lgsoar.jar (/opt/bolt/stbolt/lgsoar/lgsoar.jar)
#   april.jar  (/opt/bolt/april/java/april.jar)
#   lcm.jar    (/opt/bolt/lcm/lcm-java/lcm.jar)
#   sml.jar    (/opt/bolt/soar/out/java/sml.jar)

# all of the jars except lcm and sml can be rebuilt by running ant in their
# directory

java edu.umich.insoar.InSoar $* &
