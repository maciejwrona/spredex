package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.FunctionUtils;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Sheet.Sheet;

public class If extends SpredexFunction {
	public If() {
		super("IF", new Arity.Fixed(3));
	}

	@Override
	public Object call(List<Object> arguments, CellLoc location, Sheet sheet) {
		return (FunctionUtils.isTrue(arguments.get(0)) ?
				arguments.get(1) : arguments.get(2));
	}
}
