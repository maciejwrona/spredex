package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;

public abstract class CellRef {
	public abstract <T> T accept(CellRefVisitor<T> visitor);
	public abstract CellCoordinates toCoordinates(CellLoc currentLocation);

	public static CellLoc refToLoc(SingleCellRef ref, CellLoc currentLocation) {
		int row = 
			ref.lockedRow() ? ref.row() : ref.row() + currentLocation.row();
		int column = 
			ref.lockedColumn() ? ref.column() : ref.column() + currentLocation.column();
		return new CellLoc(row, column);
	}
}
