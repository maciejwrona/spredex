package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;

public abstract class CellRef {
	public abstract <T> T accept(CellRefVisitor<T> visitor);
	public abstract CellCoordinates toCoordinates(CellLoc currentLocation, 
												  int maxRow, int maxColumn);
}
