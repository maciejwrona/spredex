package com.maciej.spredex.Cell;

public record CellLocation(int row, int column) {
	public CellLocation(SingleCellRef reference, Cell location) {
		this(
		 	reference.row() + (reference.lockedRow() ? 0 : location.row()),
		 	reference.column() + (reference.lockedColumn() ? 0 : location.column())
		);
	}
}
