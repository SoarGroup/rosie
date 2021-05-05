import sys
import os

from rosie.testing.TesterClient import TesterClient

if len(sys.argv) <= 2 or '-h' in sys.argv or '--help' in sys.argv:
    print("python -m rosie.testing.run_rosie_test [config-file] [output-file] [FLAGS*]")
    print("  -h | --help     for help information")
    print("  -v | --verbose  for additional output during test")
    sys.exit(0)

# Argument 1 [REQ]: A rosie-client.config file
client_config = sys.argv[1]

# Argument 2 [REQ]: The output file to create
output_file = sys.argv[2]

# Optional Flag: -v|--verbose will print output to standard out
print_output = ('-v' in sys.argv or '--verbose' in sys.argv)

agent = TesterClient(
            config_filename = client_config, 
            write_to_stdout = print_output, 
            source_output   = ("summary" if print_output else "none")
        )

agent.run_test(output_file)

