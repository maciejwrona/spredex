package com.maciej.spredex.Parser;

import java.util.ArrayList;
import java.util.List;

import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Parser.Lexer.TokenType;

public class FormulaParser implements Parser {
	private final List<Token> tokens;
	private final List<CellRef> requires = new ArrayList<>();
	private final CellLoc location;

	private final int maxRow;
	private final int maxColumn;

	// Skip leading '='
	private int current = 1;

	public FormulaParser(List<Token> tokens, CellLoc location, int maxRow, int maxColumn) {
		this.tokens = tokens;
		this.location = location;
		this.maxRow = maxRow;
		this.maxColumn = maxColumn;
	}

	@Override
	public ParseResult parse() {
		Expression result;
		result = expression();

		if (!isAtEnd()) {
			throw new ParseError("Expected exactly one epxression after '='.");
		}

		return new ParseResult(result, requires);
	}

	private Expression expression() {
		return comparison();
	}

	private Expression comparison() {
		Expression left = concatenation();

		while (match(TokenType.EQUAL, TokenType.BANG_EQUAL,
					 TokenType.GREATER, TokenType.GREATER_EQUAL,
					 TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token comparator = previous();
			Expression right = comparison();
			left = new Expression.Binary(left, comparator, right);
		}

		return left;
	}

	private Expression concatenation() {
		Expression left = additionOrSubstraction();

		while (match(TokenType.AMPERSAND)) {
			Token operator = previous();
			Expression right = concatenation();
			left = new Expression.Binary(left, operator, right);
		}

		return left;
	}

	private Expression additionOrSubstraction() {
		Expression left = multiplicationOrDivision();

		while (match(TokenType.PLUS, TokenType.MINUS)) {
			Token operator = previous();
			Expression right = additionOrSubstraction();
			left = new Expression.Binary(left, operator, right);
		}

		return left;
	}

	private Expression multiplicationOrDivision() {
		Expression left = exponentiation();

		while (match(TokenType.SLASH, TokenType.ASTERISK)) {
			Token operator = previous();
			Expression right = multiplicationOrDivision();
			left = new Expression.Binary(left, operator, right);
		}

		return left;
	}

	private Expression exponentiation() {
		Expression left = percent();

		while (match(TokenType.CARET)) {
			Token operator = previous();
			Expression right = exponentiation();
			left = new Expression.Binary(left, operator, right);
		}

		return left;
	}

	// TODO:
	private Expression percent() {
		return negation();
	}

	private Expression negation() {
		if (match(TokenType.MINUS)) {
			Token operator = previous();
			Expression right = negation();
			return new Expression.Unary(operator, right);
		}

		return primary();
	}

	private Expression primary() {
		if (match(TokenType.DECIMAL, TokenType.STRING, TokenType.BOOLEAN)) {
			Token literal = previous();
			return new Expression.Literal(literal.value());
		}

		// need to distinguish between constant and row number
		if (match(TokenType.INTEGER)) {
			Token integer = previous();

			if (!isAtEnd() && peek().type() == TokenType.COLON) {
				return finishCellRef();
			}

			return new Expression.Literal(integer.value());
		}
		
		if (match(TokenType.LEFT_PAREN)) {
			Expression inner = expression();
			consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
			return new Expression.Grouping(inner);
		}

		if (match(TokenType.IDENTIFIER)) {
			return handleIdentifer();
		}

		throw new ParseError("Expected expression.");
	}

	private Expression handleIdentifer() {
		Token identifer = previous();

		if (match(TokenType.LEFT_PAREN)) {
			return finishCall(identifer);
		}
		else {
			return finishCellRef();
		}
	}

	private Expression finishCellRef() {
		Token left = previous();
		SingleCellRef leftRef = tokenToReference(left);
		CellRef result = leftRef;

		if (match(TokenType.COLON)) {
			Token right = nextToken();
			result = new RangeRef(leftRef, tokenToReference(right));
		}

		requires.add(result);
		return new Expression.Reference(result);
	}

	private SingleCellRef tokenToReference(Token token) {
		CellRefParser parser = new CellRefParser(token.lexeme(), maxRow, maxColumn);
		SingleCellRef reference = parser.parse(location);

		return reference;
	}

	private Expression finishCall(Token identifier) {
		List<Expression> arguments = new ArrayList<>();

		if (!isAtEnd() && peek().type() != TokenType.RIGHT_PAREN) {
			do {
				arguments.add(expression());
			} while (match(TokenType.COMMA));
		}

		consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments list.");
		return new Expression.Call(identifier, arguments);
	}

	private void consume(TokenType type, String errorMessage) {
		if (match(type)) return;

		throw new ParseError(errorMessage);
	}

	private boolean isAtEnd() {
		return current >= tokens.size();
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token nextToken() {
		return tokens.get(current++);
	}

	private boolean match(TokenType... types) {
		if (isAtEnd()) return false;

		for (TokenType type : types) {
			if (peek().type() == type) {
				nextToken();
				return true;
			}
		}
		return false;
	}
}
