# Replace variable tags - '<example>'
findreplace="s/<"$1">/<"$2">/g"
#echo $findreplace
#find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace standalone attributes - '^example '
findreplace="s/\^"$1" /\^"$2" /g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace attributes with a dot after - '^example.next'
findreplace="s/\^"$1"\./\^"$2"\./g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace dotted attributes - '^prev.example '
findreplace="s/\."$1" /\."$2" /g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace attributes in the middle of a list - '^prev.example.next'
findreplace="s/\."$1"\./\."$2"\./g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace standalone words surrounded by whitespace - 'prev example next'
findreplace="s/ "$1" / "$2" /g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace standalone words ending a line -  ' example\n'
findreplace="s/ "$1"$/ "$2"$/g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

# Replace standalone words ending in a parenthesis - ' example)' 
findreplace="s/ "$1"[)]/ "$2"[)]/g"
echo $findreplace
find ./ -type f -print0 | xargs -0 sed -i "$findreplace"

