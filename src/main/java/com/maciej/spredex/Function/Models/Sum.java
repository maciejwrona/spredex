package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.FunctionUtils;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Sheet.Sheet;

public class Sum extends SpredexFunction {
	public Sum() {
		super("Sum", new Arity.Minimum(1));
	}

	@Override
	public Object call(
			List<Object> arguments, CellLoc location, Sheet sheet) {
		double result = 0;
		for (Object arg : arguments) {
			result += FunctionUtils.getSum(arg, location, sheet);
		}
		return result;
	}

}
