package com.maciej.spredex.Sheet;

import com.maciej.spredex.Cell.Cell;
import com.maciej.spredex.Cell.CellLocation;
import com.maciej.spredex.Cell.CellRef;

import javax.swing.table.AbstractTableModel;

import java.util.List;

public abstract class Sheet extends AbstractTableModel {
	public abstract List<Cell> cellsAt(CellRef reference, Cell currentCell);
	public abstract Cell cellAt(CellLocation location);
	public abstract Object evaluate(CellLocation location);
	public abstract void setCell(CellLocation target, String formula);
}
