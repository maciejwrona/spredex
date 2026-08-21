package com.maciej.spredex.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.maciej.spredex.CellUpdateTracker;
import com.maciej.spredex.Dag;
import com.maciej.spredex.DependencyGraph;
import com.maciej.spredex.Cell.Cell;
import com.maciej.spredex.Cell.CellLocation;
import com.maciej.spredex.Cell.CellRef;
import com.maciej.spredex.Cell.RangeRef;
import com.maciej.spredex.Cell.SingleCellRef;
import com.maciej.spredex.Errors.ErrorReporter;
import com.maciej.spredex.Errors.StdErrReporter;
import com.maciej.spredex.Interpreter.Interpreter;
import com.maciej.spredex.Parser.ParseResult;
import com.maciej.spredex.Parser.Parser;
import com.maciej.spredex.Parser.Lexer.Lexer;
import com.maciej.spredex.Parser.Lexer.Token;

public class MatrixSheet extends Sheet {
	private final int size;
	private final List<List<Cell>> cells;
	private final DependencyGraph dependencies;
	private final Interpreter interpreter;
	private final ErrorReporter reporter;
	private final CellUpdateTracker updateTracker;

	public MatrixSheet(int size) {
		this.size = size;
		this.dependencies = new Dag(this);
		this.reporter = new StdErrReporter();
		this.interpreter = new Interpreter(this, new HashMap<>(), reporter);
		this.updateTracker = new CellUpdateTracker(dependencies);

		cells = new ArrayList<>();
		for (int i = 0; i < this.size; i++) {
			cells.add(new ArrayList<>());
		}

		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				cells.get(i).add(new Cell(i, j));
			}
		}
	}

	@Override
	public List<Cell> cellsAt(CellRef reference, Cell currentCell) {
		List<Cell> result = new ArrayList<>();

		if (reference instanceof SingleCellRef) {
			result.add(cellAt(new CellLocation((SingleCellRef)reference, currentCell)));
		}
		else if (reference instanceof RangeRef) {
			RangeRef range = (RangeRef)reference;
			CellLocation left = new CellLocation(range.left(), currentCell);
			CellLocation right = new CellLocation(range.right(), currentCell);

			for (int i = left.row(); i <= right.row(); i++) {
				for (int j = left.column(); j <= right.column(); j++) {
					result.add(cells.get(i).get(j));
				}
			}
		}

		return result;
	}

	@Override
	public Cell cellAt(CellLocation location) {
		return cells.get(location.row()).get(location.column());
	}

	@Override
	public Object evaluate(CellLocation cell) {
		return cellAt(cell).value();
	}

	@Override
	public void setCell(CellLocation target, String formula) {
		Cell cell = cellAt(target);

		cell.setFormula(formula);	

		updateAst(cell);
		updateTracker.resetDependatns(cell);	

		List<Cell> updateOrder = dependencies.getUpdateOrder(cell);
		for (Cell toUpdate : updateOrder) {
			updateValue(toUpdate);
			fireTableCellUpdated(toUpdate.row(), toUpdate.column());
		}
	}

	private void updateAst(Cell cell) {
		if (!cell.hasFormula()) {
			cell.setAst(null);
			dependencies.setRequired(cell, new HashSet<>());
		}
		else {
			Lexer lexer = new Lexer(cell.formula(), reporter);
			List<Token> tokens = lexer.getTokens();

			Parser parser = new Parser(tokens, cell, reporter);
			ParseResult result = parser.parse();

			cell.setAst(result.ast());
			dependencies.setRequired(cell, result.requires());
		}
	}

	private void updateValue(Cell cell) {
		if (cell.ast() == null) {
			Object newValue;
			try {
				newValue = Double.parseDouble(cell.formula());
			}
			catch (NumberFormatException error) {
				newValue = cell.formula();
			}
			cell.setValue(newValue);
			return;
		}
		
		cell.setValue(
				interpreter.interpret(cell.ast(), cell));
	}

	@Override
	public int getRowCount() {
		return size - 1;
	}

	@Override
	public int getColumnCount() {
		return size - 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = evaluate(new CellLocation(rowIndex + 1, columnIndex + 1));
		return (result == null ? "" : result.toString());
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setCell(new CellLocation(rowIndex + 1, columnIndex + 1), aValue.toString());
		fireTableCellUpdated(rowIndex, columnIndex);
	}
}
