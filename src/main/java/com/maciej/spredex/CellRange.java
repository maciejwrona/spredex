package com.maciej.spredex;

public record CellRange(CellLoc left, CellLoc right) implements CellCoordinates {}
