package com.maciej.spredex.Function;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Sheet.Sheet;

public class Sum extends SpredexFunction {

	public Sum() {
		super("Sum", new Arity.Any());
	}

	@Override
	public Object call(List<Object> arguments, CellLoc location, Sheet sheet) {
		double result = 0;
		for (Object arg : arguments) {
			result += getSum(arg, location, sheet);
		}
		return result;
	}

	private double getSum(Object arg, CellLoc location, Sheet sheet) {
		return switch (arg) {
			case Double d -> d;
			case RangeRef ref -> getRefSum(ref, location, sheet);
			default -> throw typeError("Invalid argument type");
		};
	}
	
	private double getRefSum(RangeRef ref, CellLoc location, Sheet sheet) {
		double result = 0;
		CellRange range = ref.toRange(
				location, sheet.getRowCount(), sheet.getColumnCount());

		for (CellLoc cell : sheet.cellsInRange(range)) {
			result += switch (sheet.valueAt(cell)) {
				case Double d -> d;
				default -> throw typeError("Invalid argument type");
			};
		}

		return result;
	}
}
