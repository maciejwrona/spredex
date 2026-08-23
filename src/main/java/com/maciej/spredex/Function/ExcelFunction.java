package com.maciej.spredex.Function;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Sheet.Sheet;

public interface ExcelFunction {
	public Object call(List<Object> arguments, CellLoc location, Sheet sheet);
	public int arity();
}
