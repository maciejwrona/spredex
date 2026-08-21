package com.maciej.spredex.Parser.Expressions;

public interface ExpressionVisitor<T> {
	T visitBinaryExpression(Expression.Binary expression);
	T visitUnaryExpression(Expression.Unary expression);
	T visitGroupingExpression(Expression.Grouping expression);
	T visitCallExpression(Expression.Call expression);
	T visitLiteralExpression(Expression.Literal expression);
	T visitReferenceExpression(Expression.Reference expression);
}
