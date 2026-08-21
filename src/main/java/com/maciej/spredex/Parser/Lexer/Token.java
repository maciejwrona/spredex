package com.maciej.spredex.Parser.Lexer;

public record Token(TokenType type, String lexeme, Object value) {}
