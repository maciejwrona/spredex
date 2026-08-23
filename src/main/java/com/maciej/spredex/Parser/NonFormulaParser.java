package com.maciej.spredex.Parser;

import com.maciej.spredex.Parser.Expressions.Expression;

import java.util.ArrayList;

public class NonFormulaParser {
	private final String nonFormula;

	public NonFormulaParser(String nonFormula) {
		this.nonFormula = nonFormula;
	}

	public ParseResult parse() {
		Object value = nonFormula;
		try {
			value = Double.parseDouble(nonFormula);
		}
		catch (NumberFormatException e) {}

		return new ParseResult(
					new Expression.Literal(value), 
					new ArrayList<>());
	}
}
