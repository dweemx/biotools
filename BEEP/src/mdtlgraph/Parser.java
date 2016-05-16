package mdtlgraph;
import java.util.ArrayList;

public class Parser {
	
	private EventSplit leftEventSplit;
	private ArrayList<EventSplit> noEventSplits = new ArrayList<EventSplit>();
	private EventSplit rightEventSplit;
	/**
	 * u
	 */
	private String cladeId;
	/**
	 * ul
	 */
	private String cladeIdLeft;
	/**
	 * ur
	 */
	private String cladeIdRight;

	public Parser(String line) {
		// If semi-colon not present, then it's the root
		line = line.replace("'OUTGROUP'", "'0'");
		line = line.replace("'","");
		if(line.indexOf(';') < 0) {
			String[] lineSplitted = line.split("[:|,|@]");
			setCladeId(lineSplitted[0]);
			setCladeIdLeft(lineSplitted[6]);
			setCladeIdRight(lineSplitted[7]);
			setLeftEventSplit(new EventSplit(
					lineSplitted[1],
					// String to enum : http://stackoverflow.com/questions/604424/convert-a-string-to-an-enum-in-java
					EventType.valueOf(lineSplitted[2]),
					lineSplitted[4],
					lineSplitted[4],
					Double.parseDouble(lineSplitted[5])));
		} else {
			String[] lineSplitted = line.split(";");
			for(int i=0; i<lineSplitted.length; i++) {
				int atPos = lineSplitted[i].indexOf('@');
				int colPos = lineSplitted[i].indexOf(':');
				String[] l = lineSplitted[i].split("[:|,|@]");
//				System.out.println("####: "+lineSplitted[i]);
				if(i == 0 && colPos < atPos) {
					// Left event split
					setCladeId(l[0]);
					setLeftEventSplit(new EventSplit(
						l[1],
						EventType.valueOf(l[2]),
						l[3],
						l[4],
						Double.parseDouble(l[5])));
				} else if (i > 0 && i < lineSplitted.length-1 && colPos < 0) {
//					System.out.println("#### NoSplit: "+lineSplitted[i]);
					// No event split
					EventSplit noEventSplit = new EventSplit(
							l[0],
							EventType.valueOf(l[1]),
							l[2],
							l[3],
							Double.parseDouble(l[4]));
					this.getNoEventSplits().add(noEventSplit);
				} else if (i == lineSplitted.length-1 && colPos > atPos) {
					// Right event split
					setCladeIdLeft(l[5]);
					setCladeIdRight(l[6]);
					setRightEventSplit(new EventSplit(
						l[0],
						EventType.valueOf(l[1]),
						l[2],
						l[3],
						Double.parseDouble(l[4])));
				}
			}
		}
	}

	public EventSplit getLeftEventSplit() {
		return leftEventSplit;
	}

	public void setLeftEventSplit(EventSplit leftEventSplit) {
		this.leftEventSplit = leftEventSplit;
	}

	public String getCladeId() {
		return cladeId;
	}

	public void setCladeId(String cladeId) {
		this.cladeId = cladeId;
	}

	public ArrayList<EventSplit> getNoEventSplits() {
		return noEventSplits;
	}

	public void setNoEventSplits(ArrayList<EventSplit> noEventSplits) {
		this.noEventSplits = noEventSplits;
	}

	public String getCladeIdRight() {
		return cladeIdRight;
	}

	public void setCladeIdRight(String cladeIdRight) {
		this.cladeIdRight = cladeIdRight;
	}

	public String getCladeIdLeft() {
		return cladeIdLeft;
	}

	public void setCladeIdLeft(String cladeIdLeft) {
		this.cladeIdLeft = cladeIdLeft;
	}

	public EventSplit getRightEventSplit() {
		return rightEventSplit;
	}

	public void setRightEventSplit(EventSplit rightEventSplit) {
		this.rightEventSplit = rightEventSplit;
	}
	
}
