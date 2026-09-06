package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Sheet.Sheet;

public class Vlookup extends SpredexFunction {
	public Vlookup() {
		super("VLOOKUP", new Arity.Fixed(3));
	}

	@Override
	public Object call(
			List<Object> arguments, CellLoc location, Sheet sheet, Interpreter interpreter) {
		Object desiredValue = arguments.get(0);
		CellRange lookupTable = FunctionUtils.getRangeArgument(arguments.get(1));
		int valueColumn = FunctionUtils.getIntArgument(arguments.get(2));

		if (valueColumn + lookupTable.left().column() > lookupTable.right().column()) {
			throw FunctionUtils.outOfBoundsRange();
		}

		CellRange lookupColumn = FunctionUtils.firstColumn(lookupTable);
		return lookup(desiredValue, lookupColumn, valueColumn, sheet, interpreter);
	}

	private Object lookup(Object value, CellRange column, int valueColumn, 
						  Sheet sheet, Interpreter interpreter) {

		for (CellLoc cell : sheet.cellsInRange(column)) {
			if (interpreter.equal(value, sheet.valueAt(cell))) {
				CellLoc answerLocation = new CellLoc(cell.row(), cell.column() + valueColumn);
				return sheet.valueAt(answerLocation);
			}
		}

		throw FunctionUtils.notFound(value);
	}
}
