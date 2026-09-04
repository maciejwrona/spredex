package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;

public abstract class CellRef {
	private final static int unbounded = Integer.MAX_VALUE;
	public static int unbounded() { return unbounded; }

	public abstract <T> T accept(CellRefVisitor<T> visitor);
	public abstract CellCoordinates toCoordinates(CellLoc currentLocation, 
												  int maxRow, int maxColumn);
}
