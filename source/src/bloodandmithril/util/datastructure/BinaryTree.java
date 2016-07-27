package bloodandmithril.util.datastructure;

import static java.lang.Integer.toBinaryString;
import static java.lang.Math.log;

import org.apache.commons.lang3.StringUtils;

import bloodandmithril.core.Copyright;

/**
 * A binary tree
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class BinaryTree<T> {

	/** The root node of the binary tree */
	private final Node rootNode;

	/** The current node */
	private Node currentNode;

	/** Depth of this binary tree */
	private final int depth;

	/**
	 * Constructor
	 */
	@SafeVarargs
	public BinaryTree(int depth, T... ts) {
		if ((int) (log(ts.length) / log(2)) != depth) {
			throw new RuntimeException("Number of arguments provided must be a power of 2");
		}

		this.depth = depth;
		rootNode = new Node();
		currentNode = rootNode;

		boolean[] keySequence = new boolean[depth];
		for (int i = 0; i < depth; i++) {
			keySequence[i] = false;
		}

		for (T t : ts) {
			currentNode = rootNode;
			for (int i = 0; i < depth; i++) {
				construct(keySequence[i], t, i);
			}
			bitIncrementKeySequence(depth - 1, keySequence);
		}
	}
	
	
	/**
	 * Prints all paths along with the values they lead to
	 */
	public void printAll() {
		for (int i = 0; i < Math.pow(2, depth); i++) {
			boolean[] path = new boolean[depth];
			String stringPath = StringUtils.leftPad(toBinaryString(i), depth, '0');
			
			for (int j = 0; j < depth; j++) {
				path[j] = stringPath.toCharArray()[j] != '0';
			}
			
			System.out.println(stringPath + ": " + get(path));
		}
	}
	

	/**
	 * Navigates through the binary tree and retrieves the element given the
	 * traversal path
	 */
	public T get(boolean... path) {
		if (path.length != depth) {
			throw new RuntimeException("Invalid path for this tree");
		}

		currentNode = rootNode;
		for (boolean step : path) {
			move(step);
		}

		return currentNode.get();
	}

	private void move(boolean right) {
		if (right) {
			currentNode = currentNode.right;
		} else {
			currentNode = currentNode.left;
		}
	}

	/**
	 * Performs a 'bit-increment' operation on a boolean array.
	 *
	 * @param i - the index to increment
	 */
	private void bitIncrementKeySequence(int i, boolean[] keySequence) {
		if (i == -1)
			return;
		if (keySequence[i]) {
			keySequence[i] = false;
			bitIncrementKeySequence(i - 1, keySequence);
		} else {
			keySequence[i] = true;
		}
	}

	/**
	 * Adds a valued node at the bottom-level of the binary tree
	 */
	private void construct(boolean right, T t, int level) {
		if (right) {
			if (currentNode.right == null) {
				if (level == depth - 1) {
					currentNode.right = new Node(t);
				} else {
					currentNode.right = new Node();
				}
			}
			currentNode = currentNode.right;
		} else {
			if (currentNode.left == null) {
				if (level == depth - 1) {
					currentNode.left = new Node(t);
				} else {
					currentNode.left = new Node();
				}
			}
			currentNode = currentNode.left;
		}
	}

	/**
	 * A Node
	 *
	 * @author Matt
	 */
	private class Node {

		/** The value of this node */
		public T value;

		/** The left node */
		public Node left;

		/** The right node */
		public Node right;

		/**
		 * Constructor
		 */
		private Node(T value) {
			this.value = value;
		}

		/**
		 * No-arg constructor
		 */
		private Node() {
		}

		/**
		 * @return {@link #value}.
		 */
		public T get() {
			return value;
		}
	}
}
