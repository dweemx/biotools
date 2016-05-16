package csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteCSV {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public WriteCSV() {
	}
	
	// Write String to file : http://www.avajava.com/tutorials/lessons/how-do-i-write-a-string-to-a-file.html
	public static void write(String content, String outputFilePath) {
		try {
			File file = new File(outputFilePath);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
