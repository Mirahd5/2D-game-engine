package engine.view;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;

public class ViewTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, false, false, 3.7, 2);
		}
	}

	@Test
	void fullMapViewComputesScale() {
		Game game = new TestGame();
		Model model = game.model();

		View view = new View(model, 370, 370);

		assertEquals(10.0, view.scaleX(), EPS);
		assertEquals(10.0, view.scaleY(), EPS);
	}

	@Test
	void fullMapViewConvertsISUToPixels() {
		Game game = new TestGame();
		Model model = game.model();

		View view = new View(model, 370, 370);

		assertEquals(0, view.toPixelX(0.0));
		assertEquals(0, view.toPixelY(0.0));

		assertEquals(37, view.toPixelX(3.7));
		assertEquals(74, view.toPixelY(7.4));
	}

	@Test
	void viewPortTranslationIsAppliedBeforeScaling() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(3.7, 7.4), game.getIsu().new Dimension(18.5, 18.5));

		model.setViewPort(vp);

		View view = new View(model, 185, 185);

		/*
		 * Le point ISU égal à l'origine du ViewPort doit devenir le pixel (0,0).
		 */
		assertEquals(0, view.toPixelX(3.7));
		assertEquals(0, view.toPixelY(7.4));

		/*
		 * 3.7 cm après l'origine, avec scale = 10 px/cm, donne 37 pixels.
		 */
		assertEquals(37, view.toPixelX(7.4));
		assertEquals(37, view.toPixelY(11.1));
	}

	@Test
	void viewConvertsPixelsBackToISU() {
		Game game = new TestGame();
		Model model = game.model();

		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(3.7, 7.4), game.getIsu().new Dimension(18.5, 18.5));

		model.setViewPort(vp);

		View view = new View(model, 185, 185);

		ISU.Coord coord = view.toISUCoord(37, 37);

		assertEquals(7.4, coord.x(), EPS);
		assertEquals(11.1, coord.y(), EPS);
	}

	@Test
	void viewConvertsISUDimensionsToPixelDimensions() {
		Game game = new TestGame();
		Model model = game.model();

		View view = new View(model, 370, 370);

		assertEquals(37, view.toPixelWidth(3.7));
		assertEquals(74, view.toPixelHeight(7.4));
	}
}
