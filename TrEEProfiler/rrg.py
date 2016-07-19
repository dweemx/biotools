import csv
import os

lnr_genomes_aa = [] # aa = assembly_accession

with open("bacteria_as_comref_genomes_wo_ecoli",'r') as f:
	next(f) # skip headings
	reader=csv.reader(f,delimiter='\t')
 	tmp_size = None
	tmp_aa = None
	tmp_s_id = None
	for line in reader:
		filepath = line[20].replace("/cygdrive/h/","H:/")
		print filepath
		statinfo = os.stat(filepath)
		# Check whether there is a duplicate
		if tmp_s_id != line[6]: # sid = species_id
			tmp_aa = line[0]
			lnr_genomes_aa.append(tmp_aa)
			tmp_size = statinfo.st_size
		else:
			print ">",tmp_s_id
			# Keep the largest genomes in a same clade
			if tmp_size < statinfo.st_size:
				tmp_size = statinfo.st_size
				tmp_aa = line[0]
		tmp_s_id = line[6]
	lnr_genomes_aa.append(tmp_aa)
	print lnr_genomes_aa
	print "TOTAL:",len(lnr_genomes_aa)


with open("bacteria_as_comref_genomes_wo_ecoli",'r') as f:	
	lines = f.readlines()
	# lnr = Largest Non-Redundant
 	nf = open("bacteria_as_comref_genomes_wo_ecoli_lnr","w")
	for line in lines:
		aa = line.split("\t")[0]
		if aa in lnr_genomes_aa:
			nf.write(line)
			lnr_genomes_aa.remove(aa)
    	else:
    		print "NOT IN: ",aa
