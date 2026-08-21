package com.maciej.spredex.Cell;

public interface CellRefVisitor<T> {
	T visitSingleCellRef(SingleCellRef ref);
	T visitRangeRef(RangeRef ref);
}
