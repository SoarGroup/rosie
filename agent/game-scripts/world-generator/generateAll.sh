suffix=".world"

for rfile in *.world
do
	rfilename=${rfile%$suffix}
	python genworldscript.py $rfilename
done
