package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ViewPortTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, false, false, 3.7, 2);
		}
	}

	@Test
	void viewPortStoresOriginAndDimension() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(
			model,
			game.getIsu().new Coord(3.7, 7.4),
			game.getIsu().new Dimension(18.5, 18.5)
		);

		assertEquals(3.7, vp.x(), EPS);
		assertEquals(7.4, vp.y(), EPS);
		assertEquals(18.5, vp.width(), EPS);
		assertEquals(18.5, vp.height(), EPS);
	}

	@Test
	void modelHasDefaultViewPortCoveringFullMap() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = model.viewPort();

		assertNotNull(vp);

		assertEquals(0.0, vp.x(), EPS);
		assertEquals(0.0, vp.y(), EPS);
		assertEquals(game.getIsu().width(), vp.width(), EPS);
		assertEquals(game.getIsu().height(), vp.height(), EPS);
	}

	@Test
	void nonTorusViewPortStaysInsideMapOnX() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(
			model,
			game.getIsu().new Coord(30.0, 0.0),
			game.getIsu().new Dimension(18.5, 18.5)
		);

		assertTrue(vp.x() + vp.width() <= game.getIsu().width() + EPS);
	}

	@Test
	void nonTorusViewPortStaysInsideMapOnY() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(
			model,
			game.getIsu().new Coord(0.0, 30.0),
			game.getIsu().new Dimension(18.5, 18.5)
		);

		assertTrue(vp.y() + vp.height() <= game.getIsu().height() + EPS);
	}
	
	@Test
	void torusViewPortAllowsOriginNearMapBorder() {
		/*
		 * Situation :
		 * La map est un tore.
		 * L'origine du ViewPort peut être n'importe où.
		 *
		 * Résultat attendu :
		 * Une origine proche du bord est acceptée.
		 */
		Game game = new Game(10, 10, true, true, 3.7, 2) {};
		Model model = game.model();

		ViewPort vp = new ViewPort(
			model,
			game.getIsu().new Coord(35.0, 35.0),
			game.getIsu().new Dimension(18.5, 18.5)
		);

		assertEquals(35.0, vp.x(), EPS);
		assertEquals(35.0, vp.y(), EPS);
	}
	
	@Test
	void nonTorusViewPortOriginIsClampedInsideMap() {
		/*
		 * Situation :
		 * La map n'est pas un tore.
		 * Le ViewPort ne doit pas sortir de la map.
		 *
		 * Résultat attendu :
		 * Si on demande une origine trop loin, elle est ramenée à l'intérieur.
		 */
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(
			model,
			game.getIsu().new Coord(35.0, 35.0),
			game.getIsu().new Dimension(18.5, 18.5)
		);

		assertTrue(vp.x() + vp.width() <= game.getIsu().width() + EPS);
		assertTrue(vp.y() + vp.height() <= game.getIsu().height() + EPS);
	}
}