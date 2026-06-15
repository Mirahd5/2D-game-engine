package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ShapeTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(20, 20, true, true, 1.0, 1);
		}
	}

	static class TestShape extends Shape {

		TestShape(ISU.Coord center) {
			super(center);
		}

		@Override
		public boolean intersects(IShape shape) {
			return false;
		}

		@Override
		public boolean intersects(Circle circle) {
			return false;
		}

		@Override
		public boolean intersects(Rect rect) {
			return false;
		}

		@Override
		public Hitbox hitbox() {
			return new Hitbox(
				this.center.x() - 1.0,
				this.center.x() + 1.0,
				this.center.y() - 1.0,
				this.center.y() + 1.0
			);
		}
	}

	@Test
	void shapeConstructorStoresCenter() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord center = isu.new Coord(5.0, 6.0);
		Shape shape = new TestShape(center);

		System.out.println("\n=== Shape constructor stores center ===");
		System.out.println("Expected center = (5.0, 6.0)");
		System.out.println("Actual center   = (" + shape.center().x() + ", " + shape.center().y() + ")");

		assertEquals(5.0, shape.center().x(), EPS);
		assertEquals(6.0, shape.center().y(), EPS);
	}

	@Test
	void shapeConstructorStoresISU() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Shape shape = new TestShape(isu.new Coord(5.0, 6.0));

		System.out.println("\n=== Shape constructor stores ISU ===");
		System.out.println("Expected shape.isu() == game.getIsu()");
		System.out.println("Actual result = " + (shape.isu() == isu));

		assertSame(isu, shape.isu());
	}

	@Test
	void shapeTranslateMovesCenter() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Shape shape = new TestShape(isu.new Coord(5.0, 6.0));

		System.out.println("\n=== Shape translate ===");
		System.out.println("Initial center = (" + shape.center().x() + ", " + shape.center().y() + ")");
		System.out.println("Vector = (3.0, 4.0)");

		shape.translate(isu.new Vector(3.0, 4.0));

		System.out.println("Expected center = (8.0, 10.0)");
		System.out.println("Actual center   = (" + shape.center().x() + ", " + shape.center().y() + ")");

		assertEquals(8.0, shape.center().x(), EPS);
		assertEquals(10.0, shape.center().y(), EPS);
	}

	@Test
	void shapeTranslateNormalizesOnTorus() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Shape shape = new TestShape(isu.new Coord(19.0, 19.0));

		System.out.println("\n=== Shape translate on torus ===");
		System.out.println("World size = " + isu.width() + " x " + isu.height());
		System.out.println("Initial center = (19.0, 19.0)");
		System.out.println("Vector = (3.0, 4.0)");

		shape.translate(isu.new Vector(3.0, 4.0));

		System.out.println("Expected center = (2.0, 3.0)");
		System.out.println("Actual center   = (" + shape.center().x() + ", " + shape.center().y() + ")");

		assertEquals(2.0, shape.center().x(), EPS);
		assertEquals(3.0, shape.center().y(), EPS);
	}

	@Test
	void testShapeIntersectionsReturnFalse() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		TestShape shape = new TestShape(isu.new Coord(5.0, 5.0));

		Circle circle = new Circle(isu.new Coord(5.0, 5.0), 1.0);
		Rect rect = new Rect(isu.new Coord(5.0, 5.0), isu.new Dimension(2.0, 2.0), 0);

		System.out.println("\n=== TestShape fake intersections ===");
		System.out.println("Situation : TestShape is a fake concrete Shape used only for testing Shape base class");
		System.out.println("Expected shape.intersects(circle) = false");
		System.out.println("Actual shape.intersects(circle)   = " + shape.intersects(circle));
		System.out.println("Expected shape.intersects(rect)   = false");
		System.out.println("Actual shape.intersects(rect)     = " + shape.intersects(rect));
		System.out.println("Expected shape.intersects(IShape) = false");
		System.out.println("Actual shape.intersects(IShape)   = " + shape.intersects((IShape) circle));

		assertFalse(shape.intersects(circle));
		assertFalse(shape.intersects(rect));
		assertFalse(shape.intersects((IShape) circle));
	}

	@Test
	void testShapeHitboxWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		TestShape shape = new TestShape(isu.new Coord(5.0, 5.0));

		Hitbox h = shape.hitbox();

		System.out.println("\n=== TestShape hitbox ===");
		System.out.println("Shape center = (5.0, 5.0)");
		System.out.println("Hitbox margin = 1.0");
		System.out.println("Expected xmin = 4.0 | Actual = " + h.xmin());
		System.out.println("Expected xmax = 6.0 | Actual = " + h.xmax());
		System.out.println("Expected ymin = 4.0 | Actual = " + h.ymin());
		System.out.println("Expected ymax = 6.0 | Actual = " + h.ymax());

		assertEquals(4.0, h.xmin(), EPS);
		assertEquals(6.0, h.xmax(), EPS);
		assertEquals(4.0, h.ymin(), EPS);
		assertEquals(6.0, h.ymax(), EPS);
	}
}