package com.maciej.spredex.Sheet.DependencyGraph;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public record FlatRange(int left, int right) {
	public FlatRange(CellRange flatRange) {
		this(flatRange.left().row(), flatRange.right().row());
	}

	public CellRange toCellRange(int column) {
		return new CellRange(
				new CellLoc(left, column),
				new CellLoc(right, column)
		);
	}

	public static boolean isFlat(CellRange range) {
		return (range.left().column() == range.right().column());
	}
}

