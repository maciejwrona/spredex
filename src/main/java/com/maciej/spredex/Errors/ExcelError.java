package com.maciej.spredex.Errors;

public abstract class ExcelError extends RuntimeException {
	private final ErrorType type;
	private final String errorMessage;

	public ErrorType type() { return type; }
	public String message() { return errorMessage; }

	public ExcelError(ErrorType type, String errorMessage) {
		this.type = type;
		this.errorMessage = errorMessage;
		System.out.println(errorMessage);
	}
}
