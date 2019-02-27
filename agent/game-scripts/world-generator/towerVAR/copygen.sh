rm removed.output -f
python genTowerVAR.py >> removed.output
sh clean.sh
sh copy.sh
