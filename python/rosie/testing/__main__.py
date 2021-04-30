import sys
import os

from rosie.testing.TestAgent import TestAgent

# Lookup $ROSIE_HOME environment variable [REQUIRED]
rosie_home = ""
if "ROSIE_HOME" in os.environ:
    rosie_home = os.environ["ROSIE_HOME"]
else:
    print("ERROR: Requires ROSIE_HOME environment variable set")
    sys.exit(0)

# Argument 1: The test name (from test-agents/task-tests)
if len(sys.argv) <= 1:
    print("python -m rosie.testing [test_name]")
    print("  test_name is a test under test-agents/task-tests")
    sys.exit(0)

test_name = sys.argv[1]

# Flag: -v|--verbose will print output to standard out
print_output = ('-v' in sys.argv or '--verbose' in sys.argv)

agent = TestAgent(config_filename=rosie_home + "/test-agents/task-tests/" + test_name + "/agent/rosie-client.config",
        write_to_stdout=print_output, source_output=("summary" if print_output else "none"))

agent.run_test(rosie_home + "/test-agents/task-tests/" + test_name + "/correct-output.txt")

