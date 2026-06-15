package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GameTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 8, true, true, 3.7, 2);
		}

		TestGame(double w_cm, double h_cm) {
			super(w_cm, h_cm, true, true, 3.7, 2);
		}
	}

	@Test
	void gameDimensionsInCellsAreCorrect() {
		Game game = new TestGame();

		System.out.println("\n=== Game dimensions in cells ===");
		System.out.println("Situation : Game created with 10 cells width and 8 cells height");
		System.out.println("Expected width cells  = 10");
		System.out.println("Actual width cells    = " + game.getWidthCell());
		System.out.println("Expected height cells = 8");
		System.out.println("Actual height cells   = " + game.getHeightCell());

		assertEquals(10, game.getWidthCell());
		assertEquals(8, game.getHeightCell());
	}

	@Test
	void gameDimensionsInCmAreCorrect() {
		Game game = new TestGame();

		System.out.println("\n=== Game dimensions in centimeters ===");
		System.out.println("Situation : width = 10 cells, height = 8 cells, cmPerCell = 3.7");
		System.out.println("Expected width cm  = 10 * 3.7 = 37.0");
		System.out.println("Actual width cm    = " + game.getWidthCm());
		System.out.println("Expected height cm = 8 * 3.7 = 29.6");
		System.out.println("Actual height cm   = " + game.getHeightCm());

		assertEquals(37.0, game.getWidthCm(), EPS);
		assertEquals(29.6, game.getHeightCm(), EPS);
	}

	@Test
	void gamePixelDimensionsAreCorrect() {
		Game game = new TestGame();

		System.out.println("\n=== Game dimensions in pixels ===");
		System.out.println("Situation : width_cm = 37.0, height_cm = 29.6, pixelPerCm = 2");
		System.out.println("Expected width pixel  = 37.0 * 2 = 74.0");
		System.out.println("Actual width pixel    = " + game.getWidthPixel());
		System.out.println("Expected height pixel = 29.6 * 2 = 59.2");
		System.out.println("Actual height pixel   = " + game.getHeightPixel());

		assertEquals(74.0, game.getWidthPixel(), EPS);
		assertEquals(59.2, game.getHeightPixel(), EPS);
	}

	@Test
	void gameCreatesGridAndISU() {
		Game game = new TestGame();

		System.out.println("\n=== Game creates Grid and ISU ===");
		System.out.println("Situation : Game constructor should create Grid and ISU");
		System.out.println("Expected Grid = not null");
		System.out.println("Actual Grid   = " + game.getGrid());
		System.out.println("Expected ISU  = not null");
		System.out.println("Actual ISU    = " + game.getIsu());

		assertNotNull(game.getGrid());
		assertNotNull(game.getIsu());
	}

	@Test
	void gameTorusConfigurationIsCorrect() {
		Game game = new TestGame();

		System.out.println("\n=== Game torus configuration ===");
		System.out.println("Situation : Game created with torusOnXaxis = true and torusOnYaxis = true");
		System.out.println("Expected torus X = true");
		System.out.println("Actual torus X   = " + game.getTorusOnXaxis());
		System.out.println("Expected torus Y = true");
		System.out.println("Actual torus Y   = " + game.getTorusOnYaxis());

		assertTrue(game.getTorusOnXaxis());
		assertTrue(game.getTorusOnYaxis());
	}

	@Test
	void gameFromCmConstructorWorks() {
		Game game = new TestGame(37.0, 29.6);

		System.out.println("\n=== Game constructor from centimeters ===");
		System.out.println("Situation : Game created with width_cm = 37.0, height_cm = 29.6, cmPerCell = 3.7");
		System.out.println("Expected width cells  = 37.0 / 3.7 = 10");
		System.out.println("Actual width cells    = " + game.getWidthCell());
		System.out.println("Expected height cells = 29.6 / 3.7 = 8");
		System.out.println("Actual height cells   = " + game.getHeightCell());
		System.out.println("Expected width cm     = 37.0");
		System.out.println("Actual width cm       = " + game.getWidthCm());
		System.out.println("Expected height cm    = 29.6");
		System.out.println("Actual height cm      = " + game.getHeightCm());

		assertEquals(10, game.getWidthCell());
		assertEquals(8, game.getHeightCell());
		assertEquals(37.0, game.getWidthCm(), EPS);
		assertEquals(29.6, game.getHeightCm(), EPS);
	}

	@Test
	void gameMethodReturnsItself() {
		Game game = new TestGame();

		System.out.println("\n=== game() method ===");
		System.out.println("Situation : game.game() should return the same object");
		System.out.println("Expected object = game");
		System.out.println("Actual object   = " + game.game());

		assertSame(game, game.game());
	}

	@Test
	void gameShowDoesNotCrash() {
		Game game = new TestGame();

		System.out.println("\n=== Game show() ===");
		System.out.println("Situation : show(System.out) should print game information without crashing");
		System.out.println("Expected = no exception");
		System.out.println("Actual output:");

		assertDoesNotThrow(() -> game.show(System.out));
	}

	@Test
	void gameGettersAreCorrect() {
		Game game = new TestGame();

		System.out.println("\n=== Game getters ===");
		System.out.println("Situation : checking general getters");
		System.out.println("Expected cmPerCell  = 3.7");
		System.out.println("Actual cmPerCell    = " + game.getCmPerCell());
		System.out.println("Expected pixelPerCm = 2");
		System.out.println("Actual pixelPerCm   = " + game.getPixelPerCm());
		System.out.println("Expected Picture    = not null");
		System.out.println("Actual Picture      = " + game.getPicture());
		System.out.println("Expected Grid       = not null");
		System.out.println("Actual Grid         = " + game.getGrid());
		System.out.println("Expected ISU        = not null");
		System.out.println("Actual ISU          = " + game.getIsu());

		assertEquals(3.7, game.getCmPerCell(), EPS);
		assertEquals(2, game.getPixelPerCm());
		assertNotNull(game.getPicture());
		assertNotNull(game.getGrid());
		assertNotNull(game.getIsu());
	}

	@Test
	void nonTorusGameConfigurationWorks() {
		Game game = new Game(10, 8, false, false, 3.7, 2) {
		};

		System.out.println("\n=== Non-torus Game configuration ===");
		System.out.println("Situation : Game created with torusOnXaxis = false and torusOnYaxis = false");
		System.out.println("Expected getTorusOnXaxis() = false");
		System.out.println("Actual getTorusOnXaxis()   = " + game.getTorusOnXaxis());
		System.out.println("Expected getTorusOnYaxis() = false");
		System.out.println("Actual getTorusOnYaxis()   = " + game.getTorusOnYaxis());
		System.out.println("Expected isTorusOnXaxis()  = false");
		System.out.println("Actual isTorusOnXaxis()    = " + game.isTorusOnXaxis());
		System.out.println("Expected isTorusOnYaxis()  = false");
		System.out.println("Actual isTorusOnYaxis()    = " + game.isTorusOnYaxis());

		assertFalse(game.getTorusOnXaxis());
		assertFalse(game.getTorusOnYaxis());
		assertFalse(game.isTorusOnXaxis());
		assertFalse(game.isTorusOnYaxis());
	}
	
	@Test
	void gameFromCmConstructorWithDifferentSizeWorks() {
		Game game = new Game(14.8, 11.1, true, false, 3.7, 3) {
		};

		System.out.println("\n=== Game constructor from centimeters with different size ===");
		System.out.println("Situation : width_cm = 14.8, height_cm = 11.1, cmPerCell = 3.7");
		System.out.println("Expected width cells  = 4");
		System.out.println("Actual width cells    = " + game.getWidthCell());
		System.out.println("Expected height cells = 3");
		System.out.println("Actual height cells   = " + game.getHeightCell());
		System.out.println("Expected width pixel  = 14.8 * 3 = 44.4");
		System.out.println("Actual width pixel    = " + game.getWidthPixel());
		System.out.println("Expected height pixel = 11.1 * 3 = 33.3");
		System.out.println("Actual height pixel   = " + game.getHeightPixel());
		System.out.println("Expected torus X      = true");
		System.out.println("Actual torus X        = " + game.getTorusOnXaxis());
		System.out.println("Expected torus Y      = false");
		System.out.println("Actual torus Y        = " + game.getTorusOnYaxis());

		assertEquals(4, game.getWidthCell());
		assertEquals(3, game.getHeightCell());
		assertEquals(14.8, game.getWidthCm(), EPS);
		assertEquals(11.1, game.getHeightCm(), EPS);
		assertEquals(44.4, game.getWidthPixel(), EPS);
		assertEquals(33.3, game.getHeightPixel(), EPS);
		assertTrue(game.getTorusOnXaxis());
		assertFalse(game.getTorusOnYaxis());
	}
	
	@Test
	void gameMixedTorusConfigurationWorks() {
		Game game = new Game(10, 8, true, false, 3.7, 2) {
		};

		System.out.println("\n=== Mixed torus Game configuration ===");
		System.out.println("Situation : torus enabled on X, disabled on Y");
		System.out.println("Expected torus X = true");
		System.out.println("Actual torus X   = " + game.getTorusOnXaxis());
		System.out.println("Expected torus Y = false");
		System.out.println("Actual torus Y   = " + game.getTorusOnYaxis());

		assertTrue(game.getTorusOnXaxis());
		assertTrue(game.isTorusOnXaxis());
		assertFalse(game.getTorusOnYaxis());
		assertFalse(game.isTorusOnYaxis());
	}
	
}
