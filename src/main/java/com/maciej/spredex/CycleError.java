package com.maciej.spredex;

import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Errors.ExcelError;

public class CycleError extends ExcelError {
	public CycleError(String errorMessage) {
		super(ErrorType.CYCLE, errorMessage);
	}
}
