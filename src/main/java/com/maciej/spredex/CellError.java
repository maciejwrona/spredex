package com.maciej.spredex;

public class CellError extends RuntimeException {
	private final ErrorType type;
	private final String message;

	public CellError(ErrorType type, String message) {
		this.type = type;
		this.message = message;
	}

	public ErrorType getType() { return type; }
	public String getMessage() { return message; }
}

