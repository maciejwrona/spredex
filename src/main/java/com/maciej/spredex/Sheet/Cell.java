package com.maciej.spredex.Sheet;

import com.maciej.spredex.Parser.Expressions.Expression;

public class Cell {
	private String formula = "";
	private Expression formulaAst = null;
	private Object value = null;
	private boolean updated = true;
	private boolean error = false;

	private final int row;
	private final int column;

	public String formula() { return formula; }
	public Expression ast() { return formulaAst; }
	public Object value() { return value; } 
	public int row() { return row; }
	public int column() { return column; }
	public boolean updated() { return updated; }
	public boolean error() { return error; }

	public void setFormula(String formula) { 
		this.formula = formula; 
		updated = false;
	}
	public void setAst(Expression ast) { 
		this.formulaAst = ast; 
		updated = false;
	}
	public void setValue(Object value) { 
		this.value = value; 
		updated = true;
	}
	public void resetUpdate() { updated = false; }
	public void setError(boolean error) {
		this.error = error;
	}

	public Cell(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public boolean hasFormula() {
		return (formula.length() > 0 && formula.charAt(0) == '=');
	}
}	
