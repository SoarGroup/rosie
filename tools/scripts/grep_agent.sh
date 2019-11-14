# This is a grep command that will search all .soar files excluding comprehension, testing, and game-learning
grep -nR --include=*.soar --exclude-dir=game-scripts --exclude-dir=language-comprehension --exclude-dir=internal-worlds --exclude-dir=lucia --exclude-dir=game-learning --exclude=interpret-game-state-change.soar --exclude=interpret-game-state-tower-change.soar $1 $ROSIE_AGENT
