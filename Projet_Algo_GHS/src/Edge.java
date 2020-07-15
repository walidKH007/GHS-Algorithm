import java.util.Stack;

/**
 * An edge from vertex u to vertex v having weight w
 *
 */

public class Edge {
	
	private Node u;

	private int uI;

	private Node v;

	private int vI;

	private int w;

	private Status SE;

	public void setStatus(Status branch) {
		this.SE = branch;
	}
 
	public Status getStatus() {
		return this.SE;
	}

	public Node getU() {
		return u;
	}

	public int getUI() {
		return uI;
	}

	public int getVI() {
		return vI;
	}

	public void setU(Node u) {
		this.u = u;
	}

	public void setUI(int uI) {
		this.uI = uI;
	}

	public Node getV() {
		return v;
	}

	public void setV(Node v) {
		this.v = v;
	}

	public void setVI(int vI) {
		this.vI = vI;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}


}
