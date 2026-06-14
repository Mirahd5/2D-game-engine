package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RectTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(30, 30, true, true, 1.0, 1);
		}
	}

	@Test
	void rectConstructorStoresCenterSizeAndAngle() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 90);

		System.out.println("\n=== Rect constructor ===");
		System.out.println("Situation : Rect(center=(10,10), size=(8,4), angle=90)");
		System.out.println("Expected center = (10.0, 10.0)");
		System.out.println("Actual center   = (" + rect.center().x() + ", " + rect.center().y() + ")");
		System.out.println("Expected halfWidth  = 8.0 / 2 = 4.0");
		System.out.println("Actual halfWidth    = " + rect.halfWidth());
		System.out.println("Expected halfHeight = 4.0 / 2 = 2.0");
		System.out.println("Actual halfHeight   = " + rect.halfHeight());
		System.out.println("Expected angle = 90");
		System.out.println("Actual angle   = " + rect.angle());

		assertEquals(10.0, rect.center().x(), EPS);
		assertEquals(10.0, rect.center().y(), EPS);
		assertEquals(4.0, rect.halfWidth(), EPS);
		assertEquals(2.0, rect.halfHeight(), EPS);
		assertEquals(90, rect.angle());
	}

	@Test
	void rectConstructorNormalizesPositiveAngle() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 450);

		System.out.println("\n=== Rect constructor normalizes positive angle ===");
		System.out.println("Situation : angle = 450");
		System.out.println("Expected angle = 450 % 360 = 90");
		System.out.println("Actual angle   = " + rect.angle());

		assertEquals(90, rect.angle());
	}

	@Test
	void rectConstructorNormalizesNegativeAngle() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), -90);

		System.out.println("\n=== Rect constructor normalizes negative angle ===");
		System.out.println("Situation : angle = -90");
		System.out.println("Expected angle = 270");
		System.out.println("Actual angle   = " + rect.angle());

		assertEquals(270, rect.angle());
	}

	@Test
	void rectRotateWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 90);

		System.out.println("\n=== Rect rotate ===");
		System.out.println("Initial angle = " + rect.angle());
		System.out.println("Rotation added = 300");

		rect.rotate(300);

		System.out.println("Expected angle = (90 + 300) % 360 = 30");
		System.out.println("Actual angle   = " + rect.angle());

		assertEquals(30, rect.angle());
	}

	@Test
	void rectTranslateMovesCenter() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);

		System.out.println("\n=== Rect translate ===");
		System.out.println("Initial center = (" + rect.center().x() + ", " + rect.center().y() + ")");
		System.out.println("Vector = (3.0, 4.0)");

		rect.translate(isu.new Vector(3.0, 4.0));

		System.out.println("Expected center = (13.0, 14.0)");
		System.out.println("Actual center   = (" + rect.center().x() + ", " + rect.center().y() + ")");

		assertEquals(13.0, rect.center().x(), EPS);
		assertEquals(14.0, rect.center().y(), EPS);
	}

	@Test
	void rectCircleIntersectsWhenCircleInsideRect() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(10.0, 10.0), 1.0);

		boolean r1 = rect.intersects(circle);
		boolean r2 = circle.intersects(rect);

		System.out.println("\n=== Rect-Circle: circle inside rect ===");
		System.out.println("Rect center = (10.0, 10.0), halfWidth = 4.0, halfHeight = 2.0");
		System.out.println("Circle center = (10.0, 10.0), radius = 1.0");
		System.out.println("Expected intersection = true");
		System.out.println("Actual rect -> circle = " + r1);
		System.out.println("Actual circle -> rect = " + r2);

		assertTrue(r1);
		assertTrue(r2);
	}

	@Test
	void rectCircleIntersectsWhenCircleTouchesSide() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(15.0, 10.0), 1.0);

		boolean r1 = rect.intersects(circle);
		boolean r2 = circle.intersects(rect);

		System.out.println("\n=== Rect-Circle: circle touches side ===");
		System.out.println("Rect x interval = [6.0, 14.0]");
		System.out.println("Circle center x = 15.0, radius = 1.0");
		System.out.println("Circle left side = 14.0, so it touches the rect right side");
		System.out.println("Expected intersection = true");
		System.out.println("Actual rect -> circle = " + r1);
		System.out.println("Actual circle -> rect = " + r2);

		assertTrue(r1);
		assertTrue(r2);
	}

	@Test
	void rectCircleDoesNotIntersectWhenSeparated() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(17.0, 10.0), 1.0);

		boolean r1 = rect.intersects(circle);
		boolean r2 = circle.intersects(rect);

		System.out.println("\n=== Rect-Circle: separated ===");
		System.out.println("Rect x interval = [6.0, 14.0]");
		System.out.println("Circle center x = 17.0, radius = 1.0");
		System.out.println("Circle left side = 16.0, so it is separated from rect right side 14.0");
		System.out.println("Expected intersection = false");
		System.out.println("Actual rect -> circle = " + r1);
		System.out.println("Actual circle -> rect = " + r2);

		assertFalse(r1);
		assertFalse(r2);
	}

	@Test
	void rectCircleIntersectsCorner() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(14.5, 12.5), 1.0);

		boolean result = rect.intersects(circle);

		System.out.println("\n=== Rect-Circle: corner collision ===");
		System.out.println("Rect right-bottom corner is near (14.0, 12.0)");
		System.out.println("Circle center = (14.5, 12.5), radius = 1.0");
		System.out.println("Expected intersection = true");
		System.out.println("Actual intersection   = " + result);

		assertTrue(result);
	}

	@Test
	void rectCircleDoesNotIntersectFarFromCorner() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(16.0, 14.0), 1.0);

		boolean result = rect.intersects(circle);

		System.out.println("\n=== Rect-Circle: far from corner ===");
		System.out.println("Rect right-bottom corner is near (14.0, 12.0)");
		System.out.println("Circle center = (16.0, 14.0), radius = 1.0");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void rectCircleIntersectsWithRotation90Degrees() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 90);
		Circle circle = new Circle(isu.new Coord(10.0, 14.0), 1.0);

		boolean result = rect.intersects(circle);

		System.out.println("\n=== Rect-Circle: rotated rect 90 degrees ===");
		System.out.println("Rect size = (8.0, 4.0), angle = 90");
		System.out.println("After rotation, long side is vertical");
		System.out.println("Circle center = (10.0, 14.0), radius = 1.0");
		System.out.println("Expected intersection = true");
		System.out.println("Actual intersection   = " + result);

		assertTrue(result);
	}

	@Test
	void rectIntersectsIShapeDispatchWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		IShape rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		IShape circle = new Circle(isu.new Coord(10.0, 10.0), 1.0);

		boolean result = rect.intersects(circle);

		System.out.println("\n=== Rect IShape dispatch ===");
		System.out.println("Situation : rect and circle are typed as IShape");
		System.out.println("Expected dispatch to Rect-Circle intersection = true");
		System.out.println("Actual result = " + result);

		assertTrue(result);
	}

	@Test
	void rectRectIntersectsWhenOverlappingWithoutRotation() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Rect r2 = new Rect(isu.new Coord(13.0, 10.0), isu.new Dimension(8.0, 4.0), 0);

		boolean a = r1.intersects(r2);
		boolean b = r2.intersects(r1);

		System.out.println("\n=== Rect-Rect: overlapping without rotation ===");
		System.out.println("r1 center = (10.0,10.0), size = (8.0,4.0)");
		System.out.println("r2 center = (13.0,10.0), size = (8.0,4.0)");
		System.out.println("Expected intersection = true");
		System.out.println("Actual r1 -> r2 = " + a);
		System.out.println("Actual r2 -> r1 = " + b);

		assertTrue(a);
		assertTrue(b);
	}

	@Test
	void rectRectDoesNotIntersectWhenSeparatedWithoutRotation() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Rect r2 = new Rect(isu.new Coord(20.0, 10.0), isu.new Dimension(4.0, 4.0), 0);

		boolean a = r1.intersects(r2);
		boolean b = r2.intersects(r1);

		System.out.println("\n=== Rect-Rect: separated without rotation ===");
		System.out.println("r1 center = (10.0,10.0), halfWidth = 4.0");
		System.out.println("r2 center = (20.0,10.0), halfWidth = 2.0");
		System.out.println("Distance on x = 10.0, halfWidth sum = 6.0");
		System.out.println("Expected intersection = false");
		System.out.println("Actual r1 -> r2 = " + a);
		System.out.println("Actual r2 -> r1 = " + b);

		assertFalse(a);
		assertFalse(b);
	}

	@Test
	void rectRectIntersectsWhenTouchingSide() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Rect r2 = new Rect(isu.new Coord(16.0, 10.0), isu.new Dimension(4.0, 4.0), 0);

		boolean result = r1.intersects(r2);

		System.out.println("\n=== Rect-Rect: touching side ===");
		System.out.println("r1 x interval = [6.0, 14.0]");
		System.out.println("r2 x interval = [14.0, 18.0]");
		System.out.println("They touch at x = 14.0");
		System.out.println("Expected intersection = true");
		System.out.println("Actual intersection   = " + result);

		assertTrue(result);
	}

	@Test
	void rectRectIntersectsWithRotation() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 45);
		Rect r2 = new Rect(isu.new Coord(12.0, 10.0), isu.new Dimension(8.0, 4.0), -30);

		boolean a = r1.intersects(r2);
		boolean b = r2.intersects(r1);

		System.out.println("\n=== Rect-Rect: rotated overlap ===");
		System.out.println("r1 angle = 45");
		System.out.println("r2 angle = -30 normalized to " + r2.angle());
		System.out.println("Expected intersection = true");
		System.out.println("Actual r1 -> r2 = " + a);
		System.out.println("Actual r2 -> r1 = " + b);

		assertTrue(a);
		assertTrue(b);
	}

	@Test
	void rectRectDoesNotIntersectWithRotationWhenSeparated() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 45);
		Rect r2 = new Rect(isu.new Coord(22.0, 22.0), isu.new Dimension(4.0, 4.0), 30);

		boolean result = r1.intersects(r2);

		System.out.println("\n=== Rect-Rect: rotated but separated ===");
		System.out.println("r1 center = (10.0,10.0), angle = 45");
		System.out.println("r2 center = (22.0,22.0), angle = 30");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void rectRectUsesTorusDistanceBetweenCenters() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect r1 = new Rect(isu.new Coord(1.0, 10.0), isu.new Dimension(4.0, 4.0), 0);
		Rect r2 = new Rect(isu.new Coord(29.0, 10.0), isu.new Dimension(4.0, 4.0), 0);

		boolean a = r1.intersects(r2);
		boolean b = r2.intersects(r1);

		System.out.println("\n=== Rect-Rect: torus left/right ===");
		System.out.println("World width = " + isu.width());
		System.out.println("r1 center = (1.0,10.0)");
		System.out.println("r2 center = (29.0,10.0)");
		System.out.println("Direct dx = " + (r2.center().x() - r1.center().x()));
		System.out.println("Torus vector dx = " + r1.center().mkVectorToward(r2.center()).x());
		System.out.println("Expected intersection through border = true");
		System.out.println("Actual r1 -> r2 = " + a);
		System.out.println("Actual r2 -> r1 = " + b);

		assertTrue(a);
		assertTrue(b);
	}

	@Test
	void rectHitboxWithoutRotationWorks() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 10.0), isu.new Dimension(8.0, 4.0), 0);
		Hitbox h = rect.hitbox();

		System.out.println("\n=== Rect hitbox without rotation ===");
		System.out.println("Rect center = (10.0,10.0), size = (8.0,4.0)");
		System.out.println("Expected xmin = 6.0  | Actual = " + h.xmin());
		System.out.println("Expected xmax = 14.0 | Actual = " + h.xmax());
		System.out.println("Expected ymin = 8.0  | Actual = " + h.ymin());
		System.out.println("Expected ymax = 12.0 | Actual = " + h.ymax());

		assertEquals(6.0, h.xmin(), EPS);
		assertEquals(14.0, h.xmax(), EPS);
		assertEquals(8.0, h.ymin(), EPS);
		assertEquals(12.0, h.ymax(), EPS);
	}

	@Test
	void rectCircleUsesTorusShortestPath() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(29.0, 10.0), isu.new Dimension(4.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(1.0, 10.0), 1.0);

		boolean a = rect.intersects(circle);
		boolean b = circle.intersects(rect);

		System.out.println("\n=== Rect-Circle: torus shortest path ===");
		System.out.println("World width = " + isu.width());
		System.out.println("Rect center = (29.0,10.0)");
		System.out.println("Circle center = (1.0,10.0)");
		System.out.println("Direct dx = " + (circle.center().x() - rect.center().x()));
		System.out.println("Torus vector dx = " + rect.center().mkVectorToward(circle.center()).x());
		System.out.println("Expected intersection through border = true");
		System.out.println("Actual rect -> circle = " + a);
		System.out.println("Actual circle -> rect = " + b);

		assertTrue(a);
		assertTrue(b);
	}

	@Test
	void rectCircleUsesTorusTopBottomWithPrints() {
		Game game = new TestGame();
		ISU isu = game.getIsu();

		Rect rect = new Rect(isu.new Coord(10.0, 29.0), isu.new Dimension(4.0, 4.0), 0);
		Circle circle = new Circle(isu.new Coord(10.0, 1.0), 1.0);

		boolean a = rect.intersects(circle);
		boolean b = circle.intersects(rect);

		System.out.println("\n=== Rect-Circle: torus top/bottom ===");
		System.out.println("World height = " + isu.height());
		System.out.println("Rect center = (10.0,29.0)");
		System.out.println("Circle center = (10.0,1.0)");
		System.out.println("Direct dy = " + (circle.center().y() - rect.center().y()));
		System.out.println("Torus vector dy = " + rect.center().mkVectorToward(circle.center()).y());
		System.out.println("Expected intersection through border = true");
		System.out.println("Actual rect -> circle = " + a);
		System.out.println("Actual circle -> rect = " + b);

		assertTrue(a);
		assertTrue(b);
	}
}