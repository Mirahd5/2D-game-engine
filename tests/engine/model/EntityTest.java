package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EntityTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 8, true, true, 3.7, 2);
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
	void entityInitialStateIsCorrect() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		assertSame(model, e.model());
		assertEquals("pacman", e.name());
		assertEquals(0, e.orientation());

		assertNotNull(e.position());
		assertNotNull(e.center());
		assertNotNull(e.size());
		assertNotNull(e.step());
		assertNotNull(e.linearSpeed());
		assertEquals(0.0, e.angularSpeed(), EPS);

		assertEquals(0, e.position().x());
		assertEquals(0, e.position().y());

		assertEquals(game.getCmPerCell() / 2.0, e.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, e.center().y(), EPS);
	}

	@Test
	void setPositionSynchronizesCenter() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Position p = game.getGrid().new Position(2, 3);
		e.setPosition(p);

		assertEquals(2, e.position().x());
		assertEquals(3, e.position().y());

		assertEquals((2 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((3 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);
	}

	@Test
	void setCoordSynchronizesPosition() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		ISU.Coord c = game.getIsu().new Coord(7.4, 11.1);
		e.setCoord(c);

		assertEquals(7.4, e.center().x(), EPS);
		assertEquals(11.1, e.center().y(), EPS);

		assertEquals(2, e.position().x());
		assertEquals(3, e.position().y());
	}

	@Test
	void translateGridVectorSynchronizesCenter() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Vector v = game.getGrid().new Vector(1, 2);
		e.translate(v);

		assertEquals(1, e.position().x());
		assertEquals(2, e.position().y());

		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((2 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);
	}

	@Test
	void translateISUVectorSynchronizesPosition() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		ISU.Vector v = game.getIsu().new Vector(7.4, 3.7);
		e.translate(v);

		assertEquals((2 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);

		assertEquals(2, e.position().x());
		assertEquals(1, e.position().y());
	}

	@Test
	void turnNormalizesOrientation() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		e.turn(90);
		assertEquals(90, e.orientation());

		e.turn(300);
		assertEquals(30, e.orientation());

		e.turn(-60);
		assertEquals(330, e.orientation());
	}

	@Test
	void moveNorthSouthEastWestWorks() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		e.moveEast(3.7);
		assertEquals(1, e.position().x());
		assertEquals(0, e.position().y());
		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);

		e.moveSouth(1);
		assertEquals(1, e.position().x());
		assertEquals(1, e.position().y());
		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);

		e.moveWest(3.7);
		assertEquals(0, e.position().x());
		assertEquals(1, e.position().y());
		assertEquals((0 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((1 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);

		e.moveNorth(1);
		assertEquals(0, e.position().x());
		assertEquals(0, e.position().y());
		assertEquals((0 + 0.5) * game.getCmPerCell(), e.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), e.center().y(), EPS);
	}

	@Test
	void entityIsMovedBetweenCells() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid grid = game.getGrid();

		Grid.Cell oldCell = grid.cellAt(grid.new Position(0, 0));
		assertTrue(oldCell.contains(e));

		e.moveEast(3.7);

		Grid.Cell newCell = grid.cellAt(grid.new Position(1, 0));

		assertFalse(oldCell.contains(e));
		assertTrue(newCell.contains(e));
	}

	@Test
	void completeTorusTurnOnXReturnsToStart() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Position start = e.position().copy();

		for (int i = 0; i < game.getWidthCell(); i++) {
			e.moveEast(game.getCmPerCell());
		}

		assertEquals(start.x(), e.position().x());
		assertEquals(start.y(), e.position().y());

		assertEquals(game.getCmPerCell() / 2.0, e.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, e.center().y(), EPS);
	}

	@Test
	void completeTorusTurnOnYReturnsToStart() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Position start = e.position().copy();

		for (int i = 0; i < game.getHeightCell(); i++) {
			e.moveSouth(1);
		}

		assertEquals(start.x(), e.position().x());
		assertEquals(start.y(), e.position().y());

		assertEquals(game.getCmPerCell() / 2.0, e.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, e.center().y(), EPS);
	}

	@Test
	void setSizeWithGridDimensionWorks() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Dimension d = game.getGrid().new Dimension(2, 3);
		e.setSize(d);

		assertEquals(2 * game.getCmPerCell(), e.size().x(), EPS);
		assertEquals(3 * game.getCmPerCell(), e.size().y(), EPS);
	}

	@Test
	void setSizeWithISUDimensionWorks() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		ISU.Dimension d = game.getIsu().new Dimension(2.0, 3.0);
		e.setSize(d);

		assertSame(d, e.size());
	}

	@Test
	void entityShowDoesNotCrash() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		assertDoesNotThrow(() -> e.show(System.out));
	}

	@Test
	void moveAroundFullTorusUsingGridVectorReturnsToStart() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Position start = e.position().copy();

		for (int i = 0; i < game.getWidthCell(); i++) {
			e.translate(game.getGrid().new Vector(1, 0));
		}

		assertEquals(start.x(), e.position().x());
		assertEquals(start.y(), e.position().y());

		assertEquals(game.getCmPerCell() / 2.0, e.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, e.center().y(), EPS);
	}

	@Test
	void moveAroundFullTorusUsingISUVectorReturnsToStart() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		Grid.Position start = e.position().copy();

		for (int i = 0; i < game.getWidthCell(); i++) {
			e.translate(game.getIsu().new Vector(game.getCmPerCell(), 0.0));
		}

		assertEquals(start.x(), e.position().x());
		assertEquals(start.y(), e.position().y());

		assertEquals(game.getCmPerCell() / 2.0, e.center().x(), EPS);
		assertEquals(game.getCmPerCell() / 2.0, e.center().y(), EPS);
	}

	@Test
	void entityHasBoundingAfterConstruction() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "pacman");

		assertNotNull(e.bounding());
	}

	@Test
	void entityIntersectsAnotherEntityWhenBoundingOverlaps() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setCoord(game.getIsu().new Coord(5.0, 5.0));
		e2.setCoord(game.getIsu().new Coord(6.0, 5.0));

		assertTrue(e1.intersects(e2));
		assertTrue(e2.intersects(e1));
	}

	@Test
	void entityDoesNotIntersectWhenBoundingIsFar() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setCoord(game.getIsu().new Coord(1.0, 1.0));
		e2.setCoord(game.getIsu().new Coord(20.0, 20.0));

		assertFalse(e1.intersects(e2));
		assertFalse(e2.intersects(e1));
	}

	@Test
	void distanceCenterToCenterWorks() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setCoord(game.getIsu().new Coord(0.0, 0.0));
		e2.setCoord(game.getIsu().new Coord(3.0, 4.0));

		assertEquals(5.0, e1.distanceCenterToCenter(e2), EPS);
	}

	@Test
	void boundingIsUpdatedAfterMove() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");

		e1.setCoord(game.getIsu().new Coord(0.0, 0.0));
		e2.setCoord(game.getIsu().new Coord(20.0, 20.0));

		assertFalse(e1.intersects(e2));

		e2.setCoord(game.getIsu().new Coord(1.0, 0.0));

		assertTrue(e1.intersects(e2));
	}

	@Test
	void entityOccupiesInitialCell() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "e");

		Grid.Cell cell = game.getGrid().cellAt(game.getGrid().new Position(0, 0));

		assertTrue(cell.contains(e));
		assertTrue(e.occupied().contains(cell));
	}

	@Test
	void entityRetractsFromOldCellAfterMove() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "e");

		Grid.Cell oldCell = game.getGrid().cellAt(game.getGrid().new Position(0, 0));

		e.setPosition(game.getGrid().new Position(2, 3));

		Grid.Cell newCell = game.getGrid().cellAt(game.getGrid().new Position(2, 3));

		assertFalse(oldCell.contains(e));
		assertTrue(newCell.contains(e));
		assertFalse(e.occupied().contains(oldCell));
		assertTrue(e.occupied().contains(newCell));
	}

	@Test
	void entityOccupiedContainsOnlyCurrentCellAfterMove() {
		Game game = new TestGame();
		Model model = game.model();
		Entity e = new TestEntity(model, "e");

		e.setPosition(game.getGrid().new Position(2, 3));

		assertEquals(1, e.occupied().size());
	}

	@Test
	void stuntIsNotifiedWhenEntityMovesAndTurns() {
		Game game = new TestGame();
		Model model = game.model();

		class SpyStunt extends Stunt {
			int orientationNotifications = 0;
			int cellNotifications = 0;
			int positionNotifications = 0;

			SpyStunt(Model model, Entity entity) {
				super(model, entity);
			}

			@Override
			public boolean walk(int degree) {
				return true;
			}

			@Override
			public void set(int orientation) {
				orientationNotifications++;
			}

			@Override
			public void set(Grid.Cell cell) {
				cellNotifications++;
			}

			@Override
			public void set(double x, double y) {
				positionNotifications++;
			}
		}

		Entity e = new TestEntity(model, "e");
		SpyStunt stunt = new SpyStunt(model, e);
		e.setStunt(stunt);

		e.setPosition(game.getGrid().new Position(2, 3));
		e.turn(90);

		assertTrue(stunt.positionNotifications >= 1);
		assertTrue(stunt.cellNotifications >= 1);
		assertTrue(stunt.orientationNotifications >= 1);
	}
	
	@Test
	void entityHasZOrderAndSOrder() {
		Game game = new TestGame();
		Model model = game.model();

		Entity entity = new TestEntity(model, "e");

		assertEquals(0, entity.zOrder());
		assertEquals(0, entity.sOrder());

		entity.setZOrder(2);
		entity.setSOrder(1);

		assertEquals(2, entity.zOrder());
		assertEquals(1, entity.sOrder());
	}
}