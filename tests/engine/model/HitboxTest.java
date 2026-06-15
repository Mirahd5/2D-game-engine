package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HitboxTest {

	private static final double EPS = 0.000001;

	@Test
	void hitboxStoresBounds() {
		Hitbox h = new Hitbox(1.0, 5.0, 2.0, 6.0);

		System.out.println("\n=== Hitbox constructor ===");
		System.out.println("Expected xmin = 1.0 | Actual = " + h.xmin());
		System.out.println("Expected xmax = 5.0 | Actual = " + h.xmax());
		System.out.println("Expected ymin = 2.0 | Actual = " + h.ymin());
		System.out.println("Expected ymax = 6.0 | Actual = " + h.ymax());

		assertEquals(1.0, h.xmin(), EPS);
		assertEquals(5.0, h.xmax(), EPS);
		assertEquals(2.0, h.ymin(), EPS);
		assertEquals(6.0, h.ymax(), EPS);
	}

	@Test
	void hitboxIntersectsWhenOverlapping() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(3.0, 8.0, 3.0, 8.0);

		boolean result1 = h1.intersects(h2);
		boolean result2 = h2.intersects(h1);

		System.out.println("\n=== Hitbox overlapping ===");
		System.out.println("h1 = [0,5] x [0,5]");
		System.out.println("h2 = [3,8] x [3,8]");
		System.out.println("Expected intersection = true");
		System.out.println("Actual h1 -> h2 = " + result1);
		System.out.println("Actual h2 -> h1 = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void hitboxIntersectsWhenTouchingOnX() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(5.0, 10.0, 0.0, 5.0);

		boolean result1 = h1.intersects(h2);
		boolean result2 = h2.intersects(h1);

		System.out.println("\n=== Hitbox touching on X ===");
		System.out.println("h1 xmax = 5.0");
		System.out.println("h2 xmin = 5.0");
		System.out.println("Expected intersection = true because touching counts");
		System.out.println("Actual h1 -> h2 = " + result1);
		System.out.println("Actual h2 -> h1 = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void hitboxIntersectsWhenTouchingOnY() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(0.0, 5.0, 5.0, 10.0);

		boolean result1 = h1.intersects(h2);
		boolean result2 = h2.intersects(h1);

		System.out.println("\n=== Hitbox touching on Y ===");
		System.out.println("h1 ymax = 5.0");
		System.out.println("h2 ymin = 5.0");
		System.out.println("Expected intersection = true because touching counts");
		System.out.println("Actual h1 -> h2 = " + result1);
		System.out.println("Actual h2 -> h1 = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void hitboxDoesNotIntersectWhenThisIsLeftOfOther() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(6.0, 10.0, 0.0, 5.0);

		boolean result = h1.intersects(h2);

		System.out.println("\n=== Hitbox separated: this is left of other ===");
		System.out.println("h1 xmax = 5.0");
		System.out.println("h2 xmin = 6.0");
		System.out.println("Condition covered: this.xmax < h.xmin");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void hitboxDoesNotIntersectWhenThisIsRightOfOther() {
		Hitbox h1 = new Hitbox(6.0, 10.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(0.0, 5.0, 0.0, 5.0);

		boolean result = h1.intersects(h2);

		System.out.println("\n=== Hitbox separated: this is right of other ===");
		System.out.println("h1 xmin = 6.0");
		System.out.println("h2 xmax = 5.0");
		System.out.println("Condition covered: h.xmax < this.xmin");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void hitboxDoesNotIntersectWhenThisIsBelowOther() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(0.0, 5.0, 6.0, 10.0);

		boolean result = h1.intersects(h2);

		System.out.println("\n=== Hitbox separated: this is below other ===");
		System.out.println("h1 ymax = 5.0");
		System.out.println("h2 ymin = 6.0");
		System.out.println("Condition covered: this.ymax < h.ymin");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void hitboxDoesNotIntersectWhenThisIsAboveOther() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 6.0, 10.0);
		Hitbox h2 = new Hitbox(0.0, 5.0, 0.0, 5.0);

		boolean result = h1.intersects(h2);

		System.out.println("\n=== Hitbox separated: this is above other ===");
		System.out.println("h1 ymin = 6.0");
		System.out.println("h2 ymax = 5.0");
		System.out.println("Condition covered: h.ymax < this.ymin");
		System.out.println("Expected intersection = false");
		System.out.println("Actual intersection   = " + result);

		assertFalse(result);
	}

	@Test
	void hitboxIntersectsWhenOneContainsTheOther() {
		Hitbox big = new Hitbox(0.0, 10.0, 0.0, 10.0);
		Hitbox small = new Hitbox(3.0, 4.0, 3.0, 4.0);

		boolean result1 = big.intersects(small);
		boolean result2 = small.intersects(big);

		System.out.println("\n=== Hitbox containment ===");
		System.out.println("Big hitbox   = [0,10] x [0,10]");
		System.out.println("Small hitbox = [3,4] x [3,4]");
		System.out.println("Expected intersection = true");
		System.out.println("Actual big -> small = " + result1);
		System.out.println("Actual small -> big = " + result2);

		assertTrue(result1);
		assertTrue(result2);
	}

	@Test
	void hitboxUnionWorks() {
		Hitbox h1 = new Hitbox(0.0, 5.0, 0.0, 5.0);
		Hitbox h2 = new Hitbox(3.0, 10.0, -2.0, 4.0);

		Hitbox union = h1.union(h2);

		System.out.println("\n=== Hitbox union ===");
		System.out.println("h1 = [0,5] x [0,5]");
		System.out.println("h2 = [3,10] x [-2,4]");
		System.out.println("Expected union xmin = 0.0  | Actual = " + union.xmin());
		System.out.println("Expected union xmax = 10.0 | Actual = " + union.xmax());
		System.out.println("Expected union ymin = -2.0 | Actual = " + union.ymin());
		System.out.println("Expected union ymax = 5.0  | Actual = " + union.ymax());

		assertEquals(0.0, union.xmin(), EPS);
		assertEquals(10.0, union.xmax(), EPS);
		assertEquals(-2.0, union.ymin(), EPS);
		assertEquals(5.0, union.ymax(), EPS);
	}

	@Test
	void hitboxUnionWithContainedHitboxWorks() {
		Hitbox big = new Hitbox(0.0, 10.0, 0.0, 10.0);
		Hitbox small = new Hitbox(3.0, 4.0, 3.0, 4.0);

		Hitbox union = big.union(small);

		System.out.println("\n=== Hitbox union with contained hitbox ===");
		System.out.println("Big hitbox contains small hitbox");
		System.out.println("Expected union = big hitbox");
		System.out.println("Actual union = [" + union.xmin() + "," + union.xmax() + "] x [" + union.ymin() + "," + union.ymax() + "]");

		assertEquals(0.0, union.xmin(), EPS);
		assertEquals(10.0, union.xmax(), EPS);
		assertEquals(0.0, union.ymin(), EPS);
		assertEquals(10.0, union.ymax(), EPS);
	}
}
