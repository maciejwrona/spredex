package com.maciej.spredex.Sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellLoc;

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

			assertEquals(null, sheet.valueAt(cells.get(1)));
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
}
