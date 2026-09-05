package com.maciej.spredex.Parser;

import com.maciej.spredex.Parser.Expressions.Expression.Binary;
import com.maciej.spredex.Parser.Expressions.Expression.Call;
import com.maciej.spredex.Parser.Expressions.Expression.Grouping;
import com.maciej.spredex.Parser.Expressions.Expression.Literal;
import com.maciej.spredex.Parser.Expressions.Expression.Reference;
import com.maciej.spredex.Parser.Expressions.Expression.Unary;
import com.maciej.spredex.Parser.Expressions.ExpressionVisitor;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRefVisitor;
import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Parser.Expressions.Expression;

public class AstPrinter implements ExpressionVisitor<Void>, CellRefVisitor<Void> {
	private StringBuilder builder;
	private CellLoc location;

	public String convert(Expression ast, CellLoc location) {
		this.builder = new StringBuilder();
		this.location = location;

		build(ast);
		return builder.toString();
	}

	private void build(Expression expression) {
		expression.accept(this);
	}

	@Override
	public Void visitBinaryExpression(Binary expression) {
		build(expression.left());
		builder.append(" " + expression.operator().lexeme() + " ");
		build(expression.right());
		
		return null;
	}

	@Override
	public Void visitUnaryExpression(Unary expression) {
		builder.append(expression.operator().lexeme());
		build(expression.right());
		
		return null;
	}

	@Override
	public Void visitGroupingExpression(Grouping expression) {
		builder.append("(");
		build(expression.expression());
		builder.append(")");

		return null;
	}

	@Override
	public Void visitCallExpression(Call expression) {
		builder.append(expression.identifier().lexeme() + "(");

		for (int i = 0; i < expression.arguments().size(); i++) {
			build(expression.arguments().get(i));

			if (i != expression.arguments().size() - 1) {
				builder.append(", ");
			}
		}
		builder.append(")");

		return null;
	}

	@Override
	public Void visitLiteralExpression(Literal expression) {
		builder.append(expression.literal().toString());

		return null;
	}

	@Override
	public Void visitReferenceExpression(Reference expression) {
		expression.reference().accept(this);

		return null;
	}

	@Override
	public Void visitSingleCellRef(SingleCellRef ref) {
		if (!ref.hasUnboundedColumn()) {
			int column = ref.column();
			if (!ref.lockedColumn()) {
				column += location.column();
			}

			builder.append((ref.lockedColumn() ?  "" : "$") + CellRefParser.numberToColumn(column));
		}

		if (!ref.hasUnboundedRow()) {
			int row = ref.row();
			if (!ref.lockedRow()) {
				row += location.row();
			}

			builder.append((ref.lockedRow() ?  "" : "$") + Integer.toString(row));
		}
		
		return null;
	}

	@Override
	public Void visitRangeRef(RangeRef ref) {
		visitSingleCellRef(ref.left());
		visitSingleCellRef(ref.right());
		
		return null;
	}
}
