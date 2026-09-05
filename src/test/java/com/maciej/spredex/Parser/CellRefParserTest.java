package com.maciej.spredex.Parser;

import com.maciej.spredex.CellError;
import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRef.CellRef;
import com.maciej.spredex.CellRef.SingleCellRef;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CellRefParserTest {
	private final int unbounded = CellRef.unbounded();

	@ParameterizedTest
	@CsvSource({ 
		"AB10,  18,   0,  false,   false", 
		"$Z45,  26,  35,   true,   false", 
		"A$10,  -9,  10,  false,    true",
		"$B$4,   2,   4,   true,    true"
	})
	@DisplayName("Should correctly parse standard single references")
	void testStandardReference(String textRef, String column, String row, 
			  								   String lockedColumn, String lockedRow) {
		CellRefParser parser = new CellRefParser(textRef, 10000, 10000);
		CellLoc currentLocation = new CellLoc(10, 10);

		SingleCellRef ref = parser.parse(currentLocation);

		assertThat(ref.column()).isEqualTo(Integer.parseInt(column));
		assertThat(ref.row()).isEqualTo(Integer.parseInt(row));
		assertThat(ref.lockedColumn()).isEqualTo(Boolean.parseBoolean(lockedColumn));
		assertThat(ref.lockedRow()).isEqualTo(Boolean.parseBoolean(lockedRow));
	}

	@ParameterizedTest
	@CsvSource({ 
		"$10, 	 0,  10,  false,    true", 
		"1,      0,  -9,  false,   false"
	})
	@DisplayName("Should correctly parse now row/column references")
	void testMissingColumn(String textRef, String column, String row, 
			  								   String lockedColumn, String lockedRow) {
		CellRefParser parser = new CellRefParser(textRef, 100000, 100000);
		CellLoc currentLocation = new CellLoc(10, 10);

		SingleCellRef ref = parser.parse(currentLocation);

		assertThat(ref.column()).isEqualTo(unbounded);
		assertThat(ref.row()).isEqualTo(Integer.parseInt(row));
		assertThat(ref.lockedColumn()).isEqualTo(Boolean.parseBoolean(lockedColumn));
		assertThat(ref.lockedRow()).isEqualTo(Boolean.parseBoolean(lockedRow));
	}

	@ParameterizedTest
	@ValueSource(strings = { "$$A32", " $32", "$$32", "$$AB", "$AB$", "!A10" })
	@DisplayName("Should correctly identify invalid references")
	void testInvalidReference(String textRef) {
		CellRefParser parser = new CellRefParser(textRef, 100000, 100000);
		CellLoc currentLocation = new CellLoc(10, 10);

		assertThatThrownBy(() -> parser.parse(currentLocation))
			.isInstanceOf(CellError.class);
	}

	@Test
	@DisplayName("Should convert row number to string")
	void testRowToString() {
		List<Integer> numbers = List.of(
				  1,   4,   26,   27,   58,   751);
		List<String> strings = List.of(
				"A", "D",  "Z", "AA", "BF", "ABW");

		for (int i = 0; i < numbers.size(); i++) {
			assertEquals(strings.get(i), CellRefParser.numberToColumn(numbers.get(i)));
		}
	}
}
