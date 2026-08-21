package com.maciej.spredex.Parser.Lexer;

import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Errors.ExcelError;

public class TokenError extends ExcelError {
	public TokenError(String errorMessage) {
		super(ErrorType.TOKEN, errorMessage);
	}
}

