package phy.newickparser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealVector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import csv.ReadCSV;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.WeightedLevenshtein;
import math.DistanceMatrix;

/**
 * 
 * @author Max
 *
 */
public class Tree {
	
	private String newickString;
	
	/*
	 * - GeLL jar not working well
	 * 		- GeLL project : http://phylo.bio.ku.edu/GeLL/
			- http://phylo.bio.ku.edu/GeLL/javadoc/Trees/Tree.html
	 * - JavaCC Newick parser not working well (https://bitbucket.org/djiao/a-lightweight-javacc-newick-parser/)
	 * - APE R package too slow for big tree (1000 leaves)
	 */
	public Tree() {	
		this.setNodes(new ArrayList<TreeNode>());
	}
	
	public static void main(String[] args) {
//		test(null);
		Tree tree = null;
		try {
//			System.out.println(tree.getOverallBootstrapSupport());
//			System.out.println(tree.getNumberPolytomies());

			/*
			 * MAPPING
			 */
			tree = new Tree().parseFromNewickFile("src/P0ABB0_P0ABB4_uniq_common.stree.sdd.res.nwk");
			tree.setEndingLeaves();
			System.out.println(tree.getNumberPolytomies());
			
			
//			HashMap<String, String> mapping = ReadCSV.readMapping("src/P0ABB0_P0ABB4_uniq_species.mapping.txt", "\t", false);
//			tree.setLeafMapping(mapping);
//			Tree treeMapped = tree.mapLeaves();
//			System.out.println(treeMapped);
			
//			System.out.println(resolvedTree.getNumberPolytomies());
//			System.out.println(StringUtils.join(tree.getLeafLabels(),"+OR+"));
//			System.out.println(((TreeInternalNode)tree.getRoot()).getBranchLength());
//			System.out.println((TreeInternalNode)tree.getRoot());
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Tree parseFromNewickString(String newickString) {
		Tree tmp = new Tree();
		tmp.newickString = newickString;
		return tmp.parseFromNewickString();
	}
	
	public Tree parseFromNewickString() {
		if(this.newickString.charAt(0) != '(') {
			throw new IllegalArgumentException("Bad tree coding: does not start with a bracket!");
		}
		// Convert Phylip to Newick
		this.newickString = newickString.replaceAll("(\r|\n|\r\n)+", "");
		/*
		 *  Why use StringTokenizer instead of split :
		 *  - http://stackoverflow.com/questions/19677919/difference-between-using-stringtokenizer-and-string-split
		 *  - http://www.javamex.com/tutorials/regular_expressions/splitting_tokenisation_performance.shtml#.VuszpHocSJ8
		 */
		int nodeCount = 0;
		// Read each char : http://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
		// Exit nested loops: stackoverflow.com/questions/6638321/how-to-exit-two-nested-loops
		main:
		for (int i = 0; i < this.newickString.length(); i++){
		    char c = this.newickString.charAt(i); 
//		    System.out.println(c);
			switch(c) {
				// Create a new InternalTreeNode
		    	case '(':
		    		TreeInternalNode tmpNode = new TreeInternalNode(nodeCount);
		    		nodeCount++;
		    		if(i == 0) {
		    			tmpNode.setRoot(true);
		    			this.setRoot(tmpNode);
		    		} else {
			    		// Handle the case when ( in the label of the leaf
			    		int j = i;
			    		char g = this.newickString.charAt(j); 
			    		while(g != '(') {
			    			g = this.newickString.charAt(j); 
			    			j--;
			    		}
			    		// Before n parenthesis there need to be a comma 
			    		if(g == ',')
			    			continue;
		    			tmpNode.setParent(this.getLastUnCompleteTreeNode());
		    			((TreeInternalNode) tmpNode.getParent()).addChildrenNode(tmpNode);
		    		}
		    		nodes.add(tmpNode);
		    		break;
		    	// Save the branch length in the ending TreeNode
		    	case ':':
		    		i = i+1;
		    		// Mutable and immutable String : http://stackoverflow.com/questions/25138587/what-is-difference-between-mutable-and-immutable-string-in-java
		    		StringBuffer bl = new StringBuffer(); 
		    		char d = this.newickString.charAt(i);
		    		while(d != ')' && d != ',') {
		    			bl.append(d);
		    			i++;
		    			d = this.newickString.charAt(i);
		    		}
		    		this.getLastTreeNode().setBranchLength(Double.parseDouble(bl.toString()));
		    		i--;
		    	case ',': break;
		    	case ')':
		    		TreeNode cladeNode = this.getLastUnCompleteTreeNode();
		    		cladeNode.setComplete(true);
		    		i = i+1;
		    		char e = this.newickString.charAt(i);
		    		if(e == ';') {
//		    			System.out.println("END OF THE FILE");
		    			break main;
		    		}
		    		if(e != ')' && e != ',') {
		    			StringBuffer inf = new StringBuffer(); 
		    			while(e != ';') {
		    				/*
		    				 * It can be that in some nodes names there are
		    				 * special characters like ,;)(; common to the Newick
		    				 * format. If this the case the name should be 
		    				 * surrounded by ' or " quotation marks.
		    				 * Here we check first if it starts with a quotation
		    				 * mark, if it's the case append characters until
		    				 * it ends with the same quotation mark and the last
		    				 * index of quotation mark is 0 and that the current
		    				 * character is a colon :. Then, continue to append
		    				 * characters with a while loop until one of the 
		    				 * following characters appears : ,; or ). 
		    				 */
		    				if(inf.toString().startsWith("'")) {
		    					if(inf.toString().endsWith("'")
		    							&& inf.toString().lastIndexOf("'") > 0
		    							&& e == ':') {
		    						while(e != ',' && e != ';' && e != ')' ) {
		    							inf.append(e);
		    			    			i++;
		    			    			e = this.newickString.charAt(i);
		    						}
		    						break;
		    					}
		    				} else {
		    					if(e == ')' || e == ',' || e == ';')
		    						break;
		    				}
			    			inf.append(e);
			    			i++;
			    			e = this.newickString.charAt(i);
			    		}
//		    			System.out.println(">>>"+inf.toString());
		    			/*
		    			 * Check if the information is either a label or
		    			 * branch support plus branch length
		    			 */
		    			int f = inf.indexOf(":");
		    			if(f > -1) {
		    				String[] s = inf.toString().split(":");
		    				if(f == 0) {
			    				double brl = Double.parseDouble(s[1]);
			    				((TreeInternalNode) cladeNode).setBranchLength(brl);
		    				} else {
		    					((TreeInternalNode) cladeNode).setLabel(s[0]);
			    				double brl = Double.parseDouble(s[1]);
			    				((TreeInternalNode) cladeNode).setBranchLength(brl);
		    				}
		    			} else {
		    				String t = inf.toString();
			    			((TreeInternalNode) cladeNode).setLabel(t);
		    			}
		    			i--;
		    		} else {
		    			i--;
		    		}
		    		break;
		    	case ';':
	    			System.out.println("Tree parsed!");
	    			break main;
		    	default:
		    		StringBuffer name = new StringBuffer(); 
		    		i--;
		    		char h = this.newickString.charAt(i);
		    		if(h == '(' || h == ',') {
		    			i++;
		    			h = this.newickString.charAt(i);
		    			while(true) {
		    				if(name.toString().startsWith("'")) {
		    					if(name.toString().endsWith("'")
		    							&& name.toString().lastIndexOf('\'') != 0
		    							&& (h == ',' || h == ':'))
		    						break;
		    				} else if(name.toString().startsWith("\"")) {
		    					if(name.toString().endsWith("\"")
		    							&& name.toString().lastIndexOf('"') != 0
		    							&& (h == ',' || h == ':'))
		    						break;
		    				} else {
		    					if(h == ',' || h == ':')
		    						break;
		    				}
		    				name.append(h);
			    			i++;
			    			h = this.newickString.charAt(i);
		    			}
			    		TreeLeafNode leafNode = new TreeLeafNode(nodeCount,name.toString());
			    		leafNode.setParent(this.getLastUnCompleteTreeNode());
			    		((TreeInternalNode) leafNode.getParent()).addChildrenNode(leafNode);
			    		nodes.add(leafNode);
			    		nodeCount++;
		    			i--;
//		    			System.out.println(name);
		    		}
		    		break;
		    }
		}
		return this;
	}
	
	public Tree parseFromNewickFile(String filePath) throws URISyntaxException, IOException {
		// Path object from file : http://stackoverflow.com/questions/15512200/get-path-object-from-file
		Path newickFilePath = new File(filePath).toPath();
		// Read plain text in Java
		this.newickString = new String(Files.readAllBytes(newickFilePath)); 
		return parseFromNewickString();
	}
	
	/**
	 * Get the leaf labels of this Tree
	 * 
	 * @return
	 */
	public List<String> getLeafLabels() {
		List<String> leaves = new ArrayList<String>();
		for(TreeNode tn: nodes) {
			if(tn instanceof TreeLeafNode) {
				String name = ((TreeLeafNode) tn).getName();
				if(name.startsWith("'") && 
						name.endsWith("'")) {
					name = ((TreeLeafNode) tn).getName();
					name = name.substring(1,name.length()-1);
				}
				leaves.add(name);
			}
		}
		return leaves;
	}
	
	/**
	 * Get the leaf TreeNode given its name
	 * 
	 * @param 	name
	 * 			Name of the leaf
	 * @return
	 */
	public TreeNode getLeafByName(String name) {
		for(TreeNode tn: nodes) {
			if(tn instanceof TreeLeafNode) {
				if(((TreeLeafNode) tn).getName().equals(name))
					return tn;
			}
		}
		return null;
	}
	
	/**
	 * Get a TreeNode list of all the leaves in this Tree
	 * @return
	 */
	public List<TreeNode> getLeaves() {
		List<TreeNode> leaves = new ArrayList<TreeNode>();
		for(TreeNode tn: nodes) {
			if(tn instanceof TreeLeafNode) {
				leaves.add(tn);
			}
		}
		return leaves;
	}
	
	public List<TreeNode> getInternalNodes() {
		List<TreeNode> internalNodes = new ArrayList<TreeNode>();
		for(TreeNode tn: nodes) {
			if(tn instanceof TreeInternalNode) {
				internalNodes.add(tn);
			}
		}
		return internalNodes;
	}
	
	public TreeNode getLastUnCompleteTreeNode() {
		for(int i = nodes.size()-1; i>-1; i--) {
			if(!nodes.get(i).isComplete()) {
				return nodes.get(i);
			}
		}
		return null;
	}
	
	public TreeNode getLastTreeNode() {
		return nodes.get(nodes.size()-1);
	}
	
	public ArrayList<String> getNodesLabels() {
		ArrayList<String> tmp = new ArrayList<String>();
		int nbNodes = 0;
		for(TreeNode tn: nodes) {
			nbNodes++;
			if(tn instanceof TreeLeafNode)
				tmp.add(((TreeLeafNode) tn).getName());
			if(tn instanceof TreeInternalNode) {
//				System.out.println(((TreeInternalNode) tn).getLabel());
				tmp.add(((TreeInternalNode) tn).getLabel());
			}
//			System.out.println("Internal:"+i);
		}
		System.out.println(nbNodes);
		return tmp;
	}

	public List<TreeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
		
	private List<TreeNode> nodes;
	
	public TreePath getLeafLeafTreePath(String leafNameA, String leafNameB) {
		TreePath leafATreePath = this.treePaths.get(leafNameA);
		TreePath leafBTreePath = this.treePaths.get(leafNameB);
		return leafATreePath.getNonOverlappingTreePath(leafBTreePath);
	}
	
	public void setEndingLeaves() {
		System.out.println("Setting up the ending leaves...");
		List<TreeNode> leaves = this.getLeaves();
		for(TreeNode leaf: leaves) {
			TreeNode parent = leaf.getParent();
			TreePath path = new TreePath(leaf);
			while(parent != null) {
				((TreeInternalNode) parent).addEndLeaf(((TreeLeafNode)leaf).getId(), path);
				// Add internal node at first position
				path.addInternalNode(0,parent);
				parent = parent.getParent();
			}
			treePaths.put(((TreeLeafNode)leaf).getName(), path);
		}
		System.out.println("Setting up the ending leaves...Done");
	}
	
	/**
	 * Paths from every leaf until the root
	 */
	HashMap<String,TreePath> treePaths = new HashMap<String,TreePath>();
	
	/*
	 * Verified distances compared with APE package and
	 * A7RJ5.sdd.gtrees
	 */
	public void computeDistanceMatrix() {
		List<TreeNode> leaves = this.getLeaves();
		for(int i=0; i<leaves.size(); i++) {
			for(int j=0; j<i; j++) {
				String leafALabel = ((TreeLeafNode)leaves.get(i)).getName();	
				String leafBLabel = ((TreeLeafNode)leaves.get(j)).getName();
				TreePath treePathLeafA = treePaths.get(leafALabel);
				TreePath treePathLeafB = treePaths.get(leafBLabel);
				double distance = treePathLeafA.getNonOverlappingDistance(treePathLeafB);
				distanceMatrix.addDistance(leafALabel, leafBLabel, distance);
//				System.out.println(leafALabel+"<-"+distance+"->"+leafBLabel);
			}	
		}
	}
	
	public DistanceMatrix getDistanceMatrix() {
		return distanceMatrix;
	}
	
	private DistanceMatrix distanceMatrix = new DistanceMatrix();
	
	public int getNumberPolytomies() {
		int numberPolytomies = 0;
		for(TreeNode tn: this.getInternalNodes()) {
			if(tn instanceof TreeInternalNode) {
				if(((TreeInternalNode) tn).hasPolytomy()) {
					numberPolytomies++;
				}
			}
		}
		return numberPolytomies;
	}
	
	int numberPolytomies;
	
	public void setRoot(TreeNode tn) {
		this.rootNode = tn;
	}
	
	public TreeNode getRoot() {
		return rootNode;
	}
	
	TreeNode rootNode;
	
	public double getOverallBootstrapSupport() {
		double overallBootstrapSupport = 0;
		for(TreeNode tn: this.getInternalNodes()) {
			TreeInternalNode tin = ((TreeInternalNode) tn);
			overallBootstrapSupport += tin.getBranchSupport();
		}
		return overallBootstrapSupport/this.getInternalNodes().size();
	}
	
	public Tree resolve() {
		for(TreeNode tn: getInternalNodes()) {
			if(((TreeInternalNode) tn).hasPolytomy()) {
//				System.out.println("Poly");
				((TreeInternalNode) tn).resolve();
			}
		}
		return this;
	}
	
	public double compareStrings(String stringA, String stringB) {
	    return StringUtils.getJaroWinklerDistance(stringA, stringB);
	    // https://github.com/tdebatty/java-string-similarity
//		Jaccard ji = new Jaccard();
//		return ji.distance(stringA, stringB);
//		NGram ng = new NGram();
//		return ng.distance(stringA, stringB);
	}
	
	public String getBestMapping(String s) {
		double maxScore = 0;
		String bestMapping = null;
		for(String k: mapping.keySet()) {
			double score = 0;
//			String p[] = k.split(" ");
//			String o[] = s.split(" ");
//			for(String l: p) {
//				for(String m: o) {
//					if(l.equals(m)) 
//						score += 1;
//				}
//			}
			score = /*((score/p.length)/o.length + */this.compareStrings(k, s);//)/2;
			if(score > maxScore) {
				maxScore = score;
				bestMapping = k;
			}
		}
//		System.out.println(maxScore);
		if(maxScore < 0.95) {
			return null;
		}
		return bestMapping;
	}
	
	public static HashMap<String,String> sddNCBI(ArrayList<String> remainders) {
		HashMap<String,String> translator = new HashMap<String,String>();
		String r = StringUtils.join(remainders, "+OR+");
//		System.out.println(r);
		// or if you prefer DOM:
		// Read XML on the web : http://stackoverflow.com/questions/2310139/how-to-read-xml-response-from-a-url-in-java
		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=genome&term="+r+"&retmax="+remainders.size();
		url = url.replace(" ", "%20");
//		System.out.println(url);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL(url).openStream());
			Element root = doc.getDocumentElement();
			NodeList rootNodes = root.getChildNodes();
			for (int i = 0; i<rootNodes.getLength(); i++) {
				Node nNode = rootNodes.item(i);
			    if(nNode.getNodeType() == Node.ELEMENT_NODE
			    		&& nNode.getNodeName().equals("TranslationSet")) {
			    	NodeList tNodes = nNode.getChildNodes();
			    	for (int j = 0; j<tNodes.getLength(); j++) {
						Node nnNode = tNodes.item(j);
						if(nnNode.getNodeType() == Node.ELEMENT_NODE
					    		&& nnNode.getNodeName().equals("Translation")) {
							String from = "", to = "";
			            	int m = 0;
			            	NodeList l = nnNode.getChildNodes();
			            	for(int k = 0; k < l.getLength(); k++) {
			            		Node nnnNode = l.item(k);
				            	if (nnnNode.getNodeType() == Node.ELEMENT_NODE) {
				            		String text = nnnNode.getTextContent();
				            		if(m == 0) {
				            			from = text;
				            			m++;
				            		} else {
				            			to = text.substring(text.indexOf('"')+1,text.lastIndexOf('"'));
				            		}
				            	}
				            }
			            	translator.put(to, from);
						}
			    	}
			    }               
			}
//	        for(String k: translator.keySet()) {
//	        	System.out.println(k+">"+translator.get(k));
//	        }
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return translator;
	}
	
	public Tree mapLeaves() {
		System.out.println("Mapping the leaves...");
		int notMappedCount = 0;
		int bestMappedCountGt1 = 0;
		for(TreeNode tn: this.getLeaves()) {
			String leafName = ((TreeLeafNode)tn).getName();
			leafName = leafName.substring(1, leafName.length()-1);
			String mappedName = mapping.get(leafName);
			// Not been able to map directly
			if(mappedName == null) {
				String bestMapping = this.getBestMapping(leafName);
				if(bestMapping != null) {
					mappedName = mapping.get(bestMapping);
					((TreeLeafNode)tn).setName(mappedName);
					mapping.remove(bestMapping);
					bestMappedCountGt1++;
				} else {
				}
				notMappedCount++;
			} else {
				mapping.remove(leafName);
				((TreeLeafNode)tn).setName(mappedName);
			}
		}
		HashMap<String,String> p = sddNCBI(new ArrayList<String>(mapping.keySet()));
		ArrayList<String> notMappedAtAll = new ArrayList<String>();
		for(TreeNode tn: this.getLeaves()) {
			String leafName = ((TreeLeafNode)tn).getName();
			leafName = leafName.substring(1, leafName.length()-1);
			try {
				Integer.parseInt(leafName);
			} catch(NumberFormatException e) {
				String translatedName = p.get(leafName);
				if(translatedName==null) {
					notMappedAtAll.add(leafName);
					((TreeLeafNode)tn).setName(null);
				} else {
					String mappedName = mapping.get(translatedName);
					mapping.remove(translatedName);
					((TreeLeafNode)tn).setName(mappedName);
				}
			}
		}
		System.out.println("Not directly mapped: "+notMappedCount);
		System.out.println("Best mapped: "+bestMappedCountGt1);
		System.out.println("NCBI mapped: "+p.size());
		System.out.println("Not mapped: "+notMappedAtAll.size());
		System.out.println("-----------------------------------------------------");
		System.out.println("Undermentionned leaves will be removed from the tree:");
		for(String leafName: notMappedAtAll) {
			System.out.println("[REMOVED] "+leafName);
		}
		System.out.println("Mapping the leaves...Done");
		return this;
	}
	
	public void setLeafMapping(HashMap<String,String> mapping) {
		this.mapping = mapping;
	}
	
	HashMap<String,String> mapping;
	
	public boolean hasBranchSupport() {
		return hasBranchSupport;
	}
	
	boolean hasBranchSupport = false;
	
	public String toString() {
		return this.getRoot()+"";
	}

}
