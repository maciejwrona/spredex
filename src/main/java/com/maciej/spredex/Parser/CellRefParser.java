package com.maciej.spredex.Parser;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.SingleCellRef;

public class CellRefParser {
	private final String textRef;

	private int current = 0;

	public CellRefParser(String textRef) {
		this.textRef = textRef;
	}

	public SingleCellRef parse(CellLoc currentCell) {
		boolean lockedColumn = match('$');
		int columnStart = current;
		while (Character.isAlphabetic(peek())) {
			nextChar();
		}
		int column = getColumnNumber(textRef.substring(columnStart, current));

		boolean lockedRow = false;
		// if no column provided, then the $ belongs to the row
		if (column == 0 && lockedRow) {
			lockedColumn = false;
			lockedRow = true;
		}
		else {
			lockedRow = match('$');
		}
		int rowStart = current;
		while (Character.isDigit(peek())) {
			nextChar();
		}
		int row = getRowNumber(textRef.substring(rowStart, current));

		// Invalid reference
		if (!isAtEnd() ||
			(lockedRow && row == 0)) {
			throw new ParseError("Reference parsing error.");
		}

		return new SingleCellRef((lockedRow ? row : row - currentCell.row()), 
								 (lockedColumn ? column : column - currentCell.column()), 
								  lockedRow, lockedColumn);
	}

	private boolean isAtEnd() {
		return current >= textRef.length();
	}

	private char peek() {
		if (isAtEnd()) return '\0';
		return textRef.charAt(current);
	}

	private char nextChar() {
		return textRef.charAt(current++);
	}

	private boolean match(char c) {
		if (peek() != c) return false;

		nextChar();
		return true;
	}

	private int getColumnNumber(String column) {
		int result = 0;

		for (int i = column.length() - 1; i >= 0; i--) {
			result += 
				Math.pow('z' - 'a' + 1, (column.length() - 1) - i) * 
				(Character.toLowerCase(column.charAt(i)) - 'a' + 1);
		}

		return result;
	}

	private int getRowNumber(String row) {
		if (row.equals("")) return 0;
		return Integer.parseInt(row);
	}
}
