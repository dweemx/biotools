#!/bin/bash
# Make Comparable Alignments
PATH=$PATH:~/thesis/samtools/samtools-1.3/
echo ""
echo "############### Make Comparable Alignments ###############"
PROTEIN_A=$1
PROTEIN_A_ALN=$2
PROTEIN_B=$3
PROTEIN_B_ALN=$4
INTERSECT_EXTORGANISM_LIST=$5
RESULTSPPI=$6

ROOT="${VSC_DATA}/thesis"

RESULTS_A="${ROOT}/results/bdb/${PROTEIN_A}"
RESULTS_B="${ROOT}/results/bdb/${PROTEIN_B}"

# # Create an index file file
# samtools faidx "${RESULTS_A}/${PROTEIN_A}_orthologues.aln"
# samtools faidx "${RESULTS_B}/${PROTEIN_B}_orthologues.aln"

COMMON_LIST_A="${RESULTSPPI}/common_list_a"
COMMON_LIST_B="${RESULTSPPI}/common_list_b"

TAXID_COMMON_LIST="${RESULTSPPI}/taxid_common_list"
PROTEIN_A_JOINED_ORTHS_IDX="${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.idx"
PROTEIN_B_JOINED_ORTHS_IDX="${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.idx"

# Compare 2 text files on different columns : http://stackoverflow.com/questions/3186215/how-can-i-compare-two-text-files-which-has-multiple-fields-in-unix
# Print common elements in both files : http://stackoverflow.com/questions/15065818/compare-files-with-awk
awk -F";" 'NR==FNR{a[$1]++;next} (a[$1])' "${RESULTS_A}/map_tid_pid" "${RESULTS_B}/map_tid_pid" | sort -t";" -k1 > $COMMON_LIST_B
awk -F";" 'NR==FNR{a[$1]++;next} (a[$1])' "${RESULTS_B}/map_tid_pid" "${RESULTS_A}/map_tid_pid" | sort -t";" -k1 > $COMMON_LIST_A

# Check if variable is empty: http://serverfault.com/questions/7503/how-to-determine-if-a-bash-variable-is-empty
if [ -z "$INTERSECT_EXTORGANISM_LIST" ]; then
	awk -F";" '{print $1}' $COMMON_LIST_A > $TAXID_COMMON_LIST
	awk -F";" '{print $2}' $COMMON_LIST_A > $PROTEIN_A_JOINED_ORTHS_IDX
	awk -F";" '{print $2}' $COMMON_LIST_B > $PROTEIN_B_JOINED_ORTHS_IDX
else
	echo "Intersect the default complete reference bacteria genomes with custom species list"
	# Get the intersection between common orthologues in both proteins and the organism in the given external organism list
	sort -k1,1 ${INTERSECT_EXTORGANISM_LIST} | dos2unix > ${RESULTSPPI}/tmp1
	join -t";" -11 -21 ${COMMON_LIST_A} ${RESULTSPPI}/tmp1 > ${RESULTSPPI}/tmp_a
	join -t";" -11 -21 ${COMMON_LIST_B} ${RESULTSPPI}/tmp1 > ${RESULTSPPI}/tmp_b
	awk -F";" '{print $1}' ${RESULTSPPI}/tmp_a > ${TAXID_COMMON_LIST}
	awk -F";" '{print $2}' ${RESULTSPPI}/tmp_a > $PROTEIN_A_JOINED_ORTHS_IDX
	awk -F";" '{print $2}' ${RESULTSPPI}/tmp_b > $PROTEIN_B_JOINED_ORTHS_IDX
	rm ${RESULTSPPI}/tmp*
fi

# Remove all non-integers : http://www.cyberciti.biz/faq/sed-remove-all-except-digits-numbers/
#sed -i 's/[^0-9]*//g' $TAXID_COMMON_LIST --> DOES NOT REMOVE ALL THE LINE PROBLEM IF INTEGERS IN THE LINE WITH STRINGS
sed -i '/[^0-9].*/d' $TAXID_COMMON_LIST

# Extract all common protein sequences from same species
# Extract sequence from alignment : https://www.biostars.org/p/49820/
xargs samtools faidx ${PROTEIN_A_ALN} < $PROTEIN_A_JOINED_ORTHS_IDX > "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln"
xargs samtools faidx ${PROTEIN_B_ALN} < $PROTEIN_B_JOINED_ORTHS_IDX > "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln"