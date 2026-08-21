package com.maciej.spredex.Cell;

public abstract class CellRef {
	public abstract <T> T accept(CellRefVisitor<T> visitor);
}
