package engine.brain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.BasicStunt;
import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Model;

public class BrainTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	static class TestEntity extends Entity {
		TestEntity(Model model, String name) {
			super(model, name);
			updateBounding();
		}

		@Override
		protected Bounding createBounding(
			engine.model.ISU.Coord center,
			engine.model.ISU.Dimension size,
			int orientationDegree
		) {
			Bounding result = new Bounding();
			result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
			return result;
		}

		@Override
		public boolean blocks(Entity other) {
			return true;
		}
	}

	@Test
	void brainCanAddBot() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity attacker = new TestEntity(model, "attacker");
		model.add(attacker);
		Entity target = new TestEntity(model, "target");
		model.add(target);

		BasicStunt stunt = new BasicStunt(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);

		brain.add(bot);

		assertEquals(1, brain.bots().size());
		assertTrue(brain.bots().contains(bot));
		assertSame(brain, bot.brain());
		assertSame(stunt, bot.stunt());
	}

	@Test
	void brainThinkCallsBots() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity attacker = new TestEntity(model, "attacker");
		model.add(attacker);
		Entity target = new TestEntity(model, "target");
		model.add(target);

		attacker.setPosition(game.getGrid().new Position(0, 0));
		target.setPosition(game.getGrid().new Position(3, 0));


		BasicStunt stunt = new BasicStunt(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);

		brain.add(bot);

		brain.think();

		// Le brain a appelé le bot, le bot a demandé un walk,
		// donc orientation + vitesse sont réglées, mais pas encore déplacement.
		assertEquals(0, attacker.position().x());
		assertEquals(0, attacker.position().y());
		assertEquals(0, attacker.orientation());

		model.tick(200L);

		assertEquals(1, attacker.position().x());
		assertEquals(0, attacker.position().y());
		assertEquals(0, attacker.orientation());
	}
}
