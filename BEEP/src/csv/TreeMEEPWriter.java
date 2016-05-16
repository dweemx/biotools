package csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TreeMEEPWriter {
	
	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = "\t";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	//CSV file header
	private static final String FILE_HEADER = "S_s\tS_t\tD_s\tD_t\tT_s\tT_t\tTTD\tTFD\tSL_s\tSL_l\tSL_k\tTL_s\tTL_t\tTLTD\tTLFD";

	public static void writeCsvFile(ArrayList<NodeMEEP> content, String fileName) {
		
		FileWriter fileWriter = null;
				
		try {
			fileWriter = new FileWriter(fileName);

			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a new student object list to the CSV file
			for (NodeMEEP nodeMEEP : content) {
				fileWriter.append(Double.toString(nodeMEEP.getS_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getS_t()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getD_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getD_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getT_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getT_t()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTTD()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTFD()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getSL_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getSL_l()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getSL_k()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTL_s()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTL_t()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTLTD()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Double.toString(nodeMEEP.getTLFD()));
				fileWriter.append(NEW_LINE_SEPARATOR);
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
}
