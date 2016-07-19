#!/bin/bash
echo "############### Make BLAST Species Databases #####################"
BAS_FILE=$1 # Bacteria Assembly Summary file

# Loop through files in directory : http://www.cyberciti.biz/faq/unix-loop-through-files-in-a-directory/
for FILE in blast/db/seq/*.*
do
	# Extract filename : http://stackoverflow.com/questions/965053/extract-filename-and-extension-in-bash
	FILENAME=$(basename "$FILE")
	FILENAME="${FILENAME%.*}"
	# Split string : http://stackoverflow.com/questions/10586153/split-string-into-an-array-in-bash
	IFS='_' read -r -a array <<< "$FILENAME"
	AA="${array[0]}_${array[1]}"
	TAXID=$(sh cbysmcn.sh $1 $AA taxid)
	ORGNAME=$(sh cbysmcn.sh $1 $AA organism_name)
	# BLAST+ : http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2803857/
	# BLAST+ user manual : http://nebc.nerc.ac.uk/bioinformatics/documentation/blast+/user_manual.pdf
	echo "Making blast db for $ORGNAME ($TAXID)..."
	makeblastdb -in $FILE -dbtype "prot" -title "$ORGNAME" -parse_seqids -hash_index -out "blast/db/$TAXID" -taxid $TAXID
	echo "done!"
done