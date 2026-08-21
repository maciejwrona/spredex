package com.maciej.spredex.Cell;

public class RangeRef extends CellRef {
	private final SingleCellRef left;
	private final SingleCellRef right;

	public SingleCellRef left() { return left; }
	public SingleCellRef right() { return right; }

	public RangeRef(SingleCellRef left, SingleCellRef right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(CellRefVisitor<T> visitor) {
		return visitor.visitRangeRef(this);
	}
}
