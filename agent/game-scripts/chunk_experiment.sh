suffix=".preparse"
stats=".chunkstats"
start=".start"
end=".end"

rm chunked-stats.txt
rm *parsetime.txt
for rfile in *.preparse
do
	
	cp $rfile soar-game.script
	rfilename=${rfile%$suffix}
	
	java SentencesToSoar soar-game.script
	cp soar-game.soar /home/jrkirk/rosie2/rosie-project/rosie/agent/language-comprehension/comprehension/test-sentences/
	/home/jrkirk/rosie2/rosie-project/soar/out/./cli < game-data-agent.soar
	cp chunked-stats.txt resultsX/$rfilename$stats
	cp startparsetime.txt resultsX/$rfilename$start
	cp endparsetime.txt resultsX/$rfilename$end
	rm startparsetime.txt
	rm endparsetime.txt
	rm chunked-stats.txt
done
