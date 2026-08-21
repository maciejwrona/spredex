package com.maciej.spredex.Interpreter;

import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Errors.ExcelError;

public class ExecutionError extends ExcelError {
	public ExecutionError(ErrorType type, String errorMessage) {
		super(type, errorMessage);
	}
}
