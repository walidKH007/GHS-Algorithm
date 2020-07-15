import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Node in a GHS MST
 * 
 */
public class Node implements Runnable {

	// the fragment to which this node belongs to
	private Fragment fragment;

	// this node's adjacent edges
	private List<Edge> adjsEdges;

	private NodeState state;

	private int nodeId;

	private MessageBus messageBus;

	private int distanceMatrix[];

	private int noofnodes;

	private int rec;

	private int parent = -1;

	// Temporary Variables
	private int bestNode = -1;
	private int bestWeight = Integer.MAX_VALUE;
	private int testNode = -1;
	
	public static final int POLL_TIMEOUT = 10;
	public static final int POLL_WAIT = 10;
//	public static final int WAIT = 500;

	public Node(int nodeId, MessageBus messageBus) {
		this.nodeId = nodeId;
		this.messageBus = messageBus;
	}

	public void run() {
		initialize();

		while (true) {
			Message message = poll();

			processMessage(message);
		}
	}

	// Algo 1:Initialization
	private void initialize() {

		int min = Integer.MAX_VALUE;
		int voisin_Node = nodeId;

		// find least weighted edge from distance matrix for this node
		for (int i = 0; i < noofnodes; i++) {
			if (min > distanceMatrix[i] && distanceMatrix[i] != 0) {
				voisin_Node = i;
				min = distanceMatrix[i];

			}
		}

		if (voisin_Node != nodeId) {
			// change state to FOUND
			state = NodeState.FOUND;

			// canal[j] = branch
			ListIterator<Edge> litr = adjsEdges.listIterator();
			while (litr.hasNext()) {
				Edge element = litr.next();
				if (element.getW() == min) {
					element.setStatus(Status.BRANCH);
					break;
				}
			}

			rec = 0;
			// rec = -1;

			// send a connect message to the nearest node id
			Message connectMessage = new Message();
			connectMessage.setFrom(nodeId);
			connectMessage.setTo(voisin_Node);
			connectMessage.setType(MessageType.CONNECT);
			connectMessage.setLevel(fragment.getLevel());
			connectMessage.setState(state);

			sendMessage(connectMessage);
			System.out.println("Node : " + nodeId + " --> Envoi (CONNECT, 0) à " + voisin_Node + "\n");

		}
	}

	private void processMessage(Message message) {

		if (message != null) {
			MessageType type = message.getType();
			if (type == null)
				return;
			switch (type) {
			case CONNECT:

				System.out.println("Node : " + message.getTo() + " --> Reception de (CONNECT, " + message.getLevel()
						+ ") de " + message.getFrom() + "\n");
				processConnectMessage(message);
				break;

			case INITIATE:

				System.out.println("Node : " + message.getTo() + " --> Reception de (INITIATE, " + message.getLevel()
						+ ", " + message.getFragName() + ", " + message.getState() + ") de " + message.getFrom()
						+ "\n");

				processInitiateMessage(message);
				break;

			case TEST:

				System.out.println("Node : " + message.getTo() + " --> Reception de (TEST, " + message.getLevel() + ", "
						+ message.getFragName() + ") de " + message.getFrom() + "\n");

				processTestMessage(message);
				break;

			case ACCEPT:

				System.out.println(
						"Node : " + message.getTo() + " --> Reception de (ACCEPT) de " + message.getFrom() + "\n");

				processAcceptMessage(message);
				break;

			case REJECT:

				System.out.println(
						"Node : " + message.getTo() + " --> Reception de (REJECT) de " + message.getFrom() + "\n");

				processRejectMessage(message);
				break;

			case REPORT:

				System.out.println("Node : " + message.getTo() + " --> Reception de (REPORT, " + message.getBestWeight()
						+ ") de " + message.getFrom() + "\n");

				processReportMessage(message);
				break;

			case CHANGEROOT:

				System.out.println(
						"Node : " + message.getTo() + " --> Reception de (CHANGEROOT) de " + message.getFrom() + "\n");

				processChangeRootMessage(message);
				break;
			}

		}

	}

	private void processConnectMessage(Message message) {

		
		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
		}

		// if L < niv then
		if (message.getLevel() < fragment.getLevel()) {

			// change status
			element.setStatus(Status.BRANCH);

			Message initMessage = new Message();
			initMessage.setType(MessageType.INITIATE);
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(fragment.getLevel());
			initMessage.setFragName(fragment.getName());
			initMessage.setState(state);

			System.out.println("Envoi (INITIATE, " + fragment.getLevel() + ", " + fragment.getName() + ", " + state
					+ ") à " + nodeId + "\n");
			
			// send INITIATE message
			sendMessage(initMessage);

			// check if status == BASIC
		} else if (element.getStatus() == Status.BASIC) {
			try {
				// PUT Message to a list for later processing
				messageBus.put(message);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			// increment level  
			int level = fragment.getLevel() + 1;

			Message initMessage = new Message();
			initMessage.setType(MessageType.INITIATE);
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(level);
			initMessage.setFragName(element.getW());
			// initMessage.setFragName(fragment.getName());
			initMessage.setState(NodeState.FIND);

			System.out.println("Envoi (INITIATE, " + level + ", " + element.getW() + ", FIND) à " + nodeId + "\n");
			
			// send INITIATE message with level increment and state FIND
			sendMessage(initMessage);
		}
	}

	private void processInitiateMessage(Message message) {

		//  variable definition (level, name, state)
		fragment.setLevel(message.getLevel());
		fragment.setName(message.getFragName());
		state = message.getState();
		parent = message.getFrom();

		// check if K € vois and state = branch and K != de j
		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			if (element.getVI() != message.getFrom() && element.getStatus() == Status.BRANCH) {
				
				Message initMessage = new Message();

				initMessage.setType(MessageType.INITIATE);
				initMessage.setFrom(nodeId);
				initMessage.setTo(element.getVI());
				initMessage.setLevel(message.getLevel());
				initMessage.setFragName(message.getFragName());
				initMessage.setState(message.getState());

				System.out.println("Envoi (INITIATE, " + message.getLevel() + ", " + message.getFragName() + ", "
						+ message.getState() + ")" + " à  " + element.getVI() + "\n");
				sendMessage(initMessage);

			}
		}

		// Find least weight edge
		if (state == NodeState.FIND) {
			rec = 0;
			ProcedureTest();
		}

	}

	private void ProcedureTest() {

		int min = Integer.MAX_VALUE;
		int nearestNode = nodeId;

		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			// Choix de j|canal[j] = basic ^ poids(i, j) minimal pour tout j € Vois
			if (element.getStatus() == Status.BASIC && element.getW() < min) {
				min = element.getW();
				nearestNode = element.getVI();
			}
		}

		// to not send the TEST message to sender
		if (nearestNode != nodeId) {
			
			testNode = nearestNode;
			Message testMessage = new Message();
			testMessage.setType(MessageType.TEST);
			testMessage.setLevel(fragment.getLevel());
			testMessage.setFragName(fragment.getName());
			testMessage.setFrom(nodeId);
			testMessage.setTo(testNode);

			System.out.println(
					"Envoi (TEST, " + fragment.getLevel() + ", " + fragment.getName() + ") à  " + testNode + "\n");
			// send Tes
			sendMessage(testMessage);

		} else {
			
			testNode = -1;
			ProcedureReport();
		}
	}

	private void processTestMessage(Message message) {

		// check if sender level is biggest than node level
		if (message.getLevel() > fragment.getLevel()) {
			try {
				// Thread.sleep(Property.WAIT);
				messageBus.put(message);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Internal sommet
		// ckeck if we are in the same node
		if (fragment.getName() == message.getFragName()) 
		{
			ListIterator<Edge> litr = adjsEdges.listIterator();
			Edge element = null;
			while (litr.hasNext()) {
				element = litr.next();
				// check if it has already received the TEST message
				if (element.getVI() == message.getFrom()) {
					break;
				}
			}

			if (element.getStatus() == Status.BASIC) {
				element.setStatus(Status.REJECT);
			}

			if (message.getFrom() != testNode) {
				Message rejectMessage = new Message();
				rejectMessage.setType(MessageType.REJECT);
				rejectMessage.setFrom(nodeId);
				rejectMessage.setTo(message.getFrom());

				System.out.println("Envoi (REJECT) à  " + message.getFrom() + "\n");

				sendMessage(rejectMessage);
			} else {
				ProcedureTest();
			}

		} else {
			Message acceptMessage = new Message();
			acceptMessage.setType(MessageType.ACCEPT);
			acceptMessage.setFrom(nodeId);
			acceptMessage.setTo(message.getFrom());

			System.out.println("Envoi (ACCEPT) à  " + message.getFrom() + "\n");

			sendMessage(acceptMessage);
		}

	}

	private void processAcceptMessage(Message message) {
		
		testNode = -1;
		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			if (element.getVI() == message.getFrom()) {
				break;
			}
		}
		if (element.getW() < bestWeight) {
			bestWeight = element.getW();
			bestNode = message.getFrom();
		}
		ProcedureReport();
	}

	private void processRejectMessage(Message message) {

		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			if (element.getVI() == message.getFrom()) {
				break;
			}
		}

		if (element.getStatus() == Status.BASIC) {
			element.setStatus(Status.REJECT);
		}
		ProcedureTest();
	}

	private void ProcedureReport() {

		int count = 0;
		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			if (element.getStatus() == Status.BRANCH && element.getVI() != parent) {
				count++;
			}
		}
		
		if (rec == count && testNode == -1) {
			state = NodeState.FOUND;
			Message reportMessage = new Message();
			reportMessage.setType(MessageType.REPORT);
			reportMessage.setFrom(nodeId);
			reportMessage.setTo(parent);
			reportMessage.setBestWeight(bestWeight);

			System.out.println("Envoi (REPORT, " + bestWeight + ") à  " + parent + "\n");

			sendMessage(reportMessage);

		}
	}

	private void processReportMessage(Message message) {

		// check if sender is != the parent
		if (message.getFrom() != parent) {
			if (message.getBestWeight() < bestWeight) {

				bestWeight = message.getBestWeight();
				bestNode = message.getFrom();
			}
			rec++;
			ProcedureReport();
		} else {
			
			//if  state == FIND Process the message later
			if (state == NodeState.FIND) {
				try {
					messageBus.put(message);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				// CALL CHANGE ROOT IF WEIGHT OF SENDER IS BIGGEST THAN CURRENT NODE
			} else if (message.getBestWeight() > bestWeight) {
				changeRoot();
			} else if (message.getBestWeight() == bestWeight && bestWeight == Integer.MAX_VALUE) {

				System.out.println("TERMINE.");

				Main.completed = true;
				return;
				// System.exit(0);
			}
		}

	}

	private void changeRoot() {

		ListIterator<Edge> litr = adjsEdges.listIterator();
		Edge element = null;
		while (litr.hasNext()) {
			element = litr.next();
			if (element.getVI() == bestNode) {
				break;
			}
		}
		if (element.getStatus() == Status.BRANCH) {
			Message crootMessage = new Message();
			crootMessage.setFrom(nodeId);
			crootMessage.setTo(bestNode);
			crootMessage.setType(MessageType.CHANGEROOT);

			System.out.println("Envoi (CHANGEROOT) à " + bestNode);

			sendMessage(crootMessage);
		} else {
			element.setStatus(Status.BRANCH);
			Message connectMessage = new Message();
			connectMessage.setFrom(nodeId);
			connectMessage.setTo(bestNode);
			connectMessage.setLevel(fragment.getLevel());
			connectMessage.setType(MessageType.CONNECT);

			System.out.println("Envoi (CONNECT, " + fragment.getLevel() + ") à " + bestNode);

			sendMessage(connectMessage);
		}
	}

	private void processChangeRootMessage(Message message) {
		changeRoot();
	}

	private Message poll() {

		Message message = null;
		long start = System.currentTimeMillis();
		while (message == null) {

			message = messageBus.getMessage(nodeId);
			long end = System.currentTimeMillis() - start;
			if (end >= POLL_TIMEOUT) {
				break;
			}

			try {
				// Wait for a few seconds before arbitrating the bus again for
				// messages
				Thread.sleep(POLL_WAIT + 100 * nodeId);// every thread
																// will get a
																// chance on the
																// lock
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return message;

	}

	private void sendMessage(Message message) {
		messageBus.put(message);
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public NodeState getState() {
		return state;
	}

	public void setState(NodeState state) {
		this.state = state;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public MessageBus getMessageBus() {
		return messageBus;
	}

	public void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	public int[] getDistanceMatrix() {
		return distanceMatrix;
	}

	public void setDistanceMatrix(int[] distanceMatrix) {
		this.distanceMatrix = new int[noofnodes];
		System.arraycopy(distanceMatrix, 0, this.distanceMatrix, 0, noofnodes);
	}

	// initialize this node's adjacent edges
	public void initAdsEdges() {
		adjsEdges = new ArrayList<Edge>();
		for (int i = 0; i < noofnodes; i++) {
			if (distanceMatrix[i] != 0) {
				Edge e = new Edge();
				e.setUI(nodeId);
				e.setVI(i);
				// e.setU(this);
				// e.setV();
				e.setW(distanceMatrix[i]);
				e.setStatus(Status.BASIC);
				adjsEdges.add(e);
			}

		}
	}

	public int getNoofnodes() {
		return noofnodes;
	}

	public void setNoofnodes(int noofnodes) {
		this.noofnodes = noofnodes;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public List<Edge> getAdjsEdges() {
		return adjsEdges;
	}

	public void setAdjsEdges(List<Edge> adjsEdges) {
		this.adjsEdges = adjsEdges;
	}

}
