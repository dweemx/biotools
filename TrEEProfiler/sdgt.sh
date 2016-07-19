#!/bin/bash
# Standardize Gene Tree and Species Tree with Taxid
echo ""
echo "############### Standardize Gene Tree with Taxid ###############"
PROTEIN_A=$1
PROTEIN_B=$2
SB=$3 # SeqBoot used?
RESULTSPPI=$4

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS="${ROOT}/results/bdb"
RESULTS_A="${BLASTRESULTS}/${PROTEIN_A}"
RESULTS_B="${BLASTRESULTS}/${PROTEIN_B}"

echo "> Standardizing the Protein Tree of $PROTEIN_A..."
# SeqBoot take only the first 10 chars into account!
# Extract nth characters : http://www.theunixschool.com/2012/05/different-ways-to-print-first-few.html
if [ "$SB" == "true" ]; then
	awk -F";" '{print $1";"substr($2,0,10)";"$3}' "$RESULTS_A/map_tid_pid" > "$RESULTS_A/map_tid_pid10"
	awk -F";" '{print "s/"$2"/"$1"/g"}' "$RESULTS_A/map_tid_pid10" > "$RESULTS_A/map_pid_tid.sed"
else
	awk -F";" '{print "s/"$2"/"$1"/g"}' "$RESULTS_A/map_tid_pid" > "$RESULTS_A/map_pid_tid.sed"
fi
	#statements
#sed -f "$RESULTS_A/map_pid_tid.sed" < "$RESULTSPPI/${PROTEIN_A}_common_orthologues.phb" > "$RESULTSPPI/${PROTEIN_A}_common_orthologues.sdd.phb"
sed -f "$RESULTS_A/map_pid_tid.sed" < "$RESULTSPPI/${PROTEIN_A}.gtrees" > "$RESULTSPPI/${PROTEIN_A}.sdd.gtrees"

echo "> Standardizing the Protein Tree of $PROTEIN_B..."
# SeqBoot take only the first 10 chars into account!
if [ "$SB" == "true" ]; then
	awk -F";" '{print $1";"substr($2,0,10)";"$3}' "$RESULTS_B/map_tid_pid" > "$RESULTS_B/map_tid_pid10"
	awk -F";" '{print "s/"$2"/"$1"/g"}' "$RESULTS_B/map_tid_pid10" > "$RESULTS_B/map_pid_tid.sed"
else
	awk -F";" '{print "s/"$2"/"$1"/g"}' "$RESULTS_B/map_tid_pid" > "$RESULTS_B/map_pid_tid.sed"
fi	
#sed -f "$RESULTS_B/map_pid_tid.sed" < "$RESULTSPPI/${PROTEIN_B}_common_orthologues.phb" > "$RESULTSPPI/${PROTEIN_B}_common_orthologues.sdd.phb"
sed -f "$RESULTS_B/map_pid_tid.sed" < "$RESULTSPPI/${PROTEIN_B}.gtrees" > "$RESULTSPPI/${PROTEIN_B}.sdd.gtrees"
