#!/bin/bash
# Automated alignment filtering (Masking and trimming)
echo ""
echo "############### Automated alignment filtering ###############"
PIPE_MOD_1_MSA=$1
PIPE_MOD_3_MSA_AF=$2
PROTEIN_A=$3
PROTEIN_B=$4

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS_A="${ROOT}/results/bdb/${PROTEIN_A}"
BLASTRESULTS_B="${ROOT}/results/bdb/${PROTEIN_B}"

PROTEIN_A_ALN="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln"
PROTEIN_B_ALN="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln"
PROTEIN_A_ALN_PHY="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln.phy"
PROTEIN_A_ALN_MASK="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln.mask"
PROTEIN_B_ALN_PHY="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln.phy"
PROTEIN_B_ALN_MASK="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln.mask"

rm_details_seq_name() {
	PROTEIN=$1
	PROTEIN_ALN=$2
	BLASTRESULTS="${ROOT}/results/bdb/${PROTEIN}"
	grep ">" ${PROTEIN_ALN} | cut -c 1- | awk -F" " '{$1=""; print $0}' | cut -c 2- | sed "s/\//\\\\\//g" | sed "s/\[/\\\[/g" | sed "s/\]/\\\]/g" | awk '{print "s/"$0"//g"}' > "${BLASTRESULTS}/${PROTEIN}_rmodsn.sed"
	sed -f "${BLASTRESULTS}/${PROTEIN}_rmodsn.sed" ${PROTEIN_ALN} > "${PROTEIN_ALN}-wood"
}

# Using ZORRO 
if [ "${PIPE_MOD_3_MSA_AF}" == "zorro" ]; then
	PATH=$PATH:~/thesis/zorro/bin
	# Protein A
	echo "> Converting fasta alignment of ${PROTEIN_A} to phylip format..."
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter ${PROTEIN_A_ALN} fasta phylip-seq ${PROTEIN_A_ALN_PHY}
	echo "> Masking alignment of ${PROTEIN_A}..."
	# Use sampling shortcut (-sample) to reduce running time
	# Check if the mask exists and is not empty
	if [ ! -s ${PROTEIN_A_ALN_MASK} ]; then
		zorro -sample ${PROTEIN_A_ALN} > ${PROTEIN_A_ALN_MASK}
	else
		echo "> Mask already cached for ${PROTEIN_B}"
	fi
	for THRESHOLD in 0.1 0.5 0.8
	do
		echo "> Trimming alignment of ${PROTEIN_A} using threshold of ${THRESHOLD}..."
		PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}-${THRESHOLD}.aln"
		if [ ! -s ${PROTEIN_A_ALN_TRD} ]; then
			java -jar bio-utils-0.1.jar BioAlignmentTrimmer ${PROTEIN_A_ALN_PHY} ${PROTEIN_A_ALN_MASK} ${THRESHOLD} ${PROTEIN_A_ALN_TRD} fasta
		else
			echo "> Trimmed alignment already cached for ${PROTEIN_A}"
		fi
	done
	# Protein B
	echo "> Converting fasta alignment of ${PROTEIN_B} to phylip format..."
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter ${PROTEIN_B_ALN} fasta phylip-seq ${PROTEIN_B_ALN_PHY}
	echo "> Masking alignment of ${PROTEIN_B}..."
	# Check if the mask exists and is not empty
	if [ ! -s ${PROTEIN_B_ALN_MASK} ]; then
		zorro -sample ${PROTEIN_B_ALN} > ${PROTEIN_B_ALN_MASK}
	else
		echo "> Mask already cached for ${PROTEIN_A}"
	fi
	for THRESHOLD in 0.1 0.5 0.8
	do
		echo "> Trimming alignment of ${PROTEIN_B} using threshold of ${THRESHOLD}..."
		PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}-${THRESHOLD}.aln"
		if [ ! -s ${PROTEIN_B_ALN_TRD} ]; then
			java -jar bio-utils-0.1.jar BioAlignmentTrimmer ${PROTEIN_B_ALN_PHY} ${PROTEIN_B_ALN_MASK} ${THRESHOLD} ${PROTEIN_B_ALN_TRD} fasta
		else
			echo "> Trimmed alignment already cached for ${PROTEIN_B}"
		fi
	done
elif [ "${PIPE_MOD_3_MSA_AF}" == "gblocks-s" ]; then # Using stringent Glocks conditions
	PATH=$PATH:~/thesis/gblocks/Gblocks_0.91b
	# Stringent (Recommended for NJ and parsimony methods see http://sysbio.oxfordjournals.org/content/56/4/564.long)
	# Remove the organism details for Protein A sequence alignment because Gblocks cannot handle such long sequence names
	PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	if [ ! -s ${PROTEIN_A_ALN_TRD} ]; then
		echo "> Trimming alignment of ${PROTEIN_B}..."
		rm_details_seq_name ${PROTEIN_A} ${PROTEIN_A_ALN}
		Gblocks "${PROTEIN_A_ALN}-wood" -t=p -e=-gbl -d=y
		mv "${PROTEIN_A_ALN}-wood-gbl" ${PROTEIN_A_ALN_TRD}
	else
		echo "> Trimmed alignment already cached for ${PROTEIN_A}"
	fi
	# Protein B
	PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	if [ ! -s ${PROTEIN_B_ALN_TRD} ]; then
		echo "> Trimming alignment of ${PROTEIN_B}..."
		rm_details_seq_name ${PROTEIN_B} ${PROTEIN_B_ALN}
		Gblocks "${PROTEIN_B_ALN}-wood" -t=p -e=-gbl -d=y
		mv "${PROTEIN_B_ALN}-wood-gbl" "${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	else
		echo "> Trimmed alignment already cached for ${PROTEIN_B}"
	fi
elif [ "${PIPE_MOD_3_MSA_AF}" == "gblocks-r" ]; then # Using relaxed Glocks conditions
	PATH=$PATH:~/thesis/gblocks/Gblocks_0.91b
	# Relaxed (Recommended for Maximum likelihood methods)
	# Protein A
	PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	if [ ! -s ${PROTEIN_A_ALN_TRD} ]; then
		echo "> Trimming alignment of ${PROTEIN_A}..."
		rm_details_seq_name ${PROTEIN_A} ${PROTEIN_A_ALN}
		Gblocks "${PROTEIN_A_ALN}-wood" -t=p -e=-gbl -d=y -b3=10 -b4=5 -b5=h
		mv "${PROTEIN_A_ALN}-wood-gbl" "${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	else
		echo "> Trimmed alignment already cached for ${PROTEIN_A}"
	fi
	# Protein B
	PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	if [ ! -s ${PROTEIN_B_ALN_TRD} ]; then
		echo "> Trimming alignment of ${PROTEIN_B}..."
		rm_details_seq_name ${PROTEIN_B} ${PROTEIN_B_ALN}
		Gblocks "${PROTEIN_B_ALN}-wood" -t=p -e=-gbl -d=y -b3=10 -b4=5 -b5=h
		mv "${PROTEIN_B_ALN}-wood-gbl" "${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	else
		echo "> Trimmed alignment already cached for ${PROTEIN_B}"
	fi
fi