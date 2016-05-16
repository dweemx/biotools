package phy.converter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PhylipInterLeaved2Fasta {

	static String[] names;
	static String[] sequences;

	public PhylipInterLeaved2Fasta(String filePath) {
	}
	
	public static void convert(String filePath, String outputFilePath) {
		// Read plain text in Java
		try {
			FileInputStream fis = new FileInputStream(filePath);
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			int i=0,j=0,k=0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(i==0) {
					String[] header = line.split("\\s+");
					names = new String[Integer.parseInt(header[0])];
					//System.out.println(names.length);
					sequences = new String[Integer.parseInt(header[0])];
				} else {
					if(line.length() > 0) {
						int blank = line.indexOf(' ');
						if(k < 1) {
							String name = line.substring(0, blank);
							names[j] = name;
							String seq = line.substring(blank+1);
							// Split on white spaces : http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
							sequences[j] = seq.replaceAll("\\s+", "");
						} else {
							sequences[j] += "\n"+line.replaceAll("\\s+", "");
						}
						j++;
					} else {
						j=0;
						k++;
					}
				}
				i++; 
			}
			br.close();
			String content = "";
			for(int l=0; l<sequences.length; l++) {
				content += ">"+names[l]+"\n";
				content += sequences[l]+"\n";
			}
//			System.out.println(content);
			Files.write(Paths.get(outputFilePath), content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PhylipInterLeaved2Fasta.convert("src/phy/sample.phy","");
	}

}
