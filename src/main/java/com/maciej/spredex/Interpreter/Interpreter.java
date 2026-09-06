package com.maciej.spredex.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.ErrorType;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Expressions.Expression.*;
import com.maciej.spredex.Parser.Expressions.ExpressionVisitor;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Sheet.EmptyCell;
import com.maciej.spredex.Sheet.Sheet;

public class Interpreter implements ExpressionVisitor<Object> {
	private final Sheet sheet;

	private final Map<String, SpredexFunction> functions;
	
	private CellLoc location;

	public Interpreter(Sheet sheet, Map<String, SpredexFunction> functions) {
		this.sheet = sheet;
		this.functions = functions;
	}

	public Object interpret(Expression ast, CellLoc location) {
		if (ast == null) {
			return null;
		}

		this.location = location;
		Object result = evaluate(ast);

		if (result instanceof RangeRef) {
			throw new CellError(ErrorType.TYPE, 
					"Invalid value of type RangeCellReferences.");
		}

		return result;
	}

	public Object evaluate(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public Object visitBinaryExpression(Binary expression) {
		Object left = evaluate(expression.left());
		Object right = evaluate(expression.right());

		switch (expression.operator().type()) {
			case PLUS:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) + castToDouble(right);
			case MINUS:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) - castToDouble(right);
			case ASTERISK:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) * castToDouble(right);
			case SLASH:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) / castToDouble(right);
			case CARET:
				verifyNumberOperands(expression.operator(), left, right);
				return Math.pow(castToDouble(left), castToDouble(right));
			case AMPERSAND:
				return left.toString() + right.toString();
			
			// TODO: 
			case EQUAL:
				return equal(left, right);
			case BANG_EQUAL:
				return !equal(left, right);
			case GREATER:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) > castToDouble(right);
			case LESS:
				verifyNumberOperands(expression.operator(), left, right);
				return castToDouble(left) < castToDouble(right);
			case GREATER_EQUAL:
				verifyNumberOperands(expression.operator(), left, right);
				return (castToDouble(left) > castToDouble(right) || 
						doubleEqual(castToDouble(left), castToDouble(right)));
			case LESS_EQUAL:
				verifyNumberOperands(expression.operator(), left, right);
				return (castToDouble(left) < castToDouble(right) || 
						doubleEqual(castToDouble(left), castToDouble(right)));
		}
		
		return null;
	}

	@Override
	public Object visitUnaryExpression(Unary expression) {
		Object right = evaluate(expression.right());

		switch (expression.operator().type()) {
			case MINUS:
				verifyNumberOperands(expression.operator(), right);
				return -castToDouble(right);
		}

		return null;
	}

	@Override
	public Object visitGroupingExpression(Grouping expression) {
		return evaluate(expression.expression());
	}

	@Override
	public Object visitCallExpression(Call expression) {
		SpredexFunction function = functions.get(expression.identifier().lexeme());
		if (function == null) {
			throw new CellError(ErrorType.IDENTIFIER, 
					expression.identifier().lexeme() + " is not a function.");
		}

		List<Object> arguments = new ArrayList<>();
		for (Expression argument : expression.arguments()) {
			arguments.add(evaluate(argument));
		}

		if (!function.arity().matches(arguments.size())) {
			throw new CellError(ErrorType.IDENTIFIER, 
					"Expected " + function.arity() + " arguments for function " + expression.identifier().lexeme() + ".");
		}
		
		return function.call(arguments, location, sheet);
	}

	@Override
	public Object visitLiteralExpression(Literal expression) {
		return expression.literal();
	}

	@Override
	public Object visitReferenceExpression(Reference expression) {
		return expression.reference()
			.toCoordinates(location, sheet.getRowCount(), sheet.getRowCount());
	}

	private double castToDouble(Object value) {
		return switch (value) {
			case Double d -> d;
			case CellLoc loc -> sheet.numberValueAt(loc);
			default -> throw new CellError(ErrorType.TYPE, "This should not happen!");
		};
	}

	private boolean equal(Object left, Object right) {
		if (isNumberValue(left) && isNumberValue(right)) {
			return doubleEqual(castToDouble(left), castToDouble(right));
		}
		else {
			return left.equals(right);
		}
	}

	private boolean doubleEqual(double left, double right) {
		double epsilon = 1e-6;
		return Math.abs(left - right) <= epsilon;
	}

	private void verifyNumberOperands(Token operator, Object... operands) {
		for (Object operand : operands) {
			if (!isNumberValue(operand)) {
				throw new CellError(ErrorType.TYPE, 
						"Expected number operands for operator '" + operator.lexeme() + "'.");
			}
		}
	}

	private boolean isNumberValue(Object value) {
		return switch (value) {
			case Double d -> true;
			case CellLoc loc -> 
				(sheet.valueAt(loc) == new EmptyCell() || sheet.valueAt(loc) instanceof Double);
			default -> false;
		};
	}
}
