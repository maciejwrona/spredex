package com.maciej.spredex;

public record CellRange(CellLoc left, CellLoc right) implements CellCoordinates {
	@Override
	public String toString() {
		return left.toString() + ":" + right.toString();
	}
}
