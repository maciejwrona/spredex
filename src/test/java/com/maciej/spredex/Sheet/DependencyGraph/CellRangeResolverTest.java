package com.maciej.spredex.Sheet.DependencyGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;

class CellRangeResolverTest {
	private final int maxRows = 1000000;
	private final CellRangeResolver resolver = new DoubleTreeResolver(maxRows);

	private void add(List<CellRange> ranges) {
		for (CellRange range : ranges) {
			resolver.add(range);
		}
	}

	@Nested
	class AddingTests {
		@Test
		@DisplayName("Should correctly handle adding flat ranges")
		void testAddingFlatRanges() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(3, 1)),
					new CellRange(new CellLoc(2, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 3), new CellLoc(4, 3))
			));
			add(ranges);

			assertEquals(4, resolver.size());
		}

		@Test
		@DisplayName("Should correctly handle adding mixed ranges")
		void testAddingMixedRange() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(3, 3)),
					new CellRange(new CellLoc(20, 32), new CellLoc(40, 40))
			));
			add(ranges);

			assertEquals(3, resolver.size());
		}

		@Test
		@DisplayName("Should correctly handle adding duplicated") 
		void testAddingDuplicates() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(20, 32), new CellLoc(40, 40)),
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(20, 32), new CellLoc(40, 40))
			));
			add(ranges);

			assertEquals(2, resolver.size());
		}
	}

	@Nested
	class DeletionTests {
		@Test
		@DisplayName("Should correctly remove single occurances") 
		void testRemovingSingluarOccurances() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(2, 1)),
					new CellRange(new CellLoc(20, 32), new CellLoc(40, 40))
			));
			add(ranges);

			resolver.deleteOccurance(ranges.get(0));
			assertEquals(2, resolver.size());

			resolver.deleteOccurance(ranges.get(2));
			assertEquals(1, resolver.size());
		}

		@Test
		@DisplayName("Should remove a range only after the number of times its been added") 
		void testRemovingMultipleOccuranes() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(2, 2)),
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(2, 2))
			));
			add(ranges);

			resolver.deleteOccurance(ranges.get(0));
			assertEquals(2, resolver.size());
			resolver.deleteOccurance(ranges.get(0));
			assertEquals(2, resolver.size());
			resolver.deleteOccurance(ranges.get(0));
			assertEquals(1, resolver.size());

			resolver.deleteOccurance(ranges.get(2));
			assertEquals(1, resolver.size());
			resolver.deleteOccurance(ranges.get(2));
			assertEquals(0, resolver.size());
		}
	}

	@Nested
	class QueryTests {
		@Test
		@DisplayName("Should correctly answer quries without overlap")
		void testNonOverlappingQueries() {
			List<CellRange> ranges = new ArrayList<>(List.of(
					new CellRange(new CellLoc(1, 1), new CellLoc(10, 1)),
					new CellRange(new CellLoc(1, 1), new CellLoc(3, 3)),
					new CellRange(new CellLoc(20, 32), new CellLoc(40, 40))
			));
			add(ranges);

			List<CellRange> intersections = resolver.getRangesContainingCell(new CellLoc(5, 1));

			assertEquals(1, intersections.size());
			assertEquals(ranges.get(0), intersections.get(0));

			intersections = resolver.getRangesContainingCell(new CellLoc(30, 32));

			assertEquals(1, intersections.size());
			assertEquals(ranges.get(2), intersections.get(0));
		}

		@Test
		@DisplayName("Should correctly answer quries with large overlap")
		void testOverlappingQueries() {
			for (int i = 1; i <= 100; i++) {
				for (int j = i; j <= 100; j++) {
					resolver.add(new CellRange(new CellLoc(i, i), new CellLoc(j, j)));
				}
			}

			List<CellRange> intersections = resolver.getRangesContainingCell(new CellLoc(100, 100));
			assertEquals(100, intersections.size());

			intersections = resolver.getRangesContainingCell(new CellLoc(50, 50));
			assertEquals((50 - 1 + 1) * (100 - 50 + 1), intersections.size());
		}

		@Test
		@DisplayName("Should correctly answer quires with large overlap on the same column")
		void testColumnOverlappingQueries() {
			for (int i = 1; i < 1000; i++) {
				resolver.add(new CellRange(new CellLoc(i, 1), new CellLoc(500 + i, 1)));
			}

			List<CellRange> intersections = resolver.getRangesContainingCell(new CellLoc(600, 1));
			assertEquals(600 - 100 + 1, intersections.size());
		}
	}
}
