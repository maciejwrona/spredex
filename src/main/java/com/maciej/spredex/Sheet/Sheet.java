package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellLoc;

import javax.swing.table.AbstractTableModel;

public abstract class Sheet extends AbstractTableModel {
	public abstract Object valueAt(CellLoc location);
	public abstract void setCell(CellLoc target, String formula);
	public abstract boolean isErrorAt(CellLoc target);
}
