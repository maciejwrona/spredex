package com.maciej.spredex.Sheet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SparseMatrix {
	private final Map<Integer, Map<Integer, Cell>> columns = new HashMap<>();

	public Cell get(int row, int column) {
		return columns
			.getOrDefault(column, new HashMap<>())
			.getOrDefault(row, null);
	}

	public void createCell(int row, int column) {
		if (get(row, column) != null) {
			return;
		}

		columns
			.computeIfAbsent(column, (k) -> new HashMap<>())
			.put(row, new Cell(row, column));
	}

	public void deleteCell(int row, int column) {
		if (!columns.containsKey(column)) {
			return;
		}

		columns.get(column).remove(row);
	}

	public Collection<Cell> getCellsInColumn(int column) {
		return columns
			.getOrDefault(column, new HashMap<>())
			.values();
	}
}
