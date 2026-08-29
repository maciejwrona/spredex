package com.maciej.spredex.Function;

import java.util.List;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.ErrorType;
import com.maciej.spredex.Sheet.Sheet;

public abstract class SpredexFunction {
	private final String name;
	private final Arity arity;

	public SpredexFunction(String name, Arity arity) {
		this.name = name;
		this.arity = arity;
	}

	public String name() { return name; }
	public Arity arity() { return arity; }

	public abstract Object call(List<Object> arguments, CellLoc location, Sheet sheet);

	protected CellError typeError(String message) {
		return new CellError(ErrorType.TYPE, message);
	}
}
