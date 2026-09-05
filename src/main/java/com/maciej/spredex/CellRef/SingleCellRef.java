package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;

public class SingleCellRef extends CellRef {
	private final int row;
	private final int column;
	private final boolean lockedRow;
	private final boolean lockedColumn;

	public int row() { return row; }
	public int column() { return column; }
	public boolean lockedRow() { return lockedRow; }
	public boolean lockedColumn() { return lockedColumn; }

	public SingleCellRef(int row, int column, boolean lockedRow, boolean lockedColumn) {
		this.row = row;
		this.column = column;
		this.lockedRow = lockedRow;
		this.lockedColumn = lockedColumn;
	}

	public boolean hasUnboundedColumn() {
		return column == unbounded();
	}

	public boolean hasUnboundedRow() {
		return row == unbounded();
	}

	@Override
	public <T> T accept(CellRefVisitor<T> visitor) {
		return visitor.visitSingleCellRef(this);
	}

	@Override
	public CellCoordinates toCoordinates(CellLoc currentLocation, int maxRow, int maxColumn) {
		return toLoc(currentLocation);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || getClass() != other.getClass()) return false;

		SingleCellRef that = (SingleCellRef)other;

		return (row == that.row && column == that.column && 
				lockedRow == that.lockedRow && lockedColumn == that.lockedColumn);
	}

	public CellLoc toLoc(CellLoc currentLocation) {
		int locRow = lockedRow ? row : (row + currentLocation.row());
		int locColumn = lockedColumn ? column : (column + currentLocation.column());

		return new CellLoc(locRow, locColumn);
	}
}
