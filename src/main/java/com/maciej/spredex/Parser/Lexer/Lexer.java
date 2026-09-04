package com.maciej.spredex.Parser.Lexer;

import java.util.ArrayList;
import java.util.List;

import com.maciej.spredex.CellError;
import com.maciej.spredex.ErrorType;

public class Lexer {
	private final String formula;
	private final List<Token> tokens = new ArrayList<>();
	
	// Point to the start of the token being parsed and the next character to be parsed
	private int start = 0;
	private int current = 0;

	public Lexer(String formula) {
		this.formula = formula;
	}

	public List<Token> tokenize() {
		while (!isAtEnd()) {
			start = current;
			nextToken();
		}

		return tokens;
	}

	private void nextToken() {
		char c = nextChar();
		switch (c) {
			case '(': addToken(TokenType.LEFT_PAREN); break;
			case ')': addToken(TokenType.RIGHT_PAREN); break;
			case '{': addToken(TokenType.LEFT_BRACE); break;
			case '}': addToken(TokenType.RIGHT_BRACE); break;
			case '[': addToken(TokenType.LEFT_BRACKET); break;
			case ']': addToken(TokenType.RIGHT_BRACKET); break;

			case '+': addToken(TokenType.PLUS); break;
			case '-': addToken(TokenType.MINUS); break;
			case '/': addToken(TokenType.SLASH); break;
			case '*': addToken(TokenType.ASTERISK); break;
			case '^': addToken(TokenType.CARET); break;
			case '%': addToken(TokenType.PERCENT); break;
			case ':': addToken(TokenType.COLON); break;
			case ';': addToken(TokenType.SEMICOLON); break;
			case '&': addToken(TokenType.AMPERSAND); break;
			case ',': addToken(TokenType.COMMA); break;

			case '=': addToken(TokenType.EQUAL); break;
			case '!': 
				if (match('=')) {
					addToken(TokenType.BANG_EQUAL);
				}
				else {
					addToken(TokenType.BANG);
				}
				break;
			case '<':
				if (match('=')) {
					addToken(TokenType.LESS_EQUAL);
				}
				else {
					addToken(TokenType.LESS);
				}
				break;
			case '>':
				if (match('=')) {
					addToken(TokenType.GREATER_EQUAL);
				}
				else {
					addToken(TokenType.GREATER);
				}
				break;

			case '"':
				consumeString();
				break;

			default:
				// Handle ranges in the default case
				if (Character.isDigit(c)) {
					consumeNumber();
				}
				else if (isPartOfIdentifier(c)) {
					consumeIdentifier();
				}
				// Whitespace does not matter
				else if (Character.isWhitespace(c)) {
					break;
				}
				else {
					throw tokenError("Unrecognized token at " + start + ".");
				}
		}
	}

	private void consumeString() {
		while (peek() == '"') nextChar(); 

		if (isAtEnd()) {
			throw tokenError("Expected closing '\"'.");
		}
		
		// Consume the ". Add the string without surrouding "
		nextChar();
		addToken(TokenType.STRING, formula.substring(start + 1, current - 1));
	}

	private void consumeNumber() {
		while (Character.isDigit(peek())) nextChar();

		// Check if its a decimal
		if (match('.')) {
			// There must be at least one digit after the .
			if (!Character.isDigit(peek())) {
				throw tokenError("Expected at least one digit after '.'");
			}

			while (Character.isDigit(peek())) nextChar();
			addToken(TokenType.DECIMAL, Double.parseDouble(formula.substring(start, current)));
		}
		else {
			addToken(TokenType.INTEGER, Double.parseDouble(formula.substring(start, current)));
		}
	}

	private void consumeIdentifier() {
		while (isPartOfIdentifier(peek())) nextChar();

		String lexeme = formula.substring(start, current);
		switch (lexeme) {
			case "FALSE": addToken(TokenType.BOOLEAN, false); return;
			case "TRUE": addToken(TokenType.BOOLEAN, true); return;
			default:
				addToken(TokenType.IDENTIFIER);
		}
	}

	private CellError tokenError(String message) {
		return new CellError(ErrorType.TOKEN, message);
	}

	private boolean isPartOfIdentifier(char c) {
		return (Character.isDigit(c) || Character.isLetter(c) || c == '$');
	}

	private boolean isAtEnd() {
		return current >= formula.length();
	}

	private char nextChar() {
		return formula.charAt(current++);
	}
	
	private char peek() {
		return (isAtEnd() ? '\0' : formula.charAt(current));
	}

	private boolean match(char expected) {
		if (peek() != expected) return false;
		
		// Must be the same
		nextChar();
		return true;
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object value) {
		tokens.add(new Token(type, formula.substring(start, current), value));
	}
}
