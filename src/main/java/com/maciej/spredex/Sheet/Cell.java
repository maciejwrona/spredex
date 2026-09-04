package com.maciej.spredex.Sheet;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Parser.Expressions.Expression;

public class Cell {
	private String formula = "";
	private Expression formulaAst = null;
	private Object value = null;
	private boolean error = false;
	private String errorMessage = "";

	private final int row;
	private final int column;

	public String formula() { return formula; }
	public Expression ast() { return formulaAst; }
	public Object value() { return value; } 
	public int row() { return row; }
	public int column() { return column; }
	public boolean error() { return error; }
	public String errorMessage() { return (error ? errorMessage : null); }
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
	public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

	public Cell(int row, int column) {
		this.row = row;
		this.column = column;
	}
}	
