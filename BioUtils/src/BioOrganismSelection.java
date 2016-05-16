import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import math.DistanceVector;
import math.Maths;
import phy.newickparser.Tree;
import phy.newickparser.TreeLeafNode;
import phy.newickparser.TreeNode;
import phy.newickparser.TreePath;

public class BioOrganismSelection {
	
	public BioOrganismSelection() {
		try {
//			tree = new Tree().parseFromNewickFile("src/phy/newickparser/A7ZRJ5.sdd.gtrees");
			tree = new Tree().parseFromNewickFile("src/RAxML_green_genes_export_1.tree.txt");
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		tree.setEndingLeaves();
		tree.computeDistanceMatrix();
		List<String> a = this.rankPD(-1);
		String filePath = "C:/Users/Max/Desktop/ThesisWork/test/organism-selection/results/raxml/organisms/";
		writeRankedLeaves(a,filePath+"/org_list-0.0.txt");
		for(double pd: pds) {
			System.out.println(pd);
			String fileName = "org_list-"+pd+".txt";
			String fP = filePath+fileName;
			List<String> r = this.rankPD(pd);
//			System.out.println(r.size());
			writeRankedLeaves(r,fP);		
		}
	}
	
	Tree tree;
	TreePath maxLeafLeafTreePath;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		new BioOrganismSelection();
		String dirPath = "C:/Users/Max/Desktop/ThesisWork/test/organism-selection/results/raxml/";
//		buildTable(dirPath);
	}
	
	public static void buildTable(String dirPath) {
		String orgsDirPath = dirPath+"trees/";
		File dir = new File(orgsDirPath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			String outFilePath = dirPath+"pd_threshold_nb_species_nb_polytomies.txt";
			File outFile = new File(outFilePath);
			try {
				outFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (File child : directoryListing) {
				if(child.isDirectory())
					continue;
				String threshold = child.getName().split("\\.txt")[0].split("-")[1];
				Tree tree = null;
				try {
					tree = new Tree().parseFromNewickFile(child.getPath());
				} catch (URISyntaxException | IOException e1) {
					e1.printStackTrace();
				}
				tree.setEndingLeaves();
				int nbLeaves = tree.getLeaves().size();
				int nbPolytomies = tree.getNumberPolytomies();
				String line = threshold+"\t"+nbLeaves+"\t"+nbPolytomies+"\n";
				try {
					Files.write(outFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
				}catch (IOException e) {
					//exception handling left as an exercise for the reader
				}
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
	}
		
	public void setDynLeaves(List<TreeNode> dynLeaves) {
		this.dynLeaves = dynLeaves;
	}
	
	// Write List to file : http://stackoverflow.com/questions/6548157/how-to-write-an-arraylist-of-strings-into-a-text-file
	public void writeRankedLeaves(List<String> arr,String filePath) {
		FileWriter writer;
		try {
			writer = new FileWriter(filePath);
			for(String str: arr) {
				writer.write(str+"\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	List<TreeNode> dynLeaves;
	List<Double> pds = new ArrayList<Double>();
	
	public List<String> rankPD(double pDThreshold) {
		this.setDynLeaves(tree.getLeaves());
		DistanceVector dv = tree.getDistanceMatrix().toVector().sortByValue();
		String[] maxTreePathLeaves = dv.firstEntry().getKey().split("_");
		maxLeafLeafTreePath = tree.getLeafLeafTreePath(maxTreePathLeaves[0], maxTreePathLeaves[1]);
		setBackBone(maxLeafLeafTreePath);
		
		List<String> rankedLeavesMaxPD = new ArrayList<String>();
//		double maxPD = backbone.getTotalBranchLength();
		while(dynLeaves.size() > 0) {
			TreePath maxPDTreePath = this.nextLeafMaximizingPD();
			TreeLeafNode nextLeafMaxPD = (TreeLeafNode) maxPDTreePath.getB();
			double pd = maxPDTreePath.getTotalBranchLength();
			if(pd < pDThreshold) {
				break;
			} else {
				if(pDThreshold == -1) {
					pds.add(Maths.round(pd,5));
				}
				rankedLeavesMaxPD.add(nextLeafMaxPD.getName());
//				System.out.println(dynLeaves.size()+":"+nextLeafMaxPD.getName()+" > "+normalizedPD);
//				System.out.println(nextLeafMaxPD.getName());
			}
			dynLeaves.remove(maxPDTreePath.getB());
		}	
		return rankedLeavesMaxPD;
	}
	
	public TreePath nextLeafMaximizingPD() {
		TreePath maxPDTreePath = null;
		double maxDistance = 0;
		for(TreeNode tn: this.dynLeaves) {
			double distance = tn.getBranchLength();
			TreePath tmp = new TreePath(tn);
			TreeNode parent = tn.getParent();
			while(parent != null) {
				// Don't add node branch length present in backbone
				if(backbone.hasTreeNode(parent)) { // Had a big error: tn instead of parent
					break;
				}
				distance += parent.getBranchLength();
				tmp.addInternalNode(parent);
				parent = parent.getParent();
			}
			if(tmp.getTotalBranchLength() > maxDistance) {
				maxDistance = distance;
				maxPDTreePath = tmp;
			}
//			System.out.println(maxDistance);
		}
		/*
		 * Add the nodes of the TreePath that maximizes the Phylogenetic Diversity
		 * to the backbone
		 */
		this.addBackBoneTreePath(maxPDTreePath);
		return maxPDTreePath;
	}
	
	public void addBackBoneTreePath(TreePath tp) {
		for(TreeNode tn: tp.getNodes()) {
			backbone.addInternalNode(tn);
		}
	}
	
	public TreePath getBackBone() {
		return this.backbone;
	}
	
	public void setBackBone(TreePath treePath) {
		this.backbone = treePath;
	}
	
	TreePath backbone;

}
