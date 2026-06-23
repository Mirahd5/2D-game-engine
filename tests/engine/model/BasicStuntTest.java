package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BasicStuntTest {

	private static final double EPS = 0.000001;

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

	@Test
	void walkEastSetsSpeedThenTickMovesOneCell() {
		Game game = new TestGame();
		Model model = game.model();
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(0);

		assertTrue(started);

		// walk ne déplace pas encore.
		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(0, entity.orientation());

		assertEquals(game.getCmPerCell(), entity.linearSpeed().x(), EPS);
		assertEquals(0.0, entity.linearSpeed().y(), EPS);

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());

		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
		assertEquals(0, entity.orientation());
	}

	@Test
	void walkSouthSetsSpeedThenTickMovesOneCell() {
		Game game = new TestGame();
		Model model = game.model();
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(90);

		assertTrue(started);

		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(90, entity.orientation());

		assertEquals(0.0, entity.linearSpeed().x(), EPS);
		assertEquals(game.getCmPerCell(), entity.linearSpeed().y(), EPS);

		model.tick(1000L);

		assertEquals(0, entity.position().x());
		assertEquals(1, entity.position().y());
		assertEquals(90, entity.orientation());

		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void walkWestSetsSpeedThenTickWrapsOnTorus() {
		Game game = new TestGame();
		Model model = game.model();
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(180);

		assertTrue(started);

		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(180, entity.orientation());

		assertEquals(-game.getCmPerCell(), entity.linearSpeed().x(), EPS);
		assertEquals(0.0, entity.linearSpeed().y(), EPS);

		model.tick(1000L);

		assertEquals(9, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(180, entity.orientation());

		assertEquals((9 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void walkNorthSetsSpeedThenTickWrapsOnTorus() {
		Game game = new TestGame();
		Model model = game.model();
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(270);

		assertTrue(started);

		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(270, entity.orientation());

		assertEquals(0.0, entity.linearSpeed().x(), EPS);
		assertEquals(-game.getCmPerCell(), entity.linearSpeed().y(), EPS);

		model.tick(1000L);

		assertEquals(0, entity.position().x());
		assertEquals(9, entity.position().y());
		assertEquals(270, entity.orientation());

		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((9 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void walkChoosesClosestCardinalDirectionThenTickMoves() {
		Game game = new TestGame();
		Model model = game.model();
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(20);

		assertTrue(started);

		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(0, entity.orientation());

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());
		assertEquals(0, entity.orientation());

		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void walkStartsButTickFailsWhenCollisionOccurs() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(1, 0));

		model.add(e1);
		model.add(e2);

		BasicStunt stunt = new BasicStunt(model, e1);
		e1.setStunt(stunt);

		boolean started = stunt.walk(0);

		assertTrue(started);

		// Avant tick, l'entité n'a pas encore bougé.
		assertEquals(0, e1.position().x());
		assertEquals(0, e1.position().y());

		model.tick(1000L);

		// Le tick tente d'aller en (1,0), collisionne, puis revient en (0,0).
		assertEquals(0, e1.position().x());
		assertEquals(0, e1.position().y());

		assertEquals((0 + 0.5) * game.getCmPerCell(), e1.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), e1.center().y(), EPS);

		// Après collision, la vitesse doit être arrêtée.
		assertEquals(0.0, e1.linearSpeed().x(), EPS);
		assertEquals(0.0, e1.linearSpeed().y(), EPS);
		assertEquals(0.0, e1.angularSpeed(), EPS);
	}

	@Test
	void walkStartsMovementButDoesNotMoveImmediately() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		boolean started = stunt.walk(0);

		assertTrue(started);
		assertTrue(stunt.walking());

		assertEquals(0, entity.position().x());
		assertEquals(0, entity.position().y());

		assertEquals(game.getCmPerCell(), entity.linearSpeed().x(), EPS);
		assertEquals(0.0, entity.linearSpeed().y(), EPS);
	}

	@Test
	void walkStopsAfterOneCell() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		assertTrue(stunt.walk(0));

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());

		assertFalse(stunt.walking());

		assertEquals(0.0, entity.linearSpeed().x(), EPS);
		assertEquals(0.0, entity.linearSpeed().y(), EPS);
	}

	@Test
	void walkCanFinishOverSeveralTicks() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		assertTrue(stunt.walk(0));

		model.tick(500L);

		assertTrue(stunt.walking());

		/*
		 * Après 500 ms, l'entité a avancé d'une demi-cellule. Elle peut déjà être dans
		 * la cellule suivante selon toGridPosition(), mais elle n'a pas encore atteint
		 * le centre de cette cellule.
		 */
		assertEquals(game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, entity.center().y(), EPS);

		model.tick(500L);

		assertFalse(stunt.walking());
		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());

		assertEquals(0.0, entity.linearSpeed().x(), EPS);
		assertEquals(0.0, entity.linearSpeed().y(), EPS);

		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void entityDoesNotContinueMovingAfterWalkIsDone() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		BasicStunt stunt = new BasicStunt(model, entity);
		entity.setStunt(stunt);

		assertTrue(stunt.walk(0));

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertFalse(stunt.walking());

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());
	}

	@Test
	void collisionStopsWalking() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(1, 0));

		model.add(e1);
		model.add(e2);

		BasicStunt stunt = new BasicStunt(model, e1);
		e1.setStunt(stunt);

		assertTrue(stunt.walk(0));

		model.tick(1000L);

		assertEquals(0, e1.position().x());
		assertEquals(0, e1.position().y());

		assertFalse(stunt.walking());

		assertEquals(0.0, e1.linearSpeed().x(), EPS);
		assertEquals(0.0, e1.linearSpeed().y(), EPS);
	}

}