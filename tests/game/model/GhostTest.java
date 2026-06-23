package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class GhostTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void ghostHasHalfCellSize() {
		Game game = new TestGame();
		
		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");

		System.out.println("\n=== Ghost size ===");
		System.out.println("Situation : Ghost size should be half of one cell");
		System.out.println("cmPerCell = " + game.getCmPerCell());
		System.out.println("Expected size x = 3.7 / 2 = 1.85");
		System.out.println("Actual size x   = " + ghost.size().x());
		System.out.println("Expected size y = 3.7 / 2 = 1.85");
		System.out.println("Actual size y   = " + ghost.size().y());

		assertEquals(1.85, ghost.size().x(), EPS);
		assertEquals(1.85, ghost.size().y(), EPS);
	}

	@Test
	void ghostHasBounding() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");

		System.out.println("\n=== Ghost bounding ===");
		System.out.println("Situation : Ghost should have a non-null Bounding");
		System.out.println("Expected bounding = not null");
		System.out.println("Actual bounding   = " + ghost.bounding());
		System.out.println("Expected shapes list not empty = true");
		System.out.println("Actual shapes list size        = " + ghost.bounding().shapes().size());

		assertNotNull(ghost.bounding());
		assertFalse(ghost.bounding().shapes().isEmpty());
	}

	@Test
	void ghostBoundingContainsTwoShapes() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");

		System.out.println("\n=== Ghost bounding shapes count ===");
		System.out.println("Situation : Ghost is represented by 2 shapes");
		System.out.println("Expected shapes count = 2");
		System.out.println("Actual shapes count   = " + ghost.bounding().shapes().size());

		assertEquals(2, ghost.bounding().shapes().size());
	}

	@Test
	void ghostIntersectsPacManAtSamePosition() {
		Game game = new TestGame();

		
		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");
		PacMan pacman = new PacMan(model, "pacman");

		ghost.setPosition(game.getGrid().new Position(2, 3));
		pacman.setPosition(game.getGrid().new Position(2, 3));

		boolean ghostToPacman = ghost.intersects(pacman);
		boolean pacmanToGhost = pacman.intersects(ghost);

		System.out.println("\n=== Ghost-PacMan collision at same position ===");
		System.out.println("Situation : Ghost and PacMan are in the same grid cell");
		System.out.println("Ghost position  = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Ghost center    = (" + ghost.center().x() + ", " + ghost.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + ghost.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual Ghost -> PacMan = " + ghostToPacman);
		System.out.println("Actual PacMan -> Ghost = " + pacmanToGhost);

		assertTrue(ghostToPacman);
		assertTrue(pacmanToGhost);
	}

	@Test
	void ghostDoesNotIntersectPacManWhenFar() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");
		PacMan pacman = new PacMan(model, "pacman");

		ghost.setPosition(game.getGrid().new Position(2, 3));
		pacman.setPosition(game.getGrid().new Position(8, 8));

		boolean ghostToPacman = ghost.intersects(pacman);
		boolean pacmanToGhost = pacman.intersects(ghost);

		System.out.println("\n=== Ghost-PacMan no collision when far ===");
		System.out.println("Situation : Ghost and PacMan are far from each other");
		System.out.println("Ghost position  = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Ghost center    = (" + ghost.center().x() + ", " + ghost.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + ghost.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = false");
		System.out.println("Actual Ghost -> PacMan = " + ghostToPacman);
		System.out.println("Actual PacMan -> Ghost = " + pacmanToGhost);

		assertFalse(ghostToPacman);
		assertFalse(pacmanToGhost);
	}

	@Test
	void ghostBoundingFollowsMovement() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");
		PacMan pacman = new PacMan(model, "pacman");

		ghost.setPosition(game.getGrid().new Position(1, 1));
		pacman.setPosition(game.getGrid().new Position(8, 8));

		boolean beforeMove = ghost.intersects(pacman);

		System.out.println("\n=== Ghost bounding follows movement ===");
		System.out.println("Before moving PacMan:");
		System.out.println("Ghost position  = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Expected collision = false");
		System.out.println("Actual collision   = " + beforeMove);

		assertFalse(beforeMove);

		pacman.setPosition(game.getGrid().new Position(1, 1));

		boolean afterMove = ghost.intersects(pacman);

		System.out.println("After moving PacMan to Ghost position:");
		System.out.println("Ghost position  = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Expected collision = true");
		System.out.println("Actual collision   = " + afterMove);

		assertTrue(afterMove);
	}

	@Test
	void ghostBoundingChangesAfterRotation() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");

		double xminBefore = ghost.bounding().hitbox().xmin();
		double xmaxBefore = ghost.bounding().hitbox().xmax();
		double yminBefore = ghost.bounding().hitbox().ymin();
		double ymaxBefore = ghost.bounding().hitbox().ymax();

		System.out.println("\n=== Ghost bounding changes after rotation ===");
		System.out.println("Before rotation:");
		System.out.println("Orientation = " + ghost.orientation());
		System.out.println("Hitbox xmin = " + xminBefore);
		System.out.println("Hitbox xmax = " + xmaxBefore);
		System.out.println("Hitbox ymin = " + yminBefore);
		System.out.println("Hitbox ymax = " + ymaxBefore);

		ghost.turn(90);

		double xminAfter = ghost.bounding().hitbox().xmin();
		double xmaxAfter = ghost.bounding().hitbox().xmax();
		double yminAfter = ghost.bounding().hitbox().ymin();
		double ymaxAfter = ghost.bounding().hitbox().ymax();

		boolean changed =
			Math.abs(xminBefore - xminAfter) > EPS ||
			Math.abs(xmaxBefore - xmaxAfter) > EPS ||
			Math.abs(yminBefore - yminAfter) > EPS ||
			Math.abs(ymaxBefore - ymaxAfter) > EPS;

		System.out.println("After rotation:");
		System.out.println("Orientation = " + ghost.orientation());
		System.out.println("Hitbox xmin = " + xminAfter);
		System.out.println("Hitbox xmax = " + xmaxAfter);
		System.out.println("Hitbox ymin = " + yminAfter);
		System.out.println("Hitbox ymax = " + ymaxAfter);
		System.out.println("Expected hitbox changed = true");
		System.out.println("Actual hitbox changed   = " + changed);

		assertTrue(changed);
	}

	@Test
	void ghostBoundingHitboxFollowsMovement() {
		Game game = new TestGame();

		Model model = game.model();

		Ghost ghost = new Ghost(model, "ghost");
		double xminBefore = ghost.bounding().hitbox().xmin();
		double xmaxBefore = ghost.bounding().hitbox().xmax();

		System.out.println("\n=== Ghost hitbox follows movement ===");
		System.out.println("Before movement:");
		System.out.println("Ghost position = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("Hitbox xmin = " + xminBefore);
		System.out.println("Hitbox xmax = " + xmaxBefore);

		ghost.moveEast(game.getCmPerCell());

		double xminAfter = ghost.bounding().hitbox().xmin();
		double xmaxAfter = ghost.bounding().hitbox().xmax();

		boolean changed = Math.abs(xminAfter - xminBefore) > EPS || Math.abs(xmaxAfter - xmaxBefore) > EPS;

		System.out.println("After moveEast(cmPerCell):");
		System.out.println("Ghost position = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("Hitbox xmin = " + xminAfter);
		System.out.println("Hitbox xmax = " + xmaxAfter);
		System.out.println("Expected hitbox changed = true");
		System.out.println("Actual hitbox changed   = " + changed);

		assertTrue(changed);
	}
}