#!/bin/bash
# syntax error near unexpected token `done' > http://unix.stackexchange.com/questions/81355/trouble-with-read-line-script-in-cygwin
# Read file line by line : http://linuxpoison.blogspot.be/2012/08/bash-script-how-read-file-line-by-line.html
# Extract substring in bash : http://stackoverflow.com/questions/428109/extract-substring-in-bash
# String concat with underscore : http://unix.stackexchange.com/questions/88452/concatenating-two-variables-with-an-underscore
echo "############### Download #####################"
awk -F "\t" '{print $20}' $1 > tmp_ftp_loc
FILE=tmp_ftp_loc

while read -r LINE; do

	FNAME=$(echo $LINE | cut -d'/' -f 6)
	DURL=$(echo ${LINE}/${FNAME}_protein.faa.gz)
	OUTPUT=$(echo blast/db/seq/${FNAME}_protein.faa.gz)
	echo $OUTPUT
	curl $DURL -o $OUTPUT
	echo "Extracting..."
	# Uncompress gz file : http://www.cyberciti.biz/faq/howto-compress-expand-gz-files/
	gzip -d $OUTPUT
	rm -f $OUTPUT
	echo $DURL
	
done <$FILE

# [NOT USED] How to download bacterial genomes : http://ncbiinsights.ncbi.nlm.nih.gov/2013/02/19/how-to-download-bacterial-genomes-using-the-entrez-api/