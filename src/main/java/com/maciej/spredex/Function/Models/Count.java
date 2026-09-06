package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Sheet.Sheet;
import com.maciej.spredex.Interpreter.Interpreter;

public class Count extends SpredexFunction {
	public Count() {
		super("COUNT", new Arity.Minimum(1));
	}

	@Override
	public Object call(
			List<Object> arguments, CellLoc location, Sheet sheet, Interpreter interpreter) {
		int result = 0;
		for (Object arg : arguments) {
			result += FunctionUtils.countNotEmpty(arg, location, sheet);
		}
		return result;
	}
}
