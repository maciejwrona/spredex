package com.maciej.spredex.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.CellRef.CellRefVisitor;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Function.ExcelFunction;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Expressions.Expression.*;
import com.maciej.spredex.Parser.Expressions.ExpressionVisitor;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Sheet.Sheet;

public class Interpreter implements ExpressionVisitor<Object>, CellRefVisitor<Object> {
	private final Sheet sheet;

	private final Map<String, ExcelFunction> functions;
	
	private CellLoc location;

	public Interpreter(Sheet sheet, Map<String, ExcelFunction> functions) {
		this.sheet = sheet;
		this.functions = functions;
	}

	public Object interpret(Expression ast, CellLoc location) {
		this.location = location;
		return evaluate(ast);
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
				return (Double)left + (Double)right;
			case MINUS:
				verifyNumberOperands(expression.operator(), left, right);
				return (Double)left - (Double)right;
			case ASTERISK:
				verifyNumberOperands(expression.operator(), left, right);
				return (Double)left * (Double)right;
			case SLASH:
				verifyNumberOperands(expression.operator(), left, right);
				return (Double)left / (Double)right;
			case CARET:
				verifyNumberOperands(expression.operator(), left, right);
				return Math.pow((Double)left, (Double)right);
			case AMPERSAND:
				return left.toString() + right.toString();
			
			// TODO: 
			case EQUAL:
				return equal(left, right);
			case BANG_EQUAL:
				return !equal(left, right);
			case GREATER:
				verifyNumberOperands(expression.operator(), left, right);
				return (Double)left > (Double)right;
			case LESS:
				verifyNumberOperands(expression.operator(), left, right);
				return (Double)left < (Double)right;
			case GREATER_EQUAL:
				verifyNumberOperands(expression.operator(), left, right);
				return ((Double)left > (Double)right || doubleEqual((Double)left, (Double)right));
			case LESS_EQUAL:
				verifyNumberOperands(expression.operator(), left, right);
				return ((Double)left < (Double)right || doubleEqual((Double)left, (Double)right));
		}
		
		return null;
	}

	@Override
	public Object visitUnaryExpression(Unary expression) {
		Object right = evaluate(expression.right());

		switch (expression.operator().type()) {
			case MINUS:
				verifyNumberOperands(expression.operator(), right);
				return -(Double)right;
		}

		return null;
	}

	@Override
	public Object visitGroupingExpression(Grouping expression) {
		return evaluate(expression.expression());
	}

	@Override
	public Object visitCallExpression(Call expression) {
		ExcelFunction function = functions.get(expression.identifier().lexeme());
		if (function == null) {
			throw new ExecutionError(ErrorType.IDENTIFIER, 
					expression.identifier().lexeme() + " is not a function.");
		}

		List<Object> arguments = new ArrayList<>();
		for (Expression argument : expression.arguments()) {
			arguments.add(evaluate(argument));
		}

		if (arguments.size() != function.arity()) {
			throw new ExecutionError(ErrorType.IDENTIFIER, 
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
		return expression.reference().accept(this);
	}

	@Override
	public Object visitSingleCellRef(SingleCellRef ref) {
		CellLoc target = SingleCellRef.refToLoc(ref, location);

		if (sheet.isErrorAt(target)) {
			throw new ExecutionError(ErrorType.TYPE, 
					"Cell at " + target + " is not available.");
		}

		return sheet.valueAt(SingleCellRef.refToLoc(ref, location));
	}

	@Override
	public Object visitRangeRef(RangeRef ref) {
		return ref;
	}

	private boolean equal(Object left, Object right) {
		if (left instanceof Double && right instanceof Double) {
			return doubleEqual((Double)left, (Double)right);
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
			if (!(operand instanceof Double)) {
				throw new ExecutionError(ErrorType.TYPE, 
						"Expected number operands for operator '" + operator.lexeme() + "'.");
			}
		}
	}
}
