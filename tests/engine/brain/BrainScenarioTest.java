package engine.brain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.BasicStunt;
import engine.model.Game;
import engine.model.Model;
import game.model.Ghost;
import game.model.PacMan;

public class BrainScenarioTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void ghostCanReachPacManAndDamageHimWithTicks() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		PacMan pacman = new PacMan(model, "pacman");
		Ghost ghost = new Ghost(model, "ghost");

		pacman.setPosition(game.getGrid().new Position(5, 5));
		ghost.setPosition(game.getGrid().new Position(1, 5));

		model.add(pacman);
		model.add(ghost);

		BasicStunt ghostStunt = new BasicStunt(model, ghost);
		ghost.setStunt(ghostStunt);

		AttackBot attackBot = new AttackBot(brain, ghostStunt, pacman);
		brain.add(attackBot);

		int initialHealth = pacman.health();

		/*
		 * Le bot décide une première action.
		 */
		brain.think();

		/*
		 * On simule le temps avec tick, comme demandé dans les slides.
		 */
		for (int i = 0; i < 20 && pacman.health() == initialHealth; i++) {
			model.tick(1000L);

			/*
			 * Si ton Bot.done() ne relance pas automatiquement think(),
			 * on force une nouvelle décision après chaque tick.
			 */
			brain.think();
		}

		assertTrue(pacman.health() < initialHealth);
	}
}