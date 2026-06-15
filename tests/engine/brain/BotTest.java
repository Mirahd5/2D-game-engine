package engine.brain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.BasicStunt;
import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Model;

public class BotTest {

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
	void attackBotMovesTowardTargetOnX() {
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

		// brain.think() règle seulement la vitesse, ne déplace pas encore.
		assertEquals(0, attacker.position().x());
		assertEquals(0, attacker.position().y());
		assertEquals(0, attacker.orientation());

		model.tick(200L);

		assertEquals(1, attacker.position().x());
		assertEquals(0, attacker.position().y());
		assertEquals(0, attacker.orientation());
	}

	@Test
	void attackBotMovesTowardTargetOnY() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity attacker = new TestEntity(model, "attacker");
		model.add(attacker);
		Entity target = new TestEntity(model, "target");
		model.add(target);

		attacker.setPosition(game.getGrid().new Position(0, 0));
		target.setPosition(game.getGrid().new Position(0, 3));


		BasicStunt stunt = new BasicStunt(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);
		brain.add(bot);

		brain.think();

		assertEquals(0, attacker.position().x());
		assertEquals(0, attacker.position().y());
		assertEquals(90, attacker.orientation());

		model.tick(200L);

		assertEquals(0, attacker.position().x());
		assertEquals(1, attacker.position().y());
		assertEquals(90, attacker.orientation());
	}

	@Test
	void fleeBotMovesAwayFromEnemyOnX() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity player = new TestEntity(model, "player");
		model.add(player);
		Entity enemy = new TestEntity(model, "enemy");
		model.add(enemy);

		player.setPosition(game.getGrid().new Position(5, 5));
		enemy.setPosition(game.getGrid().new Position(6, 5));


		BasicStunt stunt = new BasicStunt(model, player);
		player.setStunt(stunt);

		FleeBot bot = new FleeBot(brain, stunt, enemy);
		brain.add(bot);

		brain.think();

		assertEquals(5, player.position().x());
		assertEquals(5, player.position().y());
		assertEquals(180, player.orientation());

		model.tick(200L);

		assertEquals(4, player.position().x());
		assertEquals(5, player.position().y());
		assertEquals(180, player.orientation());
	}

	@Test
	void fleeBotMovesAwayFromEnemyOnY() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity player = new TestEntity(model, "player");
		model.add(player);
		Entity enemy = new TestEntity(model, "enemy");
		model.add(enemy);

		player.setPosition(game.getGrid().new Position(5, 5));
		enemy.setPosition(game.getGrid().new Position(5, 6));


		BasicStunt stunt = new BasicStunt(model, player);
		player.setStunt(stunt);

		FleeBot bot = new FleeBot(brain, stunt, enemy);
		brain.add(bot);

		brain.think();

		assertEquals(5, player.position().x());
		assertEquals(5, player.position().y());
		assertEquals(270, player.orientation());

		model.tick(200L);

		assertEquals(5, player.position().x());
		assertEquals(4, player.position().y());
		assertEquals(270, player.orientation());
	}

	@Test
	void attackBotDoesNotMoveThroughBlockingEntity() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity attacker = new TestEntity(model, "attacker");
		model.add(attacker);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);
		Entity target = new TestEntity(model, "target");
		model.add(target);

		attacker.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(1, 0));
		target.setPosition(game.getGrid().new Position(3, 0));


		BasicStunt stunt = new BasicStunt(model, attacker);
		attacker.setStunt(stunt);

		AttackBot bot = new AttackBot(brain, stunt, target);
		brain.add(bot);

		brain.think();

		assertEquals(0, attacker.position().x());
		assertEquals(0, attacker.position().y());
		// La direction directe est bloquée : le Bot choisit une route alternative.
		assertEquals(90, attacker.orientation());

		model.tick(200L);

		assertEquals(0, attacker.position().x());
		assertEquals(1, attacker.position().y());
	}

	@Test
	void fleeBotDoesNotMoveThroughBlockingEntity() {
		Game game = new TestGame();
		Model model = game.model();
		Brain brain = new Brain();

		Entity player = new TestEntity(model, "player");
		model.add(player);
		Entity enemy = new TestEntity(model, "enemy");
		model.add(enemy);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		player.setPosition(game.getGrid().new Position(5, 5));
		enemy.setPosition(game.getGrid().new Position(6, 5));
		blocker.setPosition(game.getGrid().new Position(4, 5));


		BasicStunt stunt = new BasicStunt(model, player);
		player.setStunt(stunt);

		FleeBot bot = new FleeBot(brain, stunt, enemy);
		brain.add(bot);

		brain.think();

		assertEquals(5, player.position().x());
		assertEquals(5, player.position().y());
		// La fuite directe vers l'ouest est bloquée : le Bot choisit le nord.
		assertEquals(270, player.orientation());

		model.tick(200L);

		assertEquals(5, player.position().x());
		assertEquals(4, player.position().y());
	}
}
