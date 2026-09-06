package com.maciej.spredex;

public enum ErrorType {
	CYCLE,
	IDENTIFIER,
	TOKEN,
	PARSE,
	TYPE,
	DIV,
	NOTFOUND;

	@Override
	public String toString() {
		return "#" + name();
	}
}
