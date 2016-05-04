suffix=".preparse"
stats=".stats"
maxstats=".maxstats"
smem=".smem"
topstate=".top"
csv=".csv"
dialog=".dialog"
pertaskstats=".pertaskstats"

dc=".dc"

rm pertaskstats.txt
rm smemuse.txt
rm statsdc.txt
for rfile in *.preparse
do
	
	cp $rfile soar-game.script
	rfilename=${rfile%$suffix}
	
	#b="fg34z8mrjb95top"
	#if [ "$rfilename" \< "$b" ]; then
	#	echo $rfilename
	#	continue
	#fi
	#echo "oops"
	java SentencesToSoar soar-game.script
	cp soar-game.soar /home/jrkirk/rosie2/rosie-project/rosie/agent/_agent/language-comprehension/comprehension/test-sentences/
	/home/jrkirk/rosie2/rosie-project/soar/out/./cli < game-data-agent.soar
	cp stats.txt resultsZ/$rfilename$stats
	cp statsmax.txt resultsZ/$rfilename$maxstats
	cp stats.csv resultsZ/$rfilename$csv
	cp s1.txt resultsZ/$rfilename$topstate
	cp soar-game.script resultsZ/$rfilename$dialog
	
	#cp pertaskstats.txt results7/$rfilename$pertaskstats
	#cp smemuse.txt results7/$rfilename$smem
	#cp statsdc.txt results7/$rfilename$dc
	
	
	#rm pertaskstats.txt
	#rm smemuse.txt
	#rm statsdc.txt
	
done
