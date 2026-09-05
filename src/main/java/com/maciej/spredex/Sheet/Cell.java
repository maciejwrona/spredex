package com.maciej.spredex.Sheet;

import java.util.Collections;
import java.util.List;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.Parser.AstPrinter;
import com.maciej.spredex.Parser.Expressions.Expression;

public class Cell {
	private final static AstPrinter astPrinter = new AstPrinter();

	private String formula = "";
	private Expression formulaAst = null;
	private List<CellRef> requiredRefs = Collections.emptyList();
	private Object value = null;
	private boolean error = false;
	private String errorMessage = "";

	private final int row;
	private final int column;

	public String formula() { return formula; }
	public Expression ast() { return formulaAst; }
	public List<CellRef> requiredRefs() { return requiredRefs; }
	public Object value() { return value; } 
	public int row() { return row; }
	public int column() { return column; }
	public boolean error() { return error; }
	public String errorMessage() { return (error ? errorMessage : null); }
	public CellLoc location() { return new CellLoc(row, column); }

	public void setFormula(String formula) { this.formula = formula; }

	public void setAst(Expression ast) { this.formulaAst = ast; }
	public void setRequiredRefs(List<CellRef> requiredRefs) { this.requiredRefs = requiredRefs; }

	public void setValue(Object value) { this.value = value; }

	public void setError(boolean error) { this.error = error; }
	public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

	public Cell(int row, int column) {
		this.row = row;
		this.column = column;
	}

	// to copy when swiping
	public Cell(int row, int column, Cell cell) {
		this.row = row;
		this.column = column;

		if (cell.formulaAst!= null) {
			this.formula = astPrinter.convert(cell.formulaAst, new CellLoc(row, column));
		}
		else {
			this.formula = cell.formula;
		}

		this.formulaAst = cell.formulaAst;
		this.requiredRefs = cell.requiredRefs;
		this.value = cell.value;
		this.error = cell.error;
		this.errorMessage = cell.errorMessage;
	}
}	
