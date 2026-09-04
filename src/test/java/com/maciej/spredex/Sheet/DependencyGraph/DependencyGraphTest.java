package com.maciej.spredex.Sheet.DependencyGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
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
		graph.setRequired(cells.get(1), Set.of(new CellLoc(2,3), new CellLoc(3, 2)));
		graph.setRequired(cells.get(1), Set.of(new CellLoc(3,3)));
		graph.setRequired(new CellLoc(4, 3), Set.of(new CellLoc(3,3)));
		graph.setRequired(cells.get(3), Set.of(new CellLoc(2,2)));

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
		graph.setRequired(cells.get(1), required);

		assertThat(graph.getRangesRequired(new CellLoc(1,1)))
			.containsExactlyInAnyOrderElementsOf(rangesRequired);
		assertThat(graph.getDependentOnRange(
					new CellRange(cells.get(3), cells.get(4))))
			.containsExactlyInAnyOrderElementsOf(List.of(cells.get(1)));


		graph.setRequired(cells.get(1), new ArrayList<>());
		assertEquals(0, graph.getRangesRequired(cells.get(1)).size());
	}

	@Test
	@DisplayName("Should output correct list of all dependents")
	void testAllDependents() {
		graph.setRequired(cells.get(1), new ArrayList<>());
		graph.setRequired(cells.get(2), List.of(cells.get(1)));
		graph.setRequired(cells.get(3), List.of(new CellRange(cells.get(1), cells.get(1))));

		assertEquals(List.of(cells.get(2), cells.get(3)), graph.getDependent(cells.get(1)));
	}

	@Test
	@DisplayName("TEST TO WRITE")
	void testAddingMultipleCells() {
		assertTrue(true);
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
				graph.setRequired(path.get(i), List.of(path.get(i - 1)));
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
			graph.setRequired(cells.get(2), List.of(cells.get(1), cells.get(4)));
			graph.setRequired(cells.get(3), List.of(cells.get(1)));
			graph.setRequired(cells.get(4), List.of(cells.get(3)));

			assertIterableEquals(List.of(cells.get(1), cells.get(3), cells.get(4), cells.get(2)), 
					     		 graph.getUpdateOrder(cells.get(1)));
		}

		@Test
		@DisplayName("Should return correct update order on range inputs")
		void testRangePath() {
			CellRange range = new CellRange(cells.get(2), cells.get(3));

			graph.setRequired(cells.get(1), List.of(range));
			graph.setRequired(cells.get(4), List.of(cells.get(1), cells.get(2)));

			assertIterableEquals(List.of(cells.get(2), cells.get(1), cells.get(4)), 
								 graph.getUpdateOrder(cells.get(2)));
		}

		@Test
		@DisplayName("Should return all dependent cells on cycle")
		void testCycleOrder() {
			/*
			 * 4
			 * |
			 * \/
			 * 3 <-> 2
			 * |	 |
			 * \ 	/
			 *  \||/
			 *    1
			 */
			graph.setRequired(cells.get(1), List.of(cells.get(2), cells.get(3)));
			graph.setRequired(cells.get(2), List.of(cells.get(3)));
			graph.setRequired(cells.get(3), List.of(cells.get(2), cells.get(4)));

			assertThat(List.of(cells.get(1), cells.get(2), cells.get(3), cells.get(4)))
				.hasSameElementsAs(graph.getUpdateOrder(cells.get(4)));
		}
	}

	@Nested
	@DisplayName("Test cycle detection")
	class TestCycleDetection {
		@Test
		@DisplayName("Should detect a basic cycle")
		void testCycle() {
			graph.setRequired(cells.get(1), List.of(cells.get(2)));
			graph.setRequired(cells.get(2), List.of(cells.get(1)));

			assertTrue(graph.isInCycle(cells.get(2)));
			assertTrue(graph.isInCycle(cells.get(1)));
		}

		@Test
		@DisplayName("Should output only the dependent on cycle cells")
		void testCycleDependents() {
			// 4 <- 3 <-> 2 <- 1
			graph.setRequired(cells.get(4), List.of(cells.get(3)));
			graph.setRequired(cells.get(2), List.of(cells.get(3), cells.get(1)));
			graph.setRequired(cells.get(3), List.of(cells.get(2)));
			graph.setRequired(cells.get(1), new ArrayList<>());

			assertFalse(graph.isInCycle(cells.get(1)));
			assertTrue(graph.isInCycle(cells.get(2)));
			assertTrue(graph.isInCycle(cells.get(3)));
			assertTrue(graph.isInCycle(cells.get(4)));
		}

		@Test
		@DisplayName("Should correctly detect cycles containing ranges")
		void testCycleWithRange() {
			graph.setRequired(cells.get(1), List.of(cells.get(2), cells.get(3)));
			graph.setRequired(cells.get(3), List.of(new CellRange(cells.get(1), cells.get(2))));

			assertTrue(graph.isInCycle(cells.get(1)));
			assertFalse(graph.isInCycle(cells.get(2)));	
			assertTrue(graph.isInCycle(cells.get(3)));	
		}

		@Test
		@DisplayName("Should detect self-cycle")
		void testSelfCycle() {
			graph.setRequired(cells.get(1), List.of(cells.get(1)));

			assertTrue(graph.isInCycle(cells.get(1)));
		}

		@Test
		@DisplayName("Should update cell if dependent on not reachable cycle")
		void testUnReachableCycle() {
			// 1 <-> 2
			// \| 	|/
			// 	  3
			graph.setRequired(cells.get(1), List.of(cells.get(2)));
			graph.setRequired(cells.get(2), List.of(cells.get(1)));
			graph.setRequired(cells.get(3), List.of(new CellRange(cells.get(1), cells.get(2))));

			assertTrue(graph.isInCycle(cells.get(3)));
		}

		@Test
		@DisplayName("Should detect when disconnecting from cycle")
		void testDiconnectingFromCycle() {
			// 1 <-> 2 -> 3
			graph.setRequired(cells.get(1), List.of(cells.get(2)));
			graph.setRequired(cells.get(2), List.of(cells.get(1)));
			graph.setRequired(cells.get(3), List.of(cells.get(1)));

			assertTrue(graph.isInCycle(cells.get(1)));
			assertTrue(graph.isInCycle(cells.get(2)));
			assertTrue(graph.isInCycle(cells.get(3)));

			// 1 <-> 2   3
			graph.setRequired(cells.get(3), new ArrayList<>());

			assertTrue(graph.isInCycle(cells.get(1)));
			assertTrue(graph.isInCycle(cells.get(2)));
			assertFalse(graph.isInCycle(cells.get(3)));
		}

		@Test
		@DisplayName("Should detect when cycle breaks")
		void testBreakingCycle() {
			// 1 -> 2 -> 3 -> 1
			graph.setRequired(cells.get(1), List.of(cells.get(2)));
			graph.setRequired(cells.get(2), List.of(cells.get(3)));
			graph.setRequired(cells.get(3), List.of(cells.get(1)));

			assertTrue(graph.isInCycle(cells.get(1)));
			assertTrue(graph.isInCycle(cells.get(2)));
			assertTrue(graph.isInCycle(cells.get(3)));
	
			// 1 -> 2 -> 3
			graph.setRequired(cells.get(1), new ArrayList<>());

			assertFalse(graph.isInCycle(cells.get(1)));
			assertFalse(graph.isInCycle(cells.get(2)));
			assertFalse(graph.isInCycle(cells.get(3)));

		}
	}
}

