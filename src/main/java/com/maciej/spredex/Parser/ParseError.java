package com.maciej.spredex.Parser;

import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Errors.ExcelError;

public class ParseError extends ExcelError {
	public ParseError(String errorMessage) {
		super(ErrorType.PARSE, errorMessage);
	}
}

