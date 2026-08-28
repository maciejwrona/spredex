package com.maciej.spredex.Sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellLoc;

class SheetTest {
	private final int maxRows = 100000;
	private final int maxColumns = 1000;
	private final Sheet sheet = new Sheet(maxRows, maxColumns);

	@Nested
	@DisplayName("Non formula tests")
	class NonFormulaTests {
		@Test
		@DisplayName("Should correctly set non-formula string cells")
		void testSettingStringCell() {
			sheet.setCell(new CellLoc(1, 1), "hello");

			assertEquals("hello", sheet.valueAt(new CellLoc(1, 1)));
		}

		@Test
		@DisplayName("Should correctly set non-formula decimal and integer cells")
		void testSettingNumberCell() {
			sheet.setCell(new CellLoc(1, 1), "1.1");
			sheet.setCell(new CellLoc(2, 2), "0");
			sheet.setCell(new CellLoc(3, 3), "-3.2");

			assertEquals(1.1, sheet.valueAt(new CellLoc(1, 1)));
			assertEquals(0.0, sheet.valueAt(new CellLoc(2, 2)));
			assertEquals(-3.2, sheet.valueAt(new CellLoc(3, 3)));
		}

		@Test
		@DisplayName("Should set cell as empty") 
		void testEmptyCell() {
			sheet.setCell(new CellLoc(1, 1), "hello");
			sheet.setCell(new CellLoc(1, 1), "");

			assertEquals(null, sheet.valueAt(new CellLoc(1, 1)));
		}
	}

	@Nested
	@DisplayName("Formula tests")
	class FormulaTests {
		@Test
		@DisplayName("Should set basic formula cells")
		void testBasicFormula() {
			sheet.setCell(new CellLoc(1, 1), "1.1");
			sheet.setCell(new CellLoc(2, 2), "=A1");
			sheet.setCell(new CellLoc(3, 3), "=A1+B2");

			assertEquals(1.1, sheet.valueAt(new CellLoc(2, 2)));
			assertEquals(2.2, sheet.valueAt(new CellLoc(3, 3)));
		}

		@Test
		@DisplayName("Should correctly update dependent cells")
		void testUpdateDependentCells() {
			sheet.setCell(new CellLoc(1, 1), "1.1");
			sheet.setCell(new CellLoc(2, 2), "=A1");
			sheet.setCell(new CellLoc(3, 3), "=B2 + 1");
			sheet.setCell(new CellLoc(1, 1), "2.0");

			assertEquals(2.0, sheet.valueAt(new CellLoc(2, 2)));
			assertEquals(3.0, sheet.valueAt(new CellLoc(3, 3)));
		}
	}

	@Nested
	@DisplayName("Error tests")
	class ErrorTests {
		// Add cycle tests
		@Test
		@DisplayName("Should correctly detect errors")
		void testBasicErrors() {
			sheet.setCell(new CellLoc(1, 1), "hello");
			sheet.setCell(new CellLoc(2, 2), "=A1 + 1");
			sheet.setCell(new CellLoc(3, 3), "=1 +");
			sheet.setCell(new CellLoc(4, 4), "1");
			sheet.setCell(new CellLoc(5, 5), "=D4 + 1");

			assertTrue(sheet.isErrorAt(new CellLoc(2, 2)));
			assertTrue(sheet.isErrorAt(new CellLoc(3, 3)));
			assertFalse(sheet.isErrorAt(new CellLoc(5, 5)));

			sheet.setCell(new CellLoc(4, 4), "hello");

			assertTrue(sheet.isErrorAt(new CellLoc(5, 5)));
		}

		@Test
		@DisplayName("Should correctly update dependent cells after an error has occured")
		void testErrorRecovery() {
			sheet.setCell(new CellLoc(1, 1), "hello");
			sheet.setCell(new CellLoc(2, 2), "=A1 + 1");
			sheet.setCell(new CellLoc(1, 1), "1");

			assertEquals(2.0, sheet.valueAt(new CellLoc(2, 2)));
		}
	}
}
