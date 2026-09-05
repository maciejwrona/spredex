package com.maciej.spredex;

public record CellLoc(int row, int column) implements CellCoordinates {
	private final static int numberOfChars = 'Z' - 'A' + 1;
	public static int numberOfChars() { return numberOfChars; }

	@Override
	public String toString() {
		return numberToColumn(column) + Integer.toString(row);
	}

	public static String numberToColumn(int number) {
		String result = "";

		while (number > 0) {
			result = Character.toString((number - 1) % numberOfChars + 'A') + result;
			number = (number - 1) / numberOfChars;
		}

		return result;
	}
}
