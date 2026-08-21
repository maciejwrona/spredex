package com.maciej.spredex;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.maciej.spredex.Cell.Cell;
import com.maciej.spredex.Cell.CellRef;
import com.maciej.spredex.Sheet.Sheet;

public class Dag implements DependencyGraph {
	private final Map<Cell, Set<Cell>> required = new HashMap<>();
	private final Map<Cell, Set<Cell>> dependent = new HashMap<>();

	private final Sheet sheet;

	public Dag(Sheet sheet) {
		this.sheet = sheet;
	}

	@Override
	public void setRequired(Cell from, Collection<CellRef> newRequired) {
		for (Cell cell : getRequired(from)) {
			dependent.get(cell).remove(from);
		}
	
		required.put(from, refCollectionToSet(from, newRequired));

		for (Cell cell : getRequired(from)) {
			dependent.computeIfAbsent(cell, (k) -> new HashSet<>()).add(from);
		}
	}

	private Set<Cell> refCollectionToSet(Cell from, Collection<CellRef> refs) {
		Set<Cell> result = new HashSet<>();
		for (CellRef ref : refs) {
			for (Cell cell : sheet.cellsAt(ref, from)) {
				result.add(cell);
			}
		}
		return result;
	}

	@Override
	public Set<Cell> getRequired(Cell from) {
		return required.getOrDefault(from, new HashSet<>());
	}

	@Override
	public Set<Cell> getDependent(Cell on) {
		return dependent.getOrDefault(on, new HashSet<>());
	}

	@Override 
	public List<Cell> getUpdateOrder(Cell cell) {
		List<Cell> result = new ArrayList<>();
		topoSort(cell, result);
		Collections.reverse(result);
		return result;
	}

	private void topoSort(Cell start, List<Cell> result) {
		if (start.updated()) {
			return;
		}

		for (Cell next : getDependent(start)) {
			topoSort(next, result);
		}

		result.add(start);
	}
}
