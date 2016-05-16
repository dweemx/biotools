import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.TTest;

import bgmask.NodeEventSupportDistribution;
import csv.NodeEEP2;
import csv.ReadCSV;
import csv.TreeEEPWriter2;
import csv.WriteCSV;
import mdtlgraph.Edge;
import mdtlgraph.EventSplit;
import mdtlgraph.EventType;
import mdtlgraph.Graph;
import mdtlgraph.Node;
import mdtlgraph.Parser;
import phy.newickparser.Tree;
import phy.newickparser.TreeInternalNode;
import phy.newickparser.TreeLeafNode;
import phy.newickparser.TreeNode;
import utils.Maths;

/**
 * No Pooling events 
 * 
 * @author Max
 *
 */
public class Main2 {
	
	Graph medianDTLGraph = new Graph();
	ArrayList<NodeEEP2> treeEEP = new ArrayList<NodeEEP2>();
	
	private String proteinId;
	private String ecceTERAMedianReconciliationFilePath;
	private String speciesTreeFilePath;
	private String outputFileName;
	private String selectedColumns;
	
	public Main2(String proteinId, 
			String speciesTreeFilePath,
			String ecceTERAMedianReconciliationFilePath,
			String outputFileName) {
		this(proteinId,speciesTreeFilePath,ecceTERAMedianReconciliationFilePath,outputFileName,"");
	}
	
	/**
	 * Build the Macro-Evolutionary Event Profile for the
	 * given proteinId with the species tree found at the 
	 * given speciesTreeFilePath and the given ecceTERA median
	 * reconciliation found at the given ecceTERAMedianReconciliationFilePath
	 * and save the resulting MEEP profile in the given outputFilenames
	 * 
	 * @param 	proteinId
	 * 			The Protein ID of the considered protein
	 * @param 	speciesTreeFilePath
	 * 			The species Tree (newick format) of the given protein
	 * @param 	ecceTERAMedianReconciliationFilePath
	 * 			The eccTERA media reconciliation file path of the given protein
	 * @param 	outputFileName
	 * 			The name of the EEP profile file
	 */
	public Main2(String proteinId, 
			String speciesTreeFilePath,
			String ecceTERAMedianReconciliationFilePath,
			String outputFileName,
			String selectedColumns) {
		this.proteinId = proteinId;
		this.speciesTreeFilePath = speciesTreeFilePath;
		this.ecceTERAMedianReconciliationFilePath = ecceTERAMedianReconciliationFilePath;
		this.outputFileName = outputFileName;
		this.selectedColumns = selectedColumns;
		this.buildMedianDTLGraph();
//		this.medianDTLGraph.print();
	}
	
	public static void help() {
		System.out.println("### EEP Maker (make) ###");
		System.out.println("[1]: Protein ID");
		System.out.println("[2]: Filepath of species tree (newick format) of the given protein");
		System.out.println("[3]: Filepath of the ecceTERA median reconciliation of the given protein");
		System.out.println("[4]: Output filepath of the MEEP profile");
		System.out.println("[5] (Optional) : Select particular columns");
		System.out.println();
		System.out.println("### EEP Mapper (map) ###");
		System.out.println("[1]: Filepath of species tree (newick format) of the given protein A");
		System.out.println("[2]: Filepath of species tree (newick format) of the given protein B");
		System.out.println();
		System.out.println("### EEP Compare (compare) ###");
		System.out.println("[1]: Similarity metric (eg.: cosine)");
		System.out.println("[2]: Protein A ID");
		System.out.println("[3]: Filepath of EE Profile of the given protein 1");
		System.out.println("[4]: Protein B ID");
		System.out.println("[5]: Filepath of EE Profile of the given protein B");
		System.out.println("[6]: Output file path of the similarity");
		System.out.println("[7]: Is PPI?");
		System.out.println("[8]: Number of common species");
	}
	
	
	public static void main(String[] args) {
		
		System.out.println("##############################################");
		System.out.println("#                  BEEP-1.2                  #");
		System.out.println("#     Evolutionary Event Profile Manager     #");
		System.out.println("#              © Copyright 2016              #");
		System.out.println("#         Author: Maxime De Waegeneer        #");
		System.out.println("##############################################");
		
		if(args.length > 0) {
			switch(args[0]) {
				case "map":
					if(args.length < 3 || args.length > 3) {
						help();
					} else {
						buildEEPMapping(args[1],args[2]);
					}
					break;
				case "make":
					Main2 main = null;
					if(args.length < 6 || args.length > 6) {
						help();
					} else if(args.length == 6) {
						main = new Main2(args[1],args[2],args[3],args[4],args[5]);
					} else {
						main = new Main2(args[1],args[2],args[3],args[4]);
					}
					System.out.println("Creating the median DTL-graph...");
					main.buildMedianDTLGraph();
					System.out.println("Building the EE Profile of "+args[1]+" tree...");
					System.out.println("Done!");
					main.buildEEMatrix();
					main.buildEEProfile();
					break;
				case "compare":
					computeCosineSimilarity(args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
					break;
				case "help":
					help();
			}
		}
//		String referenceTreeFilePath = "C:/Users/Max/Desktop/ThesisWork/benchmark/ecceTERA/D-pipe/!mafft-linsi_clustalw/ppi/P0A7Z4_P0A8V2/reconciled/P0A8V2_1.stree";
//		String referenceTreeFilePath = "C:/Users/Max/Desktop/ThesisWork/test/eggnog/reconciliation/P0ABB0.stree";
//		String refMRFilePath = "C:/Users/Max/Desktop/ThesisWork/test/eggnog/reconciliation/P0ABB0_symmetric.txt";
//		Main2 main = new Main2("P0ABB0",referenceTreeFilePath,refMRFilePath,"C:/Users/Max/Desktop/ThesisWork/test/eggnog/reconciliation/P0ABB0","023456789");
//		System.out.println("Creating the median DTL-graph...");
//		main.buildMedianDTLGraph();
//		System.out.println("Building the EE Profile of "+" tree...");
//		System.out.println("Done!");
//		main.buildEEMatrix();
//		main.buildEEProfile();
		
		
		//		String dataDirectory = "C:/Users/Max/Desktop/ThesisWork/benchmark/ecceTERA/D-pipe/!muscle_fasttree_zorro-0.5/ppi";
//		buildAllPPEEMs(dataDirectory);
//		Map<Integer, double[]> mask = buildBGMask(dataDirectory, referenceTreeFilePath);
//		buildAllEEProfiles(dataDirectory, mask);
//		computeAllCosineSimilarity(dataDirectory);
	}
	
	public static void computeCosineSimilarity(String protA, String protAProfileFilePath, String protB, String protBProfileFilePath, String out, String isPPI, String nbCommonSpecies) {
		// Protein A 
		double[] arrayVectorA = ReadCSV.read(protAProfileFilePath);
		ArrayRealVector vectorA = new ArrayRealVector(arrayVectorA);	
		// Protein B
		double[] arrayVectorB = ReadCSV.read(protBProfileFilePath);
		ArrayRealVector vectorB = new ArrayRealVector(arrayVectorB);
		// Compute cosine of 2 RealVector : https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/linear/RealVector.html#cosine%28org.apache.commons.math3.linear.RealVector%29
		double cosineSimilarity = vectorA.cosine(vectorB);
		// Pearson correaltion : http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/stat/correlation/PearsonsCorrelation.html
		double pearsonCorrelation =  new PearsonsCorrelation().correlation(arrayVectorA, arrayVectorB);
		System.out.println(protA+"<-("+cosineSimilarity+","+pearsonCorrelation+"):"+isPPI+"->"+protB);
		String content = protA+"/t"+protB+"\t"+cosineSimilarity+"\t"+pearsonCorrelation;
		if(isPPI.equals("FALSE") || isPPI.equals("TRUE")) {
			content += "\t"+isPPI;
		} 
		if(nbCommonSpecies != null) {
			content += "\t"+nbCommonSpecies;
		}
		content += "\n";
		// Append to a file : http://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		try {
			File f = new File(out);
			if(!f.exists()) {
				f.createNewFile();				
			}
		    Files.write(Paths.get(out), content.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
	
	public static void computeAllCosineSimilarity(String dataDirectory) {
		File dir = new File(dataDirectory);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String[] prots = child.getName().split("_");
				String protAProfileFilePath = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp3.txt";
				String protBProfileFilePath = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_mp3.txt";
				String outputFilePath = child.getParentFile().getParentFile()+File.separator+"scores_corrected"+File.separator+"scores_"+child.getName()+".txt";
				computeCosineSimilarity(prots[0], protAProfileFilePath, prots[1], protBProfileFilePath, outputFilePath,"",null);
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
	}
	
	/**
	 * Apply the given bgMask to correct for the background similarity
	 * 
	 * @param dataDirectory
	 * @param 	bgMask
	 * 			The background mask to apply on each EE profiles located in 
	 * 			the given dataDirectory
	 */
	public static void buildAllEEProfiles(String dataDirectory,Map<Integer, double[]> bgMask) {
		// Iterate over files: http://stackoverflow.com/questions/4917326/how-to-iterate-over-the-files-of-a-certain-directory-in-java
		File dir = new File(dataDirectory);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String[] prots = child.getName().split("_");
				String protAMp = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp2.txt";
				String protBMp = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_mp2.txt";
				String outA = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp3.txt";
				String outB = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp3.txt";
				buildPPEEProfiles(prots,protAMp,outA,protBMp,outB,bgMask);
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
	}
	
	public void buildEEProfile() {
		// File path of the evolutionary event matrix
		String eeMatrixFilePath = outputFileName+"_eem.txt";
		List<String[]> eem = ReadCSV.read(eeMatrixFilePath, "\t");
		// File path of the evolutionary event profile
		String eeProfileFilePath = outputFileName+"_eep.txt";
		buildEEProfile(eem, eeProfileFilePath, null, null);
	}
	
	public static void buildEEProfile(List<String[]> eem, String outFilePath, HashMap<Integer, Integer> map, Map<Integer, double[]> bgMask) {
		ArrayList<NodeEEP2> treeEEP = new ArrayList<NodeEEP2>();
		// Protein A 
		for(String[] mpNode: eem) {
			int anid = 0;
			if(bgMask != null) {
				// Get the absolute node id from the relative node id
				int rnid = Integer.parseInt(mpNode[0]);
				anid = map.get(rnid); 
			}
			double[] nEventSupportsCorrected = new double[8];
			for(int i=1; i<mpNode.length; i++) {
				double a = Double.parseDouble(mpNode[i]);
				/*
				 * Correct the event support by the corresponding background
				 * mask value
				 */
				if(bgMask != null) {
					a -= bgMask.get(anid)[i-1];
					// Negative scores are not allowed
					a = a < 0 ? 0: a;
				}
				nEventSupportsCorrected[i-1] = a;
			}
			NodeEEP2 neep2 = new NodeEEP2(0,nEventSupportsCorrected[0],
					nEventSupportsCorrected[1],nEventSupportsCorrected[2],
					nEventSupportsCorrected[3],nEventSupportsCorrected[4],
					nEventSupportsCorrected[5],nEventSupportsCorrected[6],
					nEventSupportsCorrected[7]);
			treeEEP.add(neep2);
		}
		TreeEEPWriter2.writeEEPProfile(treeEEP,outFilePath,"23456789");
	}
	
	/**
	 * Make the Evolutionary Event Profiles of the given proteins with applying
	 * a mask if the given bgMask is not null
	 * 
	 * @param prots
	 * @param protAMp
	 * @param protBMp
	 * @param bgMask
	 */
	public static void buildPPEEProfiles(String[] prots, String protAMp, String outA, String protBMp, String outB, Map<Integer, double[]> bgMask) {
		if(bgMask != null) {
			System.out.println("Masking "+prots[0]+"<->"+prots[1]);
		}
		// Get the mapping between the nodes if applying mask
		HashMap<Integer, Integer> map = null;
		if(bgMask != null) {
			map = mappings.get(prots[0]+"_"+prots[1]);
		}
		
		List<String[]> eemA = ReadCSV.read(protAMp, "\t");
		List<String[]> eemB = ReadCSV.read(protBMp, "\t");
		// Check if the size of the map is the same as the size of profiles A and B
		if(bgMask != null) {
			if(map.size() != eemA.size()
					|| map.size() != eemB.size()) 
				throw new IllegalArgumentException("Mapping and Reconciliation file have different size!");
		}
		// Protein A
		buildEEProfile(eemA, outA, map, bgMask);
		// Protein B
		buildEEProfile(eemB, outB, map, bgMask);
	}
	
	/**
	 * 
	 */
	static Map<String, HashMap<Integer, Integer>> mappings = new HashMap<String,HashMap<Integer,Integer>>();
	
	public static int getPValueMask(double pValue) {
		if(Double.isNaN(pValue))
			return 0;
		else if(pValue < 10e-4) 
			return 1;
		else
			return 0;
	}

	public static double getMeanMask(double mean) {
		return mean;
	}
	
	/**
	 * Build the distribution of event support of each evolutionary event for each node of all 
	 * the trees located in the given dataDirectory using a reference tree given by 
	 * the the referenceTreeFilePath. From this set of distributions, we create a
	 * mask (using the mean or the deviation from zero) that will be used to correct 
	 * for the background similarity.
	 * 
	 * @param dataDirectory
	 * @param referenceTreeFilePath
	 * @return
	 */
	public static Map<Integer, double[]> buildBGMask(String dataDirectory, String referenceTreeFilePath) {
		HashMap<Integer,NodeEventSupportDistribution> nodeBGDistributions = new HashMap<Integer,NodeEventSupportDistribution>();
		// Iterate over files: http://stackoverflow.com/questions/4917326/how-to-iterate-over-the-files-of-a-certain-directory-in-java
		File dir = new File(dataDirectory);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String[] prots = child.getName().split("_");
				// Species from both protein involved in the interaction are the same
				String protStree = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_1.stree";
				String protAMp = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp2.txt";
				String protBMp = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_mp2.txt";

				/*
				 * Given that the topology of the 2 trees are not exactly the same
				 * because they have both different number of leaves, we need to map
				 * each tree onto the reference tree
				 */
				HashMap<Integer, Integer> map = buildTreeMapping(referenceTreeFilePath,protStree);
				// Save the mapping
				mappings.put(child.getName(), map);
				
				List<String[]> mpA = ReadCSV.read(protAMp, "\t");
				List<String[]> mpB = ReadCSV.read(protBMp, "\t");
				if(map.size() != mpA.size()
						|| map.size() != mpB.size()) 
					throw new IllegalArgumentException("Mapping and Reconciliation file have different size!");
				NodeEventSupportDistribution nesd;
				// Protein A 
				for(String[] mpNode: mpA) {
					// Get the Absolute Node Id from the Relative Node Id
					int rnid = Integer.parseInt(mpNode[0]);
					int anid = map.get(rnid); 
					if(nodeBGDistributions.containsKey(anid))
						nesd = nodeBGDistributions.get(anid);
					else {
						nesd = new NodeEventSupportDistribution(anid);
						nodeBGDistributions.put(anid, nesd);
					}
					nesd.addD(Double.parseDouble(mpNode[1])); nesd.addT(Double.parseDouble(mpNode[2])); 
					nesd.addTTD(Double.parseDouble(mpNode[3]));	nesd.addTFD(Double.parseDouble(mpNode[4])); 
					nesd.addSL(Double.parseDouble(mpNode[5])); nesd.addTL(Double.parseDouble(mpNode[6]));
					nesd.addTLTD(Double.parseDouble(mpNode[7])); nesd.addTLFD(Double.parseDouble(mpNode[8]));
				}
				// Protein B
				for(String[] mpNode: mpB) {
					// Get the Absolute Node Id from the Relative Node Id
					int rnid = Integer.parseInt(mpNode[0]);
					int anid = map.get(rnid); 
					if(nodeBGDistributions.containsKey(anid))
						nesd = nodeBGDistributions.get(anid);
					else {
						nesd = new NodeEventSupportDistribution(anid);
						nodeBGDistributions.put(anid, nesd);
					}
					nesd.addD(Double.parseDouble(mpNode[1])); nesd.addT(Double.parseDouble(mpNode[2])); 
					nesd.addTTD(Double.parseDouble(mpNode[3]));	nesd.addTFD(Double.parseDouble(mpNode[4])); 
					nesd.addSL(Double.parseDouble(mpNode[5])); nesd.addTL(Double.parseDouble(mpNode[6]));
					nesd.addTLTD(Double.parseDouble(mpNode[7])); nesd.addTLFD(Double.parseDouble(mpNode[8]));
				}
//				System.out.println("Done for "+child.getName());
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
		System.out.println(nodeBGDistributions.size());
		
		TTest tTest = new TTest();
		Map<Integer,double[]> bgMask = new HashMap<Integer,double[]>();
		for(Integer i: nodeBGDistributions.keySet()) {
			// Calculate the deviation from zero
			int mu = 0;
			double[] distr = nodeBGDistributions.get(i).getdDAsArray();
			// T-Test : http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/stat/inference/TTest.html#tTest%28double,%20double[]%29
//			double pValueD = tTest.tTest(mu, distr);
//			double dMask = getPvalueMask(pValueD);
			// Calculate the mean : https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/stat/StatUtils.html
			double mean = StatUtils.mean(distr);
			double dMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdTAsArray();
//			double pValueT = tTest.tTest(mu, distr);
//			double tMask = getPvalueMask(pValueT);
			mean = StatUtils.mean(distr);
			double tMask = getMeanMask(mean);

			
			distr = nodeBGDistributions.get(i).getdTTDAsArray();
//			double pValueTTD = tTest.tTest(mu, nodeBGDistributions.get(i).getdTTDAsArray());
//			double tTDMask = getPvalueMask(pValueTTD);
			mean = StatUtils.mean(distr);
			double tTDMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdTFDAsArray();
//			double pValueTFD = tTest.tTest(mu, distr);
//			double tFDMask = getPvalueMask(pValueTFD);
			mean = StatUtils.mean(distr);
			double tFDMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdSLAsArray();
//			double pValueSL = tTest.tTest(mu, distr);
//			double sLMask = getPvalueMask(pValueSL);
			mean = StatUtils.mean(distr);
			double sLMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdTLAsArray();
//			double pValueTL = tTest.tTest(mu, distr);
//			double tLMask = getPvalueMask(pValueTL);
			mean = StatUtils.mean(distr);
			double tLMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdTLTDAsArray();
//			double pValueTLTD = tTest.tTest(mu, distr);
//			double tLTDMask = getPvalueMask(pValueTLTD);
			mean = StatUtils.mean(distr);
			double tLTDMask = getMeanMask(mean);
			
			distr = nodeBGDistributions.get(i).getdTLFDAsArray();
//			double pValueTLFD = tTest.tTest(mu, distr);
//			double tLFDMask = getPvalueMask(pValueTLFD);
			mean = StatUtils.mean(distr);
			double tLFDMask = getMeanMask(mean);
			
			bgMask.put(i, new double[]{dMask,tMask,tTDMask,tFDMask,sLMask,tLMask,tLTDMask,tLFDMask});
			System.out.println(i+":"+dMask+","+
									tMask+","+
									tTDMask+","+
									tFDMask+","+
									sLMask+","+
									tLMask+","+
									tLTDMask+","+
									tLFDMask
									);
		}
		return bgMask;
	}
	
    public static double similarityTreeNodes(TreeNode tn1, TreeNode tn2) {
    	List<Integer> leafSet1 = ((TreeInternalNode)tn1).getLeafSet();
    	List<Integer> leafSet2 = ((TreeInternalNode)tn2).getLeafSet();
    	return (double) Maths.intersection(leafSet1, leafSet2).size()
    			/Maths.union(leafSet1, leafSet2).size();
    }
	
	// On matching nodes between trees, 2 Preliminaries : http://www.hpl.hp.com/techreports/2003/HPL-2003-67.pdf
	/**
	 * Get a mapping between the internal nodes IDs from the given
	 * treeFilePath and the internal nodes IDs coming from the given 
	 * referenceTreeFilePath.
	 * 
	 * Verified manually
	 * 
	 * @param 	referenceTreeFilePath
	 * 			File path of the reference tree which the one that 
	 * 			has the biggest number of leaves
	 * @param 	treeFilePath
	 * 			File path of the tree
	 * @return
	 */
    public static HashMap<Integer,Integer> buildTreeMapping(String referenceTreeFilePath, String treeFilePath) {
		HashMap<Integer,Integer> mapping = new HashMap<Integer,Integer>();
		Tree referenceTree, tree;
		try {
			referenceTree = new Tree().parseFromNewickFile(referenceTreeFilePath);
			// Define the leaf sets for each internal node of the reference tree
			referenceTree.setEndingLeaves();
			tree = new Tree().parseFromNewickFile(treeFilePath);
			// Define the leaf sets for each internal node of the reference tree
			tree.setEndingLeaves();
			List<TreeNode> treeInternalNodes = tree.getInternalNodes();
			List<TreeNode> referenceTreeInternalNodes = referenceTree.getInternalNodes();
			for(TreeNode tin: treeInternalNodes) {
				double maxScore = 0.0;
				TreeNode tnMaxScore = null;
				for(TreeNode ftin: referenceTreeInternalNodes) {
					/*
					 * Compute the similarity (proposed by Stinebrickner) 
					 * measure between 2 nodes of 2 labeled trees. The 2 
					 * trees do not have to have the same set of leaves
					 */
					double stn = similarityTreeNodes(tin,ftin);
					if(tnMaxScore == null || stn > maxScore) {
						tnMaxScore = ftin;
						maxScore = stn;
					}
				}
				int treeNodeId = Integer.parseInt(((TreeInternalNode)tin).getLabel());
				int referenceNodeTreeId = tnMaxScore.getId();
				mapping.put(treeNodeId, referenceNodeTreeId);
//				System.out.println(treeNodeId+"<->"+referenceNodeTreeId);
				// Remove the one that got the highest score
				referenceTreeInternalNodes.remove(tnMaxScore);
			}
			// Add the leaf nodes IDs
			List<TreeNode> treeLeafNodes = tree.getLeaves();
			for(TreeNode tn: treeLeafNodes) {
				int id = Integer.parseInt(((TreeLeafNode)tn).getName());
				mapping.put(id, id);
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapping;
	}
	
//	/**
//	 * Build a mapping between a relative id for each node and a absolute
//	 * node id computed as sum of all its children node ids added to its node id.
//	 * @param treeFilePath
//	 * @return
//	 */
//	public static HashMap<Integer,Integer> buildEEPMapping(String treeFilePath) {
//		Tree tree = null;
//		try {
//			tree = new Tree().parseFromNewickFile(treeFilePath);
//			List<TreeNode> treeLeaves= tree.getLeaves();
//			// Take a random leaf of a tree (e.g.: first one)
//			while(!treeLeaves.isEmpty()) {
//				TreeNode l = (TreeNode) treeLeaves.get(0);
//				// Go up until root
//				TreeNode lParent = l.getParent();
//				int leafId = Integer.parseInt(((TreeLeafNode)l).getName());
//				lParent.addNuid(leafId);
//				while(lParent != null) {
//					lParent = lParent.getParent();
//					if(lParent != null)
//						lParent.addNuid(leafId);
//				}
//				treeLeaves.remove(l);
//			}
//		} catch (URISyntaxException | IOException e) {
//			e.printStackTrace();
//		}
//		
//		HashMap<Integer,Integer> eepMapping = new HashMap<Integer,Integer>();
//		for(TreeNode tn: tree.getNodes()) {
//			if(tn instanceof TreeLeafNode) {
//				int id = Integer.parseInt(((TreeLeafNode) tn).getName());
////				System.out.println(tn.getNuid()+"<->"+id);
//				eepMapping.put(id, tn.getNuid());
//			} else {
//				int id = Integer.parseInt(((TreeInternalNode) tn).getLabel());
////				System.out.println(tn.getNuid()+"<->"+id);
//				eepMapping.put(id, tn.getNuid());
//			}
//		}
//		return eepMapping;
//	}
	
    /**
     * Build all Protein-protein Evolutionary Event Matrices from data in given dataDirectory
     * 
     * @param dataDirectory
     */
	public static void buildAllPPEEMs(String dataDirectory) {
		// Iterate over files: http://stackoverflow.com/questions/4917326/how-to-iterate-over-the-files-of-a-certain-directory-in-java
		File dir = new File(dataDirectory);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String[] prots = child.getName().split("_");
				String protAMr = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mr1_symmetric.txt";
				String protBMr = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_mr1_symmetric.txt";
				String protAStree = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_1.stree";
				String protBStree = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_1.stree";
				String outA = child.getPath()+File.separator+"reconciled"+File.separator+prots[0]+"_mp2";
				String outB = child.getPath()+File.separator+"reconciled"+File.separator+prots[1]+"_mp2";
				buildPPEEMs(prots,protAMr,protAStree,outA,protBMr,protBStree,outB);
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
	}
	
	public static void buildPPEEMs(String[] prots, String protAMr, String protAStree, String outA, String protBMr, String protBStree, String outB) {
		new Main2(prots[0],protAStree,protAMr,outA,"023456789").buildEEMatrix();
		new Main2(prots[1],protBStree,protBMr,outB,"023456789").buildEEMatrix();
		System.out.println("Done for "+prots[0]+"<->"+prots[1]);
	}
		
	public static void buildEEPMapping(String treeAFilePath, String treeBFilePath) {
		LinkedHashMap<String,TreeNode> nodesAMapped = new LinkedHashMap<String,TreeNode>();
		LinkedHashMap<String,TreeNode> nodesBMapped = new LinkedHashMap<String,TreeNode>();
		try {
			Tree treeA = new Tree().parseFromNewickFile(treeAFilePath);
			Tree treeB = new Tree().parseFromNewickFile(treeBFilePath);
			List<TreeNode> treeALeaves= treeA.getLeaves();
			List<TreeNode> treeBLeaves = treeB.getLeaves();
			if(treeALeaves.size() != treeBLeaves.size())
				throw new IllegalArgumentException("The 2 trees must have the same number of leaves!");
			// Take a random leaf of a tree (e.g.: first one)
			while(!treeALeaves.isEmpty()) {
				TreeNode lA = (TreeNode) treeALeaves.get(0);
				nodesAMapped.put(((TreeLeafNode) lA).getName(),lA); 
				// Search in tree B the same leaf as the one picked in tree A
				TreeNode lB = treeB.getLeafByName(((TreeLeafNode)lA).getName());
				nodesBMapped.put(((TreeLeafNode) lB).getName(),lB);
				// Go up until root
				TreeNode lAParent = lA.getParent();
				TreeNode lBParent = lB.getParent();
				while(lAParent != null && lBParent != null) {
					String lAParentName = ((TreeInternalNode)lAParent).getLabel();
					String lBParentName = ((TreeInternalNode)lBParent).getLabel();
					// If internal tree node already contained then go next leaf
					if(nodesAMapped.containsKey(lAParentName) && nodesBMapped.containsKey(lBParentName))
						break;
					nodesAMapped.put(lAParentName,lAParent); 
					nodesBMapped.put(lBParentName,lBParent);
					lAParent = lAParent.getParent();
					lBParent = lBParent.getParent();
				}
				treeALeaves.remove(lA);
				treeBLeaves.remove(lB);
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("# nodes in tree A: "+nodesAMapped.size());
		System.out.println("# nodes in tree B: "+nodesBMapped.size());
		
		for(TreeNode tn: nodesBMapped.values()) {
			if(tn instanceof TreeLeafNode) 
				System.out.println(((TreeLeafNode) tn).getName());
			if(tn instanceof TreeInternalNode) 
				System.out.println(((TreeInternalNode) tn).getLabel());
		}
	}
	
	public void buildEEMatrix() {
		ArrayList<String> speciesTreeNodeIds = null;
		try {
			speciesTreeNodeIds = new Tree().parseFromNewickFile(speciesTreeFilePath).getNodesLabels();
		} catch (URISyntaxException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Sort ArrayList of Integer : http://beginnersbook.com/2013/12/how-to-sort-arraylist-in-java/
//		Collections.sort(speciesTreeNodeIds); // Don't need to sort them (After having checked)
		for(String nodeId: speciesTreeNodeIds) {
			
			NodeEEP2 nodeEEP = new NodeEEP2();
			nodeEEP.setNid(nodeId);
			treeEEP.add(nodeEEP);
			/*
			 * Evolutionary event considered with either the source node
			 * or the target node
			 */
			ArrayList<Edge> edges = medianDTLGraph.getEdgeListBySpeciesId2(nodeId);
			for(Edge e: edges) {
//				System.out.println(e);
				// Enum with switch : http://alvinalexander.com/java/using-java-enum-switch-tutorial
				switch(e.getEventType()) {
					case D: nodeEEP.setD(e.getEventSupport()); break;
					case S: nodeEEP.setS(e.getEventSupport()); break;
					case T: nodeEEP.setT(e.getEventSupport()); break;
					case TTD: nodeEEP.setTTD(e.getEventSupport()); break;
					case TFD: nodeEEP.setTFD(e.getEventSupport()); break;
					case SL: nodeEEP.setSL(e.getEventSupport()); break;
					case TL: nodeEEP.setTL(e.getEventSupport()); break;
					case TLFD: nodeEEP.setTLFD(e.getEventSupport()); break;
					case TLTD: nodeEEP.setTLTD(e.getEventSupport()); break;
					default:
						break;
				}
			}
		}
		TreeEEPWriter2.writeCsvFile(treeEEP, outputFileName+"_eem.txt",selectedColumns);
		println("Done for: "+proteinId);
	}
	
	public void buildMedianDTLGraph() {
		// TODO Auto-generated method stub
		// Read file line by line : http://www.programcreek.com/2011/03/java-read-a-file-line-by-line-code-example/
		Path filePath = Paths.get(ecceTERAMedianReconciliationFilePath);
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
		    String line = null;
		    int i = 0;
		    while ((line = reader.readLine()) != null) {
		    	// First line is the root
		    	Parser p = new Parser(line);
		        if(i==0) {
		        	EventSplit rootEventSplit = p.getLeftEventSplit();
		        	addSplitEvent(p,rootEventSplit,false);
		        } else {
		        	// Manage the <leftEventSplit> 
		        	// Check that is the one that led to the current clade
		        	EventSplit leftEventSplit = p.getLeftEventSplit();
		        	Node parentNode = medianDTLGraph.getNodeByCladeAndSpeciesId(p.getCladeId(), leftEventSplit.getRightId());
		        	if(parentNode == null) {
		        		parentNode = medianDTLGraph.getNodeByCladeAndSpeciesId(p.getCladeId(), leftEventSplit.getLeftId());
		        		if(!parentNode.getSpeciesId().equals(leftEventSplit.getLeftId()))
		        			throw new IllegalArgumentException("Hierarchical arborency is incorrect: "+parentNode.getSpeciesId()+"><"+leftEventSplit.getLeftId()+"!");
		        	} else {
		        		if(!parentNode.getSpeciesId().equals(leftEventSplit.getRightId()))
		        			throw new IllegalArgumentException("Hierarchical arborency is incorrect: "+parentNode.getSpeciesId()+"><"+leftEventSplit.getRightId()+"!");
		        	}
//		        	System.out.println(parentNode+" > ("+p.cladeId+","+leftEventSplit.getLeftId()+")");
//		        	System.out.println(leftEventSplit);
		        	
		        	// Manage the <noSplitEvents>
		        	if(p.getNoEventSplits().size() > 0) {
		        		for(EventSplit es: p.getNoEventSplits()) {
		        			if(es.getEventType() == EventType.SL) {
		        				addSLEvent(p,es);
		        			} else {
		        				addNoSplitEvent(p,es);
		        			}
		        		}
		        	}
		        	
		        	// Manage the <rightSplitEvent> if there is one
		        	EventSplit rightEventSplit = p.getRightEventSplit();
		        	if(rightEventSplit != null)
		        		addSplitEvent(p,rightEventSplit,false);
		        }
		        i++;
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	public void addNoSplitEvent(Parser p, EventSplit eventSplit) {
    	Node sourceNode = medianDTLGraph.getNode(p.getCladeId(), eventSplit.getLeftId());
    	Node targetNode = medianDTLGraph.getNode(p.getCladeId(), eventSplit.getRightId());
    	addMonoDirectedEvent(eventSplit,sourceNode,targetNode);
	}
	
	public void addMonoDirectedEvent(EventSplit eventSplit, Node sourceNode, Node targetNode) {
    	medianDTLGraph.addNode(sourceNode);
    	targetNode.setParent(sourceNode);
    	medianDTLGraph.addNode(targetNode);
    	Edge edge = new Edge(sourceNode,targetNode,eventSplit.getEventType(),eventSplit.getEventSupport());
    	medianDTLGraph.addEdge(edge);
	}
	
	public void addSLEvent(Parser p, EventSplit eventSplit) {
    	Node parentNode = medianDTLGraph.getNode(p.getCladeId(), eventSplit.getSpeciesId());
    	Node leftNode = medianDTLGraph.getNode("0", eventSplit.getLeftId()); // getLeftId = xLost
    	Node rightNode = medianDTLGraph.getNode(p.getCladeIdRight(), eventSplit.getRightId()); // getRightId = xKept
    	addBiFurcatingDirectedEvent(eventSplit,parentNode,leftNode,rightNode);
	}
	
	/**
	 * Add Duplication or Speciation Event
	 * @param p
	 * @param eventSplit
	 */
	public void addSplitEvent(Parser p, EventSplit eventSplit, boolean isRoot) {
    	Node parentNode = medianDTLGraph.getNode(p.getCladeId(), eventSplit.getSpeciesId());
    	if(isRoot)
    		medianDTLGraph.setRoot(parentNode);
    	Node leftNode = medianDTLGraph.getNode(p.getCladeIdLeft(), eventSplit.getLeftId());
    	Node rightNode = medianDTLGraph.getNode(p.getCladeIdRight(), eventSplit.getRightId());
    	addBiFurcatingDirectedEvent(eventSplit,parentNode,leftNode,rightNode);
	}
	
	public void addBiFurcatingDirectedEvent(EventSplit rootEventSplit, Node parentNode, Node leftNode, Node rightNode) {
    	medianDTLGraph.addNode(parentNode);
    	leftNode.setParent(parentNode);
    	medianDTLGraph.addNode(leftNode);
    	rightNode.setParent(parentNode);
    	medianDTLGraph.addNode(rightNode);
    	Edge leftEdge = new Edge(parentNode,leftNode,rootEventSplit.getEventType(),rootEventSplit.getEventSupport());
    	Edge rightEdge = new Edge(parentNode,rightNode,rootEventSplit.getEventType(),rootEventSplit.getEventSupport());
    	medianDTLGraph.addEdge(leftEdge);
    	medianDTLGraph.addEdge(rightEdge);
	}
	
	public static void println(String s) {
		System.out.println(s);
	}

}
