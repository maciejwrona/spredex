package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

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

	@Override
	public boolean equals(Object other) {
		if (other == null || getClass() != other.getClass()) return false;

		RangeRef that = (RangeRef)other;

		return (left.equals(that.left) && right.equals(that.right));
	}

	@Override
	public CellCoordinates toCoordinates(CellLoc currentLocation) {
		return new CellRange(
				SingleCellRef.refToLoc(left, currentLocation),
				SingleCellRef.refToLoc(left, currentLocation));
	}
}
