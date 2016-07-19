
setwd("C:/Users/Max/Desktop/ThesisWork/benchmark/DIP/");
df<-read.table("Ecoli_DIP_20160114CR_TP.txt",sep="\t",stringsAsFactors=FALSE)

set.seed(617)
# Select 10 percent of the TP in DIP dataset
ids<-sample(seq(1,nrow(df)),round(nrow(df)*0.1))
TP.10perc<-df[ids,]

# Select 100 percent of the TP in DIP dataset
ids<-sample(seq(1,nrow(df)),round(nrow(df)*1))
TP.100perc<-df

TP<-as.vector(paste0(df$V1,df$V2))
prots<-c(df$V1,df$V2)
uniq_prots<-unique(prots)

write.table(as.data.frame(uniq_prots),"Ecoli_DIP_20160114CR_uniq_prots.txt",quote=F,col.names=F,row.names=F)
length(uniq_prots)

tmp_prots<-c()

TN.100perc<-data.frame()
a=1
while(a <= nrow(TP.100perc)) {
  ids<-sample(seq(1,length(uniq_prots)),2)
  protA<-uniq_prots[ids[1]]
  protB<-uniq_prots[ids[2]]
  ppi<-paste0(protA,protB)
  if(!(ppi %in% TP)) {
    if(!(protA %in% tmp_prots) | length(tmp_prots) >= length(uniq_prots)) {
      TN.100perc<-rbind(TN.100perc,data.frame("protA"<-protA,"protB"<-protB,"is_ppi"<-"FALSE"))
      if(!(protA %in% tmp_prots)) {
        tmp_prots<-c(tmp_prots,protA)
      }
      a<-a+1
    }
  }
  print(a)
}
colnames(TN.100perc)<-c("protA","protB","is_ppi")
prots<-c(TN.100perc$protA,TN.100perc$protB)
length(unique(prots))
#?write.table
TP.100perc<-TP.100perc[,1:3]
colnames(TP.100perc)<-c("protA","protB","is_ppi")
DIP.Core.100perc<-rbind(TP.100perc,TN.100perc)
write.table(DIP.Core.100perc,"Ecoli_DIP_20160114CR_100%.txt",sep=",",quote=F,col.names=F,row.names=F)

# Create the full dataset
colnames(df)<-c("protA","protB","is_ppi")
FULL<-rbind(df,TN)
# Randomize data : http://stackoverflow.com/questions/6422273/how-to-randomize-or-permute-a-dataframe-rowwise-and-columnwise
FULL_RAND <- FULL[sample(nrow(FULL)),]
write.table(FULL_RAND,"Ecoli_DIP_20160114CR_FULL.txt",sep=",",quote=F,col.names=F,row.names=F)


d<-read.table("Ecoli_DIP_20160114CR_FULL.txt",sep=",")
length(which(d$V3==FALSE))

dfff<-FULL_RAND[with(FULL_RAND, order(protA,protB)), ]
write.table(dfff,"Ecoli_DIP_20160114CR_FULL_OrdByAB.txt",sep=",",quote=F,col.names=F,row.names=F)

# Randomize full dataset
FULL<-read.table("Ecoli_DIP_20160114CR_100%.txt",sep=",",stringsAsFactors=FALSE)
a<-c(FULL$V1,FULL$V2)
length(unique(a))
colnames(FULL)<-c("protA","protB","is_ppi")
# Randomize data : http://stackoverflow.com/questions/6422273/how-to-randomize-or-permute-a-dataframe-rowwise-and-columnwise
FULL_RAND <- FULL[sample(nrow(FULL)),]
write.table(FULL_RAND,"Ecoli_DIP_20160114CR_100%_Rand.txt",sep=",",quote=F,col.names=F,row.names=F)



