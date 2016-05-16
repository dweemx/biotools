import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class BioDatabaseDownloader {
	
	private BioDatabase bioDb;

	public BioDatabaseDownloader(BioDatabase bioDb) {
		this.setBioDb(bioDb);		
	}
	
	public void download(BioDatabase bioDb, String id, String outputFilePath) {
		String url = "";
		switch(bioDb) {
			case HOGENOM:
				url = "http://pbil.univ-lyon1.fr/cgi-bin/download-srs.pl?query="+id+"&db=HOGENOM&type=tree";
				get(url,outputFilePath);
				break;
			case EGGNOG:
				url = "http://eggnogapi.embl.de/nog_data/json/tree/"+id;
				get(url,outputFilePath);
				break;
		}
	}
	
	static void get(String url, String outputFilePath) {
		// TODO Auto-generated method stub
		URL website;
		FileOutputStream fos = null;
		try {
			website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(outputFilePath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Download file from GET request http://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
	public static void main(String[] args) {
		
	}

	public BioDatabase getBioDb() {
		return bioDb;
	}

	public void setBioDb(BioDatabase bioDb) {
		this.bioDb = bioDb;
	}

}
