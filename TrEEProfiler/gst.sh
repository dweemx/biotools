#!/bin/bash
# Download Species Tree from NCBI Taxonomy
echo ""
echo "############### Download Species Tree from NCBI Taxonomy ###############"
NCBI_TAXONOMY="http://www.ncbi.nlm.nih.gov/Taxonomy/CommonTree/wwwcmt.cgi"
PROTEIN_A=$1
PROTEIN_B=$2
RESULTSPPI=$3

ROOT="${VSC_DATA}/thesis"

BLASTRESULTS="${ROOT}/results/bdb/"
RESULTS_A="${BLASTRESULTS}${PROTEIN_A}"
RESULTS_B="${BLASTRESULTS}${PROTEIN_B}"
# http://stackoverflow.com/questions/19116016/what-is-the-right-way-to-post-multipart-form-data-using-curl
# Simulate click on submit button (name=value) : http://www.codediesel.com/tools/6-essential-curl-commands/
curl -F "loadfile=@${RESULTSPPI}/taxid_common_list" -F "saveas=phylip tree" -F "cmd=Save as" $NCBI_TAXONOMY > "${RESULTSPPI}/stree.phy"