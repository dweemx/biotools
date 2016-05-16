package mdtlgraph;

public class EventSplit {

	public EventSplit(String speciesId, 
			EventType eventType,
			String leftId,
			String rightId,
			double eventSupport) {
		this.setSpeciesId(speciesId);
		this.setEventType(eventType);
		this.setLeftId(leftId);
		this.setRightId(rightId);
		this.setEventSupport(eventSupport);
	}

	public String getSpeciesId() {
		return speciesId;
	}

	public void setSpeciesId(String speciesId) {
		this.speciesId = speciesId;
	}
	
	private String speciesId;
	
	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	
	private EventType eventType;
	
	/**
	 * auxId_l | lostId
	 * @return
	 */
	public String getLeftId() {
		return leftId;
	}

	public void setLeftId(String leftId) {
		this.leftId = leftId;
	}
	
	private String leftId;
	
	/**
	 * auxId_r | keptId
	 * @return
	 */
	public String getRightId() {
		return rightId;
	}

	public void setRightId(String rightId) {
		this.rightId = rightId;
	}
	
	private String rightId;
	
	public double getEventSupport() {
		return eventSupport;
	}

	public void setEventSupport(double eventSupport) {
		this.eventSupport = eventSupport;
	}
	
	private double eventSupport;	
	
	@Override
	public String toString() {
		return this.getSpeciesId()+","+this.getEventType().name()+","+
				this.getLeftId()+","+this.getRightId()+"@"+this.getEventSupport();
	}
}
