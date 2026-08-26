package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public class CellRangeContainer {
	private final Set<CellRange> ranges = new HashSet<>();

	public void add(CellRange range) {
		ranges.add(range);
	}

	public void remove(CellRange range) {
		ranges.remove(range);
	}

	public List<CellRange> getRangesContaining(CellLoc cell) {
		List<CellRange> result = new ArrayList<>();

		for (CellRange range : ranges) {
			if (contains(range, cell)) {
				result.add(range);
			}
		}

		return result;
	}

	private boolean contains(CellRange range, CellLoc cell) {
		return (range.left().column() <= cell.column() && 
				range.left().row() <= cell.row() &&
				range.right().column() >= cell.column() && 
				range.right().row() >= cell.row());
	}
}
