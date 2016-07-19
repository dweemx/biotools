#!/bin/bash

#######################################################
#
# Parallel Version of bm_satoetal_TP.sh
#
#######################################################

dos2unix *.sh

ROOT="${VSC_DATA}/thesis"

DATASETFILE="data/Ecoli_DIP_20160114CR_100%_Rand_Processed.txt"

ID=$1
NB_R_SAMPLES=25

# 1) BLAST -> MUSCLE -> ZORRO-0.1 -> ClustalW -> ecceTERA
# 2) BLAST -> MUSCLE -> ZORRO-0.5 -> ClustalW -> ecceTERA
# 3) BLAST -> MUSCLE -> ZORRO-0.8 -> ClustalW -> ecceTERA
# 4) BLAST -> MAFFT -> ZORRO-0.1 -> ClustalW -> ecceTERA
# 5) BLAST -> MAFFT -> ZORRO-0.5 -> ClustalW -> ecceTERA
# 6) BLAST -> MAFFT -> ZORRO-0.8 -> ClustalW -> ecceTERA
# 7) BLAST -> MUSCLE -> ZORRO-0.1 -> FastTree -> ecceTERA
# 8) BLAST -> MUSCLE -> ZORRO-0.5 -> FastTree -> ecceTERA
# 9) BLAST -> MUSCLE -> ZORRO-0.8 -> FastTree -> ecceTERA
# 10) BLAST -> MAFFT -> ZORRO-0.1 -> FastTree -> ecceTERA
# 11) BLAST -> MAFFT -> ZORRO-0.5 -> FastTree -> ecceTERA
# 12) BLAST -> MAFFT -> ZORRO-0.8 -> FastTree -> ecceTERA
# 13) BLAST -> MUSCLE -> ClustalW -> ecceTERA
# 14) BLAST -> MUSCLE -> FastTree -> ecceTERA
# 15) BLAST -> MAFFT -> ClustalW -> ecceTERA
# 16) BLAST -> MAFFT -> FastTree -> ecceTERA
# 17) BLAST -> MUSCLE -> Gblocks (Stringent) -> ClustalW -> ecceTERA
# 18) BLAST -> FastTree -> Gblocks (Relaxed) -> Fasttree -> ecceTERA


spccseep() {
	J=$1
	T=$2
	# Get nth line of file : http://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
	LINE=$(sed "${J}q;d" ${DATASETFILE})
	# Split string into array : http://stackoverflow.com/questions/10586153/split-string-into-an-array-in-bash
	IFS=',' read -r -a array <<< "${LINE}"
	PROTEIN_A=$(echo "${array[0]}")
	PROTEIN_B=$(echo "${array[1]}")
	IS_PPI=$(echo "${array[2]}")
	if [ "$ID" == "171" ]; then
		echo "Not implemented!"
	elif [ "$ID" == "13141" ]; then
		sh spccseep2.sh -a muscle -t clustalw -b 100 -p ${T} -s ${NB_R_SAMPLES} -d false -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	elif [ "$ID" == "13142" ]; then
		sh spccseep2.sh -a muscle -t fasttree-mp -b 1000 -p ${T} -s ${NB_R_SAMPLES} -d false -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	elif [ "$ID" == "15161" ]; then
		sh spccseep2.sh -a mafft-linsi -t clustalw -b 100 -p ${T} -s ${NB_R_SAMPLES} -d false -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}		
	elif [ "$ID" == "15162" ]; then
		sh spccseep2.sh -a mafft-linsi -t fasttree-mp -b 1000 -p ${T} -s ${NB_R_SAMPLES} -d false -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	fi
}

dpccseep() {
	J=$1
	T=$2
	# Get nth line of file : http://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
	LINE=$(sed "${J}q;d" ${DATASETFILE})
	# Split string into array : http://stackoverflow.com/questions/10586153/split-string-into-an-array-in-bash
	IFS=',' read -r -a array <<< "${LINE}"
	PROTEIN_A=$(echo "${array[0]}")
	PROTEIN_B=$(echo "${array[1]}")
	IS_PPI=$(echo "${array[2]}")
	if [ "$ID" == "171" ]; then
		echo "Not implemented!"
	elif [ "$ID" == "13141" ]; then
		sh dpccseep2.sh -a muscle -t clustalw -b 100 -p ${T} -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	elif [ "$ID" == "13142" ]; then
		sh dpccseep2.sh -a muscle -t fasttree-mp -b 1000 -p ${T} -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	elif [ "$ID" == "15161" ]; then
		sh dpccseep2.sh -a mafft-linsi -t clustalw -b 100 -p ${T} -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}		
	elif [ "$ID" == "15162" ]; then
		sh dpccseep2.sh -a mafft-linsi -t fasttree-mp -b 1000 -p ${T} -s ${NB_R_SAMPLES} -1 ${PROTEIN_A} -2 ${PROTEIN_B} -i ${IS_PPI}
	fi
}

maln() {
	J=$1
	T=$2
	# Get nth line of file : http://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
	LINE=$(sed "${J}q;d" ${DATASETFILE})
	# Split string into array : http://stackoverflow.com/questions/10586153/split-string-into-an-array-in-bash
	IFS=',' read -r -a array <<< "${LINE}"
	PROTEIN_A=$(echo "${array[0]}")
	PROTEIN_B=$(echo "${array[1]}")
	IS_PPI=$(echo "${array[2]}")
	sh maln.sh muscle "" "" ${PROTEIN_A} ${PROTEIN_B}
	sh maln.sh mafft-linsi "" "" ${PROTEIN_A} ${PROTEIN_B}
}

main() {
	I=$1
	for T in 0.0 #0.00831 0.03192 0.03668 0.06556 0.06908 0.10633 0.1647 0.18048 0.22126 0.23451 0.27219 0.30389 0.35001 0.40845 0.45031
	do 
		BEGIN=$(($I-${PAS}+1))
		END=$I
		for ((J=BEGIN;J<=END;J++)); do
			# S-pipe
			spccseep $J $T & 
			# D-pipe
			#dpccseep $J $T &
			# MALN
			#maln $J $T &
		done
		wait
	done
	# S-pipe
	printf '%s\n' ${I} >> "${ROOT}/results/S-pipe/counter_${ID}.txt"
	# D-pipe
	#printf '%s\n' ${I} >> "${ROOT}/results/D-pipe/counter_${ID}.txt"
}

# Parse CSV in bash and assign variables : http://stackoverflow.com/questions/15350208/parse-csv-in-bash-and-assign-variables
# S-pipe
PAS=4
# D-pipe
#PAS=5
NLINES=$(cat ${DATASETFILE} | wc -l)
echo "Number of interactions: $NLINES"
# Produce range with n steps : http://stackoverflow.com/questions/966020/how-to-produce-range-with-step-n-in-bash-generate-a-sequence-of-numbers-with-i
for I in `seq ${PAS} ${PAS} ${NLINES}` 
do
	main $I
done

REST=$((NLINES % PAS))
BEGIN=$((NLINES - REST + 1))
for I in `seq ${BEGIN} 1 ${NLINES}` 
do
	for T in 0.0 #0.00831 0.03192 0.03668 0.06556 0.06908 0.10633 0.1647 0.18048 0.22126 0.23451 0.27219 0.30389 0.35001 0.40845 0.45031
	do
		# S-pipe
		spccseep $J $T & 
		# D-pipe
		#dpccseep $J $T &
		# MALN
		#maln $J $T &
	done
done