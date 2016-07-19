#!/bin/bash
IN=$1
OUT=$2
# (4.2) Convert Phylip format into Newick format
#ecceTERA: use NEWICK as tree format
# Option 1 : Remove all new line separators (convert phylip to newick) : http://stackoverflow.com/questions/3134791/how-do-i-remove-newlines-from-a-text-file
tr -d '\n' < $IN > $OUT
# Add new line end of file : http://unix.stackexchange.com/questions/31947/how-to-add-a-newline-to-the-end-of-a-file
sed -i -e '$a\' $OUT
#rm $BOOT/*_common_orthologues.sdd.bs${I}.phb
# Option 2 :
# sh cfpt.sh ${PROTEIN_A} ${PROTEIN_B}