package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public class DependencyGraph {
	private final Map<CellLoc, Set<CellLoc>> singleRequired = new HashMap<>();
	private final Map<CellLoc, Set<CellRange>> rangesRequired = new HashMap<>();

	private final Map<CellLoc, Set<CellLoc>> singleDependent = new HashMap<>();

	private final Map<CellRange, Set<CellLoc>> dependentOnRange = new HashMap<>();
	private final CellRangeResolver rangeResolver;

	private final Set<CellLoc> cycleCache = new HashSet<>();

	public DependencyGraph(int maxRows) {
		this.rangeResolver = new DoubleTreeResolver(maxRows);
	}

	public boolean isInCycle(CellLoc cell) {
		return cycleCache.contains(cell);
	}

	public void setRequired(CellLoc by, Collection<CellCoordinates> required) {
		deletePreviousRequired(by);
		setNewRequired(by, required);
		updateNewDependents(by);

		updateCycles(by);
	}

	private void deletePreviousRequired(CellLoc by) {
		for (CellLoc cell : getSingleRequired(by)) {
			singleDependent.get(cell).remove(by);
		}
		for (CellRange range : getRangesRequired(by)) {
			dependentOnRange.get(range).remove(by);
			rangeResolver.deleteOccurance(range);
		}
	}

	private void setNewRequired(CellLoc by, Collection<CellCoordinates> newRequired) {
		Set<CellLoc> newSingleRequired = new HashSet<>();
		Set<CellRange> newRangesRequired = new HashSet<>();

		for (CellCoordinates coordinates : newRequired) {
			if (coordinates instanceof CellLoc) {
				newSingleRequired.add((CellLoc)coordinates);
			}
			else if (coordinates instanceof CellRange) {
				newRangesRequired.add((CellRange)coordinates);
			}
		}

		singleRequired.put(by, newSingleRequired);
		rangesRequired.put(by, newRangesRequired);
	}

	private void updateNewDependents(CellLoc by) {
		for (CellLoc cell : singleRequired.get(by)) {
			singleDependent
				.computeIfAbsent(cell, (k) -> new HashSet<>()).add(by);
		}
		for (CellRange range : rangesRequired.get(by)) {
			dependentOnRange
				.computeIfAbsent(range, (k) -> new HashSet<>()).add(by);
			rangeResolver.add(range);
		}
	}

	private void updateCycles(CellLoc start) {
		Set<CellLoc> toUpdate = getReachable(start);

		List<Set<CellLoc>> sccs = getSccs(toUpdate);

		Set<CellLoc> newCycleCacheFragment = getUpdatedCycleCacheFragment(sccs, toUpdate);

		// Synchronize with the cache
		for (CellLoc cell : toUpdate) {
			if (newCycleCacheFragment.contains(cell)) {
				cycleCache.add(cell);
			}
			else {
				cycleCache.remove(cell);
			}
		}
	}

	// gets all cells visible from start using bfs
	private Set<CellLoc> getReachable(CellLoc start) {
		Set<CellLoc> visited = new HashSet<>();
		Queue<CellLoc> next = new ArrayDeque<>();
		visited.add(start);
		next.add(start);

		while (!next.isEmpty()) {
			CellLoc currentCell = next.poll();

			for (CellLoc dependent : getDependent(currentCell)) {
				if (!visited.contains(dependent)) {
					visited.add(dependent);
					next.add(dependent);
				}
			}
		}

		return visited;
	}

	private List<Set<CellLoc>> getSccs(Set<CellLoc> toUpdate) {
		Map<CellLoc, Integer> cellId = new HashMap<>();
		Map<CellLoc, Integer> low = new HashMap<>();
		Set<CellLoc> onStack = new HashSet<>();
		Stack<CellLoc> stack = new Stack<>();
		int[] id = { 0 }; // use an array so increments percist between calls
		List<Set<CellLoc>> sccs = new ArrayList<>();

		for (CellLoc cell : toUpdate) {
			if (!cellId.containsKey(cell)) {
				tarjan(cell, id, cellId, low, stack, onStack, sccs);
			}
		}
		return sccs;
	}

	private void tarjan(
			CellLoc start, int[] id, Map<CellLoc, Integer> cellId, Map<CellLoc, Integer> low, 
			Stack<CellLoc> stack, Set<CellLoc> onStack, List<Set<CellLoc>> resultScc) {

		cellId.put(start, id[0]);
		low.put(start, id[0]);
		id[0]++;
		stack.push(start);
		onStack.add(start);

		for (CellLoc dependent : getDependent(start)) {
			if (!cellId.containsKey(dependent)) {
				tarjan(dependent, id, cellId, low, stack, onStack, resultScc);
				low.put(start, Math.min(low.get(start), low.get(dependent)));
			}
			else if (onStack.contains(dependent)) {
				low.put(start, Math.min(low.get(start), low.get(dependent)));
			}
		}

		// Found scc
		if (low.get(start) == cellId.get(start)) {
			Set<CellLoc> scc = new HashSet<>();
			while (true) {
				CellLoc node = stack.pop();
				onStack.remove(node);
				scc.add(node);

				if (node.equals(start)) {
					break;
				}
			}
			resultScc.add(scc);
		}
	}

	private Set<CellLoc> getUpdatedCycleCacheFragment(List<Set<CellLoc>> sccs, Set<CellLoc> toUpdate) {
		// Get the cycle Cells
		Set<CellLoc> cycles = new HashSet<>();
		for (Set<CellLoc> scc : sccs) {
			if (isSccCycle(scc)) {
				cycles.addAll(scc);
			}
		}

		for (CellLoc cell : toUpdate) {
			if (!cycles.contains(cell) && dependsOnExternalCycle(cell, toUpdate)) {
				cycles.add(cell);
			}
		}

		// Propagate the cycle status to dependent cells using BFS
		Queue<CellLoc> next = new ArrayDeque<>(cycles);
		Set<CellLoc> newCycleCacheFragment = new HashSet<>(cycles);

		while (!next.isEmpty()) {
			CellLoc currentCell = next.poll();
			for (CellLoc dependent : getDependent(currentCell)) {
				if (toUpdate.contains(dependent) && newCycleCacheFragment.add(dependent)) {
					next.add(dependent);
				}
			}
		}

		return newCycleCacheFragment;
	}

	private boolean dependsOnExternalCycle(CellLoc cell, Set<CellLoc> internal) {
		for (CellLoc required : getSingleRequired(cell)) {
			if (!internal.contains(required) && cycleCache.contains(required)) {
				return true;
			}
		}

		for (CellRange requiredRange : getRangesRequired(cell)) {
			for (CellLoc cycleCell : cycleCache) {
				if (!internal.contains(cycleCell) && isInRange(cycleCell, requiredRange)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isInRange(CellLoc cell, CellRange range) {
		return cell.row() >= range.left().row()
			&& cell.column() >= range.left().column()
			&& cell.row() <= range.right().row()
			&& cell.column() <= range.right().column();
	}


	private boolean isSccCycle(Set<CellLoc> scc) {
		return (scc.size() > 1 ||
				// Check if the one cell has an edge pointing to itself
				(scc.size() == 1 && getDependent(scc.iterator().next()).contains(scc.iterator().next())));
	}

	// Returns the topological sort of the subgraph connected to start
	// If the graph is not a dag then returns the cells connected to start
	public List<CellLoc> getUpdateOrder(CellLoc start) {
		List<CellLoc> result = new ArrayList<>();

		postOrderDFS(start, new HashSet<>(), result);
		Collections.reverse(result);

		return result;
	}


	// Tarjan before does the same thing??
	private void postOrderDFS(CellLoc start, Set<CellLoc> visited, List<CellLoc> result) {
		visited.add(start);
		
		for (CellLoc dependent : getDependent(start)) {
			if (!visited.contains(dependent)) {
				postOrderDFS(dependent, visited, result);
			}
		}

		result.add(start);
	}

	public List<CellLoc> getDependent(CellLoc cell) {
		List<CellLoc> result = new ArrayList<>();

		for (CellLoc dependent : getSingleDependent(cell)) {
			result.add(dependent);
		}
		for (CellRange inside : rangeResolver.getRangesContainingCell(cell)) {
			for (CellLoc dependent : getDependentOnRange(inside)) {
				result.add(dependent);
			}
		}

		return result;
	}

	Set<CellLoc> getSingleRequired(CellLoc cell) {
		return singleRequired.getOrDefault(cell, Collections.emptySet());
	}
	Set<CellRange> getRangesRequired(CellLoc cell) {
		return rangesRequired.getOrDefault(cell, Collections.emptySet());
	}
	Set<CellLoc> getSingleDependent(CellLoc cell) {
		return singleDependent.getOrDefault(cell, Collections.emptySet());
	}
	Set<CellLoc> getDependentOnRange(CellRange range) {
		return dependentOnRange.getOrDefault(range, Collections.emptySet());
	}
}
