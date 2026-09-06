package com.maciej.spredex.Function;

import javax.swing.SwingConstants;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.ErrorType;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Sheet.EmptyCell;
import com.maciej.spredex.Sheet.Sheet;

public class FunctionUtils {
	public static double getSum(Object arg, CellLoc location, Sheet sheet) {
		return switch (arg) {
			case Double d -> d;
			case CellLoc cell -> getLocSum(cell, location, sheet);
			case CellRange range -> getRangeSum(range, location, sheet);
			default -> throw argumentTypeError(arg);
		};
	}

	public static double getLocSum(CellLoc loc, CellLoc currentLocation, Sheet sheet) {
		Double result = sheet.numberValueAt(loc);

		if (result == null) {
			throw cellTypeError(loc, sheet.valueAt(loc));
		}
		return result;
	}
	
	public static double getRangeSum(CellRange range, CellLoc location, Sheet sheet) {
		double result = 0;

		for (CellLoc cell : sheet.cellsInRange(range)) {
			result += getLocSum(cell, location, sheet);
		}

		return result;
	}
	
	public static int countNotEmpty(Object arg, CellLoc location, Sheet sheet) {
		return switch (arg) {
			case CellLoc loc -> (isCellEmpty(loc, sheet) ? 0 : 1);
			case CellRange range -> sheet.cellsInRange(range).size();
			default -> 1;
		};
	}

	public static boolean isTrue(Object argument, Sheet sheet) {
		return switch (argument) {
			case Boolean b -> b;
			case CellLoc loc -> locToBoolean(loc, sheet);
			default -> throw argumentTypeError(argument);
		};
	}

	private static boolean locToBoolean(CellLoc loc, Sheet sheet) {
		return switch (sheet.valueAt(loc)) {
			case Boolean b -> b;
			default -> throw argumentTypeError(sheet.valueAt(loc));
		};
	}

	public static CellError typeError(String message) {
		return new CellError(ErrorType.TYPE, message);
	}

	public static CellError argumentTypeError(Object argument) {
		return typeError("Argument " + argument + 
						 " has invalid type " + argument.getClass() + ".");
	}

	public static CellError cellTypeError(CellLoc at, Object value) {
		return typeError("Invalid type " + value.getClass() + " at required cell " + at + ".");
	}

	public static CellError divisionByZero() {
		return new CellError(ErrorType.DIV, "Attempt to divide by zero.");
	}

	private static boolean isCellEmpty(CellLoc cell, Sheet sheet) {
		return sheet.valueAt(cell) == new EmptyCell();
	}
}
