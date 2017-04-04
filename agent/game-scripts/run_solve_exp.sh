suffix=".tower"
tstats=".tstats"
top=".top"

for rfile in *.tower
do
	rm startend.seconds
	c=1
	cp $rfile soar-game.script
	rfilename=${rfile%$suffix}
	java SentencesToSoar soar-game.script
	cp soar-game.soar /home/jrkirk/rosie2/rosie-project/rosie/agent/language-comprehension/comprehension/test-sentences/
	while [ $c -lt 5 ]
	do
		echo $c
		c=`expr $c + 1`
		/home/jrkirk/rosie2/rosie-project/soar/out/./cli < game-data-agent.soar	
		python calculateTeachSolvetime.py
	done
	cp s1.txt $rfilename$top
	cp startend.seconds $rfilename$tstats
	
done
