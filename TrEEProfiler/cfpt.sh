#!/bin/bash
# THIS FILE NEED TO BE CONVERTED : dos2unix!
# Convert Format of Phylogenetic Tree 
echo "############### Convert Format of Phylogenetic Tree ###############"
PHYLOGENY_LIRMM_CONVERTER="http://phylogeny.lirmm.fr/phylo_cgi/get_result.cgi"
PROTEIN_A=$1
PROTEIN_B=$2
BLASTRESULTS="results/bdb/"
RESULTS_A="${BLASTRESULTS}${PROTEIN_A}"
RESULTS_B="${BLASTRESULTS}${PROTEIN_B}"
RESULTSPPI="results/ppi/${PROTEIN_A}_${PROTEIN_B}"
# http://stackoverflow.com/questions/19116016/what-is-the-right-way-to-post-multipart-form-data-using-curl
# Simulate click on submit button (name=value) : http://www.codediesel.com/tools/6-essential-curl-commands/
# How to get the parameters : Firefox >
convert2() {
	echo "IN: $1"
	PARAM_1="task_data_file=@${1}"
	PARAM_2="rconv=$2"
	PARAM_3="tasks_id=00000000000000000000000000000000"
	PARAM_4="raw=1"
	# Illegal characters found : http://stackoverflow.com/questions/33768956/curl-3-illegal-characters-found-in-url
	curl -F $PARAM_1 -F $PARAM_2 -F $PARAM_3 -F $PARAM_4 $PHYLOGENY_LIRMM_CONVERTER > $3
}

convert2 "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.sdd.phb" newick "${RESULTSPPI}/${PROTEIN_A}_common_orthologues.sdd.nwk"
convert2 "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.sdd.phb" newick "${RESULTSPPI}/${PROTEIN_B}_common_orthologues.sdd.nwk"
#convert2 "${RESULTSPPI}/stree.sdd.phy" "${RESULTSPPI}/stree.sdd.nwk" 