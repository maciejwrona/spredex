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

	@Override
	public <T> T accept(CellRefVisitor<T> visitor) {
		return visitor.visitSingleCellRef(this);
	}

	@Override
	public CellCoordinates toCoordinates(CellLoc currentLocation) {
		return refToLoc(this, currentLocation);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || getClass() != other.getClass()) return false;

		SingleCellRef that = (SingleCellRef)other;

		return (row == that.row && column == that.column && 
				lockedRow == that.lockedRow && lockedColumn == that.lockedColumn);
	}

	public static CellLoc refToLoc(SingleCellRef ref, CellLoc currentLocation) {
		int row = 
			ref.lockedRow ? ref.row : ref.row + currentLocation.row();
		int column = 
			ref.lockedColumn ? ref.column : ref.column + currentLocation.column();
		return new CellLoc(row, column);
	}
}
