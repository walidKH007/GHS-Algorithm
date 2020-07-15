
public class Fragment {
	
	//Root of this MST fragment
	private Node root;

	//Level of this fragment
	private int level;
	
	private int  name;
	
	private Edge leastOutgoingEdge;

	public int  getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public Edge getLeastOutgoingEdge() {
		return leastOutgoingEdge;
	}

	public void setLeastOutgoingEdge(Edge leastOutgoingEdge) {
		this.leastOutgoingEdge = leastOutgoingEdge;
	}
	
}
