package com.maciej.spredex.Parser;

import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Parser.Lexer.TokenType;
import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.CellRef.RangeRef;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormulaParserTest {
	@Test
	@DisplayName("Should parse standard expression")
	void testStandardAst() {
		// =SUM(5) + (-10.5)
		List<Token> tokens = List.of(
			new Token(TokenType.EQUAL, "=", null),
			new Token(TokenType.IDENTIFIER, "SUM", null),
			new Token(TokenType.LEFT_PAREN, "(", null),
			new Token(TokenType.INTEGER, "5", 5.0),
			new Token(TokenType.RIGHT_PAREN, ")", null),
			new Token(TokenType.PLUS, "+", null),
			new Token(TokenType.LEFT_PAREN, "(", null),
			new Token(TokenType.MINUS, "-", null),
			new Token(TokenType.DECIMAL, "10.5", 10.5),
			new Token(TokenType.RIGHT_PAREN, ")", null)
		);
		CellLoc currentLocation = new CellLoc(10, 10);

		Parser parser = new FormulaParser(tokens, currentLocation, 100000, 100000);
		ParseResult result = parser.parse();

		Expression expected = new Expression.Binary(
			new Expression.Call(new Token(TokenType.IDENTIFIER, "SUM", null), 
				List.of(new Expression.Literal(5.0))),
			new Token(TokenType.PLUS, "+", null),
			new Expression.Grouping(new Expression.Unary(
				new Token(TokenType.MINUS, "-", null), new Expression.Literal(10.5)))
		);

		assertThat(result.ast()).isEqualTo(expected);
	}

	@Test
	@DisplayName("Should parse range references")
	void testRangeCellReference() {
		// =PROD(A1:B$10, 1:C)
		List<Token> tokens = List.of(
			new Token(TokenType.EQUAL, "=", null),
			new Token(TokenType.IDENTIFIER, "PROD", null),
			new Token(TokenType.LEFT_PAREN, "(", null),
			new Token(TokenType.IDENTIFIER, "A1", null),
			new Token(TokenType.COLON, ":", null),
			new Token(TokenType.IDENTIFIER, "B$10", null),
			new Token(TokenType.COMMA, ",", null),
			new Token(TokenType.INTEGER, "B", 1.0),
			new Token(TokenType.COLON, ":", null),
			new Token(TokenType.IDENTIFIER, "C", null),
			new Token(TokenType.RIGHT_PAREN, ")", null)
		);
		CellLoc currentLocation = new CellLoc(0, 0);

		Parser parser = new FormulaParser(tokens, currentLocation, 100000, 100000);
		ParseResult result = parser.parse();

		Expression expected = new Expression.Call(
			new Token(TokenType.IDENTIFIER, "PROD", null),
			List.of(
				new Expression.Reference(
					new RangeRef(
						new SingleCellRef(1, 1, false, false),
						new SingleCellRef(10, 2, true, false)
					)
				),
				new Expression.Reference(
					new RangeRef(
						new SingleCellRef(0, 2, false, false),
						new SingleCellRef(0, 3, false, false)
					)
				)
			)
		);

		assertThat(result.ast()).isEqualTo(expected);
	}
}
