JAVA_PATH="/usr/lib/jvm"
JAVA_LOCATION=`ls $JAVA_PATH | grep java-6-openjdk`
SPHINX_FLAGS=`pkg-config --cflags --libs pocketsphinx sphinxbase`

for file in $JAVA_LOCATION
do
    if [ -e $JAVA_PATH/$file/include ]
    then
	JAVA_INCLUDES="-I $JAVA_PATH/$file/include/ -I $JAVA_PATH/$file/include/linux/"
    fi
done

gcc -shared -fpic -o libsphinx.so $JAVA_INCLUDES src/edu/umich/insoar/sphinxJNI.c $SPHINX_FLAGS

mkdir -p lib
mv libsphinx.so lib/
