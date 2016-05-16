package csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ReadCSV {
	
	// Parse CSV file : http://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
	public static List<String[]> read(String csvFilePath, String delimiter) {
		File csvFile = new File(csvFilePath);
		List<String[]> data = new ArrayList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				// Omit header
				if(i == 0) {
					i++;
					continue;
				}
			    // use comma as separator
				String[] dataLine = line.split(delimiter);
				data.add(dataLine);
				i++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}
	
	public static double[] read(String filePath) {
		File csvFile = new File(filePath);
		List<Double> data = new ArrayList<Double>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				// Omit header
				if(i == 0) {
					i++;
					continue;
				}
			    // use comma as separator
				data.add(Double.parseDouble(line));
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// From ArrayList to Array : http://stackoverflow.com/questions/14134527/error-converting-from-arraylist-to-double
		return ArrayUtils.toPrimitive(data.toArray(new Double[data.size()]));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
