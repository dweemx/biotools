#!/bin/bash
# Download Species Tree from PhyloT Biobyte
echo "############### Download Species Tree from PhyloT Biobyte ###############"
PHYLOT_BIOBYTE="http://phylot.biobyte.de/treeGenerator.cgi"
PROTEIN_A=$1
PROTEIN_B=$2
RESULTSPPI=$3
# http://stackoverflow.com/questions/19116016/what-is-the-right-way-to-post-multipart-form-data-using-curl
# Simulate click on submit button (name=value) : http://www.codediesel.com/tools/6-essential-curl-commands/
# How to get the parameters : Firefox > 
PARAM_1="itol=0"
PARAM_2="phylot=1"
PARAM_3="elementsFile=@${RESULTSPPI}/taxid_common_list"
PARAM_4="ids=id"
PARAM_5="collapse=0"
PARAM_6="binary=1"
PARAM_7="interrupt=0"
# Problem when reading NEWICK tree in ecceTERA 
# > NodeException: Node::getName: no name associated to this node.(id:135)
# Problem when reading NEWICK tree in Notung 2.8 beta : 
# > Error when parsing tree:
# > Tree may have an extra set of parentheses around nodes
PARAM_8="format=newick"
# Illegal characters found : http://stackoverflow.com/questions/33768956/curl-3-illegal-characters-found-in-url
curl -F $PARAM_1 -F $PARAM_2 -F $PARAM_3 -F $PARAM_4 -F $PARAM_5 -F $PARAM_6 -F $PARAM_7 -F $PARAM_8 $PHYLOT_BIOBYTE > "${RESULTSPPI}/stree.sdd.res.phy"
sed -i "s/INT[0-9|\_]*//g" "${RESULTSPPI}/stree.sdd.res.phy"