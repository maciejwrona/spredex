package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.Errors.ExcelError;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Parser.NonFormulaParser;
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

		try {
			ParseResult result = formulaToAst(cell.formula(), target);
			setAstAndRequired(target, result.ast(), result.requires());
		}
		catch (ExcelError error) {
			setErrorAt(target, error);
			setAstAndRequired(target, null, new ArrayList<>());
		}
	}

	private ParseResult formulaToAst(String formula, CellLoc target) {
		if (!isFormula(formula)) {
			NonFormulaParser parser = new NonFormulaParser(formula);
			return parser.parse();
		}

		Lexer lexer = new Lexer(formula);
		List<Token> tokens = lexer.tokenize();

		Parser parser = new Parser(tokens, target);
		return parser.parse();
	}

	private boolean isFormula(String formula) {
		return (formula.length() > 0 && formula.charAt(0) == '=');
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
			updateValueOnlyAt(location);
		}
	}

	private void updateValueOnlyAt(CellLoc location) {
		Cell cell = cellAt(location);

		if (cell.ast() == null) {
			return;
		}

		try {
			Object value = interpreter.interpret(cell.ast(), location);
			cell.setValue(value);
			cell.setError(false);
		}
		catch (ExcelError error) {
			setErrorAt(location, error);
		}
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

	private void setErrorAt(CellLoc target, ExcelError error) {
		Cell cell = cellAt(target);

		String value = "#" + error.type().toString();
		cell.setValue(value);
		cell.setError(true);	
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
