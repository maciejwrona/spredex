package com.maciej.spredex.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.maciej.spredex.Cell.Cell;
import com.maciej.spredex.Cell.CellLocation;
import com.maciej.spredex.Cell.CellRefVisitor;
import com.maciej.spredex.Cell.RangeRef;
import com.maciej.spredex.Cell.SingleCellRef;
import com.maciej.spredex.Errors.ErrorType;
import com.maciej.spredex.Function.ExcelFunction;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Expressions.Expression.Binary;
import com.maciej.spredex.Parser.Expressions.Expression.Call;
import com.maciej.spredex.Parser.Expressions.Expression.Grouping;
import com.maciej.spredex.Parser.Expressions.Expression.Literal;
import com.maciej.spredex.Parser.Expressions.Expression.Reference;
import com.maciej.spredex.Parser.Expressions.Expression.Unary;
import com.maciej.spredex.Parser.Expressions.ExpressionVisitor;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Sheet.Sheet;

public class Interpreter implements ExpressionVisitor<Object>, CellRefVisitor<Object> {
	private final Sheet sheet;

	private final Map<String, ExcelFunction> functions;
	
	private Cell currentCell;

	public Interpreter(Sheet sheet, Map<String, ExcelFunction> functions) {
		this.sheet = sheet;
		this.functions = functions;
	}

	public Object interpret(Expression ast, Cell currentCell) {
		this.currentCell = currentCell;
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
			case BANG_EQUAL:
			case GREATER:
			case LESS:
			case GREATER_EQUAL:
			case LESS_EQUAL:

			case COLON:
				verifyReferenceOperands(expression.operator(), left, right);
				return new RangeRef((SingleCellRef)left, (SingleCellRef)right);
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
		
		return function.call(arguments, sheet, this);
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
		return sheet.evaluate(new CellLocation(ref, currentCell));
	}

	@Override
	public Object visitRangeRef(RangeRef ref) {
		return ref;
	}

	private void verifyNumberOperands(Token operator, Object... operands) {
		for (Object operand : operands) {
			if (!(operand instanceof Double)) {
				throw new ExecutionError(ErrorType.TYPE, 
						"Expected number operands for operator '" + operator.lexeme() + "'.");
			}
		}
	}

	private void verifyReferenceOperands(Token operator, Object... operands) {
		for (Object operand : operands) {
			if (!(operand instanceof SingleCellRef)) {
				throw new ExecutionError(ErrorType.TYPE,
						"Expected cell reference operands for operator '" + operator.lexeme() + "'.");
			}
		}
	}
}
