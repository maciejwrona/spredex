package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.Errors.ExcelError;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Parser.ParseResult;
import com.maciej.spredex.Parser.Parser;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Lexer.Lexer;
import com.maciej.spredex.Parser.Lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class Sheet extends AbstractTableModel {
	private final SparseMatrix cells;
	private final Interpreter interpreter;
	private DependencyGraph graph;
	private final int maxRows;
	private final int maxColumns;

	public Sheet(int maxRows, int maxColumns) {
		cells = new SparseMatrix();
		this.interpreter = new Interpreter(this, new HashMap<>());
		this.maxRows = maxRows;
		this.maxColumns = maxColumns;
	}

	public Object valueAt(CellLoc location) {
		if (isCellEmpty(location)) {
			return null;
		}

		return cellAt(location).value();
	}

	public void setCell(CellLoc target, String formula) {
		cells.createCell(target.row(), target.column());

		cellAt(target).setFormula(formula);
		updateAst(target);
		updateValueRecursively(target);
	}

	private void updateAst(CellLoc target) {
		Cell cell = cellAt(target);

		if (!cell.hasExecutableFormula()) {
			setAstAndRequired(target, null, new ArrayList<>());
			return;
		}

		try {
			Lexer lexer = new Lexer(cell.formula());
			List<Token> tokens = lexer.tokenize();

			Parser parser = new Parser(tokens, target);
			ParseResult result = parser.parse();

			setAstAndRequired(target, result.ast(), result.requires());
		}
		catch (ExcelError error) {
			cell.setError(true);
			setAstAndRequired(target, null, new ArrayList<>());
		}
	}

	private void setAstAndRequired(CellLoc target, Expression ast, List<CellRef> required) {
		cellAt(target).setAst(ast);

		List<CellCoordinates> newRequired = new ArrayList<>();
		for (CellRef ref : required) {
			newRequired.add(ref.toCoordinates(target));
		}
		graph.updateRequired(target, newRequired);
	}

	// Updates the value at target, also updating every cell affected
	private void updateValueRecursively(CellLoc target) {
		List<CellLoc> updateOrder = graph.getUpdateOrder(target);

		for (CellLoc location : updateOrder) {
			if (cellAt(location).ast() == null) {
				updateFromFormula(location);
			}
			else {
				updateFromAst(location);
			}
		}
	}

	// TODO: unify formula and non formula - on non formula just set Ast to a literal expression
	private void updateFromFormula(CellLoc location) {
		Cell cell = cellAt(location);
		Object value;

		try {
			value = Double.parseDouble(cell.formula());
		}
		catch (NumberFormatException e) {
			value = cell.formula();
		}

		cell.setValue(value);
	}

	private void updateFromAst(CellLoc location) {
		Cell cell = cellAt(location);
		Object value;

		try {
			value = interpreter.interpret(cell.ast(), location);
			cell.setError(false);
		}
		catch (ExcelError error) {
			value = "#" + error.type().toString();
			cell.setError(true);	
		}

		cell.setValue(value);	
	}

	public boolean isErrorAt(CellLoc location) {
		return (!isCellEmpty(location) || cellAt(location).error());
	}

	private Cell cellAt(CellLoc location) {
		return cells.get(location.row(), location.column());
	}

	private boolean isCellEmpty(CellLoc location) {
		return cellAt(location) == null;
	}

	@Override
	public int getRowCount() {
		return maxRows;
	}

	@Override
	public int getColumnCount() {
		return maxColumns;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CellLoc location = new CellLoc(rowIndex, columnIndex);

		if (isCellEmpty(location)) {
			return "";
		}

		return valueAt(location);
	}
}
