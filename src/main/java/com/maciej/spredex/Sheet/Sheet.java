package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Parser.FormulaParser;
import com.maciej.spredex.Parser.NonFormulaParser;
import com.maciej.spredex.Parser.ParseResult;
import com.maciej.spredex.Parser.Parser;
import com.maciej.spredex.Parser.Lexer.Lexer;
import com.maciej.spredex.Parser.Lexer.Token;
import com.maciej.spredex.Sheet.DependencyGraph.DependencyGraph;
import com.maciej.spredex.Function.SpredexFunction;
import com.maciej.spredex.Function.Sum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class Sheet extends AbstractTableModel {
	private final SparseMatrix cells;
	private final Map<String, SpredexFunction> functions = new HashMap<>();
	private final Interpreter interpreter;
	private final DependencyGraph graph;
	private final int maxRows;
	private final int maxColumns;

	public Sheet(int maxRows, int maxColumns) {
		initializeFunctions();
		this.cells = new SparseMatrix();
		this.graph = new DependencyGraph(maxRows);
		this.interpreter = new Interpreter(this, functions);
		this.maxRows = maxRows;
		this.maxColumns = maxColumns;
	}

	private void initializeFunctions() {
		functions.put("SUM", new Sum());
	}

	public Object valueAt(CellLoc location) {
		if (isCellEmpty(location)) {
			return new EmptyCell();
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
		ParseResult parseResult = new ParseResult(null, new ArrayList<>());

		try {
			parseResult = formulaToAst(cell.formula(), target);
		}
		catch (CellError error) {
			setErrorAt(target, error);
		}
		cell.setAst(parseResult.ast());

		setRequired(target, parseResult.requires());
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

	private void setRequired(CellLoc target, List<CellRef> required) {
		List<CellCoordinates> newRequired = new ArrayList<>();
		for (CellRef ref : required) {
			newRequired.add(ref.toCoordinates(target, maxRows, maxColumns));
		}
		graph.setRequired(target, newRequired);
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

		if (graph.isInCycle(location)) {
			setErrorAt(location, "#CYCLE");
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

		fireCellUpdate(location);
	}

	public boolean isErrorAt(CellLoc location) {
		return (!isCellEmpty(location) && cellAt(location).error());
	}

	private Cell cellAt(CellLoc location) {
		return cells.get(location.row(), location.column());
	}

	private boolean isCellEmpty(CellLoc location) {
		return cellAt(location) == null;
	}

	private void setErrorAt(CellLoc target, CellError error) {
		String errorValue = "#" + error.getType().toString();
		System.out.println(error.getMessage());
		setErrorAt(target, errorValue);
	}

	private void setErrorAt(CellLoc target, String errorValue) {
		Cell cell = cellAt(target);

		cell.setValue(errorValue);
		cell.setError(true);	
		fireCellUpdate(target);
	}

	private void fireCellUpdate(CellLoc cell) {
		fireTableCellUpdated(cell.row() - 1, cell.column() - 1);
	}

	private boolean isFormula(String formula) {
		return (formula.length() > 0 && formula.charAt(0) == '=');
	}

	public List<CellLoc> cellsInRange(CellRange range) {
		return cells.getCellLocationsInRange(range);
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
		CellLoc location = new CellLoc(rowIndex + 1, columnIndex + 1);

		if (isCellEmpty(location)) {
			return "";
		}

		return valueAt(location);
	}

	public String formulaAt(int row, int column) {
		CellLoc location = new CellLoc(row + 1, column + 1);

		if (isCellEmpty(location)) {
			return "";
		}

		return cellAt(location).formula();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setCell(new CellLoc(rowIndex + 1, columnIndex + 1), aValue.toString());
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
}
