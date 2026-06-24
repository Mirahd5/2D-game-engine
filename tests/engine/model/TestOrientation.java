package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("Orientation - directions cardinales")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestOrientation {

	@Test
	@Order(1)
	@DisplayName("01 - un angle quelconque est ramené à une direction cardinale")
	void normalizeCardinal() {
		System.out.println("Orientation/01 : angle vers cardinal");
		assertEquals(Orientation.EAST, Orientation.normalizeCardinal(10));
		assertEquals(Orientation.SOUTH, Orientation.normalizeCardinal(80));
		assertEquals(Orientation.WEST, Orientation.normalizeCardinal(200));
		assertEquals(Orientation.NORTH, Orientation.normalizeCardinal(-90));
	}

	@Test
	@Order(2)
	@DisplayName("02 - gauche, droite et arrière sont cohérents")
	void rotationsCardinales() {
		System.out.println("Orientation/02 : gauche/droite/arrière");
		assertEquals(Orientation.NORTH, Orientation.leftOf(Orientation.EAST));
		assertEquals(Orientation.SOUTH, Orientation.rightOf(Orientation.EAST));
		assertEquals(Orientation.WEST, Orientation.backOf(Orientation.EAST));
	}

	@Test
	@Order(3)
	@DisplayName("03 - neighbor respecte le tore et les bords fermés")
	void voisinSelonGeometrie() {
		System.out.println("Orientation/03 : voisin sur bord gauche");
		Grid grid = new JeuTest().getGrid();

		Grid.Position toric = Orientation.neighbor(grid, grid.new Position(0, 0), Orientation.WEST, true, false);
		Grid.Position blocked = Orientation.neighbor(grid, grid.new Position(0, 0), Orientation.NORTH, true, false);

		assertEquals(9, toric.x());
		assertEquals(0, toric.y());
		assertNull(blocked);
	}
}
