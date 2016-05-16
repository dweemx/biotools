#!/usr/bin/python
# Python name convention : http://stackoverflow.com/questions/159720/what-is-the-naming-convention-in-python-for-variable-and-function-names
import csv
import os
import math
import numpy
from Bio import PDB, pairwise2
from Bio.SeqUtils import seq1

'''
Created on 17 nov. 2015

@author: 0mician

## Remarks:
## - Hierarchy of the PDB file
##       1- Structure
##           2- Model
##               3- Chains
##                   4- Residues
## - Difference between SEQRES and ATOM
##     SEQRES  record give the whole full sequence
##     ATOM    records give the structural information each amino acid residue 
##             if there is any structural information available! Sometimes, there
##             is no data available: check the broken regions at
##             http://www.rcsb.org/pdb/explore/remediatedSequence.do?structureId=1BO1
'''
# Retrieve SEQRES record : http://www.bioinfopoint.com/index.php/code/41-extracting-the-sequence-from-a-protein-structure-using-biopython
# handle = open("data/pdb"+pdb_id+".ent", "rU")
# for record in SeqIO.parse(handle, "pdb-seqres") :
#     # Get first letter : record.seq[0]
#     print ">" + record.id + "\n" + record.seq
# handle.close()

''' 
    Important functions to debug: 
        - dir(x) : get all attributes and method available for that object
'''
# Delete multiple elements in list : http://stackoverflow.com/questions/497426/deleting-multiple-elements-from-a-list

pdbl=PDB.PDBList()

def findOccurences(s, ch):
    return [i for i, letter in enumerate(s) if letter == ch]

# Function : http://www.tutorialspoint.com/python/python_functions.htm
# Always define functions before you call it
''' Download and get the structure object of the given PDB structure with
    the given pdb_id '''
def get_pdb( pdb_id ):
    pdb_file_path = "data/pdb"+pdb_id+".ent"
    ''' Check if PDB file exists otherwise download from pdb.org ''' # http://stackoverflow.com/questions/2259382/pythonic-way-to-check-if-a-file-exists
    if os.path.isfile(pdb_file_path) is False:
        pdbl.retrieve_pdb_file(pdb_id,pdir="data")
        print "Downloading PDB structure "+ pdb_id +" from http://www.pdb.org ..."
    parser = PDB.PDBParser(PERMISSIVE=1)
    pdb = parser.get_structure(pdb_id,"data/pdb"+pdb_id+".ent")
    return pdb
 
#         ''' Remove all water molecules residues in the sequence ''' 
#         chain_seq = filter(lambda a: a != "HOH", chain_seq)

''' Return the sequence (both in raw and structured format) of 
        the given chain in 
        the given model (PDB model) 
        with water molecules residues if given with_h2o is true and 
        in 3 letter code if given _1lc is false otherwise return the sequence in 1 letter code '''
def get_pdb_chain_partial_seq (model, chain, _1lc, with_h2o):
    chain=model[chain]
    chain_seq = []
    chain_seq_raw = []
    
    for residue in chain.get_residues():   
        residue_name = residue.get_resname()
        # Remove all occurences of a value in list : http://stackoverflow.com/questions/1157106/remove-all-occurences-of-a-value-from-a-python-list
        ''' Check if sequence with water molecules has to be returned '''
        if with_h2o is False:   
            ''' Remove all water molecules residues in the sequence ''' 
            if residue_name == "HOH":
                continue
    
            ''' Check if 1 letter code sequence has to be returned '''
            if _1lc is True:        
                ''' Convert 3 letter code protein sequence to 1 letter code protein sequence ''' #http://biopython.org/DIST/docs/api/Bio.SeqUtils-module.html#seq1
                residue.resname = seq1(residue_name) # Change variable resname
        chain_seq.append(residue)
        chain_seq_raw.append(residue.resname)
    return [chain_seq, chain_seq_raw]

''' 
    Return the enriched Scheeff sequence alignment as a list of residues structured object 
    containing all structural information (e.g.: coordinates of all atoms for each residue)
    given the:
        - PDB id 
        - Scheeff sequence alignment (see Scheeff et al. nexus file)
    Meaning of NULL values:
        - Gap
        - No structural information from the PDB file
'''
def build_enr_seq_aln (pdb_id,chain_id,scheeff_aln_seq):

    print "Building the enriched sequence alignment for protein "+pdb_id+" ..."
    
    ''' Get all indexes of gaps in the Scheeff CE-like-manual structural alignement 
        performed in the paper Scheeff et al.'''
    scheeff_struct_aln_gaps_indices = findOccurences(scheeff_aln_seq, "-")

    # Delete character from a string: http://stackoverflow.com/questions/3559559/how-to-delete-a-character-from-a-string-using-python
    scheeff_aln_seq = scheeff_aln_seq.replace("-", "")
#     print "ALN:"+scheeff_aln_seq
    
    model=get_pdb(pdb_id)[0]
    # To uppercase : http://stackoverflow.com/questions/9257094/how-to-change-a-string-into-uppercase
    print "Chain selected: "+chain_id.upper()
    pdb_partial_seq = get_pdb_chain_partial_seq(model, chain_id.upper(), True, False)
    
    # Join a list in string : http://stackoverflow.com/questions/12453580/concatenate-item-in-list-to-strings-python
    pdb_partial_seq_raw = ''.join(pdb_partial_seq[1])
    
    ''' Store a list of all residues from the PDB protein sequence found to be present in
        the Scheeff CE (Combinatorial Extension), manual protein sequence alignment '''
    map_sequence = []
    '''
        Align the Scheeff structural sequences alignment with the partial PDB sequences containing only
        the amino acid residues for which there is structural information available
        
        Arguments description for pairwise.align.globalms:
            - scheeff_aln_seq: Scheeff alignment sequence
            - pdb_seq_raw:     partial PDB sequence alignment 
            - 1:               match penalty > Maximize the identical character: bonus score
            - -30:             mismatch penalty > Minimize the non-identical character: very high penalty score
            - -5:              extend gap penalties > Mimimize the number of gaps: very high penalty score when opening gaps 
            - -0.01:           extend gap penalties: Maximize the grouping: very low penalty score when extending gaps
    '''
    # Align sequence: http://biopython.org/DIST/docs/api/Bio.pairwise2-module.html
    # for a in pairwise2.align.globalms(scheeff_aln_seq, pdb_seq_raw,1,-30,-5,-0.01):
    #     print(format_alignment(*a))
    print "Align sequence alignment of protein with its PDB sequence from ATOM records..."
    aln = pairwise2.align.globalms(scheeff_aln_seq, pdb_partial_seq_raw,1,-30,-5,-0.01)    
    '''
        Problem: for 1bo1 there are 2 alignment that maximize the score,
        which one should be taken?
    '''
         
    pdb_partial_seq_aln = aln[0][1]
     
    # Find indexes of all occurences of a char in a string :http://stackoverflow.com/questions/13009675/find-all-the-occurrences-of-a-character-in-a-string
    ''' Get all indexes of the tail gaps in the Scheeff sequence alignement '''
    scheeff_seq_aln_tail_gaps_indices = findOccurences(aln[0][0], "-")
#     print scheeff_seq_aln_tail_gaps_indices
    
    ''' Get all indexes of the gaps in the partial PDB sequence alignement '''
    pdb_partial_seq_aln_gaps_indices = findOccurences(aln[0][1], "-")
#     print pdb_partial_seq_aln_gaps_indices
     
    nb_internal_gaps = 0
    # Loop by sequence index: http://www.tutorialspoint.com/python/python_for_loop.htm
    for i in range(len(pdb_partial_seq_aln)):
        if i in scheeff_seq_aln_tail_gaps_indices:
            continue
        if i in pdb_partial_seq_aln_gaps_indices:
            # Increment ++: http://stackoverflow.com/questions/2632677/python-integer-incrementing-with
            nb_internal_gaps += 1
            # Null : http://stackoverflow.com/questions/3289601/null-object-in-python
            map_sequence.append(None)
#             print None
            continue
#         print pdb_partial_seq[0][i-nb_internal_gaps]
        map_sequence.append(pdb_partial_seq[0][i-nb_internal_gaps])
    
    ''' 
        Add all the gaps from initial Scheeff CE,manual structural alignment.
        The map function execute the lambda function, which is inserting an 
        element at a particular index in the map_sequence list, on each values
        in the list scheeff_struct_aln_gaps_indices
    '''
    # Use lambda function and map : http://www.python-course.eu/lambda.php
    # Add element to list at position : http://www.thegeekstuff.com/2013/06/python-list/
    map(lambda x : map_sequence.insert(x, None), scheeff_struct_aln_gaps_indices)
    return map_sequence

def get_atom(residue, atom_id):
    if residue.has_id("CA"):
        return residue["CA"]
   
''' 
    Return as list
        - a pair list of equivalent atoms given the first protein sequence
          alignment prot_1_seq_aln and given the second protein sequence alignment
          prot_2_seq_aln as an enriched pair list of atom objects
        - the number of equivalent atoms between the two given protein sequence
          alignments
'''
def build_pair_list_atoms(prot_1_seq_aln, prot_2_seq_aln): 
    
    print "Building pair list of 'equivalent' atoms between protein sequences..."       
    nb_match_atoms = 0
    ref_atoms = []
    alt_atoms = []

    for i in range(len(prot_1_seq_aln)):
        ''' Element in protein 1 sequence alignment at position i '''
        el_1 = prot_1_seq_aln[i]
        ''' Element in protein 1 sequence alignment at position i'''
        el_2 = prot_2_seq_aln[i]
        '''    
            We calculate the RMSD only for equivalent residues i.e.:
            so we filter all positions in the alignments that are either a gap or 
            a position with none structural information
        '''
        # OR operator : http://stackoverflow.com/questions/2485466/pythons-equivalent-of-in-an-if-statement
        if el_1 == None or el_2 == None:
            continue
        
        nb_match_atoms += 1
        
        ref_atoms.append(get_atom(el_1, "CA"))
        alt_atoms.append(get_atom(el_2, "CA"))
    return [ref_atoms, alt_atoms, nb_match_atoms]

''' 
    Return the root mean squared distance of pairwise C-alpha atoms of each amino acid
    between the first given protein sequence alignment prot_1_seq_aln and the second 
    given protein sequence alignment prot_2_seq_aln both having been aligned using 
    any available method.
    
    # Remark:
    #     - We cannot just compute the RMDS between the pairwise residues by picking 
    #       straight away the coordinates of both residues. Before computing this 
    #       statistic we need to rotate and translating one protein structure such
    #       that the selected residues are best superposing each other i.e.: minimizing
    #       the mean square root deviation.
    #     
'''
def get_rmsd_prots_aln(ref_atoms,alt_atoms):
    # Superposing proteins in BioPython : http://www2.warwick.ac.uk/fac/sci/moac/people/students/peter_cock/python/protein_superposition/
    # Superposing these paired atom lists
    print "Superposing proteins structures to minimize RMSD..."
    super_imposer = PDB.Superimposer()
    super_imposer.set_atoms(ref_atoms, alt_atoms)
    # Update the structure by moving all the atoms in
    super_imposer.apply(alt_atoms)
    return super_imposer.rms

''' 
    Return the normalize RMSD given the reference value, chosen here as the value of 
    the fitted RMSD curve at 100 residues, rmsd 100 (see O. Carugo,S. Pongor paper)       
'''
def norm_rmsd(rmsd,N):
    print "Number of matched pairs: %s" % (N)
    # Math functions: https://docs.python.org/2/library/math.html
    # Convert int to float: http://stackoverflow.com/questions/31519987/convert-int-to-double-python
    return rmsd/(1+math.log(math.sqrt(float(N)/100))) # log = natural logarithm

# http://stackoverflow.com/questions/2572916/numpy-smart-symmetric-matrix
def symmetrize(a):
    return a + a.T - numpy.diag(a.diagonal())

''' Distance Similarity Alignment Tool 
    Compute a RMDS distance matrix between all pairwise pdb protein structure '''
class PyRAT(object):
    def __init__(self, string):
                
        # Read tabulated file : https://docs.python.org/2/library/csv.html
        with open('data/kinases_struct_alignment.dat', 'rb') as csvfile:
            reader = csv.reader(csvfile, delimiter='\t', quotechar='|')
             
            pdb_list = []           
            for row in reader:
                # Substring a string : http://stackoverflow.com/questions/663171/is-there-a-way-to-substring-a-string-in-python
                protein_name    = row[0][1:5]
                protein_chain   = row[0][5]
                protein_seq     = row[1]
                # Append value to list : http://stackoverflow.com/questions/252703/python-append-vs-extend
                pdb_list.append([protein_name, protein_chain, protein_seq])                 
            print pdb_list   
            
            nb_proteins = len(pdb_list)
            # How to store a matrix: http://stackoverflow.com/questions/7945722/efficient-way-to-represent-a-lower-upper-triangular-matrix
            rmsd_matrix = numpy.zeros((nb_proteins, nb_proteins))      
            
            nb_dist_to_compute = nb_proteins/2*(nb_proteins-1)
            completed = 0
            
            ''' Compute the distance similarity matrix between all pairwise proteins '''
            for i in range(len(pdb_list)):
                for j in range(len(pdb_list)):
                    ''' Matrix is symmetric: compute only triangular matrix '''
                    if j <= i:
                        continue
                    ''' Get the given chain '''
                    chain_sel_i = chain_sel_j = "A"
                    
                    protein_i = pdb_list[i]
                    protein_j = pdb_list[j]
                    ''' Check if a given chain has been selected ''' # http://stackoverflow.com/questions/1504717/why-does-comparing-strings-in-python-using-either-or-is-sometimes-produce
                    
                    if protein_i[1] != "_": 
                        chain_sel_i = protein_i[1]
                    if protein_j[1] != "_": 
                        chain_sel_j = protein_j[1]
                    
                    map_seq_i = build_enr_seq_aln(protein_i[0], chain_sel_i, protein_i[2])
                    map_seq_j = build_enr_seq_aln(protein_j[0], chain_sel_j, protein_j[2])
                    
                    paired_atom_lists = build_pair_list_atoms(map_seq_i, map_seq_j) 
                    rmsd = get_rmsd_prots_aln(paired_atom_lists[0], paired_atom_lists[1])
                    # Number to string : http://stackoverflow.com/questions/22617/format-numbers-to-strings-in-python
                    print "RMSD ["+protein_i[0]+"/"+protein_j[0]+"]: %0.3f" % (rmsd)
                    n_rmsd = norm_rmsd(rmsd,paired_atom_lists[2])
                    print "Normalized RMSD ["+protein_i[0]+"/"+protein_j[0]+"]: %0.3f" % (n_rmsd)
                    
                    rmsd_matrix[i][j] = n_rmsd
                    
                    completed += 1                    
                    percent_completed = float(completed)/nb_dist_to_compute*100
                    # Print % : http://stackoverflow.com/questions/10678229/how-can-i-selectively-escape-percent-in-python-strings
                    print "Completed: %0.3f %%" % (percent_completed)
             
            ''' 
                Symmetrize the triangular matrix i.e.: fill the empty cells by symmetry 
                Dump the RMSD similarity matrix into a csv file 
            '''
            # http://stackoverflow.com/questions/6081008/dump-a-numpy-array-into-a-csv-file       
            numpy.savetxt("Scheeff_rmsd_mat.csv", symmetrize(rmsd_matrix), delimiter=",")
                    
''' Run PyRAT '''
PyRAT("") 

#secondary_structure, accessibility=dssp[(chain_id, res_id)]

# ''' Run the DSATool for particular cases '''  
# pdb_id = "1f3m"
# scheeff_aln_seq = "YTRF-EKI-GQG-ASGTVYTAMDVA--------TGQEVAIKQMNLQQ---------QP-------KKE--LIINEILVMRENK----------------NPNIVNYLDSYLVG-------------------------------DELWVVMEYLA------GGSL-------------------------TDVVTET-------------------------CMD----------------EGQIAAVCRECLQALEFLHS--------NQ-----------------------------------------------------------------VIHRDI---------KSDNILLGM----------------------------------------------------------------------------DGSVKLTDFGFCAQITPEQ----SKR---STMVGTPYWMAPEVVTR------KA----YG-----------------PKVDIWSLGIMAIEMIEG----------E-PPYLNE-------NPLRALYLIAT-NG---------------------------------------TP--EL--Q----NPEK------------LSAIFRDFLNRCLDMDVEKRGS------AKELLQHQFLKI"
# map_seq_1 = build_enr_seq_aln(pdb_id, "C", scheeff_aln_seq)
# print map_seq_1
#  
# pdb_id = "1o6y"
# scheeff_aln_seq = "YELG-EIL-GFG-GMSEVHLARDLR--------LHRDVAVKVLRADL------ARDPS-------FYL--RFRREAQNAAALN----------------HPAIVAVYDTGEAETP---------------------------AGPLPYIVMEYVD------GVTL-------------------------RDIVHTE------------------------GPMT----------------PKRAIEVIADACQALNFSHQ--------NG-----------------------------------------------------------------IIHRDV---------KPANIMISA----------------------------------------------------------------------------TNAVKVMDFGIARAIADSGNS--VTQT--AAVIGTAQYLSPEQARG------DS----VD-----------------ARSDVYSLGCVLYEVLTG----------E-PPFTGD-------SPVSVAYQHVR-ED----------------------------------------P--IP--PS-A-RHEG------------LSADLDAVVLKALAKNPENRYQT-----AAEMRAD-LVRV"
# map_seq_2 = build_enr_seq_aln(pdb_id, "A", scheeff_aln_seq)
# print map_seq_2
#  
# paired_atom_lists = build_pair_list_atoms(map_seq_1, map_seq_2) 
# rmsd = get_rmsd_prots_aln(paired_atom_lists[0], paired_atom_lists[1])
# # Number to string : http://stackoverflow.com/questions/22617/format-numbers-to-strings-in-python
# print "RMSD: %0.3f" % (rmsd)
# n_rmsd = norm_rmsd(rmsd,paired_atom_lists[2])
# print "Normalized RMSD: %0.3f" % (n_rmsd)       