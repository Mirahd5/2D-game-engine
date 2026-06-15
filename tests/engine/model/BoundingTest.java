package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BoundingTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(30, 30, true, true, 1.0, 1);
		}
	}

	@Test
	void emptyBoundingDoesNotIntersectShape() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding bounding = new Bounding();
		Circle circle = new Circle(isu.new Coord(5.0, 5.0), 1.0);

		boolean result = bounding.intersects(circle);

		System.out.println("\n=== Empty Bounding vs Shape ===");
		System.out.println("Situation : Bounding has no shapes");
		System.out.println("Circle center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Circle radius = " + circle.radius());
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void boundingIntersectsShapeWhenOneShapeTouches() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding bounding = new Bounding();
		bounding.add(new Circle(isu.new Coord(5.0, 5.0), 2.0));

		Circle circle = new Circle(isu.new Coord(8.0, 5.0), 2.0);

		boolean result = bounding.intersects(circle);

		System.out.println("\n=== Bounding vs Shape: collision ===");
		System.out.println("Bounding contains Circle center = (5.0, 5.0), radius = 2.0");
		System.out.println("Other Circle center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Other Circle radius = " + circle.radius());
		System.out.println("Distance between centers = " + isu.new Coord(5.0, 5.0).distanceTo(circle.center()));
		System.out.println("Radius sum = 4.0");
		System.out.println("Expected intersection = true");
		System.out.println("Actual intersection   = " + result);

		assertTrue(result);
	}

	@Test
	void boundingDoesNotIntersectShapeWhenNoShapeTouches() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding bounding = new Bounding();
		bounding.add(new Circle(isu.new Coord(5.0, 5.0), 2.0));

		Circle circle = new Circle(isu.new Coord(15.0, 5.0), 2.0);

		boolean result = bounding.intersects(circle);

		System.out.println("\n=== Bounding vs Shape: no collision ===");
		System.out.println("Bounding contains Circle center = (5.0, 5.0), radius = 2.0");
		System.out.println("Other Circle center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Other Circle radius = " + circle.radius());
		System.out.println("Distance between centers = " + isu.new Coord(5.0, 5.0).distanceTo(circle.center()));
		System.out.println("Radius sum = 4.0");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void boundingIntersectsAnotherBoundingWithCircles() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(8.0, 5.0), 2.0);

		b1.add(c1);
		b2.add(c2);

		boolean result1 = b1.intersects(b2);
		boolean result2 = b2.intersects(b1);

		System.out.println("\n=== Bounding vs Bounding: two circles collide ===");
		System.out.println("Bounding 1 Circle center = (" + c1.center().x() + ", " + c1.center().y() + "), radius = " + c1.radius());
		System.out.println("Bounding 2 Circle center = (" + c2.center().x() + ", " + c2.center().y() + "), radius = " + c2.radius());
		System.out.println("Distance between centers = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected b1 intersects b2 = true");
		System.out.println("Actual b1 intersects b2   = " + result1);
		System.out.println("Expected b2 intersects b1 = true");
		System.out.println("Actual b2 intersects b1   = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void boundingDoesNotIntersectAnotherBoundingWhenSeparated() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(20.0, 5.0), 2.0);

		b1.add(c1);
		b2.add(c2);

		boolean result1 = b1.intersects(b2);
		boolean result2 = b2.intersects(b1);

		System.out.println("\n=== Bounding vs Bounding: separated circles ===");
		System.out.println("Bounding 1 Circle center = (" + c1.center().x() + ", " + c1.center().y() + "), radius = " + c1.radius());
		System.out.println("Bounding 2 Circle center = (" + c2.center().x() + ", " + c2.center().y() + "), radius = " + c2.radius());
		System.out.println("Distance between centers = " + c1.center().distanceTo(c2.center()));
		System.out.println("Radius sum = " + (c1.radius() + c2.radius()));
		System.out.println("Expected b1 intersects b2 = false");
		System.out.println("Actual b1 intersects b2   = " + result1);
		System.out.println("Expected b2 intersects b1 = false");
		System.out.println("Actual b2 intersects b1   = " + result2);

		assertFalse(result1);
		assertFalse(result2);
	}

	@Test
	void boundingIntersectsWithRectAndCircle() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(15.0, 10.0), 1.0);

		b1.add(rect);
		b2.add(circle);

		boolean result1 = b1.intersects(b2);
		boolean result2 = b2.intersects(b1);

		System.out.println("\n=== Bounding vs Bounding: Rect and Circle ===");
		System.out.println("Rect center = (" + rect.center().x() + ", " + rect.center().y() + ")");
		System.out.println("Rect halfWidth = " + rect.halfWidth());
		System.out.println("Rect halfHeight = " + rect.halfHeight());
		System.out.println("Circle center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Circle radius = " + circle.radius());
		System.out.println("Expected intersection = true");
		System.out.println("Actual b1 intersects b2 = " + result1);
		System.out.println("Actual b2 intersects b1 = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void boundingWithSeveralShapesIntersectsIfAtLeastOneTouches() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		Circle b1Far = new Circle(isu.new Coord(2.0, 2.0), 1.0);
		Circle b1Touching = new Circle(isu.new Coord(10.0, 10.0), 2.0);

		Circle b2Far = new Circle(isu.new Coord(20.0, 20.0), 1.0);
		Circle b2Touching = new Circle(isu.new Coord(13.0, 10.0), 2.0);

		b1.add(b1Far);
		b1.add(b1Touching);

		b2.add(b2Far);
		b2.add(b2Touching);

		boolean result = b1.intersects(b2);

		System.out.println("\n=== Bounding with several shapes ===");
		System.out.println("Bounding 1 contains:");
		System.out.println("- Circle center = (2.0, 2.0), radius = 1.0");
		System.out.println("- Circle center = (10.0, 10.0), radius = 2.0");
		System.out.println("Bounding 2 contains:");
		System.out.println("- Circle center = (20.0, 20.0), radius = 1.0");
		System.out.println("- Circle center = (13.0, 10.0), radius = 2.0");
		System.out.println("The second circle of b1 and second circle of b2 should touch.");
		System.out.println("Distance = " + b1Touching.center().distanceTo(b2Touching.center()));
		System.out.println("Radius sum = " + (b1Touching.radius() + b2Touching.radius()));
		System.out.println("Expected intersection = true");
		System.out.println("Actual intersection   = " + result);

		assertTrue(result);
	}

	@Test
	void boundingHitboxWithOneCircleWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding bounding = new Bounding();
		Circle circle = new Circle(isu.new Coord(5.0, 5.0), 2.0);

		bounding.add(circle);

		Hitbox h = bounding.hitbox();

		System.out.println("\n=== Bounding hitbox with one Circle ===");
		System.out.println("Circle center = (" + circle.center().x() + ", " + circle.center().y() + ")");
		System.out.println("Circle radius = " + circle.radius());
		System.out.println("Expected hitbox xmin = 5.0 - 2.0 = 3.0");
		System.out.println("Actual hitbox xmin   = " + h.xmin());
		System.out.println("Expected hitbox xmax = 5.0 + 2.0 = 7.0");
		System.out.println("Actual hitbox xmax   = " + h.xmax());
		System.out.println("Expected hitbox ymin = 5.0 - 2.0 = 3.0");
		System.out.println("Actual hitbox ymin   = " + h.ymin());
		System.out.println("Expected hitbox ymax = 5.0 + 2.0 = 7.0");
		System.out.println("Actual hitbox ymax   = " + h.ymax());

		assertEquals(3.0, h.xmin(), EPS);
		assertEquals(7.0, h.xmax(), EPS);
		assertEquals(3.0, h.ymin(), EPS);
		assertEquals(7.0, h.ymax(), EPS);
	}

	@Test
	void boundingHitboxWithSeveralShapesWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding bounding = new Bounding();

		Circle c1 = new Circle(isu.new Coord(5.0, 5.0), 2.0);
		Circle c2 = new Circle(isu.new Coord(12.0, 10.0), 1.0);

		bounding.add(c1);
		bounding.add(c2);

		Hitbox h = bounding.hitbox();

		System.out.println("\n=== Bounding hitbox with several shapes ===");
		System.out.println("Circle 1 center = (5.0, 5.0), radius = 2.0");
		System.out.println("Circle 1 hitbox = [3.0, 7.0] x [3.0, 7.0]");
		System.out.println("Circle 2 center = (12.0, 10.0), radius = 1.0");
		System.out.println("Circle 2 hitbox = [11.0, 13.0] x [9.0, 11.0]");
		System.out.println("Expected global hitbox xmin = 3.0");
		System.out.println("Actual global hitbox xmin   = " + h.xmin());
		System.out.println("Expected global hitbox xmax = 13.0");
		System.out.println("Actual global hitbox xmax   = " + h.xmax());
		System.out.println("Expected global hitbox ymin = 3.0");
		System.out.println("Actual global hitbox ymin   = " + h.ymin());
		System.out.println("Expected global hitbox ymax = 11.0");
		System.out.println("Actual global hitbox ymax   = " + h.ymax());

		assertEquals(3.0, h.xmin(), EPS);
		assertEquals(13.0, h.xmax(), EPS);
		assertEquals(3.0, h.ymin(), EPS);
		assertEquals(11.0, h.ymax(), EPS);
	}

	@Test
	void boundingIntersectsAcrossTorusBorder() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Bounding right = new Bounding();
		Bounding left = new Bounding();

		Circle cRight = new Circle(isu.new Coord(29.0, 10.0), 2.0);
		Circle cLeft = new Circle(isu.new Coord(1.0, 10.0), 2.0);

		right.add(cRight);
		left.add(cLeft);

		boolean result = right.intersects(left);

		System.out.println("\n=== Bounding torus border collision ===");
		System.out.println("World width = " + isu.width());
		System.out.println("Right Circle center = (" + cRight.center().x() + ", " + cRight.center().y() + "), radius = " + cRight.radius());
		System.out.println("Left Circle center  = (" + cLeft.center().x() + ", " + cLeft.center().y() + "), radius = " + cLeft.radius());
		System.out.println("Direct x distance = " + Math.abs(cLeft.center().x() - cRight.center().x()));
		System.out.println("Torus distance = " + cRight.center().distanceTo(cLeft.center()));
		System.out.println("Radius sum = " + (cRight.radius() + cLeft.radius()));
		System.out.println("Expected intersection through border = true");
		System.out.println("Actual intersection                  = " + result);

		assertTrue(result);
	}
}
