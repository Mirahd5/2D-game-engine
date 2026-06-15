package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StuntNotificationTest {

	private static final double EPS = 0.000001;

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
	}

	static class SpyStunt extends Stunt {

		int orientationNotifications;
		int positionNotifications;
		int cellNotifications;
		int collisionNotifications;

		int lastOrientation;
		double lastX;
		double lastY;
		Grid.Cell lastCell;

		SpyStunt(Model model, Entity entity) {
			super(model, entity);
		}

		@Override
		public boolean walk(int degree) {
			return true;
		}

		@Override
		public void set(int orientation) {
			this.orientationNotifications++;
			this.lastOrientation = orientation;
		}

		@Override
		public void set(double x, double y) {
			this.positionNotifications++;
			this.lastX = x;
			this.lastY = y;
		}

		@Override
		public void set(Grid.Cell cell) {
			this.cellNotifications++;
			this.lastCell = cell;
		}

		@Override
		public void collision(java.util.List<Entity> entities) {
			this.collisionNotifications++;
		}
	}

	@Test
	void stuntIsNotifiedWhenPositionChanges() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);
		SpyStunt stunt = new SpyStunt(model, entity);

		entity.setStunt(stunt);

		entity.setPosition(game.getGrid().new Position(2, 3));

		assertTrue(stunt.positionNotifications >= 1);

		assertEquals((2 + 0.5) * game.getCmPerCell(), stunt.lastX, EPS);
		assertEquals((3 + 0.5) * game.getCmPerCell(), stunt.lastY, EPS);
	}

	@Test
	void stuntIsNotifiedWhenOrientationChanges() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);
		SpyStunt stunt = new SpyStunt(model, entity);

		entity.setStunt(stunt);

		entity.turn(90);

		assertEquals(1, stunt.orientationNotifications);
		assertEquals(90, stunt.lastOrientation);
	}

	@Test
	void stuntIsNotifiedWhenCellChanges() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);
		SpyStunt stunt = new SpyStunt(model, entity);

		entity.setStunt(stunt);

		entity.setPosition(game.getGrid().new Position(4, 5));

		assertTrue(stunt.cellNotifications >= 1);
		assertNotNull(stunt.lastCell);
		assertTrue(stunt.lastCell.contains(entity));
	}

	@Test
	void deathRemovesEntityFromModelAndCells() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		Grid.Cell initialCell = game.getGrid().cellAt(game.getGrid().new Position(0, 0));

		assertTrue(model.entities().contains(entity));
		assertTrue(initialCell.contains(entity));
		assertTrue(entity.alive());

		entity.death();
		model.tick(0L);

		assertFalse(entity.alive());
		assertFalse(model.entities().contains(entity));
		assertFalse(initialCell.contains(entity));
		assertTrue(entity.occupied().isEmpty());
	}
}
