#!/bin/bash
# THIS FILE NEED TO BE CONVERTED : dos2unix!
PATH=$PATH:~/thesis/blast/ncbi-blast-2.3.0+/bin/
echo "############### Download protein FASTA file ###############"

ROOT="${VSC_DATA}/thesis"

DATASEQ="${ROOT}/data/seq"
mkdir -p $DATASEQ

PROTEIN=$1 # First argument is the protein to blast

download() {
	PROTEIN=$1
	wget "http://www.uniprot.org/uniprot/$PROTEIN.fasta" -O "${DATASEQ}/$PROTEIN.fasta"
}
# Download the protein and move to the data directory
if [ ! -s "${DATASEQ}/$PROTEIN.fasta" ]; then
	COUNTER=0
	while [ ! -s "${DATASEQ}/$PROTEIN.fasta" ] && [ $COUNTER -lt 3 ];
	do
		download $PROTEIN
		COUNTER=$((COUNTER+1))
		# Wait until next download if it failed
		sleep 1
	done
	if [ ! -s "${DATASEQ}/$PROTEIN.fasta" ]; then
		printf '%s\n' ${PROTEIN} >> data/err_download.txt
	fi
else
    echo "Protein sequence already downloaded for ${PROTEIN_A}"
fi

echo "############### Make FASTA file of BLASTED Orthologues + Mapping ###############"
RESULTS="${ROOT}/results"
mkdir $RESULTS

BLASTRESULTS="${RESULTS}/bdb" # Blast results containing orthologues and alignments
# Create directory storing all BLASTed sequences
mkdir -p "$BLASTRESULTS/$PROTEIN/seq"
ALLBLASTOUT="$BLASTRESULTS/${PROTEIN}/${PROTEIN}_orthologues.fasta"
rm $ALLBLASTOUT
# Create empty file : http://stackoverflow.com/questions/9381463/how-to-create-a-file-in-linux-from-terminal-window
touch $ALLBLASTOUT
MAPPING="$BLASTRESULTS/$PROTEIN/map_tid_pid" # Mapping Taxa id and Protein 
rm $MAPPING
touch $MAPPING

# BLAST Protein Against a Taxid
blastpat() {
	PROTEIN=$1 # First argument is the protein to blast
	TAXID=$2 # Second argument is the taxid of the genome species to which the protein should be blasted against
	DB="blast/db/"$TAXID
	# Against all selected genomes in blast/db/*.*
	echo "Finding orthologues in $TAXID"
	QUERY="${DATASEQ}/$PROTEIN.fasta"
	echo $QUERY
	BLASTOUT="$BLASTRESULTS/$PROTEIN/seq/blast_${TAXID}"
	# Parallelize BLAST searches : http://www.ncbi.nlm.nih.gov/books/NBK279675/
	# How many threads : http://voorloopnul.com/blog/how-to-correctly-speed-up-blast-using-num_threads/
	blastp -query $QUERY -db $DB -out $BLASTOUT -outfmt 6 -evalue 10e-5 -num_threads 4
	NBRESULTS=$(cat $BLASTOUT | wc -l)
	echo "$NBRESULTS orthologues found with minimum E-value 10e-5!"
	# Check whether there are any results
	# Comparison : http://stackoverflow.com/questions/22083651/how-can-i-check-if-and-greater-than-condition-in-linux
	if [[ $NBRESULTS -gt 0 ]];
	then
		# Get the first hit and save the ID of the sequence
		SEQID=$(head -n 1 $BLASTOUT | awk -F "\t" '{print $2}')
		echo "First hit: $SEQID"
		# Retrieve the sequence of the hit and add 
		# Append text to file : http://stackoverflow.com/questions/17701989/how-do-i-append-text-to-a-file
		blastdbcmd -dbtype "prot" -db $DB -entry $SEQID | cat >> $ALLBLASTOUT
		ORGNAME=$(sh cbysmcn.sh bacteria_as_comref_genomes_wo_ecoli_lnr $TAXID organism_name)
		echo $ORGNAME
		# Append the mapping
		echo "$TAXID;$SEQID;$ORGNAME" >> $MAPPING
	else
		echo "No Hit!"
	fi
}

# BLAST input Protein against all selected bacteria genomes in taxid_list
while read -r LINE; do
    blastpat $PROTEIN $LINE
done < "blast/db/taxid_list"