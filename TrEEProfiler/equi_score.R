library(lattice)
library(latticeExtra)
# http://stackoverflow.com/questions/20601642/how-to-plot-multiple-ecdfs-on-one-plot-in-different-colors-in-r
setwd("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/S-pipe/mafft-linsi_fasttree-mp_pdt-0.0/")
DIST <- read.table("dist_mafft-linsi_fasttree-mp_scores.txt",sep="\t",stringsAsFactors = FALSE,header = FALSE)
DIST$id <- do.call(paste, c(DIST[c("V1", "V2")], sep = ""))

setwd(paste0("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/S-pipe/mafft-linsi_fasttree-mp_pdt-0.0/scores/"))
df<-get.Spipe.scores.df("V3","")
df$id <- do.call(paste, c(df[c("namesA", "namesB")], sep = "")) 

dff<-merge(x=df,y=DIST,by="id",all.x=TRUE)
dff<-dff[,c("V1","V2","V3","V4","TT","V5")]

dff.max<-100.34144#max(dff$TT)
dff[is.infinite(dff$TT),]$TT <- dff.max 
d<-dff
setwd(paste0("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/S-pipe/mafft-linsi_fasttree-mp_pdt-0.0/"))
write.table(d,"Data (P-Value Score) - S-pipe, Mafft-LINSI, Fasttree-MP.txt",quote=FALSE,sep="\t",row.names=FALSE,col.names=FALSE)
d<-read.table(file="Data (P-Value Score) - S-pipe, Mafft-LINSI, Fasttree-MP.txt",sep="\t",header=FALSE,stringsAsFactors=FALSE)
d<-na.omit(d)

ROC.curve=Vectorize(roc.curve)
#threshold.scores<-seq(0,dff.max,by=.01)
library(liso)
threshold.scores<-seq.log(from=1,to=50,length.out=100)-1
M.ROC.vals=ROC.curve(threshold.scores,"V5","V6")
x<-threshold.scores#[1:860]
y<-M.ROC.vals[3,]
y<-y#[1:860]
eq.data<-data.frame("x"=x,"y"=y)
plot(x,y)

# fitting Hill equation
nls.fit <- nls(y ~ a+( ( (1-a)*x^b )/(c^b+x^b) ), start = c(a = 0.01, b = 0.01, c = 0.01),data=eq.data)
nls.predicted<-predict(nls.fit, newdata=data.frame(x=threshold.scores))
plot(threshold.scores,nls.predicted,type="l",col="red")
points(x,y)

# fitting code
# http://kyrcha.info/2012/07/08/tutorials-fitting-a-sigmoid-function-in-r/
# fitmodel <- nls(y~a/(1 + exp(-b * (x-c))), start=list(a=1,b=1,c=1))
# summary(fitmodel)
# # get the coefficients using the coef function
# params=coef(fitmodel)
# y2 <- sigmoid(params,x)
# raw<-data.frame("x1"=x,"x2"=y)
# 
# plot(x,y2,type="l",col="red")
# points(x,y)

library(ggplot2)
p<-ggplot(eq.data, aes(x, y)) + geom_point() + labs(x="Score",y="Positive Predicted Value") + ggtitle("Relationship between score and the positive predicted value (PPV)\nusing S-pipeline, Mafft-LINSI, Fasttree-MP")
p + geom_line(aes(x = threshold.scores, y = nls.predicted, colour = "fitted curve"))

d$Score<-predict(nls.fit, newdata=data.frame(x=d$V5))
d$CombinedScore<-(1-(1-d$V3)*(1-d$Score))
d$CombinedCorrectedScore<-(1-(1-d$V4)*(1-d$Score))
d<-d[,c("V1","V2","V3","V4","Score","CombinedScore","CombinedCorrectedScore","V6")]

# Save the transformed dataset
setwd(paste0("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/results/ecceTERA/S-pipe/mafft-linsi_fasttree-mp_pdt-0.0/"))
write.table(d,"Data (PPV Score) - S-pipe, Mafft-LINSI, Fasttree-MP.txt",quote=FALSE,sep="\t",row.names=FALSE,col.names=FALSE)

get_AUC(d,"CombinedCorrectedScore","V6")
ROC.curve=Vectorize(roc.curve)
M.ROC.V3=ROC.curve(seq(0,1,by=.01),"Score","V3")
x<-M.ROC.V3[1,]
y<-M.ROC.V3[2,]
#?plot
plot(x,y,col="grey",lwd=2,type="l",xlab="FPR",ylab="TPR",xlim=c(0,1),ylim=c(0,1))
# https://stat.ethz.ch/R-manual/R-devel/library/graphics/html/abline.html
abline(a=0,b=1,col="red")

plot(x,y)
identify(threshold.scores,M.ROC.vals[3,])

# function needed for visualization purposes
sigmoid = function(params, x) {
  params[1] / (1 + exp(-params[2] * (x - params[3])))
}

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

roc.curve=function(s,score.column.name,isppi.column.name,print=FALSE) {
  Ps=(d[score.column.name]>s)*1 # Condition
  TP=sum((Ps==1)*(d[isppi.column.name]==TRUE))
  TN=sum((Ps==0)*(d[isppi.column.name]==FALSE))
  FP=sum((Ps==1)*(d[isppi.column.name]==FALSE))
  FN=sum((Ps==0)*(d[isppi.column.name]==TRUE))
  FPR=FP/(FP+TN) # 
  TPR=TP/(TP+FN) # Sensitivity
  PPV=TP/(TP+FP) # Positive Predicted Value
  SPEC=TN/(FP+TN) # Specificity
  #   FP=sum((Ps==1)*(d$V5==FALSE))/sum(d$V5==FALSE) # FPR = FP / N = FP / (FP + TN)
  #   TP=sum((Ps==1)*(d$V5==TRUE))/sum(d$V5==TRUE) # TPR = TP / P = TP / (TP + FN)
  if(print==TRUE){
    print(table(Observed=d[isppi.column.name],Predicted=Ps))
  }
  vect=c(FPR,TPR,PPV,SPEC)
  names(vect)=c("FPR","TPR","PPV","SPEC")
  return (vect)
}

get.Spipe.scores.df<-function(score.column.name,isppi.column.name) {
  files <- list.files(path=getwd(), pattern="ee_scores*", full.names=T, recursive=FALSE)
  
  k<-lapply(files, function(x) {
    r <- read.table(x, header=F,stringsAsFactor=FALSE) # load file
    unique(r$V1)
    #list("name"=unique(r$V2),"scores"=r$V3)
  })
  namesA <- as.vector(do.call("cbind", k))
  
  k<-lapply(files, function(x) {
    r <- read.table(x, header=F,stringsAsFactor=FALSE) # load file
    unique(r$V2)
    #list("name"=unique(r$V2),"scores"=r$V3)
  })
  namesB <- as.vector(do.call("cbind", k))
  
  # k<-lapply(files, function(x) {
  #   r <- read.table(x, header=F,stringsAsFactor=FALSE) # load file
  #   print(r$V5)
  #   unique(r["V8"])
  #   #list("name"=unique(r$V2),"scores"=r$V3)
  # })
  # is_ppi <- as.vector(do.call("cbind", k))
  
  l<-lapply(files, function(x) {
    r <- read.table(x, header=F) # load file
    print(length(r$V3))
    r$V3[1:25]
    #list("name"=unique(r$V2),"scores"=r$V3)
  })
  
  df <- as.data.frame(do.call("cbind", l))
  
  #names <- do.call("cbind",l)
  # Select all columns except : http://stackoverflow.com/questions/12868581/list-all-column-except-for-one-in-r
  bg<-as.vector(as.matrix(df))
  bg<-bg[bg>0]
  mean(bg)
  hist(bg,breaks=200)
  
  TT<-apply(df, 2, function(x) {
    # http://www.inside-r.org/r-doc/base/log
    -log(t.test(bg,x,alternative="l",var.equal=FALSE)$p.value)
  })
  #return (data.frame(namesA,namesB,TT,is_ppi))
  return (data.frame(namesA,namesB,TT))
}
