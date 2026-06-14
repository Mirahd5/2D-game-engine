package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CircleTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(20, 20, true, true, 1.0, 1);
		}
	}

	@Test
	void circleConstructorStoresCenterAndRadius() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		ISU.Coord center = isu.new Coord(5.0, 6.0);
		Circle circle = new Circle(center, 2.5);

		System.out.println("\n=== Circle constructor ===");
		System.out.println("Situation : create Circle(center=(5.0,6.0), radius=2.5)");
		System.out.println("Expected center = (5.0, 6.0)");
		System.out.println("Actual center   = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Expected radius = 2.5");
		System.out.println("Actual radius   = " + circle.radius());

		assertEquals(5.0, circle.center().x(), EPS);
		assertEquals(6.0, circle.center().y(), EPS);
		assertEquals(2.5, circle.radius(), EPS);
	}

	@Test
	void circleCircleIntersectsWhenOverlapping() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(8.0, 5.0), 2.0);

		boolean r1 = c1.intersects(c2);
		boolean r2 = c2.intersects(c1);

		System.out.println("\n=== Circle-Circle overlapping ===");
		System.out.println("c1 center = (5.0, 5.0), radius = 2.0");
		System.out.println("c2 center = (8.0, 5.0), radius = 2.0");
		System.out.println("Distance = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected intersection = true");
		System.out.println("Actual c1 -> c2 = " + r1);
		System.out.println("Actual c2 -> c1 = " + r2);

		assertTrue(r1);
		assertTrue(r2);
	}

	@Test
	void circleCircleIntersectsWhenTouching() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(9.0, 5.0), 2.0);

		boolean r1 = c1.intersects(c2);
		boolean r2 = c2.intersects(c1);

		System.out.println("\n=== Circle-Circle touching ===");
		System.out.println("Distance = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected intersection = true, because distance == radius sum");
		System.out.println("Actual c1 -> c2 = " + r1);
		System.out.println("Actual c2 -> c1 = " + r2);

		assertTrue(r1);
		assertTrue(r2);
	}

	@Test
	void circleCircleDoesNotIntersectWhenSeparated() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(12.0, 5.0), 2.0);

		boolean r1 = c1.intersects(c2);
		boolean r2 = c2.intersects(c1);

		System.out.println("\n=== Circle-Circle separated ===");
		System.out.println("Distance = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected intersection = false");
		System.out.println("Actual c1 -> c2 = " + r1);
		System.out.println("Actual c2 -> c1 = " + r2);

		assertFalse(r1);
		assertFalse(r2);
	}

	@Test
	void circleCircleUsesTorusDistance() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle c1 = new Circle(isu.new Coord(1.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(19.0, 5.0), 2.0);

		boolean r1 = c1.intersects(c2);
		boolean r2 = c2.intersects(c1);

		System.out.println("\n=== Circle-Circle torus distance ===");
		System.out.println("World width = " + isu.width());
		System.out.println("c1 center = (1.0, 5.0)");
		System.out.println("c2 center = (19.0, 5.0)");
		System.out.println("Direct dx = " + (c2.center().x() - c1.center().x()));
		System.out.println("Torus distance = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected intersection through border = true");
		System.out.println("Actual c1 -> c2 = " + r1);
		System.out.println("Actual c2 -> c1 = " + r2);

		assertTrue(r1);
		assertTrue(r2);
	}

	@Test
	void circleTranslateMovesCenter() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(5.0, 5.0), 2.0);

		System.out.println("\n=== Circle translate ===");
		System.out.println("Initial center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Vector = (3.0, 4.0)");

		circle.translate(isu.new Vector(3.0, 4.0));

		System.out.println("Expected center = (8.0, 9.0)");
		System.out.println("Actual center   = (" + circle.center().x() + ", " + circle.center().y() + ")");

		assertEquals(8.0, circle.center().x(), EPS);
		assertEquals(9.0, circle.center().y(), EPS);
	}

	@Test
	void circleTranslateNormalizesOnTorus() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(19.0, 19.0), 2.0);

		System.out.println("\n=== Circle translate on torus ===");
		System.out.println("World size = " + isu.width() + " x " + isu.height());
		System.out.println("Initial center = (19.0, 19.0)");
		System.out.println("Vector = (2.0, 3.0)");

		circle.translate(isu.new Vector(2.0, 3.0));

		System.out.println("Expected center = (1.0, 2.0)");
		System.out.println("Actual center   = (" + circle.center().x() + ", " + circle.center().y() + ")");

		assertEquals(1.0, circle.center().x(), EPS);
		assertEquals(2.0, circle.center().y(), EPS);
	}

	@Test
	void circleIntersectsIShapeDispatchWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		IShape c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		IShape c2 = new Circle(isu.new Coord(8.0, 5.0), 2.0);

		boolean result = c1.intersects(c2);

		System.out.println("\n=== Circle IShape dispatch ===");
		System.out.println("Situation : c1 and c2 are typed as IShape");
		System.out.println("Expected dispatch to Circle-Circle intersection = true");
		System.out.println("Actual result = " + result);

		assertTrue(result);
	}

	@Test
	void circleHitboxWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Circle circle = new Circle(isu.new Coord(5.0, 6.0), 2.0);
		Hitbox h = circle.hitbox();

		System.out.println("\n=== Circle hitbox ===");
		System.out.println("Circle center = (5.0, 6.0)");
		System.out.println("Circle radius = 2.0");
		System.out.println("Expected hitbox xmin = 3.0 | Actual = " + h.xmin());
		System.out.println("Expected hitbox xmax = 7.0 | Actual = " + h.xmax());
		System.out.println("Expected hitbox ymin = 4.0 | Actual = " + h.ymin());
		System.out.println("Expected hitbox ymax = 8.0 | Actual = " + h.ymax());

		assertEquals(3.0, h.xmin(), EPS);
		assertEquals(7.0, h.xmax(), EPS);
		assertEquals(4.0, h.ymin(), EPS);
		assertEquals(8.0, h.ymax(), EPS);
	}
}