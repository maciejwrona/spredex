package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.Errors.ExcelError;

public class CellRangeRTree {
	public void add(CellRange range) {}
	public void remove(CellRange range) {}
	public List<CellRange> getRangesContaining(CellLoc cell) { throw new ExcelError(null, null) {
		
	};}
}
