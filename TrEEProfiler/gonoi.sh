QUERY=$(awk '{print $5,$6}' LTPs123_SSU.csv | sed ':a;N;$!ba;s/\n/+OR+/g')
# Extract information from REST XML : https://www.biostars.org/p/10959/
echo $QUERY

#curl "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=$QUERY" | grep "<Id>" | cut -d '>' -f 2 | cut -d '<' -f 1 > ORG_ID
