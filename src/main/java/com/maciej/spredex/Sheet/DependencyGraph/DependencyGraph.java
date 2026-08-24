package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Errors.ExcelError;

public class DependencyGraph {
	private final Map<CellLoc, Set<CellLoc>> singleRequired = new HashMap<>();
	private final Map<CellLoc, Set<CellRange>> rangesRequired = new HashMap<>();

	private final Map<CellLoc, Set<CellLoc>> singleDependent = new HashMap<>();

	private final Map<CellRange, Set<CellLoc>> dependentOnRange = new HashMap<>();
	private final CellRangeResolver rangeResolver = new DoubleTreeResolver();

	public void updateRequired(CellLoc by, Collection<CellCoordinates> required) {
		deletePreviousRequired(by);
		setNewRequired(by, required);
		updateNewDependents(by);
	}

	public Collection<CellLoc> getUpdateOrder(CellLoc start) {
		LinkedHashSet<CellLoc> result = new LinkedHashSet<>();

		recursiveUpdateOrder(start, result);

		return result;
	}


	private void deletePreviousRequired(CellLoc by) {
		for (CellLoc cell : singleRequired.getOrDefault(by, new HashSet<>())) {
			singleDependent.get(cell).remove(by);
		}
		for (CellRange range : rangesRequired.getOrDefault(by, new HashSet<>())) {
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

	private void recursiveUpdateOrder(CellLoc start, LinkedHashSet<CellLoc> result) {
		if (result.contains(start)) {
			throw new ExcelError(
					ErrorType.CYCLE, "Reference cycle detected at cell " + start + ".");
		}
		result.add(start);

		for (CellLoc cell : singleDependent.getOrDefault(start, new HashSet<>())) {
			recursiveUpdateOrder(cell, result);	
		}

		for (CellRange range : rangeResolver.getRangesContainingCell(start)) {
			for (CellLoc cell : dependentOnRange.get(range)) {
				recursiveUpdateOrder(cell, result);
			}
		}
	}
}
