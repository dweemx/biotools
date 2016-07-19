#!/bin/bash
PATH=$PATH:~/thesis/reconciliation/ecceTERA/bin/
module unload R/3.2.1-foss-2014a-x11-tcl
# Pipe to follow for computing the similarity of evolutionary event pattern between 2 proteins

# ############### Deterministic Pipe for Computing the Cosine Similarity between Evolutionary Event Profiles of 2 Proteins ###############
# Using:
# 	- Type : Deterministic
#   	- Multiple Sequence Alignment: MAFFT L-INS-I | MAFFT E-INS-I | MUSCLE
#   	- MSA Automated Filtering : ZORRO 
# 	- Phylogenetic Tree Construction: FasTree | ClustalW
# 	- Phylogenetic Reconciliation: ecceTERA
# 	- Species Tree: GreenGenes (16S rRNA) | BEAST (GreenGenes 16r RNA Alignment)
# 	- Polytomy Resolver: none
# 	- Gene Tree Correction: none
# 	- EE Profile Constructor: cat2 (wo S)
# #################################################################################################################"

# Set up the default values
PIPE_MOD_1_MSA="" # Multiple Sequence Alignment (MSA)""
PIPE_MOD_2_MSA_REF="" # MSA Refinement
PIPE_MOD_3_MSA_AF="" # MSA Automated Filtering
MSA_AF_TH=0.0 # Threshold of MSA Automated Filtering
PIPE_MOD_4_PTC="" # Phylogenetic Tree Construction
PTC_NB=100 # Number of bootstrap for Phylogenetic Tree Construction
PD_THRESHOLD=0.0 # Threshold for the Phylogenetic Diversity
PROTEIN_A="" # Protein A
PROTEIN_B="" # Protein B
IS_PPI="" # Does protein A and B interact?

help() {
	echo "Usage: sh dpccseep.sh [OPTION]... "
	echo ""
	echo "Deterministic pipe for computing the cosine similarity between evolutionary event profiles of 2 proteins"
	echo ""
    echo "-a|--alignment:		Multiple sequence alignment method (mafft,muscle)"
    echo "-r|--refinement:	Not yet implemented"
    echo "-f|--autofiltering:	Multiple sequence alignment automated filtering method (zorro,gblocks-s,glocks-r)"
    echo "-h|--afthreshold:	Autofiltering threshold value (only if -f zorro)"
    echo "-t|--tree:		Phylogenetic tree construction method"
    echo "-b|--bootstrap:		Number of bootstraps"
    echo "-p|--pdthreshold:	Phylogenetic diversity threshold (Number of substitutions/site)"
    echo "-1|--protein1:		A first protein"
    echo "-2|--protein2:		A second protein"
    echo "-i|--interact:		Does they interact? (true,false)"
    echo "-d|--save:		Save results on disk? (true,false)"
}

# TODO : How to parse command line arguments : http://stackoverflow.com/questions/192249/how-do-i-parse-command-line-arguments-in-bash
# Use > 1 to consume two arguments per pass in the loop (e.g. each
# argument has a corresponding value to go with it).
# Use > 0 to consume one or more arguments per pass in the loop (e.g.
# some arguments don't have a corresponding value to go with it such
# as in the --default example).
# note: if this is set to > 0 the /etc/hosts part is not recognized ( may be a bug )
while [ "$#" -ge "1" ]
do
	KEY="$1"
	case $KEY in
	    -a|--alignment) 
		PIPE_MOD_1_MSA="$2" 
	    shift # past argument
	    ;;
	    -r|--refinement)
	    PIPE_MOD_2_MSA_REF="$2"
	    shift # past argument
	    ;;
	    -f|--autofiltering)
	    PIPE_MOD_3_MSA_AF="$2"
	    shift # past argument
	    ;;
	    -h|--autofilteringthreshold)
	    MSA_AF_TH="$2"
	    shift # past argument
	    ;;
	    -t|--tree)
	    PIPE_MOD_4_PTC="$2"
	    shift # past argument
	    ;;
	    -b|--bootstrap)
	    PTC_NB="$2"
	    shift # past argument
	    ;;
	   	-p|--pdthreshold)
	    PD_THRESHOLD="$2"
	    shift # past argument
	    ;;
	    -1|--protein1)
	    PROTEIN_A="$2"
	    shift # past argument
	    ;;
	    -2|--protein2)
	    PROTEIN_B="$2"
	    shift # past argument
	    ;;
	   	-i|--isppi)
	    IS_PPI="$2"
	    shift # past argument
	    ;;
	    --help)
		help
	    exit
	    ;;
	    *)
	    # unknown option
	    ;;
	esac
	shift # past argument or value
done

# Check variable is empty : http://serverfault.com/questions/7503/how-to-determine-if-a-bash-variable-is-empty
if [ -z "$PIPE_MOD_1_MSA" ] || [ -z "$PIPE_MOD_4_PTC" ] || [ -z "$PROTEIN_A" ] || [ -z "$PROTEIN_B" ]; then
	help
	exit
fi

# PIPE_MOD_1_MSA=$1 # Multiple Sequence Alignment (MSA)
# PIPE_MOD_2_MSA_REF=$2 # MSA Refinement
# PIPE_MOD_3_MSA_AF=$3 # MSA Automated Filtering
# MSA_AF_TH=$4
# PIPE_MOD_4_PTC=$5 # Phylogenetic Tree Construction
# PTC_NB=$6
# PROTEIN_A=$7
# PROTEIN_B=$8

ROOT="${VSC_DATA}/thesis"

# Create the pipe directory 
PIPE_RESULTS="${ROOT}/results/D-pipe"
mkdir -p ${PIPE_RESULTS}
if [ "${PIPE_MOD_3_MSA_AF}" == "zorro" ]; then
	PIPE_DIR="${PIPE_RESULTS}/${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}-${MSA_AF_TH}_${PIPE_MOD_4_PTC}_pdt-${PD_THRESHOLD}"
elif [[ "${PIPE_MOD_3_MSA_AF}" == "gblocks"* ]]; then
	PIPE_DIR="${PIPE_RESULTS}/${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}_${PIPE_MOD_4_PTC}_pdt-${PD_THRESHOLD}"
else
	PIPE_DIR="${PIPE_RESULTS}/${PIPE_MOD_1_MSA}_${PIPE_MOD_4_PTC}_pdt-${PD_THRESHOLD}"
fi
mkdir -p ${PIPE_DIR}
mkdir -p ${PIPE_DIR}/ppi

# Create the results PPI directory
RESULTSPPI="${PIPE_DIR}/ppi/${PROTEIN_A}_${PROTEIN_B}"
mkdir -p $RESULTSPPI

RECONCILED="${RESULTSPPI}/reconciled"
mkdir -p ${RECONCILED}

# Create the scores directory
SCOREDIR="${PIPE_DIR}/scores"
mkdir -p $SCOREDIR

BLASTRESULTS_A="${ROOT}/results/bdb/${PROTEIN_A}"
BLASTRESULTS_B="${ROOT}/results/bdb/${PROTEIN_B}"
TAXID_COMMON_LIST="${RESULTSPPI}/taxid_common_list"

# Scores file
EE_SCORES=${SCOREDIR}/ee_scores_${PROTEIN_A}_${PROTEIN_B}.txt
DIST_SCORES=${SCOREDIR}/dist_scores_${PROTEIN_A}_${PROTEIN_B}.txt

dos2unix *.sh
##############################################################################################
#	1) BLAST : SEARCHING FOR ORTHOLOGUES
#
# Check if a directory exists : 
# - http://stackoverflow.com/questions/59838/check-if-a-directory-exists-in-a-shell-script
# - http://www.cyberciti.biz/faq/howto-check-if-a-directory-exists-in-a-bash-shellscript/
# Note: does not work with ~/thesis/data but work with cd "~/thesis" and then /data
# Check if orthologues already exists for protein A
echo "> Search orthologues for ${PROTEIN_A}..."
if [ ! -s "${BLASTRESULTS_A}/${PROTEIN_A}_orthologues.fasta" ]; then # "-f" for file "-d" for directory!
	sh mfbo.sh ${PROTEIN_A}
else
    echo "> Orthologues already cached for ${PROTEIN_A}"
fi
# Check if orthologues already exists for protein B
echo "> Search orthologues for ${PROTEIN_B}..."
if [ ! -s "${BLASTRESULTS_B}/${PROTEIN_B}_orthologues.fasta" ]; then
	sh mfbo.sh ${PROTEIN_B}
else
    echo "> Orthologues already cached for ${PROTEIN_B}"
fi

##############################################################################################
#	Check if there are more than 10 orthologues found after BLAST search
#	(minimum required to have 10 species in common)
#
NB_ORHTS_A=$(grep ">" ${BLASTRESULTS_A}/${PROTEIN_A}_orthologues.fasta | wc -l)
NB_ORHTS_B=$(grep ">" ${BLASTRESULTS_B}/${PROTEIN_B}_orthologues.fasta | wc -l)
# How to append and format : http://unix.stackexchange.com/questions/77277/how-to-append-multiple-lines-to-a-file-with-bash
printf '%s\t%s\t%s\t%s\n' ${PROTEIN_A} ${PROTEIN_B} ${NB_ORHTS_A} ${NB_ORHTS_B} >> ${PIPE_DIR}/nb_orthologues.txt
# Compare numbers : http://stackoverflow.com/questions/18668556/comparing-numbers-in-bash
if (( ${NB_ORHTS_A} < 10 )) || (( ${NB_ORHTS_B} < 10 )); then
	echo "> Less than 10 orthologues found for one of the 2 proteins: abort!"
	# Remove all files
	rm -rf ${RESULTSPPI}
	exit
fi

##############################################################################################
#	2) MULTIPLE SEQUENCE ALIGNMENT
#

PROTEIN_A_ALN="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln"
PROTEIN_B_ALN="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln"

# Check if alignment already exists for protein A
echo "> Align orthologues for ${PROTEIN_A}..."
if [ ! -s ${PROTEIN_A_ALN} ]; then
	sh afbo.sh ${PROTEIN_A} ${PIPE_MOD_1_MSA}
else
    echo "> Alignment of the orthologues already cached for ${PROTEIN_A}"
fi
# Check if alignment already exists for protein B
echo "> Align orthologues for ${PROTEIN_B}..."
if [ ! -s ${PROTEIN_B_ALN} ]; then
	sh afbo.sh ${PROTEIN_B} ${PIPE_MOD_1_MSA}
else
    echo "> Alignment of the orthologues already cached for ${PROTEIN_B}"
fi

PROTEIN_A_ALN_PHY="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln.phy"
PROTEIN_A_ALN_MASK="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln.mask"
PROTEIN_B_ALN_PHY="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln.phy"
PROTEIN_B_ALN_MASK="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln.mask"

##############################################################################################
#	3) AUTOMATED MULTIPLE SEQUENCE ALIGNMENT FILTERING (MASKING + TRIMMING)
#
sh aaf.sh ${PIPE_MOD_1_MSA} ${PIPE_MOD_3_MSA_AF} ${PROTEIN_A} ${PROTEIN_B}

# Copy alignment in local directory
if [ "${PIPE_MOD_3_MSA_AF}" == "zorro" ]; then
	# Protein A
	PROTEIN_A_ALN_NAME="${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}-${MSA_AF_TH}.aln"
	PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A_ALN_NAME}"
	PROTEIN_A_ALN_LOC="${RESULTSPPI}/${PROTEIN_A_ALN_NAME}"
	cp ${PROTEIN_A_ALN_TRD} ${PROTEIN_A_ALN_LOC}
	# Protein B
	PROTEIN_B_ALN_NAME="${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}-${MSA_AF_TH}.aln"
	PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B_ALN_NAME}"
	PROTEIN_B_ALN_LOC="${RESULTSPPI}/${PROTEIN_B_ALN_NAME}"
	cp ${PROTEIN_B_ALN_TRD} ${PROTEIN_B_ALN_LOC}
# Check if String contains another : http://stackoverflow.com/questions/229551/string-contains-in-bash
elif [[ "${PIPE_MOD_3_MSA_AF}" == "gblocks"* ]]; then
	# Protein A
	PROTEIN_A_ALN_NAME="${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A_ALN_NAME}"
	PROTEIN_A_ALN_LOC="${RESULTSPPI}/${PROTEIN_A_ALN_NAME}"
	cp ${PROTEIN_A_ALN_TRD} ${PROTEIN_A_ALN_LOC}
	# Protein B
	PROTEIN_B_ALN_NAME="${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}_${PIPE_MOD_3_MSA_AF}.aln"
	PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B_ALN_NAME}"
	PROTEIN_B_ALN_LOC="${RESULTSPPI}/${PROTEIN_B_ALN_NAME}"
	cp ${PROTEIN_B_ALN_TRD} ${PROTEIN_B_ALN_LOC}
else
	# Protein A
	PROTEIN_A_ALN_NAME="${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln"
	PROTEIN_A_ALN_TRD="${BLASTRESULTS_A}/${PROTEIN_A_ALN_NAME}"
	PROTEIN_A_ALN_LOC="${RESULTSPPI}/${PROTEIN_A_ALN_NAME}"
	cp ${PROTEIN_A_ALN_TRD} ${PROTEIN_A_ALN_LOC}
	# Protein B
	PROTEIN_B_ALN_NAME="${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln"
	PROTEIN_B_ALN_TRD="${BLASTRESULTS_B}/${PROTEIN_B_ALN_NAME}"
	PROTEIN_B_ALN_LOC="${RESULTSPPI}/${PROTEIN_B_ALN_NAME}"
	cp ${PROTEIN_B_ALN_TRD} ${PROTEIN_B_ALN_LOC}
fi

##############################################################################################
#	4) MAKE COMPARABLE (COMMON) ALIGNMENTS
#
SPECIES_LIST="data/pd-slist/org_list-${PD_THRESHOLD}.txt"
sh mca.sh ${PROTEIN_A} ${PROTEIN_A_ALN_TRD} ${PROTEIN_B} ${PROTEIN_B_ALN_TRD} ${SPECIES_LIST} ${RESULTSPPI}

##############################################################################################
#	Check if there is equal or more than 10 orthologues in same species otherwise
# 	abort!
#
if [ ! -s ${TAXID_COMMON_LIST} ]; then
	printf '%s\t%s\n' ${PROTEIN_A} ${PROTEIN_B} >> ${PIPE_DIR}/errors.txt
	# Remove all files
	rm -rf ${RESULTSPPI}
	exit
fi

NB_COMMON_SPECIES=$(cat ${TAXID_COMMON_LIST} | wc -l)
# How to append and format : http://unix.stackexchange.com/questions/77277/how-to-append-multiple-lines-to-a-file-with-bash
printf '%s\t%s\t%s\n' ${PROTEIN_A} ${PROTEIN_B} ${NB_COMMON_SPECIES} >> ${PIPE_DIR}/nb_common_species.txt
# Compare numbers : http://stackoverflow.com/questions/18668556/comparing-numbers-in-bash
if (( ${NB_COMMON_SPECIES} < 10 )); then
	echo "> Less than 10 common species: abort!"
	# Remove all files
	rm -rf ${RESULTSPPI}
	exit
fi

NB_BS_SAMPLES=1

BOOT="${RESULTSPPI}/bootstrap-samples"
mkdir -p $BOOT

##############################################################################################
#	5) GENE PHYLOGENETIC TREE CONSTRUCTION
#
# Variables within a for loop statement : http://www.cyberciti.biz/faq/unix-linux-iterate-over-a-variable-range-of-numbers-in-bash/
for (( I=1; I<=$NB_BS_SAMPLES; I++ ))
do
	# (5.1) Construct the gene trees
	echo "> Construct the Gene Phylogenetic Trees for ${PROTEIN_A} and ${PROTEIN_B}"
	sh bbpt.sh ${PROTEIN_A} ${PROTEIN_B} ${PIPE_MOD_4_PTC} ${PTC_NB} ${RESULTSPPI} # {protein 1} {protein 2} {number of bootstrap trees}
	# (5.2) Standardize the gene trees
	# Check if string contains a string : http://stackoverflow.com/questions/229551/string-contains-in-bash
	if [[ ${PIPE_MOD_4_PTC} == *"sb"* ]]; then
		# Correct for amalgamation
	  	sh sdgt.sh ${PROTEIN_A} ${PROTEIN_B} true ${RESULTSPPI}
	else
		sh sdgt.sh ${PROTEIN_A} ${PROTEIN_B} false ${RESULTSPPI}
	fi
	# (5.3) Convert Phylip format into Newick format
	# #ClustalW
	# Option 2 :
	# sh cfpt.sh ${PROTEIN_A} ${PROTEIN_B}
done

##############################################################################################
#	6) SPECIES TREE FROM GREENGENES 
#
# GreenGenes 16S rRNA species tree
# S16rRNA_GENE_TREE_LOCAL="data/bgs/16S_slctd_gg_2011_1.wodoublequotes.tree"
# SPECIES_TREE="$RECONCILED/16S_slctd_gg_2011_1.wodoublequotes.common.tree"

# GreenGenes 16S rRNA alignment + BEAST species tree (Ultrametric problematic when distance calculation)
S16rRNA_GENE_TREE_BEAST="data/bgs/beast.25M.sample.nwk.con.tre"
S16rRNA_GENE_TREE_LOCAL_BEAST="${RESULTSPPI}/beast.25M.sample.nwk.con.tre"
cp ${S16rRNA_GENE_TREE_BEAST} ${S16rRNA_GENE_TREE_LOCAL_BEAST}
SPECIES_TREE_BEAST="$RECONCILED/beast.25M.sample.nwk.con.common.tre"

# GreenGenes 16S rRNA alignment + MrBayes species tree (Not Binary -> problem with ecceTERA)
S16rRNA_GENE_TREE_MRBAYES="data/bgs/mrbayes.2M.sample.nwk.con.tre"
S16rRNA_GENE_TREE_LOCAL_MRBAYES="${RESULTSPPI}/mrbayes.2M.sample.nwk.con.tre"
cp ${S16rRNA_GENE_TREE_MRBAYES} ${S16rRNA_GENE_TREE_LOCAL_MRBAYES}
SPECIES_TREE_MRBAYES="$RECONCILED/mrbayes.2M.sample.nwk.con.common.tre"

# Prune leaves in order to have common leaves
module load R/3.2.1-foss-2014a-x11-tcl
Rscript rlnil.R ${S16rRNA_GENE_TREE_LOCAL_BEAST} $TAXID_COMMON_LIST ${SPECIES_TREE_BEAST}
Rscript rlnil.R ${S16rRNA_GENE_TREE_LOCAL_MRBAYES} $TAXID_COMMON_LIST ${SPECIES_TREE_MRBAYES}
module unload R/3.2.1-foss-2014a-x11-tcl

##############################################################################################
#	7) GENE TREE/SPECIES TREE RECONCILIATION
#
for (( I=1; I<=$NB_BS_SAMPLES; I++ ))
do
	GENE_TREE_A="${RESULTSPPI}/${PROTEIN_A}.sdd.gtrees"
	GENE_TREE_B="${RESULTSPPI}/${PROTEIN_B}.sdd.gtrees"
	# Notung 2.8 (beta)
	# SPECIES_TREE="${RESULTSPPI}/stree.sdd.phy"
	# sh mrsgt_Ng.sh ${PROTEIN_A} ${PROTEIN_B} $SPECIES_TREE $GENE_TREE_A $GENE_TREE_B

	if [[ ${PIPE_MOD_4_PTC} == *"sb"* ]]; then
		# ecceTERA w Amalgamation
		sh mrsgt_eT.sh ${PROTEIN_A} ${PROTEIN_B} $SPECIES_TREE_BEAST $GENE_TREE_A $GENE_TREE_B ${I} true ${RECONCILED}
	else
		# ecceTERA w/o Amalgamation
		sh mrsgt_eT.sh ${PROTEIN_A} ${PROTEIN_B} $SPECIES_TREE_BEAST $GENE_TREE_A $GENE_TREE_B ${I} false ${RECONCILED}
	fi
done

# (8) Background similarity

##############################################################################################
#	9) BUILD THE EE PROFILES
#
# #Notung
# sh ceep2.sh ${PROTEIN_A} ${PROTEIN_B} 
# MEEP_A="${RESULTSPPI}/${PROTEIN_A}_opt_roots.mean.eep"
# MEEP_B="${RESULTSPPI}/${PROTEIN_B}_opt_roots.mean.eep"
# HEADER="FALSE"

echo "############### Build EE Profiles ###############"
# MEE Profile of proteins A and B
for (( I=1; I<=$NB_BS_SAMPLES; I++ ))
do
	# Symmetric median reconciliation gives the best results (see paper Support Measures to Estimate the Reliability...)
	# Select all columns but speciation (S)
	java -jar eep-1.2.jar make "${PROTEIN_A}" "${RECONCILED}/${PROTEIN_A}_${I}.stree" "${RECONCILED}/${PROTEIN_A}_mr${I}_symmetric.txt" "${RECONCILED}/${PROTEIN_A}_${I}" 023456789
	java -jar eep-1.2.jar make "${PROTEIN_B}" "${RECONCILED}/${PROTEIN_B}_${I}.stree" "${RECONCILED}/${PROTEIN_B}_mr${I}_symmetric.txt" "${RECONCILED}/${PROTEIN_B}_${I}" 023456789
done
# MEE Profile of background similarity
#java -jar eep-1.2.jar make "16S_rRNA_gg" "${RECONCILED}/16S_rRNA_gg.stree" "${RECONCILED}/16S_rRNA_gg_symmetric.txt" "${RECONCILED}/16S_rRNA_gg_mp" 23456789
#rm ${RECONCILED}/*_mr*

##############################################################################################
#	10) COMPUTE EE PROFILE SIMILARITY BETWEEN 2 PROTEINS
#
# #Notung
# PROTEIN_A_MPSCC="${RESULTSPPI}/${PROTEIN_A}_mp.all.cc.txt"
# PROTEIN_B_MPSCC="${RESULTSPPI}/${PROTEIN_B}_mp.all.cc.txt"
# paste -s $MEEP_A | awk -f transpose.awk > $PROTEIN_A_MPSCC
# paste -s $MEEP_B | awk -f transpose.awk > $PROTEIN_B_MPSCC

# #ecceTERA
EEP_A="${RECONCILED}/${PROTEIN_A}_1_eep.txt"
EEP_B="${RECONCILED}/${PROTEIN_B}_1_eep.txt"
java -jar eep-1.2.jar compare cos ${PROTEIN_A} ${EEP_A} ${PROTEIN_B} ${EEP_B} ${EE_SCORES} ${IS_PPI} ${NB_COMMON_SPECIES}

##############################################################################################
#	11) COMPUTE DISTANCE PROFILE SIMILARITY BETWEEN 2 PROTEINS
# 
# This is based on the paper "The inference of protein-protein interactions by co-evolutionary analysis is improved by excluding the 
# informatin about the phylogenetic relationships" Sato et al.
java -jar bio-utils-0.1.jar BioPPIPredict "coEvolutionScoreSatoetal" ${PROTEIN_A} ${PROTEIN_B} ${GENE_TREE_A} ${GENE_TREE_B} ${SPECIES_TREE_MRBAYES} ${IS_PPI} ${DIST_SCORES}

##############################################################################################
# 	12) DATA INTEGRATION COMBINE BOTH SCORES
#
#
# Distance file
# Split string delimited by tab : http://stackoverflow.com/questions/6654849/how-to-split-a-string-in-bash-delimited-by-tab
DIST_SCORE=$(awk 'END {print $3}' ${DIST_SCORES})
DIST_SCORE_CORR=$(awk 'END {print $4}' ${DIST_SCORES})
IS_PPI=$(awk 'END {print $5}' ${DIST_SCORES})

# EE Profile file
EE_SCORE=$(awk 'END {print $3}' ${EE_SCORES})
# Floating point math: http://stackoverflow.com/questions/35634255/invalid-arithmetic-operator-doing-floating-point-math-in-bash
COMBINED_SCORE=$(echo "1-(1-${DIST_SCORE})*(1-${EE_SCORE})" | bc)
COMBINED_CORR_SCORE=$(echo "1-(1-${DIST_SCORE_CORR})*(1-${EE_SCORE})" | bc)

echo "Protein A: ${PROTEIN_A}"
echo "Protein A: ${PROTEIN_B}"
echo "Distance Score (~mirrortree): ${DIST_SCORE}"
echo "Distance Score Correction (~tol-mirrortree): ${DIST_SCORE_CORR}"
echo "Evolutionary Event Score: ${EE_SCORE}"
echo "Combined Score: ${COMBINED_SCORE}"
echo "Combined Corrected Score: ${COMBINED_CORR_SCORE}"
echo "Is PPI?: ${IS_PPI}"

printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' ${PROTEIN_A} ${PROTEIN_B} ${DIST_SCORE} ${DIST_SCORE_CORR} ${EE_SCORE} ${COMBINED_SCORE} ${COMBINED_CORR_SCORE} ${IS_PPI} >> "${PIPE_DIR}/scores.txt"

echo "> Remove all files in results/ppi/${PROTEIN_A}_${PROTEIN_B} to save space"
rm -rf ${RESULTSPPI}