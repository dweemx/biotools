#!/bin/bash
# THIS FILE NEED TO BE CONVERTED : dos2unix!
# Make Reconciliation between Species and Gene Trees
echo "############### Make Reconciliation between Species and Gene Trees ###############"
NOTUNGPATH="reconciliation/notung-2.8/Notung-2.8.1.6-beta/Notung-2.8.1.6-beta.jar"
PROTEIN_A=$1
PROTEIN_B=$2
RESULTS_A="results/${PROTEIN_A}"
RESULTS_B="results/${PROTEIN_B}"
RESULTSPPI="results/ppi/${PROTEIN_A}_${PROTEIN_B}"
java -jar $NOTUNGPATH --root -g "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.sdd.phb" -s "$RESULTSPPI/stree.sdd.phy" --parsable --infertransfers "true" --outputdir "$RESULTSPPI"
java -jar $NOTUNGPATH --root -g "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.sdd.phb" -s "$RESULTSPPI/stree.sdd.phy" --parsable --infertransfers "true" --outputdir "$RESULTSPPI"

