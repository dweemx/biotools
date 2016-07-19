#!/bin/bash
# THIS FILE NEED TO BE CONVERTED : dos2unix!
# Make Reconciliation between Species and Gene Trees using ecceTERA
echo ""
echo "############### Make Reconciliation between Species and Gene Trees using ecceTERA ###############"
PATH=$PATH:~/thesis/reconciliation/ecceTERA/bin/
PROTEIN_A=$1
PROTEIN_B=$2
SPECIES_TREE=$3
GENE_TREE_A=$4
GENE_TREE_B=$5
I=$6
ISAMALGAMATE=$7
RECONCILED=$8

# Problem when using multiple gene trees when Amalgamation : CladesAndTripartitions:: constructor Polytomy only applies to a single gene tree

if [ "$ISAMALGAMATE" == "true" ]; then
	echo "> With amalgamation ###"
	ISAMALGAMATE=true
	RESOLVETREES=false
	MAXITERS=100
elif [ "$ISAMALGAMATE" == "false" ]; then
	echo "> Without amalgamation ###"
	ISAMALGAMATE=false
	RESOLVETREES=false # /!\ Don't resolve the trees--> add randomness and scores seems to decrease 
	### Manual ecceTERA
	# If a gene tree is unrooted, it will either be rooted randomly or with an optimal rooting if
	# resolve.trees=true. Similarly non-binary gene trees will be binary by choosing
	# a random resolution per each polytomy unless resolve.trees=true, in which case all possible binary
	# resolutions will be considered in an efficient manner. 
	# Default = false
	MAXITERS=1000 # /!\ Very important parameter : don't exactly why yet?! (see below)
	### Manual ecceTERA 
	# max.iterations parameter : 	
	#		If greather than one, the costs and the amalgamation weight will be 
	#		updated at most max.iterations times, until the costs and amalgamation
	#		weight converge (how they are being update see C. Scornavacca et al 
	#		"Joint Amalgamation of Most Parsimonious Reconciled Gene Trees")
	# Default = 0
fi

#resolve.trees=true \ CladesAndTripartitions:: constructor Polytomy only applies to a single gene tree
#max.iterations=10 \
#collapse.mode=1 \
#collapse.threshold=0.5 \
#resolve.trees=true \
#amalgamate=false \
#max.iterations=${MAXITERS} \

ecceTERA_linux64 \
				species.file=$SPECIES_TREE \
				dated=0 \
				check.time.consistency=true \
				gene.file=$GENE_TREE_A \
				output.dir=$RECONCILED \
				resolve.trees=${RESOLVETREES} \
				amalgamate=${ISAMALGAMATE} \
				max.iterations=${MAXITERS} \
				verbose=false \
				print.reconciliations=1 \
				print.reconciliations.file=${PROTEIN_A}_mr${I} \
				print.newick=true \
				keep.only.canonical.reconciliations=false \
				print.newick.species.tree.file=${PROTEIN_A}_${I}.stree \
				print.newick.gene.tree.file=${PROTEIN_A}_${I}.gtree
ecceTERA_linux64 \
				species.file=$SPECIES_TREE \
				dated=0 \
				check.time.consistency=true \
				gene.file=$GENE_TREE_B \
				resolve.trees=${RESOLVETREES} \
				output.dir=$RECONCILED \
				amalgamate=${ISAMALGAMATE} \
				max.iterations=${MAXITERS} \
				verbose=false \
				print.reconciliations=1 \
				print.reconciliations.file=${PROTEIN_B}_mr${I} \
				print.newick=true \
				keep.only.canonical.reconciliations=false \
				print.newick.species.tree.file=${PROTEIN_B}_${I}.stree \
				print.newick.gene.tree.file=${PROTEIN_B}_${I}.gtree