package com.maciej.spredex.Parser;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.ErrorType;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.CellRef.SingleCellRef;

public class CellRefParser {
	private final String textRef;
	private final int maxRow;
	private final int maxColumn;

	private int current = 0;

	public CellRefParser(String textRef, int maxRow, int maxColumn) {
		this.textRef = textRef;
		this.maxRow = maxRow;
		this.maxColumn = maxColumn;
	}

	public SingleCellRef parse(CellLoc currentCell) {
		boolean lockedColumn = match('$');
		int columnStart = current;
		while (Character.isLetter(peek())) {
			nextChar();
		}
		int column = getColumnNumber(textRef.substring(columnStart, current));

		boolean lockedRow = false;
		// if no column provided, then the $ belongs to the row
		if (column == 0 && lockedColumn) {
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
			(lockedRow && row == 0) ||
			(row >= maxRow || column >= maxColumn)) {
			throw new CellError(ErrorType.PARSE, "Reference parsing error.");
		}

		return getRefFromRead(row, column, lockedRow, lockedColumn, currentCell);
	}

	private SingleCellRef getRefFromRead(int readRow, int readColumn, 
										 boolean lockedRow, boolean lockedColumn, CellLoc location) {
		int row;
		if (lockedRow) {
			row = readRow;
		}	
		else if (readRow == 0) {
			row = CellRef.unbounded();
		}
		else {
			row = readRow - location.row();
		}

		int column;
		if (lockedColumn) {
			column = readColumn;
		}	
		else if (readColumn == 0) {
			column = CellRef.unbounded();
		}
		else {
			column = readColumn - location.column();
		}

		return new SingleCellRef(row, column, lockedRow, lockedColumn);
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
