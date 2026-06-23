package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import engine.brain.Bot;
import engine.brain.Brain;

public class StuntBotNotificationTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	static class TestEntity extends Entity {
		TestEntity(Model model, String name) {
			super(model, name);
			setBounding();
		}

		@Override
		protected void setBounding() {
			this.bounding = new Bounding();
			this.bounding.add(new Circle(this.center, Math.min(this.size.x(), this.size.y()) / 2.0));
		}
	}

	static class SpyBot extends Bot {

		int doneCount;
		int collisionCount;

		SpyBot(Brain brain, Stunt stunt) {
			super(brain, stunt);
			this.doneCount = 0;
			this.collisionCount = 0;
		}

		@Override
		public void think() {
			stunt.walk(0);
		}

		@Override
		public void done() {
			this.doneCount++;
		}

		@Override
		public void collision(Entity e) {
			this.collisionCount++;
		}

		@Override
		public void collision(List<Entity> entities) {
			this.collisionCount++;
		}
	}

	@Test
	void botIsNotifiedWhenWalkIsDone() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		SpyBot bot = new SpyBot(brain, stunt);
		brain.add(bot);

		brain.think();

		assertEquals(0, bot.doneCount);

		model.tick(1000L);

		assertEquals(1, bot.doneCount);
		assertEquals(0, bot.collisionCount);
	}

	@Test
	void botIsNotifiedWhenWalkCollides() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(1, 0));

		model.add(e1);
		model.add(e2);

		BasicStunt stunt = new BasicStunt(model, e1);
		e1.setStunt(stunt);

		SpyBot bot = new SpyBot(brain, stunt);
		brain.add(bot);

		brain.think();

		model.tick(1000L);

		assertEquals(0, bot.doneCount);
		assertEquals(1, bot.collisionCount);
	}
}