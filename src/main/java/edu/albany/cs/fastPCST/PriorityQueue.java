package edu.albany.cs.fastPCST;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Priority Queue data structure. We set the value type to <Double,Integer>
 * 
 * @author baojian bzhou6@albany.edu
 * 
 */
public class PriorityQueue {

	private SortedSet<Pair<Double, Integer>> sortedSet = new TreeSet<Pair<Double, Integer>>(new Comp());
	private ArrayList<Pair<Double, Integer>> indexToIterator = new ArrayList<Pair<Double, Integer>>();

	// reference parameters
	public Double getMinFirstP = -1d;
	public Integer getMinSecondP = -1;
	public Double deleteMinFirstP = -1d;
	public Integer deleteMinSecondP = -1;

	public PriorityQueue() {
	}

	public boolean isEmpty() {
		return sortedSet.isEmpty();
	}

	public boolean getMin(Double value, Integer index) {
		if (sortedSet == null || sortedSet.isEmpty()) {
			return false;
		}
		value = sortedSet.first().getFirst();
		index = sortedSet.first().getSecond();
		this.getMinFirstP = value;
		this.getMinSecondP = index;
		return true;
	}

	public boolean deleteMin(Double value, Integer index) {
		if (sortedSet.isEmpty()) {
			return false;
		}
		value = sortedSet.first().getFirst();
		index = sortedSet.first().getSecond();
		sortedSet.remove(sortedSet.first());
		this.deleteMinFirstP = value;
		this.deleteMinSecondP = index;
		return true;
	}

	public void insert(Double value, Integer index) {
		if (index >= indexToIterator.size()) {
			// resize the index_to_iterator
			int newSize = (index + 1) - indexToIterator.size();
			if (indexToIterator.size() > (index + 1)) {
				for (int i = 0; i < Math.abs(newSize); i++) {
					indexToIterator.remove(indexToIterator.size() - 1);
				}
			} else {
				for (int i = 0; i < newSize; i++) {
					indexToIterator.add(new Pair<Double, Integer>(0.0, 1));
				}
			}
		}
		Pair<Double, Integer> pair = new Pair<Double, Integer>(value, index);
		sortedSet.add(pair);
		indexToIterator.set(index, pair);
	}

	public final void decreaseKey(Double new_value, Integer index) {
		sortedSet.remove(indexToIterator.get(index));
		Pair<Double, Integer> pair = new Pair<Double, Integer>(new_value, index);
		sortedSet.add(pair);
		indexToIterator.set(index, pair);
	}

	public final void deleteElement(Integer index) {
		sortedSet.remove(indexToIterator.get(index));
	}

	public int getQueueSize() {
		return sortedSet.size();
	}

	public void print() {
		int ii = 0;
		for (Pair<Double, Integer> pair : sortedSet) {
			System.out.format("(ii:%d) %f %d\n", ii, pair.getFirst(), pair.getSecond());
			ii++;
		}
	}

	public static class Comp implements Comparator<Pair<Double, Integer>> {
		public int compare(Pair<Double, Integer> pair1, Pair<Double, Integer> pair2) {
			if (pair1.equals(null) || pair2.equals(null)) {
				new IllegalArgumentException("cannot be null");
				System.exit(0);
				return -1;
			}
			if ((pair1.getFirst() - pair2.getFirst()) > 0) {
				return 1;
			} else if ((pair1.getFirst() - pair2.getFirst()) < 0) {
				return -1;
			} else {
				if ((pair1.getSecond() - pair2.getSecond()) > 0) {
					return 1;
				} else if ((pair1.getSecond() - pair2.getSecond()) < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}

	public static void main(String args[]) {
		SortedSet<Pair<Double, Integer>> sorted_set = new TreeSet<Pair<Double, Integer>>(new Comp());
		sorted_set.add(new Pair<Double, Integer>(1.0, 1));
		sorted_set.add(new Pair<Double, Integer>(1.0, 2));
		sorted_set.add(new Pair<Double, Integer>(1.5, 1));
		int i = 0;
		for (Pair<Double, Integer> pair : sorted_set) {
			System.out.println("(i:" + i++ + ") " + pair.getFirst() + " " + pair.getSecond());
		}
	}
}