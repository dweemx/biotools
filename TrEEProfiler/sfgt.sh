#!/bin/bash
# Standardize Family Gene Tree with Taxid
echo "############### Standardize Family Gene Tree with Taxid ###############"
PROTEIN_A=$1
PROTEIN_B=$2
TEST="results/bdb/"

wget "http://www.uniprot.org/uniprot/$PROTEIN.fasta" -O "${DATASEQ}/$PROTEIN.fasta"

# Multiple replacements with sed : http://www.linuxask.com/questions/replace-multiple-strings-using-sed
echo "Standardizing the Species Tree..."
awk -F ";" '{print "s/"$3"/"$1"/g"}' "$RESULTS_A/map_tid_pid" > "$RESULTSPPI/map_on_tid.sed"
sed -f "$RESULTSPPI/map_on_tid.sed" < "$RESULTSPPI/stree.phy" > "$RESULTSPPI/stree.sdd.phy"