package com.maciej.spredex.Sheet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public class SparseMatrix {
	private final Map<Integer, Map<Integer, Cell>> columns = new HashMap<>();

	public Cell get(int row, int column) {
		return columns
			.getOrDefault(column, Collections.emptyMap())
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

	public List<CellLoc> getCellLocationsInRange(CellRange range) {
		return columns.entrySet().stream()
			// Extract the right colums
			.filter(column -> column.getKey() >= range.left().column() && column.getKey() <= range.right().column())
			.flatMap(column -> column.getValue().entrySet().stream())
			// Extract the right rows
			.filter(cell -> cell.getKey() >= range.left().row() && cell.getKey() <= range.right().row())
			.map(cell -> cell.getValue().location())
			.collect(Collectors.toList());

	}
}
