suffix=".world"
strnames=""
for rfile in *.world
do
	rfilename=${rfile%$suffix}
	strnames+=${rfile}
	python genworldscript.py $rfilename
done
echo $strnames
