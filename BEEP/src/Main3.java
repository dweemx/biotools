import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import csv.NodeMEEP3;
import csv.TreeMEEPWriter3;
import mdtlgraph.Edge;
import mdtlgraph.EventSplit;
import mdtlgraph.EventType;
import mdtlgraph.Graph;
import mdtlgraph.Node;
import mdtlgraph.Parser;
import phy.newickparser.Tree;

/*
 * Pooling events 
 * S 	-> S
 * D 	-> D
 * TD 	-> T(source),TTD,TLTD,TL(source)
 * TR	-> T(target),TFD,TLFD,TL(target)
 * L	-> SL,TLTD,TLFD,TL(source) 
 */
public class Main3 {
	
	Graph medianDTLGraph = new Graph();
	ArrayList<NodeMEEP3> treeMEEP = new ArrayList<NodeMEEP3>();
	
	private String proteinId;
	private String ecceTERAMedianReconciliationFilePath;
	private String speciesTreeFilePath;
	private String outputFileName;
	private String selectedColumns;
	
	public Main3(String proteinId, 
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
	 * 			The name of the MEEP profile file
	 */
	public Main3(String proteinId, 
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
		println("------>");

	}
	
	public static void main(String[] args) {
		
		if(args.length < 4 || args.length > 5) {
			System.out.println("### MEEP Builder ###");
			System.out.println("Arg 1: Protein ID");
			System.out.println("Arg 2: Filepath of species tree (newick format) of the given protein");
			System.out.println("Arg 3: Filepath of the ecceTERA median reconciliation of the given protein");
			System.out.println("Arg 4: Output filepath of the MEEP profile");
			System.out.println("Arg 5 (Optional) : Select particular columns");
		} else {
			Main3 main;
			if(args.length == 5) {
				main = new Main3(args[0],args[1],args[2],args[3],args[4]);
			} else {
				main = new Main3(args[0],args[1],args[2],args[3]);
			}
			main.buildMedianDTLGraph();
			main.buildMEEProfile();
		}
		
//		String p = "P0ABB0";
//		Main2 main = new Main2(p,"src/data/"+p+".stree","src/data/"+p+"_asymmetric.txt","src/data/"+p);
//		main.buildMedianDTLGraph();
//		main.medianDTLGraph.print();
//		main.buildMEEProfile();
//		println("------>");
	}
	
	public void buildMEEProfile() {
		ArrayList<String> speciesTreeNodeIds = null;
		try {
			speciesTreeNodeIds = new Tree().parseFromNewickFile(speciesTreeFilePath).getNodesLabels();
		} catch (URISyntaxException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Sort ArrayList of Integer : http://beginnersbook.com/2013/12/how-to-sort-arraylist-in-java/
//		Collections.sort(speciesTreeNodeIds); Don't need to sort them (After having checked)
		for(String nodeId: speciesTreeNodeIds) {
			
			NodeMEEP3 nodeMEEP = new NodeMEEP3();
			treeMEEP.add(nodeMEEP);
			/*
			 * Macro-evolutionary event considered with either the source node
			 * or the target node
			 */
			ArrayList<Edge> edges = medianDTLGraph.getEdgeListBySpeciesId2(nodeId);
			for(Edge e: edges) {
				System.out.println(e);
				// Enum with switch : http://alvinalexander.com/java/using-java-enum-switch-tutorial
				switch(e.getEventType()) {
					case S: nodeMEEP.setS(e.getEventSupport()); break;
					case D: nodeMEEP.setD(e.getEventSupport()); break;
					case T: 
						if(e.getSourceNode().getSpeciesId() == nodeId) {
							nodeMEEP.addTD(e.getEventSupport());
						} else {
							nodeMEEP.addTR(e.getEventSupport());
						}
						break;
					case TTD: nodeMEEP.addTD(e.getEventSupport()); break;
					case TFD: nodeMEEP.addTR(e.getEventSupport()); break;
					case SL: nodeMEEP.addL(e.getEventSupport()); break;
					case TL: 
						if(e.getSourceNode().getSpeciesId() == nodeId) {
							nodeMEEP.addTD(e.getEventSupport());
							nodeMEEP.addL(e.getEventSupport());
						} else {
							nodeMEEP.addTR(e.getEventSupport());
						}
						break;
					case TLFD: 
						nodeMEEP.addTR(e.getEventSupport()); 
						nodeMEEP.addL(e.getEventSupport()); 
						break;
					case TLTD: 
						nodeMEEP.addTD(e.getEventSupport()); 
						nodeMEEP.addL(e.getEventSupport()); 
						break;
					default:
						break;
				}
			}
			println(nodeId+"");
		}
		TreeMEEPWriter3.writeCsvFile(treeMEEP, outputFileName+".txt",this.selectedColumns);
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
		    	
//		    	System.out.println(p.cladeId);
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
		        		if(parentNode.getSpeciesId() != leftEventSplit.getLeftId())
		        			throw new IllegalArgumentException("Hierarchical arborency is incorrect!");
		        	} else {
		        		if(parentNode.getSpeciesId() != leftEventSplit.getRightId())
		        			throw new IllegalArgumentException("Hierarchical arborency is incorrect!");
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
