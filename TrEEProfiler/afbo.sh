#!/bin/bash
# THIS FILE NEED TO BE CONVERTED : dos2unix!
# Align FASTA file of BLASTed Orthologues using muscle | mafft
# "bo" = Bacteria Orthologues
PROTEIN=$1
PROGRAM=$2

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS="${ROOT}/results/bdb/${PROTEIN}"
# Using Mafft with multi-threading : http://mafft.cbrc.jp/alignment/software/multithreading.html
echo "############### Align FASTA file of BLASTed Orthologues... ###############"
if [ "$PROGRAM" == "muscle" ]; then
	echo "> using MUSCLE..."
	muscle/muscle3.8.31_i86linux64 -in "$BLASTRESULTS/${PROTEIN}_orthologues.fasta" -out "$BLASTRESULTS/${PROTEIN}_bo_${PROGRAM}.aln"
elif [ "$PROGRAM" == "mafft-linsi" ]; then
	echo "> using MAFFT L-INS-I..."
	# Probably the most accurate
	# unusual characters (e.g., U as selenocysteine in protein sequence): http://mafft.cbrc.jp/alignment/software/anysymbol.html
	# Mafft cannot handle special cases of amino acids : C,U,G,T
	PATH=$PATH:~/thesis/mafft/mafft-7.273-with-extensions/bin/
	mafft --thread -1 --localpair --maxiterate 1000 --anysymbol "$BLASTRESULTS/${PROTEIN}_orthologues.fasta" > "$BLASTRESULTS/${PROTEIN}_bo_${PROGRAM}.aln"
elif [ "$PROGRAM" == "mafft-einsi" ]; then
	echo "> using MAFFT E-INS-I..."
	# Probably the most accurate
	PATH=$PATH:~/thesis/mafft/mafft-7.273-with-extensions/bin/
	mafft --thread -1 --genafpair --maxiterate 1000 --anysymbol "$BLASTRESULTS/${PROTEIN}_orthologues.fasta" > "$BLASTRESULTS/${PROTEIN}_bo_${PROGRAM}.aln"
elif [ "$PROGRAM" == "mafft-fftnsi" ]; then
	echo "> using MAFFT E-INS-I..."
	# Probably the most accurate
	PATH=$PATH:~/thesis/mafft/mafft-7.273-with-extensions/bin/
	mafft --thread -1 --maxiterate 1000 --anysymbol "$BLASTRESULTS/${PROTEIN}_orthologues.fasta" > "$BLASTRESULTS/${PROTEIN}_bo_${PROGRAM}.aln"
fi
