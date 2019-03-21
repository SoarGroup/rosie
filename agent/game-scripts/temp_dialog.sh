suffix=".parse"
dialog=".dialog"

for rfile in *.parse
do
	rfilename=${rfile%$suffix}	
	cp $rfile data/$rfilename$dialog
done
