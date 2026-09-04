package com.maciej.spredex.Parser;

import com.maciej.spredex.Parser.Expressions.Expression.Binary;
import com.maciej.spredex.Parser.Expressions.Expression.Call;
import com.maciej.spredex.Parser.Expressions.Expression.Grouping;
import com.maciej.spredex.Parser.Expressions.Expression.Literal;
import com.maciej.spredex.Parser.Expressions.Expression.Reference;
import com.maciej.spredex.Parser.Expressions.Expression.Unary;
import com.maciej.spredex.Parser.Expressions.ExpressionVisitor;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.CellRef.CellRefVisitor;
import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Parser.Expressions.Expression;

public class AstPrinter implements ExpressionVisitor<String>, CellRefVisitor<String> {
	private CellLoc location;

	public String convert(Expression ast, CellLoc location) {
		this.location = location;
		return getString(ast);
	}

	private String getString(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitBinaryExpression(Binary expression) {
		return getString(expression.left()) + " " +
			   expression.operator().lexeme() + " " +
			   getString(expression.right());
	}

	@Override
	public String visitUnaryExpression(Unary expression) {
		return expression.operator().lexeme() + " " +
			   getString(expression.right());
	}

	@Override
	public String visitGroupingExpression(Grouping expression) {
		return "( " + getString(expression.expression()) + " )";
	}

	@Override
	public String visitCallExpression(Call expression) {
		String result = expression.identifier().lexeme() + "(";
		for (int i = 0; i < expression.arguments().size(); i++) {
			result = result + getString(expression.arguments().get(i));
			if (i != expression.arguments().size() - 1) {
				result = result + ", ";
			}
		}
		return result + ")";
	}

	@Override
	public String visitLiteralExpression(Literal expression) {
		return expression.literal().toString();
	}

	private String refToString(CellRef ref) {
		return ref.accept(this);
	}

	@Override
	public String visitReferenceExpression(Reference expression) {
		return expression.reference().accept(this);
	}

	@Override
	public String visitSingleCellRef(SingleCellRef ref) {
		return (ref.lockedColumn() ? "$" : "") + ref.column() + "c" +
			   (ref.lockedRow() ? "$" : "") + ref.row() + "r";
	}

	@Override
	public String visitRangeRef(RangeRef ref) {
		return refToString(ref.left()) + ":" + refToString(ref.right());
	}
}
