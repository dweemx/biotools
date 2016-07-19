# Set location of a library : http://www.r-bloggers.com/installing-r-packages/
#?library
library(ape, lib.loc="~/R/x86_64-unknown-linux-gnu-library/3.2")
#library(ape)
#setwd("C:/Users/Max/Desktop/")
setwd("~/thesis")
# stree_in <- "greengenes_export_0_wodots.aln.phy_phyml_tree.txt"
# stree_out <- "greengenes_export_0_wodots.aln.phy_phyml_tree_res3.txt"
args <- commandArgs(TRUE)
stree_in <- args[1]
stree_out <- args[2]
t <- read.tree(stree_in)
t.res <- multi2di(t)
write.tree(t.res,stree_out)
#plot(t.res, type = "cladogram")
