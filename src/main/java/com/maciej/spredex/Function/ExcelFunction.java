package com.maciej.spredex.Function;

import java.util.List;

import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Sheet.Sheet;

public interface ExcelFunction {
	public Object call(List<Object> arguments, Sheet sheet, Interpreter interpreter);
	public int arity();
}
