package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public interface CellRangeResolver {
	public void add(CellRange range);
	public void deleteOccurance(CellRange range);
	public List<CellRange> getRangesContainingCell(CellLoc cell);
	public int size();
}
