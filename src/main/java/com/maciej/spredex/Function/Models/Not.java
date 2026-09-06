package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Sheet.Sheet;

public class Not extends SpredexFunction {
	public Not() {
		super("NOT", new Arity.Fixed(1));
	}

	@Override
	public Object call(
			List<Object> arguments, CellLoc location, Sheet sheet, Interpreter interpreter) {
		return FunctionUtils.isTrue(arguments.get(0), sheet);
	}
}
