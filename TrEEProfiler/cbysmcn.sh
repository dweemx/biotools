# Get Cell Content By String Matching and Column Name
# Get line matching a string : http://stackoverflow.com/questions/7103531/how-to-get-the-part-of-file-after-the-line-that-matches-grep-expression-first
# Use variable in sed : http://stackoverflow.com/questions/11146098/bash-use-a-variable-in-a-sed-command
CID="$(sh gcn.sh $1 $3)"
# Replace all occurences : http://stackoverflow.com/questions/15849119/why-does-sed-not-replace-all-occurrences
# Escape speciale characters /,[,]
# Multiple replacements : http://stackoverflow.com/questions/26568952/how-to-replace-multiple-patterns-at-once-with-sed
cat $1 | sed -n -e "/$2/p" | awk -F "\t" -v cid=$CID '{print $(cid)}' | sed "s/\//\\\\\//g; s/\[/\\\[/g; s/\]/\\\]/g"