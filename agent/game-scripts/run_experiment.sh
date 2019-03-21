suffix=".parse"
stats=".stats"
maxstats=".maxstats"
smem=".smem"
csv=".csv"

#smemq=".smemq"
#smems=".smems"
#smemtq=".smemtq"
#smemts=".smemts"
#smemtr=".smemtr"
#smemt3=".smemt3"
topstate=".top"
dialog=".dialog"
pertaskstats=".pertaskstats"
#pertaskstats2=".pertaskstats2"
dc=".dc"


rm statsmax.txt -f
rm pertaskstats.txt -f
rm smemuse.txt -f
rm statsdc.txt -f

for rfile in *.parse
do
	
	cp $rfile soar-game.script
	rfilename=${rfile%$suffix}
	
	#b="xnrvamwfKp5LhMtbc2yBzlHq1u3giTkjsP64FI8T"
	#if [ "$rfilename" \< "$b" ]; then
	#	echo $rfilename
	#	continue
	#fi
	#echo "oops"
	
	#BUILD VERSION 2
    cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
    /home/jrkirk/rosie2/scripts/build_agent game >> out.txt
	
    #BUILD VERSION 1
	#java SentencesToSoar soar-game.script
	#cp soar-game.soar /home/jrkirk/rosie2/rosie-project/rosie/agent/language-comprehension/comprehension/test-sentences/
	/home/jrkirk/rosie2/rosie-project/soar/out/./soar -s game-agent.soar stop
	#/home/jrkirk/rosie2/rosie-project/soar/out/./soar -s game-data-agent.soar stop
	cp stats.txt data/$rfilename$stats
	cp statsm.txt data/$rfilename$maxstats
	cp s1.txt data/$rfilename$topstate
	cp soar-game.script data/$rfilename$dialog
	
	cp pertaskstats.txt data/$rfilename$pertaskstats
	cp smemuse.txt data/$rfilename$smem
	cp statsdc.txt data/$rfilename$dc

	#cp smemquery.txt newresults4/$rfilename$smemq
	#cp smemstore.txt newresults4/$rfilename$smems
	#cp smemtimestore.txt newresults4/$rfilename$smemts
	#cp smemtimequery.txt newresults4/$rfilename$smemtq
	#cp smemtimeret.txt newresults4/$rfilename$smemtr
	#cp smemtimethree.txt newresults4/$rfilename$smemt3

	#rm smemtimethree.txt
	#rm smemtimeret.txt
	#rm smemtimequery.txt
	#rm smemtimestore.txt
	#rm smemstore.txt
	#rm smemquery.txt
	
	rm statsm.txt
	rm pertaskstats.txt
	rm smemuse.txt
	rm statsdc.txt

		
done
