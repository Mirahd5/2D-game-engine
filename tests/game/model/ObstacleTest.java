package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class ObstacleTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void obstacleHasCorrectPosition() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));

		assertEquals(2, obstacle.position().x());
		assertEquals(3, obstacle.position().y());

		assertEquals((2 + 0.5) * game.getCmPerCell(), obstacle.center().x(), EPS);
		assertEquals((3 + 0.5) * game.getCmPerCell(), obstacle.center().y(), EPS);
	}

	@Test
	void obstacleHasOneCellSize() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));

		assertEquals(game.getCmPerCell(), obstacle.size().x(), EPS);
		assertEquals(game.getCmPerCell(), obstacle.size().y(), EPS);
	}

	@Test
	void obstacleHasBounding() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));

		
		assertNotNull(obstacle.bounding());
		assertFalse(obstacle.bounding().shapes().isEmpty());
	}

	@Test
	void obstacleIntersectsPacManAtSamePosition() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		pacman.setPosition(game.getGrid().new Position(2, 3));

		assertEquals(obstacle.center().x(), pacman.center().x(), EPS);
		assertEquals(obstacle.center().y(), pacman.center().y(), EPS);

		assertTrue(obstacle.intersects(pacman));
		assertTrue(pacman.intersects(obstacle));
	}

	@Test
	void obstacleDoesNotIntersectPacManWhenFar() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		pacman.setPosition(game.getGrid().new Position(8, 8));

		assertFalse(obstacle.intersects(pacman));
		assertFalse(pacman.intersects(obstacle));
	}

	@Test
	void obstacleOccupiesOneCell() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));

		assertEquals(1, obstacle.occupied().size());
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(2, 3)).contains(obstacle));
	}

	@Test
	void obstacleBoundingFollowsPositionChange() {
		Game game = new TestGame();

		Model model = game.model();
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 3));
		
		double xminBefore = obstacle.bounding().hitbox().xmin();
		double xmaxBefore = obstacle.bounding().hitbox().xmax();
		double yminBefore = obstacle.bounding().hitbox().ymin();
		double ymaxBefore = obstacle.bounding().hitbox().ymax();

		obstacle.setPosition(game.getGrid().new Position(5, 5));

		double xminAfter = obstacle.bounding().hitbox().xmin();
		double xmaxAfter = obstacle.bounding().hitbox().xmax();
		double yminAfter = obstacle.bounding().hitbox().ymin();
		double ymaxAfter = obstacle.bounding().hitbox().ymax();

		boolean changed =
			Math.abs(xminAfter - xminBefore) > EPS ||
			Math.abs(xmaxAfter - xmaxBefore) > EPS ||
			Math.abs(yminAfter - yminBefore) > EPS ||
			Math.abs(ymaxAfter - ymaxBefore) > EPS;

		assertTrue(changed);

		assertEquals(5, obstacle.position().x());
		assertEquals(5, obstacle.position().y());

		assertEquals((5 + 0.5) * game.getCmPerCell(), obstacle.center().x(), EPS);
		assertEquals((5 + 0.5) * game.getCmPerCell(), obstacle.center().y(), EPS);
	}
}
