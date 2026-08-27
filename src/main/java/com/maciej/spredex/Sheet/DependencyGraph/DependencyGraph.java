package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public class DependencyGraph {
	private final Map<CellLoc, Set<CellLoc>> singleRequired = new HashMap<>();
	private final Map<CellLoc, Set<CellRange>> rangesRequired = new HashMap<>();

	private final Map<CellLoc, Set<CellLoc>> singleDependent = new HashMap<>();

	private final Map<CellRange, Set<CellLoc>> dependentOnRange = new HashMap<>();
	private final CellRangeResolver rangeResolver;

	public DependencyGraph(int maxRows) {
		this.rangeResolver = new DoubleTreeResolver(maxRows);
	}

	public void updateRequired(CellLoc by, Collection<CellCoordinates> required) {
		deletePreviousRequired(by);
		setNewRequired(by, required);
		updateNewDependents(by);
	}

	private void deletePreviousRequired(CellLoc by) {
		for (CellLoc cell : singleRequired.getOrDefault(by, Collections.emptySet())) {
			singleDependent.get(cell).remove(by);
		}
		for (CellRange range : rangesRequired.getOrDefault(by, Collections.emptySet())) {
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

	private enum VisitedState {
		IN_CURRENT_SUBGRAPH,
		IN_WHOLE_GRAPH,
	}

	public List<CellLoc> getUpdateOrder(CellLoc start) {
		Map<CellLoc, VisitedState> visited = new HashMap<>();
		List<CellLoc> result = new ArrayList<>();

		recursiveUpdateOrder(start, visited, result);


		Collections.reverse(result);
		return result;
	}

	private void recursiveUpdateOrder(CellLoc start, Map<CellLoc, VisitedState> visited, 
									  List<CellLoc> result) {
		if (visited.get(start) == VisitedState.IN_CURRENT_SUBGRAPH) {
			throw new CycleError("Reference cycle detected at cell " + start + ".");
		}
		else if (visited.get(start) == VisitedState.IN_WHOLE_GRAPH) {
			return;
		}

		visited.put(start, VisitedState.IN_CURRENT_SUBGRAPH);

		for (CellLoc cell : singleDependent.getOrDefault(start, Collections.emptySet())) {
			recursiveUpdateOrder(cell, visited, result);	
		}

		for (CellRange range : rangeResolver.getRangesContainingCell(start)) {
			for (CellLoc cell : dependentOnRange.get(range)) {
				recursiveUpdateOrder(cell, visited, result);
			}
		}

		result.add(start);
		visited.put(start, VisitedState.IN_WHOLE_GRAPH);
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
