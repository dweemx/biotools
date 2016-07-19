#!/bin/bash
# Make DTLI Pair Vectors
echo "############### Make DTLI Pair Vectors ###############"
# Remove first line : http://stackoverflow.com/questions/10460919/how-to-delete-first-two-lines-and-last-four-lines-from-a-text-file-with-bash
# Concat lines of file by separator : http://stackoverflow.com/questions/15758814/turning-multiple-lines-into-one-line-with-comma-separated-perl-sed-awk

PROTEIN_A=$1
PROTEIN_B=$2
RESULTS_A="results/${PROTEIN_A}"
RESULTS_B="results/${PROTEIN_B}"
RESULTSPPI="results/ppi/${PROTEIN_A}_${PROTEIN_B}"
PPEVV="${RESULTSPPI}/${PROTEIN_A}_${PROTEIN_B}_dtli.pairvector"
rm $PPEVV
grep "#S" "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.sdd.phb.rooting.0.parsable.txt" | tail -n +2 | awk -F "\t" '{print $3,$4,$5,$6,$7}' | paste -d ' ' -s >> $PPEVV
grep "#S" "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.sdd.phb.rooting.0.parsable.txt" | tail -n +2 | awk -F "\t" '{print $3,$4,$5,$6,$7}' | paste -d ' ' -s >> $PPEVV
