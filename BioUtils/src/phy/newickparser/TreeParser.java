package phy.newickparser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * REMOVED USE TREE.JAVA INSTEAD
 * @author Max
 *
 */

/*
 * - GeLL jar not working well
 * - JavaCC Newick parser not working well (https://bitbucket.org/djiao/a-lightweight-javacc-newick-parser/)
 * - APE R package too slow for big tree (1000 leaves)
 */
public class TreeParser {
	
//	private String newickString;
//	private Tree tree;
//	
//	public TreeParser(String filePath) {
//	}
//
//	public Tree parseFromNewickString() {
//		/*
//		 *  Why use StringTokenizer instead of split :
//		 *  - http://stackoverflow.com/questions/19677919/difference-between-using-stringtokenizer-and-string-split
//		 *  - http://www.javamex.com/tutorials/regular_expressions/splitting_tokenisation_performance.shtml#.VuszpHocSJ8
//		 */
//		int nodeCount = 0;
//		// Read each char : http://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
//		main:
//		for (int i = 0; i < this.newickString.length(); i++){
//		    char c = this.newickString.charAt(i); 
////		    System.out.println(c);
//			switch(c) {
//				// Create a new InternalTreeNode
//		    	case '(':
//		    		TreeInternalNode tmpNode = new TreeInternalNode(nodeCount);
//		    		nodeCount++;
//		    		if(i == 0) {
//		    			tmpNode.setRoot(true);
//		    		} else {
//		    			tmpNode.setParent(tree.getLastUnCompleteTreeNode());
//		    		}
//		    		tree.getNodes().add(tmpNode);
//		    		break;
//		    	// Save the branch length in the ending TreeNode
//		    	case ':':
//		    		i = i+1;
//		    		// Mutable and immutable String : http://stackoverflow.com/questions/25138587/what-is-difference-between-mutable-and-immutable-string-in-java
//		    		StringBuffer bl = new StringBuffer(); 
//		    		char d = this.newickString.charAt(i);
//		    		while(d != ')' && d != ',') {
//		    			bl.append(d);
//		    			i++;
//		    			d = this.newickString.charAt(i);
//		    		}
////		    		System.out.println(bl);
//		    		tree.getLastTreeNode().setBranchLength(Double.parseDouble(bl.toString()));
//		    		i--;
//		    	case ',': break;
//		    	case ')':
//		    		TreeNode cladeNode = tree.getLastUnCompleteTreeNode();
//		    		cladeNode.setComplete(true);
//		    		i = i+1;
//		    		char e = this.newickString.charAt(i);
//		    		if(e == ';') {
////		    			System.out.println("END OF THE FILE");
//		    			break main;
//		    		}
//		    		if(e != ')' && e != ',') {
//		    			StringBuffer inf = new StringBuffer(); 
//		    			while(e != ')' && e != ',') {
//			    			inf.append(e);
//			    			i++;
//			    			e = this.newickString.charAt(i);
//			    		}
////		    			System.out.println(inf);
//		    			/*
//		    			 * Check if the information is either a label or
//		    			 * branch support plus branch length
//		    			 */
//		    			int f = inf.indexOf(":");
//		    			if(f > -1) {
//		    				String[] s = inf.toString().split(":");
//		    				if(f == 0) {
//			    				double brl = Double.parseDouble(s[1]);
//			    				((TreeInternalNode) cladeNode).setBranchLength(brl);
//		    				} else {
//		    					double brs = Double.parseDouble(s[0]);
//		    					((TreeInternalNode) cladeNode).setBranchSupport(brs);
//			    				double brl = Double.parseDouble(s[1]);
//			    				((TreeInternalNode) cladeNode).setBranchLength(brl);
//		    				}
//		    			} else {
//		    				String t = inf.toString();
//		    				((TreeInternalNode) cladeNode).setLabel(t);
//		    			}
//		    			i--;
//		    		} else {
//		    			i--;
//		    		}
//		    		break;
//		    	default:
//		    		StringBuffer name = new StringBuffer(); 
//		    		i--;
//		    		char h = this.newickString.charAt(i);
//		    		if(h == '(' || h == ',') {
//		    			i++;
//		    			h = this.newickString.charAt(i);
//		    			while(h != ')' && h != ',' && h != ':') {
//		    				name.append(h);
//			    			i++;
//			    			h = this.newickString.charAt(i);
//		    			}
//			    		TreeLeafNode leafNode = new TreeLeafNode(nodeCount,name.toString());
//			    		tree.getNodes().add(leafNode);
//			    		nodeCount++;
//		    			i--;
////		    			System.out.println(name);
//		    		}
//		    		break;
//		    }
//		}
//		return tree;
//	}
//	
//	public Tree parseFromNewickFile(String filePath) throws URISyntaxException, IOException {
//		// Path object from file : http://stackoverflow.com/questions/15512200/get-path-object-from-file
//		Path newickFilePath = new File(filePath).toPath();
//		// Read plain text in Java
//		this.newickString = new String(Files.readAllBytes(newickFilePath)); 
//		return parseFromNewickString();
//	}
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		Tree tree = null;
//		try {
//			tree = new Tree().parseFromNewickFile("src/phy/journal.pone.0062510.s010.TXT");
//			tree.setEndingLeaves();
//		} catch (URISyntaxException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(TreeNode n: tree.getNodes()) {
//			if(n instanceof TreeInternalNode) {
//				System.out.println(((TreeInternalNode) n).getEndLeaves().size());
//			}
//		}
////		System.out.println(tree.getLeaves().size());
//	}
}
