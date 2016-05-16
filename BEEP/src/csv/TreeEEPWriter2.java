package csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

// Write to CSV in java : https://examples.javacodegeeks.com/core-java/writeread-csv-files-in-java-example/
public class TreeEEPWriter2 {
	
	//Delimiter used in CSV file
	private static final String TAB_DELIMITER = "\t";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	//CSV file header
	private static String FILE_HEADER = "";

	public static void writeCsvFile(ArrayList<NodeEEP2> content, String fileName, String selectedColumns) {
		
		FileWriter fileWriter = null;
				
		try {
			fileWriter = new FileWriter(fileName);

			ArrayList<String> headers = new ArrayList<String>();
			if(selectedColumns.indexOf("0") > -1 || selectedColumns.length() == 0)
				headers.add("NID");
			if(selectedColumns.indexOf("1") > -1 || selectedColumns.length() == 0)
				headers.add("S");
			if(selectedColumns.indexOf("2") > -1 || selectedColumns.length() == 0)
				headers.add("D");
			if(selectedColumns.indexOf("3") > -1 || selectedColumns.length() == 0) 
				headers.add("T");
			if(selectedColumns.indexOf("4") > -1 || selectedColumns.length() == 0)
				headers.add("TTD");
			if(selectedColumns.indexOf("5") > -1 || selectedColumns.length() == 0)
				headers.add("TFD");
			if(selectedColumns.indexOf("6") > -1 || selectedColumns.length() == 0)
				headers.add("SL");
			if(selectedColumns.indexOf("7") > -1 || selectedColumns.length() == 0)
				headers.add("TL");
			if(selectedColumns.indexOf("8") > -1 || selectedColumns.length() == 0)
				headers.add("TLTD");
			if(selectedColumns.indexOf("9") > -1 || selectedColumns.length() == 0)
				headers.add("TLFD");
			FILE_HEADER = StringUtils.join(headers, "\t");
			
			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a new student object list to the CSV file
			for (NodeEEP2 nodeMEEP : content) {
				if(selectedColumns.indexOf("0") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(nodeMEEP.getNid());
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("1") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getS()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("2") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getD()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("3") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getT()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("4") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTTD()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("5") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTFD()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("6") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getSL()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("7") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTL()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("8") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTLTD()));
					fileWriter.append(TAB_DELIMITER);
				}
				if(selectedColumns.indexOf("9") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTLFD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
			}

			
			
			System.out.println("CSV file was created successfully !!!");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	public static void writeEEPProfile(ArrayList<NodeEEP2> content, String fileName, String selectedColumns) {
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(fileName);
			
			//Write a new student object list to the CSV file
			for (NodeEEP2 nodeMEEP : content) {
				if(selectedColumns.indexOf("0") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(nodeMEEP.getNid());
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("1") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getS()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("2") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("3") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getT()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("4") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTTD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("5") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTFD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("6") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getSL()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("7") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTL()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("8") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTLTD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
				if(selectedColumns.indexOf("9") > -1 || selectedColumns.length() == 0) {
					fileWriter.append(Double.toString(nodeMEEP.getTLFD()));
					fileWriter.append(NEW_LINE_SEPARATOR);
				}
			}

			
			
			System.out.println("Profile was created successfully !!!");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
}
