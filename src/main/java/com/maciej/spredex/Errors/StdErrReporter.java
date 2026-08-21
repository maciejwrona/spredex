package com.maciej.spredex.Errors;

public class StdErrReporter implements ErrorReporter {
	@Override
	public void report(ExcelError error) {
		System.err.println(error.message());
	}
}
