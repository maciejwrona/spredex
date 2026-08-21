package com.maciej.spredex.CellRef;

public interface CellRefVisitor<T> {
	T visitSingleCellRef(SingleCellRef ref);
	T visitRangeRef(RangeRef ref);
}
