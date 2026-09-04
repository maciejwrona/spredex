package com.maciej.spredex.CellRef;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.ErrorType;

public class RangeRef extends CellRef {
	private final SingleCellRef left;
	private final SingleCellRef right;

	public SingleCellRef left() { return left; }
	public SingleCellRef right() { return right; }

	public RangeRef(SingleCellRef left, SingleCellRef right) {
		this.left = left;
		this.right = right;

		// Is invalid if left has unbounded row and right not, same for column
		if ((left.hasUnboundedColumn() != right.hasUnboundedColumn()) ||
			(left.hasUnboundedRow() != right.hasUnboundedRow())) {
			throw new CellError(ErrorType.PARSE, "Invalid range cell reference.");
		}
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
	public CellCoordinates toCoordinates(CellLoc currentLocation, int maxRow, int maxColumn) {
		return toRange(currentLocation, maxRow, maxColumn);
	}

	public CellRange toRange(CellLoc currentLocation, int maxRow, int maxColumn) {
		// Convert unbounded rows / columns to 0-max
		CellLoc newLeft = left.toLoc(currentLocation);
		CellLoc newRight = right.toLoc(currentLocation);

		if (right.row() == unbounded()) {
			newRight = new CellLoc(maxRow, newRight.column());
		}
		else if (right.column() == unbounded()) {
			newRight = new CellLoc(newRight.row(), maxColumn);
		}

		return new CellRange(newLeft, newRight);
	}
}
