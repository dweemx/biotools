import org.apache.commons.lang3.StringUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import csv.ReadCSV;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BioDIPParser {

	static int uniProtRefCount = 0;
	static int obsoleteCount = 0;
	static int redundantCount = 0;
	static int identicalCount = 0;
	static int finalRefCount = 0;
	static int errors = 0;
	
	static ArrayList<String> uniqueTypes = new ArrayList<String>();
	
	public static void main(String argv[]) {
		List<String[]> dipPpi = ReadCSV.read("C:/Users/Max/Desktop/ThesisWork/benchmark/dip/Ecoli20160114CR.txt", "\t",true);
//		System.out.println(dipPpi.get(0)[14]);
		for(String[] interaction: dipPpi) {
//			if((interaction[0].contains("P10408") && interaction[1].contains("P0AGA2"))
//					|| (interaction[1].contains("P10408") && interaction[0].contains("P0AGA2")))
//			System.out.println(interaction[15]);
			String[] t = interaction[11].split("\\|");
			for(String s: t) {
				if(!uniqueTypes.contains(s)) {
					uniqueTypes.add(s);
				}
			}
			
			try {
				String protAUniProtRef = interaction[0].split("uniprotkb:")[1];
				String protBUniProtRef = interaction[1].split("uniprotkb:")[1];
				String validAUniProtRef = getValidUniProtProteinRef(protAUniProtRef);
				String validBUniProtRef = getValidUniProtProteinRef(protBUniProtRef);
				if(validAUniProtRef == null || validBUniProtRef == null) {
					continue;
				}
				uniProtRefCount++;
				if(validAUniProtRef.equals(validBUniProtRef)) {
					identicalCount++;
					continue;
				}
				finalRefCount++;
				String interactionText = validAUniProtRef+"\t"+validBUniProtRef+"\tTRUE\t"+StringUtils.join(getTypes(interaction),",")+"\n";
				System.out.print(" > Err: "+errors+" | "+interactionText);
				File outFile = new File("C:/Users/Max/Desktop/ThesisWork/benchmark/dip/Ecoli_DIP_20160114CR_TP.txt");
				if(!outFile.exists()) {
					outFile.createNewFile();
				}
				try {
					// Append text : http://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
				    Files.write(outFile.toPath(), interactionText.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
				    //exception handling left as an exercise for the reader
				}
			} catch(ArrayIndexOutOfBoundsException | IOException e) {
//				System.out.println(interaction[0]+"\n"+interaction[1]);
			}
		}
		for(String g: uniqueTypes) {
			System.out.println(g);
		}
		System.out.println("1. Total DIP PPI: "+dipPpi.size());
		System.out.println("2. (1) with UniProt Reference: "+uniProtRefCount);
		System.out.println("3. (2) as Obsolete Entry: "+obsoleteCount);
		System.out.println("4. (2) as Redundant Entry: "+redundantCount);
		System.out.println("4. (2) as Identitical Entry: "+identicalCount);
		System.out.println("5. Final Total DIP PPI: "+finalRefCount+" (all obsolete replaced, redundant remove, identicals removed)");
	}
	
	public static ArrayList<String> getTypes(String[] interaction) {
		ArrayList<String> types = new ArrayList<String>();
		if(interaction[11].contains("MI:0915")) {
			types.add("PA");
		}
		if(interaction[11].contains("MI:0914")) {
			types.add("A");
		}
		if(interaction[11].contains("MI:0218")) {
			types.add("PI");
		}
		if(interaction[11].contains("MI:0407")) {
			types.add("DI");
		}
		if(interaction[11].contains("MI:0254")) {
			types.add("GI");
		}
		if(interaction[11].contains("MI:0915")) {
			types.add("PA");
		}
		if(interaction[11].contains("MI:0195")) {
			types.add("CB");
		}
		if(interaction[11].contains("MI:0570")) {
			types.add("PC");
		}
		if(interaction[11].contains("MI:0414")) {
			types.add("ER");
		}
		if(interaction[11].contains("MI:0217")) {
			types.add("PR");
		}
		if(interaction[11].contains("MI:0945")) {
			types.add("OAETA");
		}
		if(interaction[11].contains("MI:0203")) {
			types.add("DR");
		}
		if(interaction[11].contains("MI:0844")) {
			types.add("PTR");
		}
		// Remove duplicates
		// add elements to al, including duplicates
		Set<String> hs = new HashSet<>();
		hs.addAll(types);
		types.clear();
		types.addAll(hs);
		return types;
	}
	
	public static String getValidUniProtProteinRef(String uniProtRef) {
		try {
			Document doc;
			try {
				doc = Jsoup.connect("http://www.uniprot.org/uniprot/"+uniProtRef+".fasta").get();
				if(doc.text().length() == 0) {
					redundantCount++;
					return null;
				}
				return uniProtRef;
			} catch(HttpStatusException e) {
				obsoleteCount++;
				doc = Jsoup.connect("http://www.uniprot.org/uniprot/?query=replaces:"+uniProtRef+"&format=fasta").get();
				String[] data = doc.text().split(">");
				for(String p: data) {
					if(p.contains("Escherichia coli (strain K12)")) {
						String protUniProtRef = p.split("\\|")[1];
						return protUniProtRef;
					}
				}
			}
		} catch (IOException e) {
			errors++;
			e.printStackTrace();
		}
		return uniProtRef;
	}
	
}