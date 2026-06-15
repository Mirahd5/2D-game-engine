package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ISUTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 8, true, true, 3.7, 2);
		}
	}

	@Test
	void isuDimensionsAreCorrect() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		System.out.println("\n=== ISU dimensions ===");
		System.out.println("Situation : Game 10x8 cells, cmPerCell = 3.7");
		System.out.println("Expected width  = 10 * 3.7 = 37.0");
		System.out.println("Actual width    = " + isu.width());
		System.out.println("Expected height = 8 * 3.7 = 29.6");
		System.out.println("Actual height   = " + isu.height());
		System.out.println("Expected cmPerCell = 3.7");
		System.out.println("Actual cmPerCell   = " + isu.cmPerCell());

		assertEquals(37.0, isu.width(), EPS);
		assertEquals(29.6, isu.height(), EPS);
		assertEquals(3.7, isu.cmPerCell(), EPS);
	}

	@Test
	void coordZeroZeroIsValid() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(0.0, 0.0);

		System.out.println("\n=== ISU Coord zero ===");
		System.out.println("Situation : create Coord(0.0, 0.0)");
		System.out.println("Expected coord = (0.0, 0.0)");
		System.out.println("Actual coord   = (" + c.x() + ", " + c.y() + ")");

		assertEquals(0.0, c.x(), EPS);
		assertEquals(0.0, c.y(), EPS);
	}

	@Test
	void coordIsNormalizedOnTorus() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(37.0, 29.6);

		System.out.println("\n=== ISU Coord normalization on torus ===");
		System.out.println("Situation : world size = 37.0 x 29.6");
		System.out.println("Create Coord(37.0, 29.6)");
		System.out.println("Expected coord = (0.0, 0.0)");
		System.out.println("Actual coord   = (" + c.x() + ", " + c.y() + ")");

		assertEquals(0.0, c.x(), EPS);
		assertEquals(0.0, c.y(), EPS);
	}

	@Test
	void coordToGridPositionWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(7.4, 11.1);
		Grid.Position p = c.toGridPosition();

		System.out.println("\n=== ISU Coord to Grid Position ===");
		System.out.println("Situation : cmPerCell = 3.7");
		System.out.println("Coord = (7.4, 11.1)");
		System.out.println("Expected grid position x = 7.4 / 3.7 = 2");
		System.out.println("Actual grid position x   = " + p.x());
		System.out.println("Expected grid position y = 11.1 / 3.7 = 3");
		System.out.println("Actual grid position y   = " + p.y());

		assertEquals(2, p.x());
		assertEquals(3, p.y());
	}

	@Test
	void vectorTranslationWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(0.0, 0.0);
		ISU.Vector v = isu.new Vector(3.7, 0.0);

		System.out.println("\n=== ISU Coord translation by Vector ===");
		System.out.println("Initial coord = (" + c.x() + ", " + c.y() + ")");
		System.out.println("Vector        = (" + v.x() + ", " + v.y() + ")");

		c.translate(v);

		System.out.println("Expected coord after translation = (3.7, 0.0)");
		System.out.println("Actual coord after translation   = (" + c.x() + ", " + c.y() + ")");

		assertEquals(3.7, c.x(), EPS);
		assertEquals(0.0, c.y(), EPS);
	}

	@Test
	void vectorNormWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Vector v = isu.new Vector(3.0, 4.0);

		System.out.println("\n=== ISU Vector norm ===");
		System.out.println("Vector = (3.0, 4.0)");
		System.out.println("Expected norm = sqrt(3² + 4²) = 5.0");
		System.out.println("Actual norm   = " + v.norm());

		assertEquals(5.0, v.norm(), EPS);
	}

	@Test
	void vectorDotProductWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Vector v1 = isu.new Vector(1.0, 2.0);
		ISU.Vector v2 = isu.new Vector(3.0, 4.0);

		System.out.println("\n=== ISU Vector dot product ===");
		System.out.println("v1 = (1.0, 2.0)");
		System.out.println("v2 = (3.0, 4.0)");
		System.out.println("Expected dot = 1*3 + 2*4 = 11.0");
		System.out.println("Actual dot   = " + v1.dot(v2));

		assertEquals(11.0, v1.dot(v2), EPS);
	}

	@Test
	void vectorUnityWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Vector v = isu.new Vector(3.0, 4.0);

		System.out.println("\n=== ISU Vector unity ===");
		System.out.println("Initial vector = (3.0, 4.0)");
		System.out.println("Initial norm = " + v.norm());

		v.unity();

		System.out.println("Expected norm after unity = 1.0");
		System.out.println("Actual norm after unity   = " + v.norm());

		assertEquals(1.0, v.norm(), EPS);
	}

	@Test
	void distanceOnTorusWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c1 = isu.new Coord(0.0, 0.0);
		ISU.Coord c2 = isu.new Coord(33.3, 0.0);

		double distance = c1.distanceTo(c2);

		System.out.println("\n=== ISU Coord distance on torus ===");
		System.out.println("World width = " + isu.width());
		System.out.println("c1 = (0.0, 0.0)");
		System.out.println("c2 = (33.3, 0.0)");
		System.out.println("Direct x distance = 33.3");
		System.out.println("Torus shortest x distance = 37.0 - 33.3 = 3.7");
		System.out.println("Expected distance = 3.7");
		System.out.println("Actual distance   = " + distance);

		assertEquals(3.7, distance, EPS);
	}

	@Test
	void dimensionEqualsAndEquivWork() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Dimension d1 = isu.new Dimension(2.0, 3.0);
		ISU.Dimension d2 = isu.new Dimension(2.0, 3.0);
		ISU.Dimension d3 = isu.new Dimension(3.0, 2.0);

		System.out.println("\n=== ISU Dimension equals and equiv ===");
		System.out.println("d1 = (2.0, 3.0)");
		System.out.println("d2 = (2.0, 3.0)");
		System.out.println("d3 = (3.0, 2.0)");
		System.out.println("Expected d1.equals(d2) = true  | Actual = " + d1.equals(d2));
		System.out.println("Expected d1.equiv(d2)  = true  | Actual = " + d1.equiv(d2));
		System.out.println("Expected d1.equiv(d1)  = true  | Actual = " + d1.equiv(d1));
		System.out.println("Expected d1.equals(d3) = false | Actual = " + d1.equals(d3));
		System.out.println("Expected d1.equals(\"not a dimension\") = false | Actual = " + d1.equals("not a dimension"));

		assertTrue(d1.equals(d2));
		assertTrue(d1.equiv(d2));
		assertTrue(d1.equiv(d1));
		assertFalse(d1.equals(d3));
		assertFalse(d1.equals("not a dimension"));
	}

	@Test
	void dimensionSetxyWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Dimension d = isu.new Dimension(1.0, 1.0);

		System.out.println("\n=== ISU Dimension setxy ===");
		System.out.println("Initial dimension = (" + d.x() + ", " + d.y() + ")");
		System.out.println("Apply setxy(2.0, 3.0)");

		d.setxy(2.0, 3.0);

		System.out.println("Expected dimension = (2.0, 3.0)");
		System.out.println("Actual dimension   = (" + d.x() + ", " + d.y() + ")");

		assertEquals(2.0, d.x(), EPS);
		assertEquals(3.0, d.y(), EPS);
	}

	@Test
	void dimensionFactoriesWork() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Dimension d = isu.new Dimension(2.0, 3.0);

		ISU.Vector v1 = d.mkVector();
		ISU.Vector v2 = d.mkScaledVector(2.0);
		ISU.Vector v3 = d.mkScaledVector(2.0, 3.0);

		System.out.println("\n=== ISU Dimension factories ===");
		System.out.println("Dimension = (2.0, 3.0)");
		System.out.println("mkVector expected = (2.0, 3.0)");
		System.out.println("mkVector actual   = (" + v1.x() + ", " + v1.y() + ")");
		System.out.println("mkScaledVector(2.0) expected = (4.0, 6.0)");
		System.out.println("mkScaledVector(2.0) actual   = (" + v2.x() + ", " + v2.y() + ")");
		System.out.println("mkScaledVector(2.0, 3.0) expected = (4.0, 9.0)");
		System.out.println("mkScaledVector(2.0, 3.0) actual   = (" + v3.x() + ", " + v3.y() + ")");

		assertEquals(2.0, v1.x(), EPS);
		assertEquals(3.0, v1.y(), EPS);

		assertEquals(4.0, v2.x(), EPS);
		assertEquals(6.0, v2.y(), EPS);

		assertEquals(4.0, v3.x(), EPS);
		assertEquals(9.0, v3.y(), EPS);
	}

	@Test
	void dimensionShowDoesNotCrash() {
		Game game = new TestGame();
		ISU.Dimension d = game.getIsu().new Dimension(2.0, 3.0);

		System.out.println("\n=== ISU Dimension show() ===");
		System.out.println("Situation : show(System.out) should not crash");
		System.out.println("Expected = no exception");
		System.out.println("Actual output:");

		assertDoesNotThrow(() -> d.show(System.out));
	}

	@Test
	void coordEqualsWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c1 = isu.new Coord(1.0, 2.0);
		ISU.Coord c2 = isu.new Coord(1.0, 2.0);
		ISU.Coord c3 = isu.new Coord(2.0, 1.0);

		System.out.println("\n=== ISU Coord equals ===");
		System.out.println("c1 = (1.0, 2.0)");
		System.out.println("c2 = (1.0, 2.0)");
		System.out.println("c3 = (2.0, 1.0)");
		System.out.println("Expected c1.equals(c2) = true  | Actual = " + c1.equals(c2));
		System.out.println("Expected c1.equals(c3) = false | Actual = " + c1.equals(c3));
		System.out.println("Expected c1.equals(\"not a coord\") = false | Actual = " + c1.equals("not a coord"));

		assertTrue(c1.equals(c2));
		assertFalse(c1.equals(c3));
		assertFalse(c1.equals("not a coord"));
	}

	@Test
	void coordShowDoesNotCrash() {
		Game game = new TestGame();
		ISU.Coord c = game.getIsu().new Coord(1.0, 2.0);

		System.out.println("\n=== ISU Coord show() ===");
		System.out.println("Situation : show(System.out) should not crash");
		System.out.println("Expected = no exception");
		System.out.println("Actual output:");

		assertDoesNotThrow(() -> c.show(System.out));
	}

	@Test
	void coordMkCopyWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c1 = isu.new Coord(1.0, 2.0);
		ISU.Coord c2 = c1.mkCopy();

		System.out.println("\n=== ISU Coord mkCopy ===");
		System.out.println("Original coord = (" + c1.x() + ", " + c1.y() + ")");
		System.out.println("Copied coord   = (" + c2.x() + ", " + c2.y() + ")");
		System.out.println("Expected different objects = true");
		System.out.println("Actual different objects   = " + (c1 != c2));
		System.out.println("Expected equals = true");
		System.out.println("Actual equals   = " + c1.equals(c2));

		assertNotSame(c1, c2);
		assertEquals(c1, c2);
	}

	@Test
	void coordMkTranslatedWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c1 = isu.new Coord(1.0, 2.0);
		ISU.Vector v = isu.new Vector(3.0, 4.0);

		ISU.Coord c2 = c1.mkTranslated(v);

		System.out.println("\n=== ISU Coord mkTranslated ===");
		System.out.println("Initial coord = (1.0, 2.0)");
		System.out.println("Vector        = (3.0, 4.0)");
		System.out.println("Expected translated coord = (4.0, 6.0)");
		System.out.println("Actual translated coord   = (" + c2.x() + ", " + c2.y() + ")");

		assertEquals(4.0, c2.x(), EPS);
		assertEquals(6.0, c2.y(), EPS);
	}

	@Test
	void coordMkVectorTowardWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c1 = isu.new Coord(1.0, 2.0);
		ISU.Coord c2 = isu.new Coord(4.0, 6.0);

		ISU.Vector v = c1.mkVectorToward(c2);

		System.out.println("\n=== ISU Coord mkVectorToward ===");
		System.out.println("From c1 = (1.0, 2.0)");
		System.out.println("To c2   = (4.0, 6.0)");
		System.out.println("Expected vector = (3.0, 4.0)");
		System.out.println("Actual vector   = (" + v.x() + ", " + v.y() + ")");

		assertEquals(3.0, v.x(), EPS);
		assertEquals(4.0, v.y(), EPS);
	}

	@Test
	void coordMkVectorTowardUsesTorusShortestPath() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord right = isu.new Coord(36.0, 10.0);
		ISU.Coord left = isu.new Coord(1.0, 10.0);

		ISU.Vector v = right.mkVectorToward(left);

		System.out.println("\n=== ISU Coord mkVectorToward on torus ===");
		System.out.println("World width = " + isu.width());
		System.out.println("From right coord = (36.0, 10.0)");
		System.out.println("To left coord    = (1.0, 10.0)");
		System.out.println("Direct dx = 1.0 - 36.0 = -35.0");
		System.out.println("Torus shortest dx should be +2.0");
		System.out.println("Actual vector = (" + v.x() + ", " + v.y() + ")");

		assertEquals(2.0, v.x(), EPS);
		assertEquals(0.0, v.y(), EPS);
	}

	@Test
	void coordRotationAroundOriginWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord c = isu.new Coord(1.0, 0.0);

		System.out.println("\n=== ISU Coord rotation around origin ===");
		System.out.println("Initial coord = (1.0, 0.0)");
		System.out.println("Rotation = 90 degrees");

		c.rotation(90);

		System.out.println("Expected coord after rotation = (0.0, 1.0)");
		System.out.println("Actual coord after rotation   = (" + c.x() + ", " + c.y() + ")");

		assertEquals(0.0, c.x(), EPS);
		assertEquals(1.0, c.y(), EPS);
	}

	@Test
	void coordRotateAroundCenterWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord center = isu.new Coord(1.0, 1.0);
		ISU.Coord c = isu.new Coord(2.0, 1.0);

		System.out.println("\n=== ISU Coord rotateAround center ===");
		System.out.println("Center = (1.0, 1.0)");
		System.out.println("Initial coord = (2.0, 1.0)");
		System.out.println("Rotation = 90 degrees");

		c.rotateAround(center, 90);

		System.out.println("Expected coord after rotation = (1.0, 2.0)");
		System.out.println("Actual coord after rotation   = (" + c.x() + ", " + c.y() + ")");

		assertEquals(1.0, c.x(), EPS);
		assertEquals(2.0, c.y(), EPS);
	}

	@Test
	void vectorAddScaleTurnAndShowWork() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Vector v = isu.new Vector(1.0, 0.0);

		System.out.println("\n=== ISU Vector add / scale / turn / show ===");
		System.out.println("Initial vector = (" + v.x() + ", " + v.y() + ")");

		v.add(isu.new Vector(1.0, 2.0));
		System.out.println("After add(1.0, 2.0), expected = (2.0, 2.0), actual = (" + v.x() + ", " + v.y() + ")");
		assertEquals(2.0, v.x(), EPS);
		assertEquals(2.0, v.y(), EPS);

		v.scale(2.0);
		System.out.println("After scale(2.0), expected = (4.0, 4.0), actual = (" + v.x() + ", " + v.y() + ")");
		assertEquals(4.0, v.x(), EPS);
		assertEquals(4.0, v.y(), EPS);

		v.scale(0.5, 0.25);
		System.out.println("After scale(0.5, 0.25), expected = (2.0, 1.0), actual = (" + v.x() + ", " + v.y() + ")");
		assertEquals(2.0, v.x(), EPS);
		assertEquals(1.0, v.y(), EPS);

		System.out.println("Calling turn(90) and show(System.out)");
		assertDoesNotThrow(() -> v.turn(90));
		assertDoesNotThrow(() -> v.show(System.out));
	}
	@Test
	void vectorKeepsItsDirectionAndLengthOnTorus() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Vector left = isu.new Vector(-isu.cmPerCell(), 0.0);
		ISU.Vector longVector = isu.new Vector(2.0 * isu.width() + 1.5, -isu.height() - 2.5);

		assertEquals(-isu.cmPerCell(), left.x(), EPS);
		assertEquals(0.0, left.y(), EPS);
		assertEquals(2.0 * isu.width() + 1.5, longVector.x(), EPS);
		assertEquals(-isu.height() - 2.5, longVector.y(), EPS);
	}

	@Test
	void translatingByLongVectorNormalizesOnlyTheFinalCoordinate() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord start = isu.new Coord(1.0, 2.0);
		ISU.Vector path = isu.new Vector(2.0 * isu.width() + 3.0, -isu.height() - 4.0);
		ISU.Coord end = start.mkTranslated(path);

		assertEquals(4.0, end.x(), EPS);
		assertEquals(isu.height() - 2.0, end.y(), EPS);
		assertEquals(2.0 * isu.width() + 3.0, path.x(), EPS);
		assertEquals(-isu.height() - 4.0, path.y(), EPS);
	}


}
