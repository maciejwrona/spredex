package com.maciej.spredex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CellLocTest {
	@Test
	@DisplayName("Should convert row number to string")
	void testRowToString() {
		List<Integer> numbers = List.of(
				  1,   4,   26,   27,   58,   751);
		List<String> strings = List.of(
				"A", "D",  "Z", "AA", "BF", "ABW");

		for (int i = 0; i < numbers.size(); i++) {
			assertEquals(strings.get(i), CellLoc.numberToColumn(numbers.get(i)));
		}
	}
}
