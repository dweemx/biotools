package phy.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fasta2PhylipSeq {
	
	private static String content;

	public Fasta2PhylipSeq(String filePath) {
	}
	
	public static void convert(String filePath, String outputFilePath) {
		Path fPath = new File(filePath).toPath();
		// Read plain text in Java
		try {
			content = new String(Files.readAllBytes(fPath));
			String[] sequences = content.split(">");
			String phylipSeqFormatContent = "";
			int nbSites = 0, nbTaxa = 0;
			for(int i = 0; i<sequences.length; i++) {
				String seq = sequences[i];					
				seq = seq.replaceFirst("(\\r|\\n|\\r\\n)", "\t");
				// Replace all new lines : http://stackoverflow.com/questions/19909579/find-and-replace-all-newline-or-breakline-characters-with-n-in-a-string-platf
				seq = seq.replaceAll("(\\r|\\n|\\r\\n)+", "");
				if(seq.length() > 0) {
					phylipSeqFormatContent += seq +"\n";
					nbTaxa++;
					if(nbSites == 0) {
						nbSites = seq.split("\t")[1].length();
					}
				}
			}
			String contentConverted = " "+nbTaxa+" "+nbSites+"\n"+phylipSeqFormatContent;
//			System.out.println(contentConverted);
			// Write string to file : http://www.adam-bien.com/roller/abien/entry/java_7_writing_a_string
			Files.write(Paths.get(outputFilePath), contentConverted.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Fasta2PhylipSeq.convert("src/phy/greengenes_export_0.aln","src/phy/greengenes_export_0.aln.phy");
	}

}
