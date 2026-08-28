package com.maciej.spredex.Sheet.DependencyGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.CellRange;
import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellError;

class DependencyGraphTest {

	private final int maxRows = 1000000;
	private final DependencyGraph graph = new DependencyGraph(maxRows);
	
	@Test
	@DisplayName("Should correctly add single dependencies")
	void testAddSingleDependencies() {
		graph.updateRequired(new CellLoc(1, 1), Set.of(new CellLoc(2,3), new CellLoc(3, 2)));
		graph.updateRequired(new CellLoc(1, 1), Set.of(new CellLoc(3,3)));
		graph.updateRequired(new CellLoc(4, 3), Set.of(new CellLoc(3,3)));
		graph.updateRequired(new CellLoc(3, 3), Set.of(new CellLoc(2,2)));

		assertThat(graph.getSingleRequired(new CellLoc(1, 1)))
			.containsExactlyInAnyOrderElementsOf(List.of(new CellLoc(3, 3)));
		assertThat(graph.getSingleDependent(new CellLoc(3, 3)))
			.containsExactlyInAnyOrderElementsOf(List.of(new CellLoc(1, 1), new CellLoc(4, 3)));
	}

	@Test
	@DisplayName("Should correctly add range dependencies")
	void testAddRangeDependencies() {
		List<CellRange> rangesRequired = List.of(
					new CellRange(new CellLoc(3, 3), new CellLoc(4, 4)),
					new CellRange(new CellLoc(4, 4), new CellLoc(5, 5)));
		List<CellCoordinates> required = new ArrayList<>(rangesRequired);
		graph.updateRequired(new CellLoc(1, 1), required);

		assertThat(graph.getRangesRequired(new CellLoc(1,1)))
			.containsExactlyInAnyOrderElementsOf(rangesRequired);
		assertThat(graph.getDependentOnRange(
					new CellRange(new CellLoc(3, 3), new CellLoc(4, 4))))
			.containsExactlyInAnyOrderElementsOf(List.of(new CellLoc(1, 1)));


		graph.updateRequired(new CellLoc(1, 1), new ArrayList<>());
		assertEquals(0, graph.getRangesRequired(new CellLoc(1, 1)).size());
	}

	@Nested
	@DisplayName("Test update order")
	class UpdateOrderTests {
		@Test
		@DisplayName("Should return correct update order on a straight path")
		void testSimplePath() {
			List<CellLoc> path = List.of(
					new CellLoc(1, 1),
					new CellLoc(2, 2),
					new CellLoc(3, 3),
					new CellLoc(4, 4),
					new CellLoc(5, 5)
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
			List<CellLoc> cells = new ArrayList<>();
			cells.add(null); // cover the 0
			for (int i = 1; i <= 4; i++) {
				cells.add(new CellLoc(i, i));
			}

			graph.updateRequired(cells.get(2), List.of(cells.get(1), cells.get(4)));
			graph.updateRequired(cells.get(3), List.of(cells.get(1)));
			graph.updateRequired(cells.get(4), List.of(cells.get(3)));

			assertIterableEquals(List.of(cells.get(1), cells.get(3), cells.get(4), cells.get(2)), 
					     		 graph.getUpdateOrder(cells.get(1)));
		}

		@Test
		@DisplayName("Should return correct update order on range inputs")
		void testRangePath() {
			List<CellLoc> cells = new ArrayList<>();
			cells.add(null); // cover the 0
			for (int i = 1; i <= 4; i++) {
				cells.add(new CellLoc(i, i));
			}
			CellRange range = new CellRange(cells.get(2), cells.get(3));

			graph.updateRequired(cells.get(1), List.of(range));
			graph.updateRequired(cells.get(4), List.of(cells.get(1), cells.get(2)));

			assertIterableEquals(List.of(cells.get(2), cells.get(1), cells.get(4)), 
								 graph.getUpdateOrder(cells.get(2)));
		}

		@Test
		@DisplayName("Should throw CycleError")
		void testCycle() {
			graph.updateRequired(
					new CellLoc(1, 1), List.of(new CellLoc(2, 2), new CellLoc(3, 3)));
			graph.updateRequired(
					new CellLoc(3, 3), List.of(new CellRange(new CellLoc(1, 1), new CellLoc(2, 2))));

			assertThatThrownBy(() -> graph.getUpdateOrder(new CellLoc(1, 1)))
				.isInstanceOf(CellError.class);
		}
	}
}

