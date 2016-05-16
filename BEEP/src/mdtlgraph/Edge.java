package mdtlgraph;

public class Edge {
	
	public Edge(Node source,
			Node target,
			EventType eventType, 
			double eventSupport) {
		this.source = source;
		this.target = target;
		this.eventType = eventType;
		this.eventSupport = eventSupport;		
	}
	
	@Override
	public String toString() {
		return source+"<-("+eventType+":"+eventSupport+")->"+target;
	}
	
	public Node getSourceNode() {
		return this.source;
	}
	
	private Node source;
	
	public Node getTargetNode() {
		return this.target;
	}
	
	private Node target;
	
	public EventType getEventType() {
		return this.eventType;
	}
	
	private EventType eventType;
	
	public double getEventSupport() {
		return this.eventSupport;
	}
	
	private double eventSupport;
	
}
