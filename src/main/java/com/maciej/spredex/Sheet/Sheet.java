package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Parser.FormulaParser;
import com.maciej.spredex.Parser.NonFormulaParser;
import com.maciej.spredex.Parser.ParseResult;
import com.maciej.spredex.Parser.Parser;
import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Parser.Lexer.Lexer;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Sheet.DependencyGraph.DependencyGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class Sheet extends AbstractTableModel {
	private final SparseMatrix cells;
	private final Interpreter interpreter;
	private final DependencyGraph graph;
	private final int maxRows;
	private final int maxColumns;

	public Sheet(int maxRows, int maxColumns) {
		this.cells = new SparseMatrix();
		this.graph = new DependencyGraph(maxRows);
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
		if (formula.length() == 0) {
			cells.deleteCell(target.row(), target.column());
			return;
		}

		cells.createCell(target.row(), target.column());

		cellAt(target).setFormula(formula);
		updateAst(target);
		updateValueRecursively(target);
	}

	private void updateAst(CellLoc target) {
		Cell cell = cellAt(target);

		// TODO: seperate catching parsing errors from cycles
		// Catches parsing and cycle errors
		try {
			ParseResult result = formulaToAst(cell.formula(), target);
			setAstAndRequired(target, result.ast(), result.requires());
		}
		catch (CellError error) {
			setErrorAt(target, error);
			setAstAndRequired(target, null, new ArrayList<>());
		}
	}

	private ParseResult formulaToAst(String formula, CellLoc target) {
		Parser parser;

		if (!isFormula(formula)) {
			parser = new NonFormulaParser(formula);
		}
		else {
			Lexer lexer = new Lexer(formula);
			List<Token> tokens = lexer.tokenize();

			parser = new FormulaParser(tokens, target, maxRows, maxColumns);
		}

		return parser.parse();
	}

	private void setAstAndRequired(CellLoc target, Expression ast, List<CellRef> required) {
		cellAt(target).setAst(ast);

		List<CellCoordinates> newRequired = new ArrayList<>();
		for (CellRef ref : required) {
			newRequired.add(ref.toCoordinates(target, maxRows, maxColumns));
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
		catch (CellError error) {
			setErrorAt(location, error);
			return;
		}

	}

	public boolean isErrorAt(CellLoc location) {
		return (isCellEmpty(location) || cellAt(location).error());
	}

	private Cell cellAt(CellLoc location) {
		return cells.get(location.row(), location.column());
	}

	private boolean isCellEmpty(CellLoc location) {
		return cellAt(location) == null;
	}

	private void setErrorAt(CellLoc target, CellError error) {
		Cell cell = cellAt(target);

		String value = "#" + error.getType().toString();
		cell.setValue(value);
		cell.setError(true);	
	}

	private boolean isFormula(String formula) {
		return (formula.length() > 0 && formula.charAt(0) == '=');
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

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setCell(new CellLoc(rowIndex, columnIndex), aValue.toString());
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (rowIndex >= 1 && columnIndex >= 1);
	}
}
