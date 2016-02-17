suffix=".preparse"
stats=".stats"
maxstats=".maxstats"
smem=".smem"
topstate=".top"
csv=".csv"
dialog=".dialog"
pertaskstats=".pertaskstats"
dc=".dc"

for rfile in *.preparse
do
	cp $rfile soar-game.script
	rfilename=${rfile%$suffix}
	
	java SentencesToSoar soar-game.script
	cp soar-game.soar /home/jrkirk/rosie2/rosie-project/rosie/agent/_agent/language-comprehension/comprehension/test-sentences/
	/home/jrkirk/rosie2/rosie-project/soar/out/./cli < game-data-agent.soar
	cp stats.txt results/$rfilename$stats
	cp statsmax.txt results/$rfilename$maxstats
	cp stats.csv results/$rfilename$csv
	cp s1.txt results/$rfilename$topstate
	cp soar-game.script results/$rfilename$dialog
	
	cp pertaskstats.txt results/$rfilename$pertaskstats
	cp smemuse.txt results/$rfilename$smem
	cp statsdc.txt results/$rfilename$dc
	
	
	rm pertaskstats.txt
	rm smemuse.txt
	rm statsdc.txt
	
done
