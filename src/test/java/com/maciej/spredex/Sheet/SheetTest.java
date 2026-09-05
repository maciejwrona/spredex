package com.maciej.spredex.Sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

class SheetTest {
	private final int maxRows = 100000;
	private final int maxColumns = 1000;
	private final Sheet sheet = new Sheet(maxRows, maxColumns);

	private static final List<CellLoc> cells = new ArrayList<>();

	@BeforeAll
	static void InitilaizeCellsList() {
		cells.add(null);
		for (int i = 1; i <= 10; i++) {
			cells.add(new CellLoc(i, i));
		}
	}

	@Nested
	@DisplayName("Non formula tests")
	class NonFormulaTests {
		@Test
		@DisplayName("Should correctly set non-formula string cells")
		void testSettingStringCell() {
			sheet.setCell(cells.get(1), "hello");

			assertEquals("hello", sheet.valueAt(cells.get(1)));
		}

		@Test
		@DisplayName("Should correctly set non-formula decimal and integer cells")
		void testSettingNumberCell() {
			sheet.setCell(cells.get(1), "1.1");
			sheet.setCell(cells.get(2), "0");
			sheet.setCell(cells.get(3), "-3.2");

			assertEquals(1.1, sheet.valueAt(cells.get(1)));
			assertEquals(0.0, sheet.valueAt(cells.get(2)));
			assertEquals(-3.2, sheet.valueAt(cells.get(3)));
		}

		@Test
		@DisplayName("Should set cell as empty") 
		void testEmptyCell() {
			sheet.setCell(cells.get(1), "hello");
			sheet.setCell(cells.get(1), "");

			assertEquals(new EmptyCell(), sheet.valueAt(cells.get(1)));
		}
	}

	@Nested
	@DisplayName("Formula tests")
	class FormulaTests {
		@Test
		@DisplayName("Should set basic formula cells")
		void testBasicFormula() {
			sheet.setCell(cells.get(1), "1.1");
			sheet.setCell(cells.get(2), "=A1");
			sheet.setCell(cells.get(3), "=A1+B2");

			assertEquals(1.1, sheet.valueAt(cells.get(2)));
			assertEquals(2.2, sheet.valueAt(cells.get(3)));
		}

		@Test
		@DisplayName("Should correctly update dependent cells")
		void testUpdateDependentCells() {
			sheet.setCell(cells.get(1), "1.1");
			sheet.setCell(cells.get(2), "=A1");
			sheet.setCell(cells.get(3), "=B2 + 1");
			sheet.setCell(cells.get(1), "2.0");

			assertEquals(2.0, sheet.valueAt(cells.get(2)));
			assertEquals(3.0, sheet.valueAt(cells.get(3)));
		}
	}

	@Nested
	@DisplayName("Error tests")
	class ErrorTests {
		// Add cycle tests
		@Test
		@DisplayName("Should correctly detect errors")
		void testBasicErrors() {
			sheet.setCell(cells.get(1), "hello");
			sheet.setCell(cells.get(2), "=A1 + 1");
			sheet.setCell(cells.get(3), "=1 +");
			sheet.setCell(cells.get(4), "1");
			sheet.setCell(cells.get(5), "=D4 + 1");
			sheet.setCell(cells.get(6), "=A1:B2");

			assertTrue(sheet.isErrorAt(cells.get(2)));
			assertTrue(sheet.isErrorAt(cells.get(3)));
			assertFalse(sheet.isErrorAt(cells.get(5)));
			assertEquals("#TYPE", sheet.valueAt(cells.get(6)));

			sheet.setCell(cells.get(4), "hello");

			assertTrue(sheet.isErrorAt(cells.get(5)));
		}

		@Test
		@DisplayName("Should only output errors at cells that are part of a cycle")
		void testCycleLocation() {
			sheet.setCell(cells.get(1), "=1");
			sheet.setCell(cells.get(2), "=A1 + C3");
			sheet.setCell(cells.get(3), "=B2");

			assertFalse(sheet.isErrorAt(cells.get(1)));
			assertEquals(1.0, sheet.valueAt(cells.get(1)));
			assertTrue(sheet.isErrorAt(cells.get(2)));
			assertTrue(sheet.isErrorAt(cells.get(3)));
		}

		@Test
		@DisplayName("Should recover after a cycle has been removed")
		void testCycleRecovery() {
			sheet.setCell(cells.get(1), "=1");
			sheet.setCell(cells.get(2), "=A1 + C3");
			sheet.setCell(cells.get(3), "=B2");

			sheet.setCell(cells.get(3), "=1");

			assertFalse(sheet.isErrorAt(cells.get(2)));
			assertFalse(sheet.isErrorAt(cells.get(3)));
			assertEquals(2.0, sheet.valueAt(cells.get(2)));

		}
		@Test
		@DisplayName("Should correctly update dependent cells after an error has occured")
		void testErrorRecovery() {
			sheet.setCell(cells.get(1), "hello");
			sheet.setCell(cells.get(2), "=A1 + 1");
			sheet.setCell(cells.get(1), "1");

			assertEquals(2.0, sheet.valueAt(cells.get(2)));
		}
	}

	@Test
	@DisplayName("Should correctly return cells in range")
	void testGetCellRange() {
		sheet.setCell(cells.get(1), "hello");
		sheet.setCell(cells.get(3), "bye");

		assertIterableEquals(List.of(cells.get(1)), 
				sheet.cellsInRange(new CellRange(cells.get(1), cells.get(2))));
		assertIterableEquals(List.of(cells.get(1)), 
				sheet.cellsInRange(new CellRange(cells.get(1), new CellLoc(maxRows - 1, 1))));
		assertIterableEquals(List.of(cells.get(1), cells.get(3)), 
				sheet.cellsInRange(new CellRange(cells.get(1), cells.get(5))));
	}

	@Test
	@DisplayName("Should correctly add multiple cells")
	void testAddingMultipleCells() {
		Map<CellLoc, String> formulas = new HashMap<>();
		for (CellLoc cell : cells) {
			if (cell != null) {
				formulas.put(cell, "=A2");
			}
		}
		formulas.put(cells.get(5), "=E5");

		sheet.setMultipleCells(formulas);
		
		for (CellLoc cell : cells) {
			if (cell != null && cell != cells.get(5)) {
				assertEquals(0.0, sheet.valueAt(cell));
				assertFalse(sheet.isErrorAt(cell));
			}
			else if (cell != null) {
				assertTrue(sheet.isErrorAt(cell));
			}
		}
	}

	@Nested
	@DisplayName("Test swiping")
	class SwipeTests {
		@Test
		@DisplayName("Should correctly perform basic swipe")
		void testBasicSwipe() {
			Set<CellLoc> toFill = Set.of(
					new CellLoc(2, 1), new CellLoc(3, 1), new CellLoc(4, 1));
			sheet.setCell(cells.get(1), "hello!");
			sheet.swipeCellToFillList(cells.get(1), toFill);

			assertEquals("hello!", sheet.valueAt(cells.get(1)));
			for (CellLoc cell : toFill) {
				assertEquals("hello!", sheet.valueAt(cell));
				assertFalse(sheet.isErrorAt(cell));
			}
		}

		@Test
		@DisplayName("Should perform reference swipe") 
		void testReferenceSwipe() {
			CellLoc start = new CellLoc(1, 2);
			sheet.setCell(start, "=A1");
			Set<CellLoc> toFill = new HashSet<>();

			for (int i = 2; i <= 5; i++) {
				sheet.setCell(new CellLoc(i, 1), Integer.toString(i));
				toFill.add(new CellLoc(i, 2));
			}

			sheet.swipeCellToFillList(start, toFill);

			for (CellLoc filled : toFill) {
				assertEquals((double)filled.row(), sheet.valueAt(filled));
				assertFalse(sheet.isErrorAt(filled));
			}
		}

		@Test
		@DisplayName("Should perform a swipe with locked references")
		void testLockedReferenceSwipe() {
			sheet.setCell(new CellLoc(1, 2), "=A$1");
			sheet.setCell(new CellLoc(1, 1), "A1");
			sheet.swipeCellToFillList(new CellLoc(1, 2), Set.of(cells.get(2)));

			assertEquals("A1", sheet.valueAt(cells.get(2)));
			assertFalse(sheet.isErrorAt(cells.get(2)));
		}

		@Test
		@DisplayName("Should propagate errors on swipe")
		void testErrorSwipe() {
			sheet.setCell(cells.get(1), "=E1 +");
			assertTrue(sheet.isErrorAt(cells.get(1)));

			sheet.swipeCellToFillList(cells.get(1), Set.of(cells.get(2)));
			assertTrue(sheet.isErrorAt(cells.get(2)));
		}

		@Test
		@DisplayName("Should update cycles on swipe")
		void testCycleSwipe() {
			List<CellLoc> column = new ArrayList<>();
			column.add(null);
			for (int i = 1; i <= 5; i++) {
				column.add(new CellLoc(i, 2));
			}

			// Set A2
			sheet.setCell(new CellLoc(2, 1), "=B2");

			sheet.setCell(column.get(1), "=$A1");

			// Create cycle
			sheet.setCell(column.get(4), "=B5");
			sheet.setCell(column.get(5), "=B4");
			sheet.setCell(column.get(3), "=B4");
			assertTrue(sheet.isErrorAt(column.get(4)));
			assertTrue(sheet.isErrorAt(column.get(5)));
			assertTrue(sheet.isErrorAt(column.get(3)));

			// Should remove the previous cycle and create one at B2
			sheet.swipeCellToFillList(column.get(1), Set.of(
						column.get(2), column.get(3), column.get(4), column.get(5)));

			assertFalse(sheet.isErrorAt(column.get(1)));
			assertTrue(sheet.isErrorAt(column.get(2)));
			assertFalse(sheet.isErrorAt(column.get(3)));
			assertFalse(sheet.isErrorAt(column.get(4)));
			assertFalse(sheet.isErrorAt(column.get(5)));
		}

		@Test
		@DisplayName("Should swipe empty cell")
		void testSwipeEmptyCell() {
			sheet.setCell(cells.get(2), "Hello!");
			sheet.swipeCellToFillList(cells.get(1), Set.of(cells.get(2), cells.get(3)));

			assertEquals(new EmptyCell(), sheet.valueAt(cells.get(1)));
			assertEquals(new EmptyCell(), sheet.valueAt(cells.get(2)));
			assertEquals(new EmptyCell(), sheet.valueAt(cells.get(3)));
		}
	}
}
