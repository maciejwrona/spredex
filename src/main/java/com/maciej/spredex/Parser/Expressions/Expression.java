package com.maciej.spredex.Parser.Expressions;

import java.util.List;

import com.maciej.spredex.Cell.CellRef;
import com.maciej.spredex.Parser.Lexer.Token;

public abstract class Expression {
	public abstract <T> T accept(ExpressionVisitor<T> visitor);

	public static class Binary extends Expression {
		private final Expression left;
		private final Token operator;
		private final Expression right;

		public Expression left() { return left; }
		public Token operator() { return operator; }
		public Expression right() { return right; }

		public Binary(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitBinaryExpression(this);
		}
	}

	public static class Unary extends Expression {
		private final Token operator;
		private final Expression right;

		public Token operator() { return operator; }
		public Expression right() { return right; }

		public Unary(Token operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}
		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitUnaryExpression(this);
		}
	}

	public static class Grouping extends Expression {
		private final Expression expression;

		public Expression expression() { return expression; }

		public Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitGroupingExpression(this);
		}
	}

	public static class Reference extends Expression {
		private final CellRef reference;

		public CellRef reference() { return reference; }

		public Reference(CellRef reference) {
			this.reference = reference;
		}

		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitReferenceExpression(this);
		}
	}

	public static class Call extends Expression {
		private final Token identifier;
		private final List<Expression> arguments;

		public Token identifier() { return identifier; }
		public List<Expression> arguments() { return arguments; }

		public Call(Token identifier, List<Expression> arguments) {
			this.identifier = identifier;
			this.arguments = arguments;
		}
		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitCallExpression(this);
		}
	}

	public static class Literal extends Expression {
		private final Object literal;

		public Object literal() { return literal; }

		public Literal(Object literal) {
			this.literal = literal;
		}

		@Override
		public <T> T accept(ExpressionVisitor<T> visitor) {
			return visitor.visitLiteralExpression(this);
		}

	}
}
