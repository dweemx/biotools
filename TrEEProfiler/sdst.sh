#!/bin/bash
# Standardize Gene Tree and Species Tree with Taxid
echo ""
echo "############### Standardize Species Tree with Taxid ###############"
PROTEIN_A=$1
PROTEIN_B=$2
RESULTSPPI=$3
OUTTREEPATH=$4

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS="${ROOT}/results/bdb/"
RESULTS_A="${BLASTRESULTS}/${PROTEIN_A}"
RESULTS_B="${BLASTRESULTS}/${PROTEIN_B}"

# Multiple replacements with sed : http://www.linuxask.com/questions/replace-multiple-strings-using-sed
# Remove single quotes : escape single quotes: http://stackoverflow.com/questions/9899001/how-to-escape-single-quote-in-awk-inside-printf
#awk -F ";" '{print "s/'\''"$3"'\''/"$1"/g"}' "$RESULTS_A/map_tid_pid" > "$RESULTSPPI/map_on_tid.sed"
#sed -f "$RESULTSPPI/map_on_tid.sed" < "$RESULTSPPI/stree.phy" > ${OUTTREEPATH}

# map_on_tid.sed from P0ABB0 vs P0ABB4 (considering all the species)
sed -f "data/map_on_tid.sed" < "$RESULTSPPI/stree.phy" > ${OUTTREEPATH}