package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class GumTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void gumHasQuarterCellSize() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		System.out.println("\n=== Gum size ===");
		System.out.println("Situation : Gum size should be one quarter of one cell");
		System.out.println("cmPerCell = " + game.getCmPerCell());
		System.out.println("Expected size x = 3.7 / 4 = 0.925");
		System.out.println("Actual size x   = " + gum.size().x());
		System.out.println("Expected size y = 3.7 / 4 = 0.925");
		System.out.println("Actual size y   = " + gum.size().y());

		assertEquals(0.925, gum.size().x(), EPS);
		assertEquals(0.925, gum.size().y(), EPS);
	}

	@Test
	void gumHasBounding() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		System.out.println("\n=== Gum bounding ===");
		System.out.println("Situation : Gum should have a Bounding with one Circle");
		System.out.println("Expected bounding = not null");
		System.out.println("Actual bounding   = " + gum.bounding());
		System.out.println("Expected shapes list not empty = true");
		System.out.println("Actual shapes list size        = " + gum.bounding().shapes().size());
		System.out.println("Expected number of shapes      = 1");
		System.out.println("Actual number of shapes        = " + gum.bounding().shapes().size());

		assertNotNull(gum.bounding());
		assertFalse(gum.bounding().shapes().isEmpty());
		assertEquals(1, gum.bounding().shapes().size());
	}

	@Test
	void gumBoundingFollowsPositionChange() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		double xminBefore = gum.bounding().hitbox().xmin();
		double xmaxBefore = gum.bounding().hitbox().xmax();

		System.out.println("\n=== Gum bounding follows position change ===");
		System.out.println("Before movement:");
		System.out.println("Gum position = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("Gum center   = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("Hitbox xmin  = " + xminBefore);
		System.out.println("Hitbox xmax  = " + xmaxBefore);

		gum.setPosition(game.getGrid().new Position(3, 0));

		double xminAfter = gum.bounding().hitbox().xmin();
		double xmaxAfter = gum.bounding().hitbox().xmax();

		System.out.println("After setPosition(3,0):");
		System.out.println("Gum position = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("Gum center   = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("Hitbox xmin  = " + xminAfter);
		System.out.println("Hitbox xmax  = " + xmaxAfter);
		System.out.println("Expected hitbox changed = true");
		System.out.println("Actual hitbox changed   = " + (Math.abs(xminAfter - xminBefore) > EPS));

		assertNotEquals(xminBefore, xminAfter, EPS);
		assertNotEquals(xmaxBefore, xmaxAfter, EPS);
	}

	@Test
	void gumIntersectsPacManAtSamePosition() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		gum.setPosition(game.getGrid().new Position(2, 2));
		pacman.setPosition(game.getGrid().new Position(2, 2));

		boolean gumToPacman = gum.intersects(pacman);
		boolean pacmanToGum = pacman.intersects(gum);

		System.out.println("\n=== Gum-PacMan collision at same position ===");
		System.out.println("Situation : Gum and PacMan are in the same grid cell");
		System.out.println("Gum position    = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Gum center      = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + gum.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);

		assertTrue(gumToPacman);
		assertTrue(pacmanToGum);
	}

	@Test
	void gumDoesNotIntersectPacManWhenFar() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		gum.setPosition(game.getGrid().new Position(1, 1));
		pacman.setPosition(game.getGrid().new Position(8, 8));

		boolean gumToPacman = gum.intersects(pacman);
		boolean pacmanToGum = pacman.intersects(gum);

		System.out.println("\n=== Gum-PacMan no collision when far ===");
		System.out.println("Situation : Gum and PacMan are far from each other");
		System.out.println("Gum position    = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Gum center      = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + gum.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = false");
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);

		assertFalse(gumToPacman);
		assertFalse(pacmanToGum);
	}

	@Test
	void gumOccupiesOneCell() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		System.out.println("\n=== Gum occupied cells ===");
		System.out.println("Situation : Gum is a small entity, so it occupies only its center cell");
		System.out.println("Gum position = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("Expected occupied size = 1");
		System.out.println("Actual occupied size   = " + gum.occupied().size());

		assertEquals(1, gum.occupied().size());
	}

	@Test
	void gumMovesBetweenCells() {
		Game game = new TestGame();

		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);
		
		boolean oldCellBefore = game.getGrid().cellAt(game.getGrid().new Position(0, 0)).contains(gum);

		System.out.println("\n=== Gum moves between cells ===");
		System.out.println("Initial position = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("Old cell (0,0) contains Gum before move = " + oldCellBefore);
		System.out.println("Occupied size before move = " + gum.occupied().size());

		assertTrue(oldCellBefore);

		gum.setPosition(game.getGrid().new Position(4, 5));

		boolean oldCellAfter = game.getGrid().cellAt(game.getGrid().new Position(0, 0)).contains(gum);
		boolean newCellAfter = game.getGrid().cellAt(game.getGrid().new Position(4, 5)).contains(gum);

		System.out.println("After setPosition(4,5):");
		System.out.println("Gum position = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("Expected old cell (0,0) contains Gum = false");
		System.out.println("Actual old cell (0,0) contains Gum   = " + oldCellAfter);
		System.out.println("Expected new cell (4,5) contains Gum = true");
		System.out.println("Actual new cell (4,5) contains Gum   = " + newCellAfter);
		System.out.println("Expected occupied size = 1");
		System.out.println("Actual occupied size   = " + gum.occupied().size());

		assertFalse(oldCellAfter);
		assertTrue(newCellAfter);
		assertEquals(1, gum.occupied().size());
	}

	@Test
	void gumIntersectsPacManAcrossTorusBorder() {
		Game game = new TestGame();

		
		Model model = game.model();
		Gum gum = new Gum(model, "gum");
		model.add(gum);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		

		/*
		 * On utilise setCoord pour placer précisément les centres près du bord.
		 * World width = 10 * 3.7 = 37.0 cm.
		 */
		gum.setCoord(game.getIsu().new Coord(0.1, 18.5));
		pacman.setCoord(game.getIsu().new Coord(36.8, 18.5));

		boolean gumToPacman = gum.intersects(pacman);
		boolean pacmanToGum = pacman.intersects(gum);

		System.out.println("\n=== Gum-PacMan collision across torus border ===");
		System.out.println("Situation : Gum near left border, PacMan near right border");
		System.out.println("World width = " + game.getIsu().width());
		System.out.println("Gum center    = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("PacMan center = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Direct dx = " + (pacman.center().x() - gum.center().x()));
		System.out.println("Torus distance = " + gum.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);

		assertTrue(gumToPacman);
		assertTrue(pacmanToGum);
	}
}
