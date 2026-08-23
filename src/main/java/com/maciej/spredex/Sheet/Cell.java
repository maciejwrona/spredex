package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Parser.Expressions.Expression;

public class Cell {
	private String formula = "";
	private Expression formulaAst = null;
	private Object value = null;
	private boolean error = false;

	private final int row;
	private final int column;

	public String formula() { return formula; }
	public Expression ast() { return formulaAst; }
	public Object value() { return value; } 
	public int row() { return row; }
	public int column() { return column; }
	public boolean error() { return error; }
	public CellLoc location() { return new CellLoc(row, column); }

	public void setFormula(String formula) { 
		this.formula = formula; 
	}

	public void setAst(Expression ast) { 
		this.formulaAst = ast; 
	}

	public void setValue(Object value) { 
		this.value = value; 
	}

	public void setError(boolean error) { this.error = error; }

	public Cell(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public boolean hasExecutableFormula() {
		return (formula.length() > 0 && formula.charAt(0) == '=');
	}
}	
