# http://www.r-bloggers.com/installing-r-packages/
#.libPaths()
library(ape, lib.loc="~/R/x86_64-unknown-linux-gnu-library/3.2")
library(ape)
############# Remove all leaves not in given list ############# 
# http://blog.phytools.org/2011/03/prune-tree-to-list-of-taxa.html
#setwd("C:/Users/Max/Desktop/ThesisWork/test/background-similarity")
setwd("~/thesis")
setwd("C:/Users/Max/Desktop")
stree_in <- "stree.sdd.phy.tmp"
file.leaves<-"taxid_common_list"
stree_out <- "stree.sdd.phy"
args <- commandArgs(TRUE)
stree_in <- args[1]
file.leaves <- args[2]
stree_out <- args[3]
tree <- read.tree(stree_in)
tree$tip.label
# Read the obsolete leaves from corresponding file
tips.keep<-read.table(file.leaves,stringsAsFactor=FALSE)$V1
setdiff(tree$tip.label, tips.keep)
#?drop.tip
# Delete all obsolete leaves
# http://blog.phytools.org/2011/03/prune-tree-to-list-of-taxa.html
# http://svitsrv25.epfl.ch/R-doc/library/ape/html/drop.tip.html
pruned.tree<-drop.tip(tree, setdiff(tree$tip.label, tips.keep));
write.tree(pruned.tree,stree_out)