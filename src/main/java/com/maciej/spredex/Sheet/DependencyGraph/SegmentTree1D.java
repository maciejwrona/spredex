package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SegmentTree1D {
	private enum UpdateType {
		ADD,
		REMOVE
	}

	private final int minValue;
	private final int maxValue;
	private final Map<Integer, Set<FlatRange>> tree = new HashMap<>();
	private int size = 0;

	public SegmentTree1D(int minValue, int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public int size() {
		return size;
	}

	public void add(FlatRange range) {
		updateRecursively(range, 1, minValue, maxValue, UpdateType.ADD);		
		size++;
	}

	public void remove(FlatRange range) {
		updateRecursively(range, 1, minValue, maxValue, UpdateType.REMOVE);
		size--;
	}

	private void updateRecursively(FlatRange range, 
			int node, int nodeStart, int nodeEnd, UpdateType updateType) {
		
		if (range.left() <= nodeStart && range.right() >= nodeEnd) {
			switch (updateType) {
				case ADD: 
					tree.computeIfAbsent(node, (k) -> new HashSet<>()).add(range);
					return;
				case REMOVE:
					tree.get(node).remove(range);
					return;
			}
		}
		
		int mid = (nodeStart + nodeEnd) / 2;
		if (range.left() <= mid) {
			updateRecursively(range, 2 * node, nodeStart, mid, updateType);
		}
		if (range.right() > mid) {
			updateRecursively(range, 2 * node + 1, mid + 1, nodeEnd, updateType);
		}
	}

	public List<FlatRange> getRangesContaining(int point) {
		List<FlatRange> result = new ArrayList<>();
		recursiveQuery(point, 1, minValue, maxValue, result);
		return result;
	}

	private void recursiveQuery(int point, 
			int node, int nodeStart, int nodeEnd, List<FlatRange> result) {

		if (tree.containsKey(node)) {
			for (FlatRange range : tree.get(node)) {
				result.add(range);
			}
		}

		if (nodeStart == nodeEnd) {
			return;
		}

		int mid = (nodeStart + nodeEnd) / 2;
		if (point <= mid) {
			recursiveQuery(point, 2 * node, nodeStart, mid, result);
		}
		else {
			recursiveQuery(point, 2 * node + 1, mid + 1, nodeEnd, result);
		}
	}
}
