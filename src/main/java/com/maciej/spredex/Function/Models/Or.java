package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Sheet.Sheet;

public class Or extends SpredexFunction {
	public Or() {
		super("OR", new Arity.Minimum(2));
	}

	@Override
	public Object call(
			List<Object> arguments, CellLoc location, Sheet sheet, Interpreter interpreter) {
		for (Object arg : arguments) {
			if (FunctionUtils.isTrue(arg, sheet)) {
				return true;
			}
		}

		return false;
	}
}
