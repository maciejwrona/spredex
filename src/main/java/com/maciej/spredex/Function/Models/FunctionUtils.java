package com.maciej.spredex.Function.Models;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.ErrorType;
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
			case CellLoc loc -> (sheet.isCellEmpty(loc) ? 0 : 1);
			case CellRange range -> sheet.cellsInRange(range).size();
			default -> 1;
		};
	}

	public static boolean isTrue(Object argument, Sheet sheet) {
		return switch (argument) {
			case CellLoc loc -> isLocTrue(loc, sheet);
			default -> isValueTrue(argument);
		};
	}

	private static boolean isLocTrue(CellLoc loc, Sheet sheet) {
		return (!sheet.isCellEmpty(loc) && isValueTrue(sheet.valueAt(loc)));
	}

	private static boolean isValueTrue(Object value) {
		return switch (value) {
			case Boolean b -> b;
			case Double d -> d != 0;
			case String s -> !s.isBlank();
			default -> throw argumentTypeError(value);
		};
	}

	public static CellRange firstColumn(CellRange range) {
		return new CellRange(
				new CellLoc(range.left().row(), range.left().column()), 
				new CellLoc(range.right().row(), range.left().column())
				);
	}

	public static CellError typeError(String message) {
		return new CellError(ErrorType.TYPE, message);
	}

	public static CellError notFound(Object value) {
		return new CellError(ErrorType.NOTFOUND, "Did not find value " + value + ".");
	}

	public static CellError outOfBoundsRange() {
		return new CellError(ErrorType.IDENTIFIER, "Out of bounds range.");
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

	public static CellRange getRangeArgument(Object argument) {
		return switch (argument) {
			case CellRange range -> range;
			default -> throw argumentTypeError(argument);
		};
	}

	public static int getIntArgument(Object argument) {
		return switch (argument) {
			case Integer i -> i;
			default -> throw argumentTypeError(argument);
		};
	}
}
