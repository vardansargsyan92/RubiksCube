package sample.model;
import sample.math.Rotations;

import java.util.*;

public class IDA {

	public final List<String> possibleActions = Arrays.asList("F", "Fi", "F2", "R", "Ri", "R2",
			"B", "Bi", "B2", "L", "Li", "L2",
			"U", "Ui", "U2", "D", "Di", "D2");

	private class AStarFunction {
		public int getValue(Node node) {
			return node.state.getManhattenHeuristic()+node.pathCost;
		}
	}

	public class Node {


		public final Node parent;
		public final String action;
		public final Rotations state;
		public int value;
		public int pathCost;

		public Node(Node parent, String action, Rotations state) {
			this.parent = parent;
			this.action = action;
			this.state = state;
			this.value = 0;

			// For root
			if (parent == null)
				pathCost = 0;
			else
				pathCost = parent.pathCost + 1;
		}
	}
	public class NodeComparator implements Comparator<Node> {

		public int compare(Node o1, Node o2) {

			if (o1.value < o2.value)
			{
				return -1;
			}
			if (o1.value > o2.value)
			{
				return 1;
			}
			return 0;
		}
	}

	public Node findSolution(Rotations rotations) {

		Node rootNode = new Node(null,null,rotations);
		Comparator<Node> comparator = new NodeComparator();
		BestFirstFrontier frontier = new BestFirstFrontier();

		if(!frontier.isEmpty())
			frontier.clearContent();

		HashSet<Rotations> visited = new HashSet<>();

		frontier.addNode(rootNode);

		while(!frontier.isEmpty())
		{
			Node node = null;
			try {
				node = frontier.removeNode();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (node.state.getManhattenHeuristic()==0)
				return node;
			else {
				for (String action : possibleActions)
				{
					Rotations newState = node.state;
					newState.turn(action);
					boolean exists = visited.add(newState);
					if(exists) {
						frontier.addNode(new Node(node, action, newState));
					}
				}
			}
		}
		return null;


	}



	private class BestFirstFrontier {

		private PriorityQueue<Node> m_priorQueue;

		private int m_maxNodeCnt;

		private AStarFunction m_nodeFunction;

		public BestFirstFrontier() {


			m_nodeFunction =  new AStarFunction();

			Comparator<Node> comparator = new NodeComparator();
			m_priorQueue = new PriorityQueue<Node>(10,comparator);
		}


		public void addNode(Node newNode) {

			// Compute and cache/store the node function value into the node
			newNode.value = m_nodeFunction.getValue(newNode);

			m_priorQueue.add(newNode);

			if(m_priorQueue.size() > m_maxNodeCnt)
				m_maxNodeCnt = m_priorQueue.size();
		}

		public void clearContent() {
			m_priorQueue.clear();
		}

		public boolean isEmpty() {
			return m_priorQueue.isEmpty();
		}

		public Node removeNode() throws Exception {
			if(!isEmpty())
				return m_priorQueue.remove();
			else
				throw new Exception("Empty Queue");
		}

		public int maxNumberOfNodes() {
			return m_maxNodeCnt;
		}
	}


}
