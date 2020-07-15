
/**
 * A message sent from one node to another 
 *
 */
public class Message {

	private MessageType type;
	private int from;
	private int to;	
	private int level;
	private int fragName;
	private NodeState state;
	private int bestWeight ;//=0;//= Integer.MAX_VALUE;
	
	public int getBestWeight() {
		return bestWeight;
	}
	public void setBestWeight(int bestWeight) {
		this.bestWeight = bestWeight;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public NodeState getState() {
		return state;
	}
	public void setState(NodeState state) {
		this.state = state;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFragName() {
		return fragName;
	}
	public void setFragName(int fragName) {
		this.fragName = fragName;
	}
	
	

}
