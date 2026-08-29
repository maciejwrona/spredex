package com.maciej.spredex.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.SingleCellRef;
import com.maciej.spredex.CellRef.RangeRef;
import com.maciej.spredex.Sheet.Sheet;

class SumTest {
	private final int maxRows = 10000;
	private final int maxColumns = 10000;
	private final Sheet sheet = new Sheet(maxRows, maxColumns);

	private static final List<CellLoc> cells = new ArrayList<>();

	@BeforeAll
	static void initializeCells() {
		cells.add(null);
		for (int i = 1; i <= 10; i++) {
			cells.add(new CellLoc(i, i));
		}
	}

	@Test
	@DisplayName("Should add numbers correctly")
	void testNumbers() {
		assertEquals(42.0, 
				new Sum().call(List.of(28.0, 21.0, -7.0), cells.get(1), sheet));
	}

	@Test
	@DisplayName("Should add numbers in ranges correctly")
	void testRangeReferences() {
		sheet.setCell(cells.get(1), "1");
		sheet.setCell(cells.get(2), "-2");

		assertEquals(3.0, 
				new Sum().call(
					List.of(new RangeRef(new SingleCellRef(1, 1, true, true),
										 new SingleCellRef(3, 3, true, true)),
							4.0),
					cells.get(1),
					sheet)
		);
	}

	@Test
	@DisplayName("Should error invalid types")
	void testTypeError() {
		assertThrows(CellError.class, () -> new Sum().call(List.of("hello"), cells.get(1), sheet));
	}
}
