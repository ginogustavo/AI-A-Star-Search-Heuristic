package edu.uic.ai.puzzle;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class TreeSearch {

	private int[][] goal_state = new int[4][4]; // Target goal configuration
	private int nodeCount = 0; // Number of Nodes Expanded
	private String problem = null;
	private Heuristic heuristic = null;

	public TreeSearch(String initialProblem) {
		this.problem = initialProblem;
		initGoalState();
	}

	public TreeSearch() {
		initGoalState();
	}

	public void initGoalState() {
		int tileNumber = 1;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == 3 && j == 3) {
					goal_state[i][j] = 0;
				} else
					goal_state[i][j] = tileNumber;
				tileNumber++;
			}
		}
	}

	/**
	 * Perform A* search with Heuristic previously specified in the "heuristic"
	 * variable.
	 * 
	 * @return
	 */
	public Result a_start() {

		// Making a node based on initial state
		Node initialNode = new Node();
		initialNode.setState(this.getProblem());
		initialNode.parseState(); // Parse from string to 2-D Array.
		initialNode.setParent(null);

		Set<Node> explored = new HashSet<Node>();
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		
		// Add first (unique) element
		frontier.add(initialNode); 
		
		Node node = null;

		while (!frontier.isEmpty()) { // loop until frontier is empty

			node = frontier.poll(); // choose the lowest cost in frontier
			this.nodeCount++;	// node expanded
			
			// if node passed the test return it
			if (goal_test(node.getStateArray())) {
				return node; // return solution
			}
			explored.add(node);

			// Getting the list of actions available per Node and iterate
			List<Action> actions = node.getActions();
			for (Action action : actions) {
				
				Node child = childNode(node, action);
				child.setPathLenght(getPath(child));

				// Child must not be in Explored nor Frontier.
				if (!explored.contains(child) && !frontier.contains(child)) {

					child.setPriority(getHeuristic(child) + child.getPathLenght() - 1);
					frontier.add(child);

				} else if (frontier.contains(child)) {

					// if child is on the frontier, validate the value,
					// and if it's better, add it.
					int priority = getHeuristic(child) + child.getPathLenght() - 1;

					if (priority < child.getPriority()) {
						frontier.remove(child);
						child.setPriority(priority);
						frontier.add(child);
					}
				}
			}
		}
		return node;
	}

	/**
	 * Validate Heuristic Type
	 * 
	 * @param node containing the Heuristic and the State(array)
	 * @return Number of Steps
	 */
	public int getHeuristic(Node node) {
		int number_steps = 0;

		switch (this.getHeuristic()) {
		case MISPLACED_TILES:
			number_steps = getMisplacedTilesHeuristic(node.getStateArray());
			break;

		default: // MANHATTAN_DISTANCE
			number_steps = getManhattanHeuristic(node.getStateArray());
			break;
		}
		return number_steps;
	}

	/**
	 * Calculate Number based on Misplaced Tiles
	 * 
	 * @param nodeState
	 * @return number of steps
	 */
	public int getMisplacedTilesHeuristic(int[][] nodeState) {

		int number_misplaced_tiles = 0;
		// Iterate over the matrix to compare if each tiles has correct number
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (nodeState[i][j] != 0 && nodeState[i][j] != goal_state[i][j]) {
					number_misplaced_tiles++;
				}
			}
		}
		return number_misplaced_tiles;
	}

	/**
	 * Calculate the number based on Manhattan Heuristic
	 * 
	 * @param node state
	 * @return number of steps
	 */
	public int getManhattanHeuristic(int[][] nodeState) {

		int current_value, i_index_goal, j_index_goal, moves_i_axes, moves_j_axes, tile_moves, total_moves = 0;

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {

				// Getting value of current position(i,j) and its goal position
				current_value = nodeState[i][j];

				if (current_value == 0) { // if 0 goal position must be 3,3
					i_index_goal = j_index_goal = 3;

				} else { // otherwise calculate position in a 4x4 matrix
					i_index_goal = (current_value / 4);
					j_index_goal = (current_value % 4) - 1;
				}

				// Calculating moves for individual current tile
				moves_i_axes = Math.abs((i - i_index_goal));
				moves_j_axes = Math.abs((j - j_index_goal));
				tile_moves = moves_i_axes + moves_j_axes;

				// Adding up this moves to the total of moves
				total_moves += tile_moves;
			}
		}
		return total_moves;
	}

	/**
	 * Return the length from the given node to the root
	 * 
	 * @param node
	 * @return length of node
	 */
	public static int getPath(Node node) {
		int path_lenght = 0;
		while (node.getParent() != null) {
			node = node.getParent();
			path_lenght++;
		}
		return path_lenght;
	}

	////////////////////////// Utility methods ///////////////////

	/**
	 * Test the goal by comparing the given state(array) with the target
	 * 
	 * @param nodeState
	 * @return
	 */
	public boolean goal_test(int[][] nodeState) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (nodeState[i][j] != goal_state[i][j]) { // if only one is different
					return false; // goal test failed
				}
			}
		}
		return true; // by default
	}

	/**
	 * Create a new Child
	 * 
	 * @param parent
	 * @param action
	 * @return
	 */
	public Node childNode(Node parent, Action action) {

		// Initialize new Child Node with parameters
		Node newChild = new Node();
		newChild.setMoves(parent.getMoves());
		newChild.setStateArray(parent.getStateArray());
		newChild.setParent(parent);

		// Getting position of 0 in the matrix: e.g. 2,3
		String xyPosition = Node.getZeroPosition(newChild.getStateArray());
		int xpos = Integer.parseInt(xyPosition.charAt(0) + "");
		int ypos = Integer.parseInt(xyPosition.charAt(2) + "");
		int oldXPosition = xpos;
		int newXPosition = xpos;
		int oldYPosition = ypos;
		int newYPosition = ypos;

		// Calculate the new position
		switch (action) {
		case UP:
			newXPosition--;
			newChild.setMoves(newChild.getMoves() + "U");
			break;
		case DOWN:
			newXPosition++;
			newChild.setMoves(newChild.getMoves() + "D");
			break;
		case LEFT:
			newYPosition--;
			newChild.setMoves(newChild.getMoves() + "L");
			break;
		case RIGHT:
			newYPosition++;
			newChild.setMoves(newChild.getMoves() + "R");
			break;
		default:
			break;
		}

		// make copy of the state to process it
		int[][] tempNew = new int[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				tempNew[i][j] = newChild.getStateArray()[i][j];
			}
		}

		// Value to be exchanged with 0
		int valueToExchange = newChild.getStateArray()[newXPosition][newYPosition];

		// Updated new Values for the new matrix
		tempNew[newXPosition][newYPosition] = 0;
		tempNew[oldXPosition][oldYPosition] = valueToExchange;

		// Assign new matrix to the created Child node
		newChild.setStateArray(tempNew);
		return newChild;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public Heuristic getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

}
