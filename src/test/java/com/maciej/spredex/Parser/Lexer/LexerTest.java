package com.maciej.spredex.Parser.Lexer;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.maciej.spredex.CellError;

import static org.assertj.core.api.Assertions.*;

class LexerTest {

	@Test
	@DisplayName("Should tokenize standard formulas")
	void testStandardFormula() {
		String formula = "=A1 + (10.5 - 43) FALSE";
		
		Lexer lexer = new Lexer(formula);
		List<Token> tokens = lexer.tokenize();

		assertThat(tokens).containsExactly(
				new Token(TokenType.EQUAL, "=", null),
				new Token(TokenType.IDENTIFIER, "A1", null),
				new Token(TokenType.PLUS, "+", null),
				new Token(TokenType.LEFT_PAREN, "(", null),
				new Token(TokenType.DECIMAL, "10.5", 10.5),
				new Token(TokenType.MINUS, "-", null),
				new Token(TokenType.INTEGER, "43", 43.0),
				new Token(TokenType.RIGHT_PAREN, ")", null),
				new Token(TokenType.BOOLEAN, "FALSE", false)
		);
	}

	@Test
	@DisplayName("Should correctly handle identifiers")
	void testIdentifiers() {
		String formula = "=A1 + $A10 + TF$10 + $GG$154 + SUM(A1)";

		Lexer lexer = new Lexer(formula);
		List<Token> tokens = lexer.tokenize();

		assertThat(tokens).
			extracting(Token::lexeme)
			.containsExactly("=", "A1", "+", "$A10", "+", "TF$10", "+", "$GG$154", "+", "SUM", "(", "A1", ")");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"=A1 + @",	// Invalid token '@'
		"=456.",	// Expected digit after '.'
	})
	@DisplayName("Should throw exceptions for invalid output")
	void testInvalidInput(String formula) {
		Lexer lexer = new Lexer(formula);

		assertThatThrownBy(() -> lexer.tokenize())
			.isInstanceOf(CellError.class);
	}
}
