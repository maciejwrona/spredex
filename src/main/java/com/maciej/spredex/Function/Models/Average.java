package com.maciej.spredex.Function.Models;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Function.Arity;
import com.maciej.spredex.Function.FunctionUtils;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Sheet.Sheet;

public class Average extends SpredexFunction {
	public Average() {
		super("Average", new Arity.Minimum(1));
	}

	@Override
	public Object call(List<Object> arguments, CellLoc location, Sheet sheet) {
		double sum = 0;
		int numberOfRecords = 0;

		for (Object arg : arguments) {
			sum += FunctionUtils.getSum(arg, location, sheet);
			numberOfRecords += FunctionUtils.countNotEmpty(arg, location, sheet);
		}

		if (numberOfRecords == 0) {
			throw FunctionUtils.divisionByZero();
		}

		return sum / numberOfRecords;
	}
}
