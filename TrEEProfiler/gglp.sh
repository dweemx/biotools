#!/bin/bash
# Create new assembly summary of complete reference bacteria genome without E. coli and with local path of genomes
# Take only the taking only one representative from each clade at the first level of the National Center for Biotechnology
# Information taxonomic tree (the one with the largest genome
echo "########### Build Assembly Summary Complete Reference Bacteria Genome w/ E.coli + Local Path to Genomes  #####################"
# Extract FULLY SEQUENCED AND REFERENCE GENOME from NCBI bacteria assembly_summary.txt
awk -F "\t" '$12=="Complete Genome" && $5=="reference genome" && $7!="562"{print $0}' bacteria_assembly_summary.txt > tmp0
# assembly_accession=1, asm_name=16
awk -F "\t" '{print $1}' tmp0 > assembly_accession
awk -F "\t" '{print $16}' tmp0 > asm_name
# http://www.techrepublic.com/article/lesser-known-linux-commands-join-paste-and-sort/
paste -d "_" assembly_accession asm_name | awk '{print $1,$2}' > tmp1
# Remove leading and trailing spaces : http://www.theunixschool.com/2012/12/howto-remove-leading-trailing-spaces.html
awk '$1=$1' tmp1 > tmp2
# http://stackoverflow.com/questions/27028928/awk-concatenate-two-string-variable-and-assign-to-a-third
cat tmp2 | awk '{{b="/cygdrive/h/thesis_data/blast/seq/"$0"_protein.faa"; print b}}' > tmp3
paste -d "\t" tmp0 tmp3 > bacteria_as_comref_genomes_wo_ecoli
rm assembly_accession asm_name tmp0 tmp1 tmp2 tmp3