#!/bin/bash

PATH=$PATH:~/thesis/reconciliation/ecceTERA/bin/
# export OMP_NUM_THREADS=5

############### Stochastic Pipe for Computing the Cosine Similarity between Evolutionary Event Profiles of 2 Proteins ###############
# Using:
# - Type : Stochastic
# - Multiple Sequence Alignment: MAFFT L-INS-I | MAFFT E-INS-I | MUSCLE
# - MSA Automated Filtering : ZORRO | Gblocks (Stringent and Relaxed conditions)
# - Phylogenetic Tree Construction: FasTree | ClustalW
# - Phylogenetic Reconciliation: ecceTERA
# - Species Tree: NCBI Taxonomy Species Tree
# - Polytomy Resolver: multi2di (APE R package)
# - Gene Tree Correction: none
# - EE Profile Constructor: cat2 (wo S)
# - Background Correction: no
#################################################################################################################"

# Set up the default values
PIPE_MOD_1_MSA="" # Multiple Sequence Alignment (MSA)""
PIPE_MOD_2_MSA_REF="" # MSA Refinement
PIPE_MOD_3_MSA_AF="" # MSA Automated Filtering
MSA_AF_TH=0.0 # Threshold of MSA Automated Filtering
PIPE_MOD_4_PTC="" # Phylogenetic Tree Construction
PTC_NB=100 # Number of bootstrap for Phylogenetic Tree Construction
PD_THRESHOLD=0.0 # Threshold for the Phylogenetic Diversity
NB_SAMPLES=1 # Number of samples for the Reconciliation Sampling
PROTEIN_A="" # Protein A
PROTEIN_B="" # Protein B
IS_PPI="" # Does protein A and B interact?

help() {
	echo "Usage: sh spccseep.sh [OPTION]... "
	echo ""
	echo "Stochastic pipe for computing the cosine similarity between evolutionary event profiles of 2 proteins"
	echo ""
    echo "-a|--alignment:		Multiple sequence alignment method (mafft,muscle)"
    echo "-r|--refinement:	Not yet implemented"
    echo "-f|--autofiltering:	Multiple sequence alignment automated filtering method (zorro,gblocks-s,glocks-r)"
    echo "-h|--afthreshold:	Autofiltering threshold value (only if -f zorro)"
    echo "-t|--tree:		Phylogenetic tree construction method"
    echo "-b|--bootstrap:		Number of bootstraps"
    echo "-p|--pdthreshold:	Phylogenetic diversity threshold (Number of substitutions/site)"
    echo "-r|--samples:		Number samples for reconciliation sampling"
    echo "-1|--protein1:		A first protein"
    echo "-2|--protein2:		A second protein"
    echo "-i|--interact:	Does they interact? (true,false)"
    echo "-d|--save:	Save results on disk? (true,false)"
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
	    -s|--samples)
	    NB_SAMPLES="$2"
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
	   	-d|--save)
	    SAVE="$2"
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
# PD_THRESHOLD=$7 # Threshold for the Phylogenetic Diversity
# NB_SAMPLES=$8 # Number of samples during the Reconciliation Sampling
# PROTEIN_A=$9
# PROTEIN_B=${10}
# IS_PPI=${11}

ROOT="${VSC_DATA}/thesis"

# Create the pipe directory 
# Create directory only if not exists : http://stackoverflow.com/questions/793858/how-to-mkdir-only-if-a-dir-does-not-already-exist
PIPE_RESULTS="${ROOT}/results/S-pipe"
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

# Create the scores directory
SCOREDIR="${PIPE_DIR}/scores"
mkdir -p $SCOREDIR

BLASTRESULTS_A="${ROOT}/results/bdb/${PROTEIN_A}"
BLASTRESULTS_B="${ROOT}/results/bdb/${PROTEIN_B}"
TAXID_COMMON_LIST="${RESULTSPPI}/taxid_common_list"

# Scores file
EE_SCORES=${SCOREDIR}/ee_scores_${PROTEIN_A}_${PROTEIN_B}.txt
DIST_SCORES=${SCOREDIR}/dist_scores_${PROTEIN_A}_${PROTEIN_B}.txt

# Create directory only if not exists : http://stackoverflow.com/questions/793858/how-to-mkdir-only-if-a-dir-does-not-already-exist
# Gene trees directory
# Check if variable true : http://stackoverflow.com/questions/2953646/how-to-declare-and-use-boolean-variables-in-shell-script
if [ "$SAVE" = true ]; then
	GENETREESDIR="${PIPE_DIR}/gtrees/${PROTEIN_A}_${PROTEIN_B}"
	mkdir -p $GENETREESDIR

	# Taxid common list
	TCLISTDIR="${PIPE_DIR}/tclist/${PROTEIN_A}_${PROTEIN_B}"
	mkdir -p $TCLISTDIR
fi

reconciliation_sampling() {

	GENE_TREE_A=$1
	GENE_TREE_B=$2
	SAMPLE_RUN=$3
	NB_COMMON_SPECIES=$4

	COUNTER=0
	# Redo the computation 3 times if it has failed
	while [ $COUNTER -lt 3 ]; do 
		# Create the reconciliation sample run directory
		RECONCILED="${RESULTSPPI}/rs-run/${SAMPLE_RUN}"
		mkdir -p $RECONCILED

		echo "Run ${SAMPLE_RUN} > Randomly resolve the species tree..."
		SPECIES_TREE="${RECONCILED}/stree.sdd.res.phy"
		# Using bio-utils
		java -jar bio-utils-0.1.jar BioPhyloRandomTreeResolver "${RESULTSPPI}/stree.sdd.phy" ${SPECIES_TREE}
		# Using APE R Package
		#Rscript restree.R "${RESULTSPPI}/stree.sdd.phy" ${SPECIES_TREE}

		##############################################################################################
		#	7) GENE TREE/SPECIES TREE RECONCILIATION
		#
		if [[ ${PIPE_MOD_4_PTC} == *"sb"* ]]; then
			# ecceTERA w Amalgamation
			sh mrsgt_eT.sh ${PROTEIN_A} ${PROTEIN_B} $SPECIES_TREE $GENE_TREE_A $GENE_TREE_B ${SAMPLE_RUN} true ${RECONCILED}
		else
			# ecceTERA w/o Amalgamation
			sh mrsgt_eT.sh ${PROTEIN_A} ${PROTEIN_B} $SPECIES_TREE $GENE_TREE_A $GENE_TREE_B ${SAMPLE_RUN} false ${RECONCILED}
		fi
		##############################################################################################
		#	9) BUILD THE EE PROFILE
		#
		echo "Run ${SAMPLE_RUN} > Build the EE profiles..."
		# MEE Profile of proteins A and B
		# Symmetric median reconciliation gives the best results (see paper Support Measures to Estimate the Reliability...)
		# Select all columns but speciation (S)
		java -jar eep-1.2.jar make "${PROTEIN_A}" "${RECONCILED}/${PROTEIN_A}_${SAMPLE_RUN}.stree" "${RECONCILED}/${PROTEIN_A}_mr${SAMPLE_RUN}_symmetric.txt" "${RECONCILED}/${PROTEIN_A}_${SAMPLE_RUN}" 023456789
		java -jar eep-1.2.jar make "${PROTEIN_B}" "${RECONCILED}/${PROTEIN_B}_${SAMPLE_RUN}.stree" "${RECONCILED}/${PROTEIN_B}_mr${SAMPLE_RUN}_symmetric.txt" "${RECONCILED}/${PROTEIN_B}_${SAMPLE_RUN}" 023456789

		##############################################################################################
		#	10) COMPUTE EE PROFILE SIMILARITY BETWEEN 2 PROTEINS
		#
		# #ecceTERA
		echo "Run ${SAMPLE_RUN} > Compute the EE profile similarity..."
		# Using Java eep-1.2
		EEP_A="${RECONCILED}/${PROTEIN_A}_${SAMPLE_RUN}_eep.txt"
		EEP_B="${RECONCILED}/${PROTEIN_B}_${SAMPLE_RUN}_eep.txt"
		java -jar eep-1.2.jar compare cos ${PROTEIN_A} ${EEP_A} ${PROTEIN_B} ${EEP_B} ${EE_SCORES} ${IS_PPI} ${NB_COMMON_SPECIES}
		# Check if file exists : http://stackoverflow.com/questions/638975/how-do-i-tell-if-a-regular-file-does-not-exist-in-bash
		# Check if a file not empty : http://www.cyberciti.biz/faq/linux-unix-script-check-if-file-empty-or-not/
		# And operator : http://stackoverflow.com/questions/13408493/bash-an-and-operator-for-if-statment
		if [ -s "${EEP_A}" ] && [ -s "${EEP_B}" ]; then
			# Check whether the computation has succeeded
			echo "Run ${SAMPLE_RUN} > Reconciliation sample run has succeeded..."
			# If the first reconciliation sample run has been completed save the gene trees and the taxid common list
			if [ ${SAMPLE_RUN} -eq 0 ] && [ "$SAVE" = true ]; then
				echo "Run ${SAMPLE_RUN} > Save gene trees and taxid common list"
				if [ ! -s ${GENETREESDIR}/${PROTEIN_A}.sdd.gtrees ] && [ ! -s ${GENETREESDIR}/${PROTEIN_B}.sdd.gtrees ]; then
					cp ${RESULTSPPI}/*.sdd.gtrees $GENETREESDIR
				fi
				if [ ! -s ${TCLISTDIR}/taxid_common_list ]; then
					cp ${RESULTSPPI}/taxid_common_list $TCLISTDIR/${PROTEIN_A}_${PROTEIN_B}.tclist
				fi
			fi
			echo "Run ${SAMPLE_RUN} > Remove files in ${RECONCILED} to save space ########"
			rm -rf ${RECONCILED}
			# Increment variable : http://askubuntu.com/questions/385528/how-to-increment-a-variable-in-bash
			# Break the loop
			break
		else 
			echo "Run ${SAMPLE_RUN} > Reconciliation sample run has failed... redo"
			# Remove all files and redo reconciliation sample run
			rm -rf ${RECONCILED}
			echo ""
			# How to increment variable : http://askubuntu.com/questions/385528/how-to-increment-a-variable-in-bash
			COUNTER=$((COUNTER+1))
		fi
	done
}

main() { # BEGIN MAIN BASH FUNCTION
	
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
	# Check if alignment already exists for protein A
	PROTEIN_A_ALN="${BLASTRESULTS_A}/${PROTEIN_A}_bo_${PIPE_MOD_1_MSA}.aln"
	PROTEIN_B_ALN="${BLASTRESULTS_B}/${PROTEIN_B}_bo_${PIPE_MOD_1_MSA}.aln"

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
	#"data/bgs/bacteria_as_comref_genomes_wo_ecoli_lnr_gg_2011_taxid.txt"
	SPECIES_LIST="${VSC_HOME}/thesis/data/pd-slist/org_list-${PD_THRESHOLD}.txt"
	sh mca.sh ${PROTEIN_A} ${PROTEIN_A_ALN_LOC} ${PROTEIN_B} ${PROTEIN_B_ALN_LOC} ${SPECIES_LIST} ${RESULTSPPI}

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

	##############################################################################################
	#	5) GENE PHYLOGENETIC TREE CONSTRUCTION AND STANDARDIZATION
	#
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

	##############################################################################################
	#	6) DOWNLOAD, STANDARDIZE SPECIES TREE FROM NCBI TAXONOMY
	#
	# (6.1) Download the species tree
	# NCBI Taxonomy Species Tree (Multifurcating)
	# Don't get the species tree via download on NCBI server when using the script
	# in parallel version (bm_satoetal_TP_pv.sh) because server will refuse the connection
	# and sometimes will not get the tree
	#sh gst.sh ${PROTEIN_A} ${PROTEIN_B} ${RESULTSPPI}

	# PhyloT Biobyte (Resolved binary species tree: without polytomies)
	# sh gst_pb.sh ${PROTEIN_A} ${PROTEIN_B} ${RESULTSPPI}
	# ecceTERA error : ERROR: Species tree is not binary. (while it is!!!)

	# (6.2) Standardize and resolve the species tree (LIMITING STEP)
	# Instead of downloading it each time copy it because sometimes problems
	# Generate a Seg fault error when resolving the tree because those trees
	# were not intersected with the custom species list
	cp "data/pd-stree/stree-${PD_THRESHOLD}.txt.phy" "${RESULTSPPI}/stree.phy"
	sh sdst.sh ${PROTEIN_A} ${PROTEIN_B} ${RESULTSPPI} "${RESULTSPPI}/stree.sdd.phy.tmp"
	# Remove the leaves that are not in the list
	module load R/3.2.1-foss-2014a-x11-tcl
	echo "> Remove All Leaves Not Part of the Taxid Common List"
	Rscript rlnil.R "${RESULTSPPI}/stree.sdd.phy.tmp" ${TAXID_COMMON_LIST} "${RESULTSPPI}/stree.sdd.phy"
	module unload R/3.2.1-foss-2014a-x11-tcl 

	# (6.3) Resolve the species tree (LIMITING STEP)
	# NOTE: Take a some time to run (several seconds)
	# #ecceTERA
	# #Notung: BAD, slows down the speed of computation

	# (6.4) Get a non-binary species tree in order to calculate the distance score
	# GreenGenes 16S rRNA alignment + MrBayes species tree (Not Binary -> problem with ecceTERA)
	S16rRNA_GENE_TREE_MRBAYES="${VSC_HOME}/thesis/data/bgs/mrbayes.2M.sample.nwk.con.tre"
	S16rRNA_GENE_TREE_LOCAL_MRBAYES="${RESULTSPPI}/mrbayes.2M.sample.nwk.con.tre"
	cp ${S16rRNA_GENE_TREE_MRBAYES} ${S16rRNA_GENE_TREE_LOCAL_MRBAYES}
	SPECIES_TREE_MRBAYES="$RESULTSPPI/mrbayes.2M.sample.nwk.con.common.tre"

	# Prune leaves in order to have common leaves
	module load R/3.2.1-foss-2014a-x11-tcl
	Rscript rlnil.R ${S16rRNA_GENE_TREE_LOCAL_MRBAYES} $TAXID_COMMON_LIST ${SPECIES_TREE_MRBAYES}
	module unload R/3.2.1-foss-2014a-x11-tcl

	GENE_TREE_A="${RESULTSPPI}/${PROTEIN_A}.sdd.gtrees"
	GENE_TREE_B="${RESULTSPPI}/${PROTEIN_B}.sdd.gtrees"

	##############################################################################################
	#	7) RECONCILIATION SAMPLING
	#
	echo "> Reconciliation Sampling ###############"
	# Run the reconciliation sampling in parallel
	for ((I=1;I<=${NB_SAMPLES};I++)); do
		reconciliation_sampling $GENE_TREE_A $GENE_TREE_B ${I} ${NB_COMMON_SPECIES} &
	done
	# Wait until all jobs have finished : http://stackoverflow.com/questions/9258387/bash-ampersand-operator
	wait

	echo "> Compute Distance Profile Similarity Score ###############"
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

	echo "Protein A: ${PROTEIN_A}"
	echo "Protein B: ${PROTEIN_B}"
	echo "Distance Score (~mirrortree): ${DIST_SCORE}"
	echo "Distance Score Correction (~tol-mirrortree): ${DIST_SCORE_CORR}"
	echo "Is PPI?: ${IS_PPI}"

	echo "> Reconciliation sampling finished"
	# Remove all files in ppi directory
	rm -rf ${RESULTSPPI}
} # END MAIN BASH FUNCTION

# Execute main program
# Calculate execution time of program : http://unix.stackexchange.com/questions/12068/how-to-measure-time-of-program-execution-and-store-that-inside-a-variable
START=$(date +%s.%N)
main
END=$(date +%s.%N)
DIFF=$(echo "$END - $START" | bc)
echo "Elapsed time : $DIFF seconds"