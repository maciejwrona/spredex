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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public void loadTable(List<List<String>> table) {
		Map<CellLoc, String> formulas = new HashMap<>();
		for (int i = 0; i < table.size(); i++) {
			List<String> row = table.get(i);
			for (int j = 0; j < row.size(); j++) {
				CellLoc location = new CellLoc(i + 1, j + 1);
				formulas.put(location, row.get(j));
			}
		}
		setMultipleCells(formulas);
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

	public void setCell(CellLoc location, String formula) {
		Map<CellLoc, String> formulas = new HashMap<>();
		formulas.put(location, formula);
		setMultipleCells(formulas);
	}

	public void setMultipleCells(Map<CellLoc, String> formulas) {
		Map<CellLoc, Collection<CellCoordinates>> required = new HashMap<>();

		for (var entry : formulas.entrySet()) {
			CellLoc location = entry.getKey();
			String formula = entry.getValue();

			if (formula.isBlank()) {
				cells.deleteCell(location.row(), location.column());
				required.put(location, new ArrayList<>());
			}
			else {
				cells.createCell(location.row(), location.column());
				Cell cell = cellAt(location);

				cell.setFormula(formula);

				ParseResult parseResult = parseFormulaAt(location);
				cell.setAst(parseResult.ast());
				required.put(location, toCoordinates(location, parseResult.requires()));
			}
		}

		graph.setRequiredForMultipleCells(required);
		updateAll(formulas.keySet());
	}

	private ParseResult parseFormulaAt(CellLoc location) {
		Cell cell = cellAt(location);
		ParseResult parseResult = new ParseResult(null, new ArrayList<>());

		try {
			parseResult = formulaToAst(cell.formula(), location);
		}
		catch (CellError error) {
			setErrorAt(location, error);
		}

		return parseResult;
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

	private List<CellCoordinates> toCoordinates(CellLoc location, List<CellRef> refs) {
		List<CellCoordinates> coordinates = new ArrayList<>();
		for (CellRef ref : refs) {
			coordinates.add(ref.toCoordinates(location, maxRows, maxColumns));
		}
		return coordinates;
	}

	// Updates every cell reachable from cells
	private void updateAll(Set<CellLoc> cells) {
		List<CellLoc> updateOrder = graph.getUpdateOrder(cells);

		for (CellLoc location : updateOrder) {
			updateValueOnlyAt(location);
		}
	}

	private void updateValueOnlyAt(CellLoc location) {
		Cell cell = cellAt(location);

		if (isCellEmpty(location) || cell.ast() == null) {
			return;
		}

		if (graph.isInCycle(location)) {
			setErrorAt(location, "#CYCLE", "Cilcular cell reference detected.");
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

	public boolean isErrorAt(int row, int column) {
		return isErrorAt(new CellLoc(row + 1, column + 1));
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
		setErrorAt(target, errorValue, error.getMessage());
	}

	private void setErrorAt(CellLoc target, String errorValue, String errorMessage) {
		Cell cell = cellAt(target);

		cell.setValue(errorValue);
		cell.setError(true);	
		cell.setErrorMessage(errorMessage);
		fireCellUpdate(target);
	}

	private void fireCellUpdate(CellLoc cell) {
		fireTableCellUpdated(cell.row() - 1, cell.column() - 1);
	}

	private boolean isFormula(String formula) {
		return (formula.length() > 0 && formula.charAt(0) == '=');
	}

	public String getErrorMessageAt(int row, int column) {
		CellLoc location = new CellLoc(row + 1, column + 1);
		return (isCellEmpty(location) ? null : cellAt(new CellLoc(row + 1, column + 1)).errorMessage());
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
