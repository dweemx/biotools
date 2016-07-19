#!/bin/bash
# Build and bootstrap a phylogenetic tree
echo ""
echo "############### Build and bootstrap a phylogenetic tree ###############"
PROTEIN_A=$1
PROTEIN_B=$2
PROGRAM=$3
NB_BOOT=$4
RESULTSPPI=$5

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS="${ROOT}/results/bdb/"
RESULTS_A="${BLASTRESULTS}${PROTEIN_A}"
RESULTS_B="${BLASTRESULTS}${PROTEIN_B}"
BOOTSAMPLES="${RESULTSPPI}/bootstrap-samples"

# Compare strings : http://stackoverflow.com/questions/2237080/how-to-compare-strings-in-bash-script
if [ "$PROGRAM" == "clustalw" ]; then
  	# ClustalW2 - Parsimonious method
  	echo "> Phylogenetic Tree Construction using ClustalW"
	PATH=$PATH:~/thesis/clustalw/clustalw-2.1-linux-x86_64-libcppstatic/
	clustalw2 -bootstrap=$NB_BOOT -infile="${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" -outputtree=phylip -clustering=NJ -bootlabels=node #-seed=999
	clustalw2 -bootstrap=$NB_BOOT -infile="${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" -outputtree=phylip -clustering=NJ -bootlabels=node #-seed=999
	mv "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.phb" "${RESULTSPPI}/${PROTEIN_A}.gtrees"
	mv "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.phb" "${RESULTSPPI}/${PROTEIN_B}.gtrees"
elif [ "$PROGRAM" == "sb-clustalw" ]; then
	PATH=$PATH:~/thesis/clustalw/clustalw-2.1-linux-x86_64-libcppstatic/
	PATH=$PATH:~/thesis/phylip/phylip-3.696/exe
	rm ${BOOTSAMPLES}/*

	# Protein A
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy"
	python seqboot.py "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy" $NB_BOOT
	mv ./outfile "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.maln.phy"
	LENGTH=$(cat ${RESULTSPPI}/${PROTEIN_A}_common_orthologues.maln.phy | wc -l)
	NB_LINES_ALN=$(($LENGTH / $NB_BOOT))
	# http://stackoverflow.com/questions/4701114/how-do-i-specify-where-to-save-files-that-result-from-running-the-unix-split-com
	split -d -l $NB_LINES_ALN ${RESULTSPPI}/${PROTEIN_A}_common_orthologues.maln.phy ${BOOTSAMPLES}/${PROTEIN_A}_
	for F in ${BOOTSAMPLES}/${PROTEIN_A}_*
	do
		java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter $F phylip-il fasta "${F}.fasta"
		clustalw2 -bootstrap=1 -infile="${F}.fasta" -outputtree=phylip -clustering=NJ -bootlabels=node #-seed=999
		sh cp2n.sh "$F.phb" "$F.nwk"
	done
	cat ${BOOTSAMPLES}/${PROTEIN_A}_*.nwk > ${RESULTSPPI}/${PROTEIN_A}.gtrees
	#rm ${BOOTSAMPLES}/${PROTEIN_A}*

	# Protein B
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy"
	python seqboot.py "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy" $NB_BOOT
	mv ./outfile "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.maln.phy"
	LENGTH=$(cat ${RESULTSPPI}/${PROTEIN_B}_common_orthologues.maln.phy | wc -l)
	NB_LINES_ALN=$(($LENGTH / $NB_BOOT))
	# http://stackoverflow.com/questions/4701114/how-do-i-specify-where-to-save-files-that-result-from-running-the-unix-split-com
	split -d -l $NB_LINES_ALN ${RESULTSPPI}/${PROTEIN_B}_common_orthologues.maln.phy ${BOOTSAMPLES}/${PROTEIN_B}_
	for F in ${BOOTSAMPLES}/${PROTEIN_B}_*
	do
		# Convert PHYLIP inter-leaved to Fasta
		java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter $F phylip-il fasta "${F}.fasta"
		clustalw2 -bootstrap=1 -infile="${F}.fasta" -outputtree=phylip -clustering=NJ -bootlabels=node #-seed=999
		sh cp2n.sh "$F.phb" "$F.nwk"
	done
	cat ${BOOTSAMPLES}/${PROTEIN_B}_*.nwk > ${RESULTSPPI}/${PROTEIN_B}.gtrees
	rm ${BOOTSAMPLES}/*
# Else if statement : http://www.thegeekstuff.com/2010/06/bash-if-statement-examples/
# FastTree HELP : http://manpages.ubuntu.com/manpages/raring/man1/fasttree.1.html
elif [ "$PROGRAM" == "fasttree" ]; then
	# FastTree - Maximum Likelihood method
	echo "> Phylogenetic Tree Construction using FastTree"
	chmod 0777 ~/thesis/fasttree/FastTree
	fasttree/FastTree -boot $NB_BOOT -seed 617 "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" > "${RESULTSPPI}/${PROTEIN_A}.gtrees"
	fasttree/FastTree -boot $NB_BOOT -seed 617 "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" > "${RESULTSPPI}/${PROTEIN_B}.gtrees"
elif [ "$PROGRAM" == "fasttree-mp" ]; then
	# FastTree - Maximum Likelihood method
	echo "> Phylogenetic Tree Construction using FastTree (OpenMP version)"
	chmod 0777 ~/thesis/fasttree/FastTreeMP
	INIT_NUM_THREADS=${OMP_NUM_THREADS}
	export OMP_NUM_THREADS=5
	fasttree/FastTreeMP -boot $NB_BOOT -seed 617 "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" > "${RESULTSPPI}/${PROTEIN_A}.gtrees"
	fasttree/FastTreeMP -boot $NB_BOOT -seed 617 "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" > "${RESULTSPPI}/${PROTEIN_B}.gtrees"
	# Get number of threads : http://stackoverflow.com/questions/6481005/how-to-obtain-the-number-of-cpus-cores-in-linux-from-the-command-line
	export OMP_NUM_THREADS=$(grep -c ^processor /proc/cpuinfo)
# Perform first seqboot on the alignment and then use FastTree on the multiple alignments file
elif [ "$PROGRAM" == "sb-fasttree" ]; then
	PATH=$PATH:~/thesis/phylip/phylip-3.696/exe
	dos2unix seqboot.py
	# Protein A
	# Convert Fasta to PHYLIP alignment into a file named "infile"
	# SeqBoot need a input file called "infile"
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy"
	# Use the python wrapper to run SeqBoot
	# Output is written in outfile
	# Remove all .1 because lead when running FastTree then ending leaf will end by . instead of .1 and then trigger ERROR: Gene tree 1 has no valid leaves.
	#sed -i -e 's/\.[0-9]//g' "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy"
	python seqboot.py "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy" $NB_BOOT
	mv ./outfile "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.maln.phy"
	#-seed 617
	fasttree/FastTree -boot $NB_BOOT -n $NB_BOOT "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.maln.phy" > "${RESULTSPPI}/${PROTEIN_A}.gtrees"
	#rm ${RESULTSPPI}/infile ${RESULTSPPI}/outfile
	# Protein B
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy"
	#sed -i -e 's/\.[0-9]//g' "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy"
	python seqboot.py "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy" $NB_BOOT
	mv ./outfile "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.maln.phy"
	fasttree/FastTree -boot $NB_BOOT -n $NB_BOOT "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.maln.phy" > "${RESULTSPPI}/${PROTEIN_B}.gtrees"
	#rm ${RESULTSPPI}/infile ${RESULTSPPI}/outfile
elif [ "$PROGRAM" == "phyml" ]; then
	echo "Phylogenetic Tree Construction using PhyML"
	# Input file format : PHYLIP -> need to convert Fasta alignment to PHYLIP alignment
	echo "Converting Fasta alignment to PHYLIP alignment..."
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy"
	java -jar bio-utils-0.1.jar BioPhyloAlignmentFormatConverter "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln" fasta phylip-seq "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy"
	# Construct the gene tree
	# Check if file exists: http://www.unix.com/shell-programming-and-scripting/174496-how-check-if-file-exists-directory.html
	if [ ! -s "${RESULTSPPI}/${PROTEIN_A}.gtrees" ]; then
		phyml/PhyML-3.1/PhyML-3.1_linux64 --r_seed 617 -d aa -b $NB_BOOT -i "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy"
		# Rename the files generated
		mv "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.aln.phy_phyml_boot_trees.txt" "${RESULTSPPI}/${PROTEIN_A}.gtrees"
	fi
	if [ ! -s "${RESULTSPPI}/${PROTEIN_B}.gtrees" ]; then
		phyml/PhyML-3.1/PhyML-3.1_linux64 --r_seed 617 -d aa -b $NB_BOOT -i "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy"
		# Will generate file ${PROTEIN_X}_common_orthologues.aln.phy_phyml_boot_trees.txt
		mv "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.aln.phy_phyml_boot_trees.txt" "${RESULTSPPI}/${PROTEIN_B}.gtrees"
	fi
fi