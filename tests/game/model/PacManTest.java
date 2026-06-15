package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class PacManTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void pacManHasHalfCellSize() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		System.out.println("\n=== PacMan size ===");
		System.out.println("Situation : PacMan size should be half of one cell");
		System.out.println("cmPerCell = " + game.getCmPerCell());
		System.out.println("Expected size x = 3.7 / 2 = 1.85");
		System.out.println("Actual size x   = " + pacman.size().x());
		System.out.println("Expected size y = 3.7 / 2 = 1.85");
		System.out.println("Actual size y   = " + pacman.size().y());

		assertEquals(1.85, pacman.size().x(), EPS);
		assertEquals(1.85, pacman.size().y(), EPS);
	}

	@Test
	void pacManHasBounding() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		System.out.println("\n=== PacMan bounding ===");
		System.out.println("Situation : PacMan should have a Bounding with one Circle");
		System.out.println("Expected bounding = not null");
		System.out.println("Actual bounding   = " + pacman.bounding());
		System.out.println("Expected shapes list not empty = true");
		System.out.println("Actual shapes list size        = " + pacman.bounding().shapes().size());
		System.out.println("Expected number of shapes = 1");
		System.out.println("Actual number of shapes   = " + pacman.bounding().shapes().size());

		assertNotNull(pacman.bounding());
		assertFalse(pacman.bounding().shapes().isEmpty());
		assertEquals(1, pacman.bounding().shapes().size());
	}

	@Test
	void pacManBoundingFollowsPositionChange() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		double xminBefore = pacman.bounding().hitbox().xmin();
		double xmaxBefore = pacman.bounding().hitbox().xmax();

		System.out.println("\n=== PacMan bounding follows position change ===");
		System.out.println("Before movement:");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Hitbox xmin = " + xminBefore);
		System.out.println("Hitbox xmax = " + xmaxBefore);

		pacman.setPosition(game.getGrid().new Position(2, 0));

		double xminAfter = pacman.bounding().hitbox().xmin();
		double xmaxAfter = pacman.bounding().hitbox().xmax();

		boolean changed = Math.abs(xminAfter - xminBefore) > EPS || Math.abs(xmaxAfter - xmaxBefore) > EPS;

		System.out.println("After setPosition(2,0):");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Hitbox xmin = " + xminAfter);
		System.out.println("Hitbox xmax = " + xmaxAfter);
		System.out.println("Expected hitbox changed = true");
		System.out.println("Actual hitbox changed   = " + changed);

		assertTrue(changed);
		assertNotEquals(xminBefore, xminAfter, EPS);
	}

	@Test
	void pacManIntersectsGumAtSamePosition() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		pacman.setPosition(game.getGrid().new Position(2, 2));
		gum.setPosition(game.getGrid().new Position(2, 2));

		boolean pacmanToGum = pacman.intersects(gum);
		boolean gumToPacman = gum.intersects(pacman);

		System.out.println("\n=== PacMan-Gum collision at same position ===");
		System.out.println("Situation : PacMan and Gum are in the same grid cell");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Gum position    = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Gum center      = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("Distance center to center = " + pacman.distanceCenterToCenter(gum));
		System.out.println("Expected collision = true");
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);

		assertTrue(pacmanToGum);
		assertTrue(gumToPacman);
	}

	@Test
	void pacManDoesNotIntersectGumWhenFar() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		pacman.setPosition(game.getGrid().new Position(1, 1));
		gum.setPosition(game.getGrid().new Position(8, 8));

		boolean pacmanToGum = pacman.intersects(gum);
		boolean gumToPacman = gum.intersects(pacman);

		System.out.println("\n=== PacMan-Gum no collision when far ===");
		System.out.println("Situation : PacMan and Gum are far from each other");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Gum position    = (" + gum.position().x() + ", " + gum.position().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Gum center      = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("Distance center to center = " + pacman.distanceCenterToCenter(gum));
		System.out.println("Expected collision = false");
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);

		assertFalse(pacmanToGum);
		assertFalse(gumToPacman);
	}

	@Test
	void pacManIntersectsObstacleAtSamePosition() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 2));

		pacman.setPosition(game.getGrid().new Position(2, 2));

		boolean pacmanToObstacle = pacman.intersects(obstacle);
		boolean obstacleToPacman = obstacle.intersects(pacman);

		System.out.println("\n=== PacMan-Obstacle collision at same position ===");
		System.out.println("Situation : PacMan and Obstacle are in the same grid cell");
		System.out.println("PacMan position   = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Obstacle position = (" + obstacle.position().x() + ", " + obstacle.position().y() + ")");
		System.out.println("PacMan center     = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Obstacle center   = (" + obstacle.center().x() + ", " + obstacle.center().y() + ")");
		System.out.println("Distance center to center = " + pacman.distanceCenterToCenter(obstacle));
		System.out.println("Expected collision = true");
		System.out.println("Actual PacMan -> Obstacle = " + pacmanToObstacle);
		System.out.println("Actual Obstacle -> PacMan = " + obstacleToPacman);

		assertTrue(pacmanToObstacle);
		assertTrue(obstacleToPacman);
	}

	@Test
	void pacManDoesNotIntersectObstacleWhenFar() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(8, 8));

		pacman.setPosition(game.getGrid().new Position(1, 1));

		boolean pacmanToObstacle = pacman.intersects(obstacle);
		boolean obstacleToPacman = obstacle.intersects(pacman);

		System.out.println("\n=== PacMan-Obstacle no collision when far ===");
		System.out.println("Situation : PacMan and Obstacle are far from each other");
		System.out.println("PacMan position   = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Obstacle position = (" + obstacle.position().x() + ", " + obstacle.position().y() + ")");
		System.out.println("PacMan center     = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Obstacle center   = (" + obstacle.center().x() + ", " + obstacle.center().y() + ")");
		System.out.println("Distance center to center = " + pacman.distanceCenterToCenter(obstacle));
		System.out.println("Expected collision = false");
		System.out.println("Actual PacMan -> Obstacle = " + pacmanToObstacle);
		System.out.println("Actual Obstacle -> PacMan = " + obstacleToPacman);

		assertFalse(pacmanToObstacle);
		assertFalse(obstacleToPacman);
	}

	@Test
	void pacManOccupiesOneCell() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		System.out.println("\n=== PacMan occupied cells ===");
		System.out.println("Situation : PacMan occupies only its center cell");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Expected occupied size = 1");
		System.out.println("Actual occupied size   = " + pacman.occupied().size());

		assertEquals(1, pacman.occupied().size());
	}

	@Test
	void pacManIntersectsGumAcrossTorusBorder() {
		Game game = new TestGame();

		Model model = game.model();
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		pacman.setCoord(game.getIsu().new Coord(36.8, 18.5));
		gum.setCoord(game.getIsu().new Coord(0.1, 18.5));

		boolean pacmanToGum = pacman.intersects(gum);
		boolean gumToPacman = gum.intersects(pacman);

		System.out.println("\n=== PacMan-Gum collision across torus border ===");
		System.out.println("Situation : PacMan near right border, Gum near left border");
		System.out.println("World width = " + game.getIsu().width());
		System.out.println("PacMan center = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Gum center    = (" + gum.center().x() + ", " + gum.center().y() + ")");
		System.out.println("Direct dx = " + (gum.center().x() - pacman.center().x()));
		System.out.println("Torus distance center to center = " + pacman.distanceCenterToCenter(gum));
		System.out.println("Expected collision = true");
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);

		assertTrue(pacmanToGum);
		assertTrue(gumToPacman);
	}

	@Test
	void pacmanLosesHealthWhenCollidingWithGhost() {
		Game game = new TestGame();
		Model model = game.model();

		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Ghost ghost = new Ghost(model, "ghost");
		model.add(ghost);

		pacman.setPosition(game.getGrid().new Position(0, 0));
		ghost.setPosition(game.getGrid().new Position(1, 0));


		int before = pacman.health();

		pacman.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		boolean moved = model.move(pacman, 1000L);

		assertTrue(moved);
		assertEquals(before - ghost.damage(), pacman.health());

		assertEquals(1, pacman.position().x());
		assertEquals(0, pacman.position().y());
	}

	@Test
	void pacmanDiesWhenHealthReachesZero() {
		Game game = new TestGame();
		Model model = game.model();

		PacMan pacman = new PacMan(model, "pacman");

		model.add(pacman);

		pacman.takeDamage(pacman.health());
		model.tick(0L);

		assertEquals(0, pacman.health());
		assertFalse(pacman.alive());
		assertFalse(model.entities().contains(pacman));
	}

	@Test
	void pacmanEatsGumAndScoreIncreases() {
		Game game = new TestGame();
		Model model = game.model();

		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		pacman.setPosition(game.getGrid().new Position(0, 0));
		gum.setPosition(game.getGrid().new Position(1, 0));


		pacman.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		int before = pacman.score();

		boolean moved = model.move(pacman, 1000L);

		assertTrue(moved);
		assertEquals(before + gum.points(), pacman.score());
		assertFalse(gum.alive());
		model.tick(0L);
		assertFalse(model.entities().contains(gum));
	}
}
