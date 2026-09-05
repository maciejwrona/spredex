package com.maciej.spredex.Function;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.ErrorType;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Sheet.Sheet;

public class FunctionUtils {
	public static double getSum(Object arg, CellLoc location, Sheet sheet) {
		return switch (arg) {
			case Double d -> d;
			case RangeRef ref -> getRefSum(ref, location, sheet);
			default -> throw argumentTypeError(arg);
		};
	}
	
	public static double getRefSum(RangeRef ref, CellLoc location, Sheet sheet) {
		double result = 0;
		CellRange range = ref.toRange(
				location, sheet.getRowCount(), sheet.getColumnCount());

		for (CellLoc cell : sheet.cellsInRange(range)) {
			result += switch (sheet.valueAt(cell)) {
				case Double d -> d;
				default -> throw cellTypeError(cell, sheet.valueAt(cell));
			};
		}

		return result;
	}
	
	public static int getNumberOfRecordsInArgument(
			Object argument, CellLoc location, Sheet sheet) {
		return switch(argument) {
			case RangeRef ref -> getNumberOfCellsInRange(ref, location, sheet);
			default -> 1;
		};
	}

	public static int getNumberOfCellsInRange(RangeRef ref, CellLoc location, Sheet sheet) {
		CellRange range = ref.toRange(
				location, sheet.getRowCount(), sheet.getColumnCount());

		return (range.right().row() - range.left().row() + 1) * 
			   (range.right().column() - range.left().column() + 1);
	}

	public static boolean isTrue(Object argument) {
		return switch(argument) {
			case Boolean b -> b;
			default -> throw argumentTypeError(argument);
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
}
