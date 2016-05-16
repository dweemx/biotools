import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import csv.ReadCSV;
import phy.converter.Fasta2PhylipSeq;
import phy.converter.PhylipInterLeaved2Fasta;
import phy.newickparser.Tree;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length < 1) {
			System.out.println("##############################################");
			System.out.println("#                 BioUtils-0.1               #");
			System.out.println("#               © Copyright 2016             #");
			System.out.println("#            Author: M. De Waegeneer         #");
			System.out.println("##############################################");
			System.out.println("---------- Arguments (arg)");
			System.out.println("1 (1): Module");
			System.out.println("\t1.1: BioDatabaseDownloader");
			System.out.println("\t1.2: BioDatabaseMapper");
			System.out.println();
			System.out.println("----- Module Arguments");
			System.out.println("\t1.1: BioDatabaseDownloader");
			System.out.println("\t\t1.1.1 (2): Database name");
			System.out.println("\t\t1.1.2 (3): Element IDentifier name e.g.: P0ABB0");
			System.out.println("\t\t1.1.3 (4): Output filepath");
			System.out.println("\t1.2: BioDatabaseMapper");
			System.out.println("\t\t1.2.1 (2): Source database IDentifier name e.g. ACC");
			System.out.println("\t\t1.2.2 (3): Target database IDentifier name e.g.: PDB_ID");
			System.out.println("\t\t1.2.3 (4): Query of IDentifiers");
			System.out.println("\t\tNOTE: Available mappings can be found at: http://www.uniprot.org/help/programmatic_access#id_mapping_examples");
			System.out.println("\t1.3: BioPhyloNewickParser");
			System.out.println("\t\t1.3.1 (2): File path of phylogenetic tree file");
			System.out.println("\t\t1.3.2: Command task");
			System.out.println("\t\t\t1.3.2.1 (3): eLL = extract the leaf labels");
			System.out.println("\t\t1.3.3 (4): Output filepath");
			System.out.println("\t1.4: BioPhyloAlignmentFormatConverter");
			System.out.println("\t\t1.4.1 (2): File path of alignment file");
			System.out.println("\t\t1.4.2: Command task");
			System.out.println("\t\t\t1.4.2.1 (3): Source format (fasta, ...)");
			System.out.println("\t\t\t1.4.2.2 (4): Target format (phylip-seq, ...)");
			System.out.println("\t\t1.4.3 (5): Output filepath");
			System.out.println("\t1.5: BioAlignmentTrimmer");
			System.out.println("\t\t1.5.1 (2): File path of alignment file");
			System.out.println("\t\t1.5.2: Command task");
			System.out.println("\t\t\t1.5.2.1 (3): File path of mask file");
			System.out.println("\t\t\t1.5.2.2 (4): Threshold (between 0 and 1)");
			System.out.println("\t\t1.5.3 (5): Output filepath");
			System.out.println("\t\t1.5.4 (6): Output format");
			System.out.println("\t1.6: BioPhyloRandomTreeResolver");
			System.out.println("\t\t1.6.1 (2): File path of newick tree file");
			System.out.println("\t\t1.6.2 (3): Output filepath");
			System.out.println("\t1.7: BioPhyloTreeStandardizer");
			System.out.println("\t\t1.7.1 (2): File path of newick tree file");
			System.out.println("\t\t1.7.2: Command task");
			System.out.println("\t\t\t1.5.2.1 (3): File path of mapping file");
			System.out.println("\t\t\t1.5.2.2 (4): Delimiter");
			System.out.println("\t\t1.7.3 (5): Output filepath");
		} else {
			switch(args[0]) {
				case "BioDatabaseDownloader":
					// String to enum : http://stackoverflow.com/questions/604424/convert-a-string-to-an-enum-in-java
					BioDatabase bioDbName = BioDatabase.valueOf(args[1]);
					BioDatabaseDownloader bdd = new BioDatabaseDownloader(bioDbName);
					bdd.download(bioDbName, args[2], args[3]);
					break;
				case "BioDatabaseMapper":
					try {
						String result = BioDatabaseMapper.map(args[1], args[2], args[3]);
						System.out.println(result);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case "BioPhyloNewickParser":
					Tree tre = null;
					try {
						tre = new Tree().parseFromNewickFile(args[1]);
					} catch (URISyntaxException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(args[2].equals("eLL")) {
						if(args.length < 3) {
							Main.print(tre.getLeafLabels());
						} else {
							Main.writeFile(tre.getLeafLabels(), args[3]);
						}
					}
					break;
				case "BioPhyloAlignmentFormatConverter":
					String cc = (args[2]+"->"+args[3]).toLowerCase();
					switch(cc) {
						case "fasta->phylip-seq":
							Fasta2PhylipSeq.convert(args[1],args[4]);
							break;
						case "phylip-il->fasta":
							PhylipInterLeaved2Fasta.convert(args[1],args[4]);
							break;
						default:
							System.out.println("Not yet implemented :(");
					}
					break;
				case "BioAlignmentTrimmer":
					BioAlignmentTrimmer.trim(args[1], args[2], Double.parseDouble(args[3]), args[4], args[5]);
					break;
				case "BioPhyloRandomTreeResolver":
					Tree tree = null;
					try {
						tree = new Tree().parseFromNewickFile(args[1]);
					} catch (URISyntaxException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Tree resolvedTree = tree.resolve();
					writeStringToFile(resolvedTree+"", args[2]);
					break;
				case "BioPhyloTreeStandardizer":
					Tree treee = null;
					try {
						treee = new Tree().parseFromNewickFile(args[1]);
						treee.setEndingLeaves();
						HashMap<String, String> mapping = ReadCSV.readMapping(args[2], args[3], false);
						treee.setLeafMapping(mapping);
						Tree treeMapped = treee.mapLeaves();
						writeStringToFile(treeMapped+"", args[4]);
					} catch (URISyntaxException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
			}
		}
	}
	
	// Write String to File : http://www.adam-bien.com/roller/abien/entry/java_7_writing_a_string
	public static void writeStringToFile(String content, String outputFilePath) {
		try {
			Files.write(Paths.get(outputFilePath), content.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Write to file : http://www.programcreek.com/2011/03/java-write-to-a-file-code-example/
	public static void writeFile(List<String> content, String outputFilePath) {
		File fout = new File(outputFilePath);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));	 
			for (String l: content) {
				bw.write(l);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void print(List<String> content) {
		for (String l: content) {
			System.out.println(l);
		}
	}

}
