package com.maciej.spredex.Sheet.DependencyGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.CellCoordinates;

class DependencyGraphTest {

	private final int maxRows = 1000000;
	private final DependencyGraph graph = new DependencyGraph(maxRows);

	private static final List<CellLoc> cells = new ArrayList<>();

	@BeforeAll
	static void InitializeCellsList() {
		cells.add(null);
		for (int i = 1; i <= 10; i++) {
			cells.add(new CellLoc(i, i));
		}
	}
	
	@Test
	@DisplayName("Should correctly add single dependencies")
	void testAddSingleDependencies() {
		graph.updateRequired(cells.get(1), Set.of(new CellLoc(2,3), new CellLoc(3, 2)));
		graph.updateRequired(cells.get(1), Set.of(new CellLoc(3,3)));
		graph.updateRequired(new CellLoc(4, 3), Set.of(new CellLoc(3,3)));
		graph.updateRequired(cells.get(3), Set.of(new CellLoc(2,2)));

		assertThat(graph.getSingleRequired(cells.get(1)))
			.containsExactlyInAnyOrderElementsOf(List.of(cells.get(3)));
		assertThat(graph.getSingleDependent(cells.get(3)))
			.containsExactlyInAnyOrderElementsOf(List.of(cells.get(1), new CellLoc(4, 3)));
	}

	@Test
	@DisplayName("Should correctly add range dependencies")
	void testAddRangeDependencies() {
		List<CellRange> rangesRequired = List.of(
					new CellRange(cells.get(3), cells.get(4)),
					new CellRange(cells.get(4), cells.get(5)));
		List<CellCoordinates> required = new ArrayList<>(rangesRequired);
		graph.updateRequired(cells.get(1), required);

		assertThat(graph.getRangesRequired(new CellLoc(1,1)))
			.containsExactlyInAnyOrderElementsOf(rangesRequired);
		assertThat(graph.getDependentOnRange(
					new CellRange(cells.get(3), cells.get(4))))
			.containsExactlyInAnyOrderElementsOf(List.of(cells.get(1)));


		graph.updateRequired(cells.get(1), new ArrayList<>());
		assertEquals(0, graph.getRangesRequired(cells.get(1)).size());
	}

	@Nested
	@DisplayName("Test update order")
	class UpdateOrderTests {
		@Test
		@DisplayName("Should return correct update order on a straight path")
		void testSimplePath() {
			List<CellLoc> path = List.of(
					cells.get(1),
					cells.get(2),
					cells.get(3),
					cells.get(4),
					cells.get(5)
			);

			for (int i = path.size() - 1; i >= 1; i--) {
				graph.updateRequired(path.get(i), List.of(path.get(i - 1)));
			}

			assertIterableEquals(path, graph.getUpdateOrder(path.get(0)));
		}

		@Test
		@DisplayName("Should return correct update order on complex path")
		void testComplexSingleDependencyPath() {
			/*
			 * (1, 1) 
			 *    |  \
			 *   \|/  \|
			 * (2, 2)  (3, 3)   On updating 1 we should get 1->3->4->2
			 *   /|\  /
			 *    | |/
			 * (4, 4)
			 */
			graph.updateRequired(cells.get(2), List.of(cells.get(1), cells.get(4)));
			graph.updateRequired(cells.get(3), List.of(cells.get(1)));
			graph.updateRequired(cells.get(4), List.of(cells.get(3)));

			assertIterableEquals(List.of(cells.get(1), cells.get(3), cells.get(4), cells.get(2)), 
					     		 graph.getUpdateOrder(cells.get(1)));
		}

		@Test
		@DisplayName("Should return correct update order on range inputs")
		void testRangePath() {
			CellRange range = new CellRange(cells.get(2), cells.get(3));

			graph.updateRequired(cells.get(1), List.of(range));
			graph.updateRequired(cells.get(4), List.of(cells.get(1), cells.get(2)));

			assertIterableEquals(List.of(cells.get(2), cells.get(1), cells.get(4)), 
								 graph.getUpdateOrder(cells.get(2)));
		}

		@Test
		@DisplayName("Should correctly detect cycles")
		void testCycle() {
			Optional<CycleError> noError = graph.updateRequired(cells.get(1), 
					List.of(cells.get(2), cells.get(3)));
			Optional<CycleError> error = graph.updateRequired(cells.get(3), 
					List.of(new CellRange(cells.get(1), cells.get(2))));

			assertTrue(noError.isEmpty());
			assertTrue(error.isPresent());
		}

		@Test
		@DisplayName("Should output correct cycle location")
		void testCycleLocation() {
			graph.updateRequired(cells.get(2), List.of(cells.get(3), cells.get(1)));
			graph.updateRequired(cells.get(3), List.of(cells.get(2)));
			Optional<CycleError> error = graph.updateRequired(
					cells.get(1), new ArrayList<>());

			assertTrue(error.isPresent());

			CellLoc location = error.get().location();
			assertTrue(location.equals(cells.get(2)) || location.equals(cells.get(3)));
		}
	}
}

