

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BioAlignmentTrimmer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BioAlignmentTrimmer.trim("src/phy/P0AGA2_common_orthologues.aln.phy", "src/phy/P0AGA2.aln.mask", 0.9, "", "fasta");
	}
	
	public static void trim(String alnFilePath, String maskFilePath, double threshold, String outputFilePath, String outputFormat) {
		Path fPath = new File(alnFilePath).toPath();
		Path mPath = new File(maskFilePath).toPath();
		// Read plain text in Java
		try {
			String alnContent = new String(Files.readAllBytes(fPath));
			String[] maskContent = new String(Files.readAllBytes(mPath)).split("\n");
			String head = null;
			int trimmedSeqLength = 0;
			String trimmedContent = "";
			String[] sequences = alnContent.split("\n");
			for(int i = 0; i<sequences.length; i++) {
				if(i == 0) {
					head = sequences[i];
				} else {
					String[] seq = sequences[i].split("\t");
					StringBuffer sq = new StringBuffer(""); 
					for(int j=0; j<maskContent.length; j++) {
						double pp = Double.parseDouble(maskContent[j]);
						if(pp > threshold*10) {
							sq.append(seq[1].split("")[j]); 
						}
					}
					if(trimmedSeqLength == 0) {
						trimmedSeqLength = sq.length();
					}
					
					String trimmedSequence;
					switch(outputFormat) {
						case "fasta":
							trimmedSequence = toFastaSequenceBlock(sq.toString(),60);
							trimmedContent += ">"+ seq[0] +"\n"+ trimmedSequence+ "\n";
							break;
						case "phylip":
							trimmedSequence = sq.toString();
							trimmedContent += seq[0] +"\t"+ trimmedSequence+ "\n";
							break;
					}
				}
			}
			if(outputFormat.equals("phylip")) {
				String[] headers = head.trim().split(" ");
				trimmedContent = " "+ headers[0] +" "+trimmedSeqLength+"\n"+ trimmedContent;
			}
//			System.out.println(trimmedContent);
			// Write string to file : http://www.adam-bien.com/roller/abien/entry/java_7_writing_a_string
			Files.write(Paths.get(outputFilePath), trimmedContent.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static String toFastaSequenceBlock(String sequence, int blockSize) {
		// Split string by number of characters : http://stackoverflow.com/questions/9276639/java-how-to-split-a-string-by-a-number-of-characters
		List<String> strings = new ArrayList<String>();
		int index = 0;
		while (index < sequence.length()) {
		    strings.add(sequence.substring(index, Math.min(index + blockSize,sequence.length())));
		    index += blockSize;
		}
		return StringUtils.join(strings, "\n");
	}

}
