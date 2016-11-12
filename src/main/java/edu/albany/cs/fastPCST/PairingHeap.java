package edu.albany.cs.fastPCST;

import java.util.ArrayList;

/**
 * Pairing heap data structure
 *
 * @author baojian bzhou6@albany.edu
 */
public class PairingHeap {

	private Node root;
	private ArrayList<Node> buffer;

	public Double getMinFirstP;
	public Integer getMinSecondP;
	public Double deleteMinFirstP;
	public Integer deleteMinSecondP;
	public Node linkFirstP;
	public Node linkSecondP;

	public PairingHeap(ArrayList<Node> sharedBuffer) {
		this.root = null;
		buffer = sharedBuffer;
	}

	/**
	 * link two nodes
	 * @param node1
	 * @param node2
	 * @return a new node
	 */
	private Node link(Node node1, Node node2) {
		if (node1 == null) {
			return node2;
		}
		if (node2 == null) {
			return node1;
		}
		Node smallerNode = node2;
		Node largerNode = node1;
		if (node1.value < node2.value) {
			smallerNode = node1;
			largerNode = node2;
		}
		largerNode.sibling = smallerNode.child;
		if (largerNode.sibling != null) {
			largerNode.sibling.leftUp = largerNode;
		}
		largerNode.leftUp = smallerNode;
		smallerNode.child = largerNode;
		largerNode.value -= smallerNode.childOffset;
		largerNode.childOffset -= smallerNode.childOffset;
		this.linkFirstP = node1;
		this.linkSecondP = node2;
		return smallerNode;
	}

	public boolean is_empty() {
		return root == null;
	}

	public boolean get_min(Double value, Integer payload) {

		if (root != null) {
			if (!root.equals(null)) {
				value = root.value;
				payload = root.payload;
				this.getMinFirstP = value;
				this.getMinSecondP = payload;
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * insert a node
	 * @param value
	 * @param payload
	 * @return
	 */
	public Node insert(Double value, Integer payload) {
		Node newNode = new Node();
		newNode.sibling = null;
		newNode.child = null;
		newNode.leftUp = null;
		newNode.value = value;
		newNode.payload = payload;
		newNode.childOffset = 0D;
		root = link(root, newNode);
		return newNode;
	}

	public void addToHeap(Double value) {
		if (root != null) {
			root.value += value;
			root.childOffset += value;
		}
	}

	public void decreaseKey(Node node, Double fromValue, Double toValue) {
		Double additionalOffset = fromValue - node.value;
		node.childOffset += additionalOffset;
		node.value = toValue;
		if (node.leftUp != null) {
			if (node.leftUp.child == node) {
				node.leftUp.child = node.sibling;
			} else {
				node.leftUp.sibling = node.sibling;
			}
			if (node.sibling != null) {
				node.sibling.leftUp = node.leftUp;
			}
			node.leftUp = null;
			node.sibling = null;
			root = link(root, node);
			node = this.linkSecondP;
		}
	}

	public Node getRoot() {
		return root;
	}

	/**
	 * print this heap
	 * @param root
	 */
	public void print(Node root) {
		if (root != null) {
			System.out.format("Node [value=%f, child_offset=%f, payload=%d]\n", root.value, root.childOffset,
					root.payload);
			print(root.child);
			print(root.sibling);
		} else {
			return;
		}
	}

	/**
	 * @param value
	 *            the value to be deleted from heap
	 * @param payload
	 *            decrease a constant value
	 * @return true if we can find this value and delete it; otherwise, returns
	 *         false.
	 */
	public boolean deleteMin(Double value, Integer payload) {
		if (root == null) {
			return false;
		}
		Node result = root;
		buffer = new ArrayList<Node>();
		Node curChild = root.child;
		Node nextChild = null;
		while (curChild != null) {
			buffer.add(curChild);
			nextChild = curChild.sibling;
			curChild.leftUp = null;
			curChild.sibling = null;
			curChild.value += result.childOffset;
			curChild.childOffset += result.childOffset;
			curChild = nextChild;
		}
		int mergedChildren = 0;
		while (mergedChildren + 2 <= buffer.size()) {
			Node n = link(buffer.get(mergedChildren), buffer.get(mergedChildren + 1));
			buffer.set(mergedChildren, this.linkFirstP);
			buffer.set(mergedChildren + 1, this.linkSecondP);
			buffer.set(mergedChildren / 2, n);
			mergedChildren += 2;
		}
		if (mergedChildren != buffer.size()) {
			buffer.set(mergedChildren / 2, buffer.get(mergedChildren));
			int newSize = ((mergedChildren / 2) + 1) - buffer.size();
			if (buffer.size() > ((mergedChildren / 2) + 1)) {
				for (int i = 0; i < Math.abs(newSize); i++) {
					buffer.remove(buffer.size() - 1);
				}
			} else {
				for (int i = 0; i < newSize; i++) {
					buffer.add(new Node());
				}
			}
		} else {
			int newSize = (mergedChildren / 2) - buffer.size();
			if (buffer.size() > (mergedChildren / 2)) {
				for (int i = 0; i < Math.abs(newSize); i++) {
					buffer.remove(buffer.size() - 1);
				}
			} else {
				for (int i = 0; i < newSize; i++) {
					buffer.add(new Node());
				}
			}
		}
		if (buffer.size() > 0) {
			root = buffer.get(buffer.size() - 1);
			for (int ii = buffer.size() - 2; ii >= 0; --ii) {
				root = link(root, buffer.get(ii));
				buffer.set(ii, this.linkSecondP);
			}
		} else {
			root = null;
		}
		for (int i = 0; i < buffer.size(); i++) {

		}

		this.deleteMinFirstP = result.value;
		this.deleteMinSecondP = result.payload;
		return true;
	}

	/**
	 * @param heap1
	 * @param heap2
	 * @return combined two heaps
	 */
	public PairingHeap meld(PairingHeap heap1, PairingHeap heap2) {
		PairingHeap result = new PairingHeap(buffer);
		result.root = link(heap1.root, heap2.root);
		heap1.root = null;
		heap2.root = null;
		return result;
	}

	public class Node {
		public Node sibling;
		public Node child;
		public Node leftUp;
		public Double value;
		public Double childOffset;
		public Integer payload;

		@Override
		public String toString() {
			return "Node [value=" + value + ", childOffset=" + childOffset + ", payload=" + payload + "]";
		}
	}

}
