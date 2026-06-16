package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GridTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 8, true, true, 3.7, 2);
		}
	}

	@Test
	void gridDimensionsAreCorrect() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		System.out.println("\n=== Grid dimensions ===");
		System.out.println("Situation : Game created with width = 10 cells and height = 8 cells");
		System.out.println("Expected width  = 10");
		System.out.println("Actual width    = " + grid.width());
		System.out.println("Expected height = 8");
		System.out.println("Actual height   = " + grid.height());

		assertEquals(10, grid.width());
		assertEquals(8, grid.height());
	}

	@Test
	void positionZeroZeroIsValid() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(0, 0);

		System.out.println("\n=== Grid Position (0,0) ===");
		System.out.println("Situation : create position (0,0)");
		System.out.println("Expected position = (0,0)");
		System.out.println("Actual position   = (" + p.x() + "," + p.y() + ")");

		assertEquals(0, p.x());
		assertEquals(0, p.y());
	}

	@Test
	void positionIsNormalizedOnTorus() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(10, 8);

		System.out.println("\n=== Grid Position normalization on torus ===");
		System.out.println("Situation : grid is 10x8, create position (10,8)");
		System.out.println("Because torus is active, (10,8) wraps to (0,0)");
		System.out.println("Expected position = (0,0)");
		System.out.println("Actual position   = (" + p.x() + "," + p.y() + ")");

		assertEquals(0, p.x());
		assertEquals(0, p.y());
	}

	@Test
	void negativePositionIsNormalizedOnTorus() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(-1, -1);

		System.out.println("\n=== Grid negative Position normalization on torus ===");
		System.out.println("Situation : grid is 10x8, create position (-1,-1)");
		System.out.println("Expected x = 9 because -1 wraps to last x cell");
		System.out.println("Actual x   = " + p.x());
		System.out.println("Expected y = 7 because -1 wraps to last y cell");
		System.out.println("Actual y   = " + p.y());

		assertEquals(9, p.x());
		assertEquals(7, p.y());
	}

	@Test
	void cellAtReturnsNormalizedCell() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(10, 8);
		Grid.Cell cell = grid.cellAt(p);

		System.out.println("\n=== Grid cellAt normalized position ===");
		System.out.println("Situation : ask cellAt(Position(10,8)) on a 10x8 torus grid");
		System.out.println("Position is normalized to (0,0)");
		System.out.println("Expected cell position = (0,0)");
		System.out.println("Actual cell position   = (" + cell.position().x() + "," + cell.position().y() + ")");

		assertNotNull(cell);
		assertEquals(0, cell.position().x());
		assertEquals(0, cell.position().y());
	}

	@Test
	void vectorTranslationWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(0, 0);
		Grid.Vector v = grid.new Vector(2, 3);

		System.out.println("\n=== Grid Position translation by Vector ===");
		System.out.println("Initial position = (" + p.x() + "," + p.y() + ")");
		System.out.println("Vector           = (" + v.x() + "," + v.y() + ")");

		p.translate(v);

		System.out.println("Expected position after translation = (2,3)");
		System.out.println("Actual position after translation   = (" + p.x() + "," + p.y() + ")");

		assertEquals(2, p.x());
		assertEquals(3, p.y());
	}

	@Test
	void moveDirectionsWork() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(0, 0);

		System.out.println("\n=== Grid Position move directions ===");
		System.out.println("Initial position = (" + p.x() + "," + p.y() + ")");

		p.moveEast(1);
		System.out.println("After moveEast(1), expected = (1,0), actual = (" + p.x() + "," + p.y() + ")");
		assertEquals(1, p.x());
		assertEquals(0, p.y());

		p.moveSouth(1);
		System.out.println("After moveSouth(1), expected = (1,1), actual = (" + p.x() + "," + p.y() + ")");
		assertEquals(1, p.x());
		assertEquals(1, p.y());

		p.moveWest(1);
		System.out.println("After moveWest(1), expected = (0,1), actual = (" + p.x() + "," + p.y() + ")");
		assertEquals(0, p.x());
		assertEquals(1, p.y());

		p.moveNorth(1);
		System.out.println("After moveNorth(1), expected = (0,0), actual = (" + p.x() + "," + p.y() + ")");
		assertEquals(0, p.x());
		assertEquals(0, p.y());
	}

	@Test
	void distanceOnGridTorusWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p1 = grid.new Position(0, 0);
		Grid.Position p2 = grid.new Position(9, 0);

		double distance = p1.distanceTo(p2);

		System.out.println("\n=== Grid Position distance on torus ===");
		System.out.println("Situation : grid width = 10");
		System.out.println("p1 = (0,0)");
		System.out.println("p2 = (9,0)");
		System.out.println("Direct distance on x would be 9");
		System.out.println("Torus shortest distance on x is 1");
		System.out.println("Expected distance = 1.0");
		System.out.println("Actual distance   = " + distance);

		assertEquals(1.0, distance, EPS);
	}

	@Test
	void gridPositionToISUCoordWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p = grid.new Position(2, 3);
		ISU.Coord c = p.toISUCoord();

		System.out.println("\n=== Grid Position to ISU Coord ===");
		System.out.println("Situation : cmPerCell = 3.7");
		System.out.println("Grid position = (2,3)");
		System.out.println("Expected ISU coord x = 2 * 3.7 = 7.4");
		System.out.println("Actual ISU coord x   = " + c.x());
		System.out.println("Expected ISU coord y = 3 * 3.7 = 11.1");
		System.out.println("Actual ISU coord y   = " + c.y());

		assertEquals(7.4, c.x(), EPS);
		assertEquals(11.1, c.y(), EPS);
	}

	@Test
	void rotateAround90DegreesWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position center = grid.new Position(1, 1);
		Grid.Position p = grid.new Position(2, 1);

		System.out.println("\n=== Grid Position rotateAround 90 degrees ===");
		System.out.println("Center = (1,1)");
		System.out.println("Initial position = (2,1)");
		System.out.println("Rotation = 90 degrees");

		p.rotateAround(center, 90);

		System.out.println("Expected position after rotation = (1,2)");
		System.out.println("Actual position after rotation   = (" + p.x() + "," + p.y() + ")");

		assertEquals(1, p.x());
		assertEquals(2, p.y());
	}

	@Test
	void dimensionEqualsAndEquivWork() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Dimension d1 = grid.new Dimension(2, 3);
		Grid.Dimension d2 = grid.new Dimension(2, 3);
		Grid.Dimension d3 = grid.new Dimension(3, 2);

		System.out.println("\n=== Grid Dimension equals and equiv ===");
		System.out.println("d1 = (2,3)");
		System.out.println("d2 = (2,3)");
		System.out.println("d3 = (3,2)");
		System.out.println("Expected d1.equals(d2) = true  | Actual = " + d1.equals(d2));
		System.out.println("Expected d1.equiv(d2)  = true  | Actual = " + d1.equiv(d2));
		System.out.println("Expected d1.equiv(d1)  = true  | Actual = " + d1.equiv(d1));
		System.out.println("Expected d1.equals(d3) = false | Actual = " + d1.equals(d3));
		System.out
				.println("Expected d1.equals(\"not a dimension\") = false | Actual = " + d1.equals("not a dimension"));

		assertTrue(d1.equals(d2));
		assertTrue(d1.equiv(d2));
		assertTrue(d1.equiv(d1));
		assertFalse(d1.equals(d3));
		assertFalse(d1.equals("not a dimension"));
	}

	@Test
	void dimensionNormalizeWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Dimension d = grid.new Dimension(10, 8);

		System.out.println("\n=== Grid Dimension normalize exact perimeter ===");
		System.out.println("Situation : grid dimension = (10,8), same as grid perimeter");
		System.out.println("Expected after normalize = (10,8)");

		d.normalize();

		System.out.println("Actual after normalize   = (" + d.x() + "," + d.y() + ")");

		assertEquals(10, d.x());
		assertEquals(8, d.y());
	}

	@Test
	void dimensionGreaterThanPerimeterIsNormalized() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Dimension d = grid.new Dimension(12, 10);

		System.out.println("\n=== Grid Dimension greater than perimeter ===");
		System.out.println("Situation : grid is 10x8, dimension is (12,10)");
		System.out.println("Expected x = 2 because 12 wraps modulo 10");
		System.out.println("Actual x   = " + d.x());
		System.out.println("Expected y = 2 because 10 wraps modulo 8");
		System.out.println("Actual y   = " + d.y());

		assertEquals(2, d.x());
		assertEquals(2, d.y());
	}

	@Test
	void dimensionToISUDimensionWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Dimension d = grid.new Dimension(2, 3);
		ISU.Dimension isuDim = d.toISUDimension();

		System.out.println("\n=== Grid Dimension to ISU Dimension ===");
		System.out.println("Grid dimension = (2,3)");
		System.out.println("cmPerCell = " + game.getCmPerCell());
		System.out.println("Expected ISU dimension x = 2 * 3.7 = 7.4");
		System.out.println("Actual ISU dimension x   = " + isuDim.x());
		System.out.println("Expected ISU dimension y = 3 * 3.7 = 11.1");
		System.out.println("Actual ISU dimension y   = " + isuDim.y());

		assertEquals(7.4, isuDim.x(), EPS);
		assertEquals(11.1, isuDim.y(), EPS);
	}

	@Test
	void vectorAddWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Vector v1 = grid.new Vector(1, 2);
		Grid.Vector v2 = grid.new Vector(3, 4);

		System.out.println("\n=== Grid Vector add ===");
		System.out.println("Initial v1 = (1,2)");
		System.out.println("v2         = (3,4)");
		System.out.println("Expected v1 after add = (4,6)");

		v1.add(v2);

		System.out.println("Actual v1 after add   = (" + v1.x() + "," + v1.y() + ")");

		assertEquals(4, v1.x());
		assertEquals(6, v1.y());
	}

	@Test
	void positionCopyWorks() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position p1 = grid.new Position(2, 3);
		Grid.Position p2 = p1.copy();

		System.out.println("\n=== Grid Position copy ===");
		System.out.println("p1 = (" + p1.x() + "," + p1.y() + ")");
		System.out.println("p2 = (" + p2.x() + "," + p2.y() + ")");
		System.out.println("Expected p1 and p2 are different objects");
		System.out.println("Actual p1 == p2 = " + (p1 == p2));
		System.out.println("Expected p1.equals(p2) = true");
		System.out.println("Actual p1.equals(p2)   = " + p1.equals(p2));

		assertNotSame(p1, p2);
		assertEquals(p1, p2);
	}

	@Test
	void positionEqualsFalseForOtherObject() {
		Game game = new TestGame();
		Grid.Position p = game.getGrid().new Position(1, 1);

		System.out.println("\n=== Grid Position equals with other object ===");
		System.out.println("Position = (1,1)");
		System.out.println("Compared object = \"hello\"");
		System.out.println("Expected equals = false");
		System.out.println("Actual equals   = " + p.equals("hello"));

		assertFalse(p.equals("hello"));
	}

	@Test
	void rotateAroundAllAnglesWork() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position center = grid.new Position(2, 2);

		System.out.println("\n=== Grid Position rotateAround all valid angles ===");
		System.out.println("Center = (2,2)");
		System.out.println("Initial point for each test = (3,2)");

		Grid.Position p0 = grid.new Position(3, 2);
		p0.rotateAround(center, 0);
		System.out.println("Angle 0   expected = (3,2), actual = (" + p0.x() + "," + p0.y() + ")");
		assertEquals(3, p0.x());
		assertEquals(2, p0.y());

		Grid.Position p90 = grid.new Position(3, 2);
		p90.rotateAround(center, 90);
		System.out.println("Angle 90  expected = (2,3), actual = (" + p90.x() + "," + p90.y() + ")");
		assertEquals(2, p90.x());
		assertEquals(3, p90.y());

		Grid.Position p180 = grid.new Position(3, 2);
		p180.rotateAround(center, 180);
		System.out.println("Angle 180 expected = (1,2), actual = (" + p180.x() + "," + p180.y() + ")");
		assertEquals(1, p180.x());
		assertEquals(2, p180.y());

		Grid.Position p270 = grid.new Position(3, 2);
		p270.rotateAround(center, 270);
		System.out.println("Angle 270 expected = (2,1), actual = (" + p270.x() + "," + p270.y() + ")");
		assertEquals(2, p270.x());
		assertEquals(1, p270.y());
	}

	@Test
	void rotateAroundInvalidAngleThrowsException() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Position center = grid.new Position(2, 2);
		Grid.Position p = grid.new Position(3, 2);

		System.out.println("\n=== Grid Position rotateAround invalid angle ===");
		System.out.println("Center = (2,2)");
		System.out.println("Position = (3,2)");
		System.out.println("Invalid angle = 45");
		System.out.println("Expected = IllegalArgumentException");

		assertThrows(IllegalArgumentException.class, () -> p.rotateAround(center, 45));
	}

	@Test
	void gridShowDoesNotCrash() {
		Game game = new TestGame();

		System.out.println("\n=== Grid show() ===");
		System.out.println("Situation : grid.show(System.out) should not crash");
		System.out.println("Expected = no exception");
		System.out.println("Actual output:");

		assertDoesNotThrow(() -> game.getGrid().show(System.out));
	}

	@Test
	void cellShowAndGettersWork() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Cell cell = grid.cellAt(grid.new Position(0, 0));

		System.out.println("\n=== Grid Cell show and getters ===");
		System.out.println("Cell position expected = (0,0)");
		System.out.println("Cell position actual   = (" + cell.position().x() + "," + cell.position().y() + ")");
		System.out.println("Expected size     = not null");
		System.out.println("Actual size       = " + cell.size());
		System.out.println("Expected position = not null");
		System.out.println("Actual position   = " + cell.position());
		System.out.println("Expected entities = not null");
		System.out.println("Actual entities   = " + cell.entities());
		System.out.println("cell.show(System.out) output:");

		assertNotNull(cell.size());
		assertNotNull(cell.position());
		assertNotNull(cell.entities());

		assertDoesNotThrow(() -> cell.show(System.out));
	}

	@Test
	void vectorIsNotNormalizedOnTorus() {
		Game game = new TestGame();
		Grid grid = game.getGrid();

		Grid.Vector left = grid.new Vector(-1, 0);
		Grid.Vector severalTurns = grid.new Vector(21, -17);

		assertEquals(-1, left.x());
		assertEquals(0, left.y());
		assertEquals(21, severalTurns.x());
		assertEquals(-17, severalTurns.y());
	}

	@Test
	void positionsFromDifferentGridsAreNotEqual() {
		Game game1 = new TestGame();
		Game game2 = new TestGame();

		Grid.Position p1 = game1.getGrid().new Position(2, 3);

		Grid.Position p2 = game2.getGrid().new Position(2, 3);

		assertNotEquals(p1, p2, "Deux positions de grilles différentes ne sont pas égales");
	}

}
