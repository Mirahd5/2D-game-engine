package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class BossTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void bossHasBounding() {
		Game game = new TestGame();
		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);

		System.out.println("\n=== Boss bounding ===");
		System.out.println("Situation : create a Boss");
		System.out.println("Expected bounding = not null");
		System.out.println("Actual bounding   = " + boss.bounding());
		System.out.println("Expected number of shapes = 2");
		System.out.println("Actual number of shapes   = " + boss.bounding().shapes().size());

		assertNotNull(boss.bounding());
		assertFalse(boss.bounding().shapes().isEmpty());
		assertEquals(2, boss.bounding().shapes().size());
	}

	@Test
	void bossOccupiesSixCellsAfterConstruction() {
		Game game = new TestGame();
		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);

		System.out.println("\n=== Boss occupied cells after construction ===");
		System.out.println("Situation : Boss is created at initial position");
		System.out.println("Boss position = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("Boss orientation = " + boss.orientation());
		System.out.println("Expected occupied cells count = 6");
		System.out.println("Actual occupied cells count   = " + boss.occupied().size());

		assertEquals(6, boss.occupied().size());
	}

	@Test
	void bossOccupiesSixCellsAfterMove() {
		Game game = new TestGame();

		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);
		boss.setPosition(game.getGrid().new Position(4, 4));

		System.out.println("\n=== Boss occupied cells after move ===");
		System.out.println("Situation : Boss moved to position (4,4), orientation = 0");
		System.out.println("Expected occupied cells:");
		System.out.println("(4,4), (4,3), (4,5), (4,6), (5,4), (6,4)");
		System.out.println("Actual occupied cells count = " + boss.occupied().size());

		System.out.println("Cell (4,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss));
		System.out.println("Cell (4,3) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 3)).contains(boss));
		System.out.println("Cell (4,5) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 5)).contains(boss));
		System.out.println("Cell (4,6) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 6)).contains(boss));
		System.out.println("Cell (5,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(5, 4)).contains(boss));
		System.out.println("Cell (6,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(6, 4)).contains(boss));

		assertEquals(6, boss.occupied().size());

		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss));
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 3)).contains(boss));
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 5)).contains(boss));
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 6)).contains(boss));
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(5, 4)).contains(boss));
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(6, 4)).contains(boss));
	}

	@Test
	void bossRetractsFromOldCellsAfterMove() {
		Game game = new TestGame();

		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);
		
		boss.setPosition(game.getGrid().new Position(4, 4));

		boolean oldCellBeforeMove = game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss);

		boss.setPosition(game.getGrid().new Position(1, 1));

		boolean oldCellAfterMove = game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss);
		boolean newCellAfterMove = game.getGrid().cellAt(game.getGrid().new Position(1, 1)).contains(boss);

		System.out.println("\n=== Boss retracts from old cells after move ===");
		System.out.println("Situation : Boss moves from (4,4) to (1,1)");
		System.out.println("Before move, old cell (4,4) contains boss = " + oldCellBeforeMove);
		System.out.println("Expected after move: old cell (4,4) contains boss = false");
		System.out.println("Actual old cell (4,4) contains boss          = " + oldCellAfterMove);
		System.out.println("Expected after move: new cell (1,1) contains boss = true");
		System.out.println("Actual new cell (1,1) contains boss              = " + newCellAfterMove);
		System.out.println("Occupied cells count after move = " + boss.occupied().size());

		assertTrue(oldCellBeforeMove);
		assertFalse(oldCellAfterMove);
		assertTrue(newCellAfterMove);
		assertEquals(6, boss.occupied().size());
	}

	@Test
	void bossIntersectsPacManWhenClose() {
		Game game = new TestGame();
		
		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		boss.setPosition(game.getGrid().new Position(4, 4));
		pacman.setPosition(game.getGrid().new Position(4, 4));

		boolean bossToPacman = boss.intersects(pacman);
		boolean pacmanToBoss = pacman.intersects(boss);

		System.out.println("\n=== Boss-PacMan collision when close ===");
		System.out.println("Boss position = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Boss center = (" + boss.center().x() + ", " + boss.center().y() + ")");
		System.out.println("PacMan center = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + boss.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual boss -> pacman = " + bossToPacman);
		System.out.println("Actual pacman -> boss = " + pacmanToBoss);

		assertTrue(bossToPacman);
		assertTrue(pacmanToBoss);
	}

	@Test
	void bossDoesNotIntersectPacManWhenFar() {
		Game game = new TestGame();

		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		boss.setPosition(game.getGrid().new Position(1, 1));
		pacman.setPosition(game.getGrid().new Position(8, 8));

		boolean bossToPacman = boss.intersects(pacman);
		boolean pacmanToBoss = pacman.intersects(boss);

		System.out.println("\n=== Boss-PacMan no collision when far ===");
		System.out.println("Boss position = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Boss center = (" + boss.center().x() + ", " + boss.center().y() + ")");
		System.out.println("PacMan center = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Distance center to center = " + boss.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = false");
		System.out.println("Actual boss -> pacman = " + bossToPacman);
		System.out.println("Actual pacman -> boss = " + pacmanToBoss);

		assertFalse(bossToPacman);
		assertFalse(pacmanToBoss);
	}

	@Test
	void bossDeployRotatesOccupiedCellsAt90Degrees() {
		Game game = new TestGame();
		
		Model model = game.model();

		Boss boss = new Boss(model, "boss");
		model.add(boss);

		boss.setPosition(game.getGrid().new Position(4, 4));

		System.out.println("\n=== Boss deploy before rotation ===");
		System.out.println("Boss position = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("Boss orientation = " + boss.orientation());
		System.out.println("Expected occupied cells at 0 degrees:");
		System.out.println("(4,4), (4,3), (4,5), (4,6), (5,4), (6,4)");
		System.out.println("Actual occupied size = " + boss.occupied().size());

		boss.turn(90);

		System.out.println("\n=== Boss deploy after rotation 90 degrees ===");
		System.out.println("Boss position = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("Boss orientation = " + boss.orientation());
		System.out.println("Expected occupied cells after rotation:");
		System.out.println("(4,4), (5,4), (3,4), (2,4), (4,5), (4,6)");
		System.out.println("Actual occupied size = " + boss.occupied().size());

		System.out.println("Cell (4,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss));
		System.out.println("Cell (5,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(5, 4)).contains(boss));
		System.out.println("Cell (3,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(3, 4)).contains(boss));
		System.out.println("Cell (2,4) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(2, 4)).contains(boss));
		System.out.println("Cell (4,5) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 5)).contains(boss));
		System.out.println("Cell (4,6) contains boss = "
				+ game.getGrid().cellAt(game.getGrid().new Position(4, 6)).contains(boss));

		assertEquals(6, boss.occupied().size());

		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 4)).contains(boss));

		// Offset (0,-1) tourné à 90° -> (1,0)
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(5, 4)).contains(boss));

		// Offset (0,1) tourné à 90° -> (-1,0)
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(3, 4)).contains(boss));

		// Offset (0,2) tourné à 90° -> (-2,0)
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(2, 4)).contains(boss));

		// Offset (1,0) tourné à 90° -> (0,1)
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 5)).contains(boss));

		// Offset (2,0) tourné à 90° -> (0,2)
		assertTrue(game.getGrid().cellAt(game.getGrid().new Position(4, 6)).contains(boss));
	}
}
