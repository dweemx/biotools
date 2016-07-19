# Draw ROC curve 
# Roc curve in R : http://www.r-bloggers.com/roc-curves-and-classification/
?read.csv
setwd("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/D-pipe/mafft-linsi_fasttree-mp_pdt-0.0/")
#setwd("C:/Users/Max/Desktop/ThesisWork/results/ecceTERA/(no)pooling,bgc/GbClustalW/")
roc.curve=function(s,score.column.name,isppi.column.name,print=FALSE) {
  Ps=(d[score.column.name]>s)*1 # Condition
  TP=sum((Ps==1)*(d[isppi.column.name]==TRUE))
  TN=sum((Ps==0)*(d[isppi.column.name]==FALSE))
  FP=sum((Ps==1)*(d[isppi.column.name]==FALSE))
  FN=sum((Ps==0)*(d[isppi.column.name]==TRUE))
  FPR=FP/(FP+TN)
  TPR=TP/(TP+FN)
#   FP=sum((Ps==1)*(d$V5==FALSE))/sum(d$V5==FALSE) # FPR = FP / N = FP / (FP + TN)
#   TP=sum((Ps==1)*(d$V5==TRUE))/sum(d$V5==TRUE) # TPR = TP / P = TP / (TP + FN)
  if(print==TRUE){
    print(table(Observed=d[isppi.column.name],Predicted=Ps))
  }
  vect=c(FPR,TPR)
  names(vect)=c("FPR","TPR")
  return(vect)
}


setwd("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/S-pipe/mafft-linsi_fasttree-mp_pdt-0.0/")
d <- read.table("Data (PPV Score) - S-pipe, Mafft-LINSI, Fasttree-MP.txt",sep="\t",stringsAsFactors = FALSE)

# setwd("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/organism-selection/results/ecceTERA/S-pipe/muscle_clustalw/")
# CUBE <- read.table("CUBE - Muscle, ClustalW.txt",sep="\t",stringsAsFactors = FALSE,header = TRUE)


# d_neg<-d[d$V4==FALSE, ]
# d_pos<-d[d$V4==TRUE, ]
# d_pos_sel<-d_pos[sample(nrow(d_pos), nrow(d_neg)), ]
# d_balanced<-rbind(d_pos_sel,d_neg)
# d<-d[order(-d[,3]),]
# d<-d[d$V5>=61,]
# d<-na.omit(d)
ROC.curve=Vectorize(roc.curve)
M.ROC.V3=ROC.curve(seq(0,1,by=.01),"V3","V8")
M.ROC.V4=ROC.curve(seq(0,1,by=.01),"V4","V8")
M.ROC.V5=ROC.curve(seq(0,1,by=.01),"V5","V8")
M.ROC.V6=ROC.curve(seq(0,1,by=.01),"V6","V8")
M.ROC.V7=ROC.curve(seq(0,1,by=.01),"V7","V8")
x<-M.ROC[1,]
y<-M.ROC[2,]
#?plot
plot(x,y,col="grey",lwd=2,type="l",xlab="FPR",ylab="TPR",xlim=c(0,1),ylim=c(0,1))
# https://stat.ethz.ch/R-manual/R-devel/library/graphics/html/abline.html
abline(a=0,b=1,col="red")

# http://stackoverflow.com/questions/3777174/plotting-two-variables-as-lines-using-ggplot2-on-the-same-graph
ggplot() + 
  geom_line(aes(x = M.ROC.V3[1,], y = M.ROC.V3[2,], colour = "mirrortree")) + 
  geom_line(aes(x = M.ROC.V4[1,], y = M.ROC.V4[2,], colour = "~tol-mirrortree")) +
  geom_line(aes(x = M.ROC.V5[1,], y = M.ROC.V5[2,], colour = "ee method")) +
  geom_line(aes(x = M.ROC.V6[1,], y = M.ROC.V6[2,], colour = "mirrortree + ee method")) +
  geom_line(aes(x = M.ROC.V7[1,], y = M.ROC.V7[2,], colour = "~tol-mirrortree + ee method")) +
  geom_line(aes(x = c(0,1), y = c(0,1), colour = "random")) +
  ggtitle("ROC curves of different PPI prediction classifiers\nusing S-pipeline, Mafft-LINSI, Fasttree-MP") +
  labs(x="False Positive Rate",y="True Positive Rate")

# Calculate Area Under the Curve in R : http://stackoverflow.com/questions/4954507/calculate-the-area-under-a-curve-in-r
library(zoo)
id <- order(x)
AUC <- sum(diff(x[id])*rollmean(y[id],2))
AUC

get_AUC(d,"V7","V8")

# http://stackoverflow.com/questions/4903092/calculate-auc-in-r
# You can compute the AUC directly without using any package by using the fact that the AUC 
# is equal to the probability that a true positive is scored greater than a true negative.

get_AUC<-function(d,score.column.name,isppi.column.name) {
  pos.scores <- d[score.column.name][d[isppi.column.name]==TRUE]
  #length(pos.scores)
  neg.scores <- d[score.column.name][d[isppi.column.name]==FALSE]
  #length(neg.scores)
  #mean(sample(pos.scores,1000,replace=T) > sample(neg.scores,1000,replace=T))
  aucs = replicate(1000,mean(sample(pos.scores,1000,replace=T) > sample(neg.scores,1000,replace=T)))
  #mean(aucs)
  #plot(aucs)
  #abline(h=mean(aucs),col="red")
  sd(aucs)
  return (mean(aucs))
}

di<-data.frame()
for(i in min(d$V5):max(d$V5)){
  d_subset<-d[d$V5>=i,]
  if(nrow(d_subset)==0) 
    break
  AUC<-get_AUC(d_subset)
  size<-nrow(d_subset)
  pos<-nrow(d_subset[d_subset$V4==TRUE,])
  neg<-nrow(d_subset[d_subset$V4==FALSE,])
  di<-rbind(di,data.frame(i,size,pos,neg,AUC))
  print(paste("Nb Common:",i,"Size:",size,"TP:",pos,"TN:",neg,"AUC: ",AUC))
}

pos.neg<-di$pos/di$neg
par(mfrow=c(2,2))
plot(di$i,di$AUC)
plot(di$i,di$size)
plot(di$i,pos.neg)
