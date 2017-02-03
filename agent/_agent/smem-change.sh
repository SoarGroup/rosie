cd $1
sed -i 's/@A/@10/g' *.soar
sed -i 's/@G/@20/g' *.soar
sed -i 's/@N/@30/g' *.soar
sed -i 's/@P/@40/g' *.soar
sed -i 's/@R/@50/g' *.soar
sed -i 's/@T/@60/g' *.soar
sed -i 's/@X/@70/g' *.soar
sed -i 's/@Y/@80/g' *.soar
sed -i 's/@Z/@90/g' *.soar
sed -i 's/@D/@99/g' *.soar

