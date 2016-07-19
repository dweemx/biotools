#!/bin/bash
# http://linux.die.net/man/1/head
# This script will get the column number of the given column name
# Use variables with awk : http://stackoverflow.com/questions/18111781/awk-cannot-concatenate-strings
head -n 1 $1 | awk -F "\t" -v CNAME=$2 '{ for(i=0;i<NF;i++){ if($i==CNAME) {print i} } }'
