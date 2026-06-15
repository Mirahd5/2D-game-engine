package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;

public class EntityCollisionTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void pacManIntersectsGumAtSamePositionWithPrints() {
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
	void pacManDoesNotIntersectGumWhenFarWithPrints() {
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
	void pacManIntersectsObstacleAtSamePositionWithPrints() {
		Game game = new TestGame();
		
		
		Model model = game.model();

		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Obstacle obstacle = new Obstacle(model, "wall");
		model.add(obstacle, model.grid().new Position(2, 2));

		pacman.setPosition(game.getGrid().new Position(2, 2));

		boolean pacmanToObstacle = pacman.intersects(obstacle);
		boolean obstacleToPacman = obstacle.intersects(pacman);

		System.out.println("\n=== PacMan-Obstacle collision ===");
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
	void pacManDoesNotIntersectObstacleWhenFarWithPrints() {
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
		System.out.println("Distance center to center = " + pacman.distanceCenterToCenter(obstacle));
		System.out.println("Expected collision = false");
		System.out.println("Actual PacMan -> Obstacle = " + pacmanToObstacle);
		System.out.println("Actual Obstacle -> PacMan = " + obstacleToPacman);

		assertFalse(pacmanToObstacle);
		assertFalse(obstacleToPacman);
	}

	@Test
	void ghostIntersectsPacManAtSamePositionWithPrints() {
		Game game = new TestGame();
		
		Model model = game.model();
		Ghost ghost = new Ghost(model, "ghost");
		model.add(ghost);
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);

		ghost.setPosition(game.getGrid().new Position(4, 4));
		pacman.setPosition(game.getGrid().new Position(4, 4));

		boolean ghostToPacman = ghost.intersects(pacman);
		boolean pacmanToGhost = pacman.intersects(ghost);

		System.out.println("\n=== Ghost-PacMan collision ===");
		System.out.println("Situation : Ghost and PacMan are in the same grid cell");
		System.out.println("Ghost position  = (" + ghost.position().x() + ", " + ghost.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Ghost center    = (" + ghost.center().x() + ", " + ghost.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Ghost bounding shapes count = " + ghost.bounding().shapes().size());
		System.out.println("Distance center to center = " + ghost.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual Ghost -> PacMan = " + ghostToPacman);
		System.out.println("Actual PacMan -> Ghost = " + pacmanToGhost);

		assertTrue(ghostToPacman);
		assertTrue(pacmanToGhost);
	}

	@Test
	void bossIntersectsPacManWithPrints() {
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

		System.out.println("\n=== Boss-PacMan collision ===");
		System.out.println("Situation : Boss and PacMan are in the same grid cell");
		System.out.println("Boss position   = (" + boss.position().x() + ", " + boss.position().y() + ")");
		System.out.println("PacMan position = (" + pacman.position().x() + ", " + pacman.position().y() + ")");
		System.out.println("Boss center     = (" + boss.center().x() + ", " + boss.center().y() + ")");
		System.out.println("PacMan center   = (" + pacman.center().x() + ", " + pacman.center().y() + ")");
		System.out.println("Boss occupied cells count = " + boss.occupied().size());
		System.out.println("Boss bounding shapes count = " + boss.bounding().shapes().size());
		System.out.println("Distance center to center = " + boss.distanceCenterToCenter(pacman));
		System.out.println("Expected collision = true");
		System.out.println("Actual Boss -> PacMan = " + bossToPacman);
		System.out.println("Actual PacMan -> Boss = " + pacmanToBoss);

		assertTrue(bossToPacman);
		assertTrue(pacmanToBoss);
	}

	@Test
	void pacManIntersectsGumAcrossTorusBorderWithPrints() {
		Game game = new TestGame();
		
		Model model = game.model();
		
		PacMan pacman = new PacMan(model, "pacman");
		model.add(pacman);
		Gum gum = new Gum(model, "gum");
		model.add(gum);

		/*
		 * Ici on utilise setCoord plutôt que setPosition pour placer les centres
		 * très proches du bord gauche/droite du tore.
		 *
		 * Monde largeur = 10 cellules * 3.7 = 37.0 cm
		 */
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
		System.out.println("Expected collision = true because they are close through the border");
		System.out.println("Actual PacMan -> Gum = " + pacmanToGum);
		System.out.println("Actual Gum -> PacMan = " + gumToPacman);

		assertTrue(pacmanToGum);
		assertTrue(gumToPacman);
	}
}
