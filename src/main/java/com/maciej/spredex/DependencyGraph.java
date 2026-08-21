package com.maciej.spredex;

import java.util.Set;
import java.util.Collection;
import java.util.List;

import com.maciej.spredex.Cell.Cell;
import com.maciej.spredex.Cell.CellRef;

public interface DependencyGraph {
	public void setRequired(Cell from, Collection<CellRef> required);
	public Set<Cell> getRequired(Cell from);
	public Set<Cell> getDependent(Cell on);
	public List<Cell> getUpdateOrder(Cell from);
}
