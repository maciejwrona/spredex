package com.maciej.spredex;

import java.util.HashSet;
import java.util.Set;

import com.maciej.spredex.Cell.Cell;

public class CellUpdateTracker {
	private final DependencyGraph dependencies;

	public CellUpdateTracker(DependencyGraph dependencies) {
		this.dependencies = dependencies;
	}

	public void resetDependatns(Cell cell) {
		resetDependatns(cell, new HashSet<>());	
	}

	private void resetDependatns(Cell cell, Set<Cell> visited) {
		if (visited.contains(cell)) {
			throw new CycleError("Cycle detected at " + cell + ".");
		}

		cell.resetUpdate();

		visited.add(cell);
		for (Cell next : dependencies.getDependent(cell)) {
			resetDependatns(next, visited);
		}
		visited.remove(cell);
	}
}
