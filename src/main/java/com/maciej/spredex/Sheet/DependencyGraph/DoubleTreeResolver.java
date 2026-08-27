package com.maciej.spredex.Sheet.DependencyGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

public class DoubleTreeResolver implements CellRangeResolver {
	private final int maxRows;
	private final Map<CellRange, AtomicInteger> refCounter = new HashMap<>();

	private final Map<Integer, SegmentTree1D> columnTrees = new HashMap<>();
	private final CellRangeContainer mainTree = new CellRangeContainer();

	public DoubleTreeResolver(int maxRows) {
		this.maxRows = maxRows;
	}

	public int size() {
		int result = 0;
		for (SegmentTree1D column : columnTrees.values()) {
			result += column.size();
		}

		result += mainTree.size();

		return result;
	}

	@Override
	public void add(CellRange range) {
		if (
			refCounter	
				.computeIfAbsent(range, (k) -> new AtomicInteger(0))
				.incrementAndGet() >= 2) {
			return;
		}

		if (FlatRange.isFlat(range)) {
			FlatRange flat = new FlatRange(range);
			addFlatRange(flat, range.left().column());
		}
		else {
			add2dRange(range);
		}
	}

	@Override
	public void deleteOccurance(CellRange range) {
		if (refCounter.get(range).decrementAndGet() != 0) {
			return;
		}

		refCounter.remove(range);
		if (FlatRange.isFlat(range)) {
			FlatRange flat = new FlatRange(range);
			removeFlatRange(flat, range.left().column());
		}
		else {
			remove2dRange(range);
		}
	}

	@Override
	public List<CellRange> getRangesContainingCell(CellLoc cell) {
		List<CellRange> result = new ArrayList<>();

		for (FlatRange range : getFlatRangesContaining(cell)) {
			result.add(range.toCellRange(cell.column()));
		}
		for (CellRange range : mainTree.getRangesContaining(cell)) {
			result.add(range);
		}

		return result;
	}

	private List<FlatRange> getFlatRangesContaining(CellLoc cell) {
		if (!columnTrees.containsKey(cell.column())) {
			return new ArrayList<>();
		}

		return columnTrees.get(cell.column()).getRangesContaining(cell.row());
	}

	private void addFlatRange(FlatRange range, int column) {
		columnTrees
			.computeIfAbsent(column, (k) -> new SegmentTree1D(0, maxRows))
			.add(range);
	}

	private void add2dRange(CellRange range) {
		mainTree.add(range);
	}

	private void removeFlatRange(FlatRange range, int column) {
		columnTrees.get(column).remove(range);
	}

	private void remove2dRange(CellRange range) {
		mainTree.remove(range);
	}
}
