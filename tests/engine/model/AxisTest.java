package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AxisTest {

	private static final double EPS = 0.000001;

	// =========================
	// NORMALIZE INT - TORUS
	// =========================

	@Test
	void normalizeIntegerOnTorus() {
		Axis axis = new Axis(true, 5);

		System.out.println("\n=== Axis normalize(int) on torus ===");
		System.out.println("Situation : torus axis with perimeter = 5");
		System.out.println("Expected normalize(0)  = 0 | Actual = " + axis.normalize(0));
		System.out.println("Expected normalize(1)  = 1 | Actual = " + axis.normalize(1));
		System.out.println("Expected normalize(4)  = 4 | Actual = " + axis.normalize(4));
		System.out.println("Expected normalize(5)  = 0 | Actual = " + axis.normalize(5));
		System.out.println("Expected normalize(6)  = 1 | Actual = " + axis.normalize(6));
		System.out.println("Expected normalize(-1) = 4 | Actual = " + axis.normalize(-1));
		System.out.println("Expected normalize(-7) = 3 | Actual = " + axis.normalize(-7));

		assertEquals(0, axis.normalize(0));
		assertEquals(1, axis.normalize(1));
		assertEquals(4, axis.normalize(4));
		assertEquals(0, axis.normalize(5));
		assertEquals(1, axis.normalize(6));
		assertEquals(4, axis.normalize(-1));
		assertEquals(3, axis.normalize(-7));
	}

	@Test
	void normalizeIntegerWithoutTorus() {
		Axis axis = new Axis(false, 5);

		System.out.println("\n=== Axis normalize(int) without torus ===");
		System.out.println("Situation : non-torus axis with perimeter = 5");
		System.out.println("No wrapping should happen.");
		System.out.println("Expected normalize(0)  = 0  | Actual = " + axis.normalize(0));
		System.out.println("Expected normalize(5)  = 5  | Actual = " + axis.normalize(5));
		System.out.println("Expected normalize(6)  = 6  | Actual = " + axis.normalize(6));
		System.out.println("Expected normalize(-1) = -1 | Actual = " + axis.normalize(-1));
		System.out.println("Expected normalize(-7) = -7 | Actual = " + axis.normalize(-7));

		assertEquals(0, axis.normalize(0));
		assertEquals(5, axis.normalize(5));
		assertEquals(6, axis.normalize(6));
		assertEquals(-1, axis.normalize(-1));
		assertEquals(-7, axis.normalize(-7));
	}

	// =========================
	// MODP INT
	// =========================

	@Test
	void modpIntegerWorksWithPositiveValues() {
		Axis axis = new Axis(true, 5);

		System.out.println("\n=== Axis modp(int) with positive values ===");
		System.out.println("Situation : perimeter = 5");
		System.out.println("Expected modp(0, 5) = 0 | Actual = " + axis.modp(0, 5));
		System.out.println("Expected modp(1, 5) = 1 | Actual = " + axis.modp(1, 5));
		System.out.println("Expected modp(4, 5) = 4 | Actual = " + axis.modp(4, 5));
		System.out.println("Expected modp(5, 5) = 0 | Actual = " + axis.modp(5, 5));
		System.out.println("Expected modp(6, 5) = 1 | Actual = " + axis.modp(6, 5));

		assertEquals(0, axis.modp(0, 5));
		assertEquals(1, axis.modp(1, 5));
		assertEquals(4, axis.modp(4, 5));
		assertEquals(0, axis.modp(5, 5));
		assertEquals(1, axis.modp(6, 5));
	}

	@Test
	void modpIntegerWorksWithNegativeValues() {
		Axis axis = new Axis(true, 5);

		System.out.println("\n=== Axis modp(int) with negative values ===");
		System.out.println("Situation : perimeter = 5");
		System.out.println("Expected modp(-1, 5) = 4 | Actual = " + axis.modp(-1, 5));
		System.out.println("Expected modp(-2, 5) = 3 | Actual = " + axis.modp(-2, 5));
		System.out.println("Expected modp(-5, 5) = 0 | Actual = " + axis.modp(-5, 5));
		System.out.println("Expected modp(-6, 5) = 4 | Actual = " + axis.modp(-6, 5));

		assertEquals(4, axis.modp(-1, 5));
		assertEquals(3, axis.modp(-2, 5));
		assertEquals(0, axis.modp(-5, 5));
		assertEquals(4, axis.modp(-6, 5));
	}

	// =========================
	// NORMALIZE DOUBLE
	// =========================

	@Test
	void normalizeDoubleOnTorus() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis normalize(double) on torus ===");
		System.out.println("Situation : torus axis with perimeter = 10.0");
		System.out.println("Expected normalize(0.0)  = 0.0 | Actual = " + axis.normalize(0.0));
		System.out.println("Expected normalize(4.0)  = 4.0 | Actual = " + axis.normalize(4.0));
		System.out.println("Expected normalize(5.0)  = 5.0 | Actual = " + axis.normalize(5.0));
		System.out.println("Expected normalize(6.0)  = 6.0 | Actual = " + axis.normalize(6.0));
		System.out.println("Expected normalize(10.0) = 0.0 | Actual = " + axis.normalize(10.0));
		System.out.println("Expected normalize(11.5) = 1.5 | Actual = " + axis.normalize(11.5));
		System.out.println("Expected normalize(-6.0) = 4.0 | Actual = " + axis.normalize(-6.0));
		System.out.println("Expected normalize(-1.0) = 9.0 | Actual = " + axis.normalize(-1.0));

		assertEquals(0.0, axis.normalize(0.0), EPS);
		assertEquals(4.0, axis.normalize(4.0), EPS);
		assertEquals(5.0, axis.normalize(5.0), EPS);
		assertEquals(6.0, axis.normalize(6.0), EPS);
		assertEquals(0.0, axis.normalize(10.0), EPS);
		assertEquals(1.5, axis.normalize(11.5), EPS);
		assertEquals(4.0, axis.normalize(-6.0), EPS);
		assertEquals(9.0, axis.normalize(-1.0), EPS);
	}

	@Test
	void normalizeDoubleWithoutTorus() {
		Axis axis = new Axis(false, 10.0);

		System.out.println("\n=== Axis normalize(double) without torus ===");
		System.out.println("Situation : non-torus axis with perimeter = 10.0");
		System.out.println("No wrapping should happen.");
		System.out.println("Expected normalize(0.0)  = 0.0  | Actual = " + axis.normalize(0.0));
		System.out.println("Expected normalize(10.0) = 10.0 | Actual = " + axis.normalize(10.0));
		System.out.println("Expected normalize(11.5) = 11.5 | Actual = " + axis.normalize(11.5));
		System.out.println("Expected normalize(-1.0) = -1.0 | Actual = " + axis.normalize(-1.0));
		System.out.println("Expected normalize(-6.0) = -6.0 | Actual = " + axis.normalize(-6.0));

		assertEquals(0.0, axis.normalize(0.0), EPS);
		assertEquals(10.0, axis.normalize(10.0), EPS);
		assertEquals(11.5, axis.normalize(11.5), EPS);
		assertEquals(-1.0, axis.normalize(-1.0), EPS);
		assertEquals(-6.0, axis.normalize(-6.0), EPS);
	}

	// =========================
	// MODP DOUBLE
	// =========================

	@Test
	void modpDoubleWorksWithPositiveValues() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis modp(double) with positive values ===");
		System.out.println("Situation : perimeter = 10.0");
		System.out.println("Expected modp(0.0, 10.0)  = 0.0 | Actual = " + axis.modp(0.0, 10.0));
		System.out.println("Expected modp(4.0, 10.0)  = 4.0 | Actual = " + axis.modp(4.0, 10.0));
		System.out.println("Expected modp(10.0, 10.0) = 0.0 | Actual = " + axis.modp(10.0, 10.0));
		System.out.println("Expected modp(11.5, 10.0) = 1.5 | Actual = " + axis.modp(11.5, 10.0));

		assertEquals(0.0, axis.modp(0.0, 10.0), EPS);
		assertEquals(4.0, axis.modp(4.0, 10.0), EPS);
		assertEquals(0.0, axis.modp(10.0, 10.0), EPS);
		assertEquals(1.5, axis.modp(11.5, 10.0), EPS);
	}

	@Test
	void modpDoubleWorksWithNegativeValues() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis modp(double) with negative values ===");
		System.out.println("Situation : perimeter = 10.0");
		System.out.println("Expected modp(-1.0, 10.0)  = 9.0 | Actual = " + axis.modp(-1.0, 10.0));
		System.out.println("Expected modp(-6.0, 10.0)  = 4.0 | Actual = " + axis.modp(-6.0, 10.0));
		System.out.println("Expected modp(-10.0, 10.0) = 0.0 | Actual = " + axis.modp(-10.0, 10.0));
		System.out.println("Expected modp(-11.5, 10.0) = 8.5 | Actual = " + axis.modp(-11.5, 10.0));

		assertEquals(9.0, axis.modp(-1.0, 10.0), EPS);
		assertEquals(4.0, axis.modp(-6.0, 10.0), EPS);
		assertEquals(0.0, axis.modp(-10.0, 10.0), EPS);
		assertEquals(8.5, axis.modp(-11.5, 10.0), EPS);
	}

	// =========================
	// NORMALIZE DIMENSION INT
	// =========================

	@Test
	void normalizeIntegerDimensionOnTorus() {
		Axis axis = new Axis(true, 10);

		System.out.println("\n=== Axis normalizeDimension(int) on torus ===");
		System.out.println("Situation : dimension must stay in [1, perimeter]");
		System.out.println("Expected normalizeDimension(1)  = 1  | Actual = " + axis.normalizeDimension(1));
		System.out.println("Expected normalizeDimension(5)  = 5  | Actual = " + axis.normalizeDimension(5));
		System.out.println("Expected normalizeDimension(10) = 10 | Actual = " + axis.normalizeDimension(10));
		System.out.println("Expected normalizeDimension(11) = 1  | Actual = " + axis.normalizeDimension(11));
		System.out.println("Expected normalizeDimension(12) = 2  | Actual = " + axis.normalizeDimension(12));
		System.out.println("Expected normalizeDimension(20) = 10 | Actual = " + axis.normalizeDimension(20));

		assertEquals(1, axis.normalizeDimension(1));
		assertEquals(5, axis.normalizeDimension(5));
		assertEquals(10, axis.normalizeDimension(10));
		assertEquals(1, axis.normalizeDimension(11));
		assertEquals(2, axis.normalizeDimension(12));
		assertEquals(10, axis.normalizeDimension(20));
	}

	@Test
	void normalizeIntegerDimensionWithoutTorus() {
		Axis axis = new Axis(false, 10);

		System.out.println("\n=== Axis normalizeDimension(int) without torus ===");
		System.out.println("Situation : non-torus dimension should not wrap");
		System.out.println("Expected normalizeDimension(1)  = 1  | Actual = " + axis.normalizeDimension(1));
		System.out.println("Expected normalizeDimension(10) = 10 | Actual = " + axis.normalizeDimension(10));
		System.out.println("Expected normalizeDimension(12) = 12 | Actual = " + axis.normalizeDimension(12));
		System.out.println("Expected normalizeDimension(20) = 20 | Actual = " + axis.normalizeDimension(20));

		assertEquals(1, axis.normalizeDimension(1));
		assertEquals(10, axis.normalizeDimension(10));
		assertEquals(12, axis.normalizeDimension(12));
		assertEquals(20, axis.normalizeDimension(20));
	}

	// =========================
	// NORMALIZE DIMENSION DOUBLE
	// =========================

	@Test
	void normalizeDoubleDimensionOnTorus() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis normalizeDimension(double) on torus ===");
		System.out.println("Situation : dimension must stay in (0, perimeter]");
		System.out.println("Expected normalizeDimension(1.0)  = 1.0  | Actual = " + axis.normalizeDimension(1.0));
		System.out.println("Expected normalizeDimension(5.0)  = 5.0  | Actual = " + axis.normalizeDimension(5.0));
		System.out.println("Expected normalizeDimension(10.0) = 10.0 | Actual = " + axis.normalizeDimension(10.0));
		System.out.println("Expected normalizeDimension(11.5) = 1.5  | Actual = " + axis.normalizeDimension(11.5));
		System.out.println("Expected normalizeDimension(12.0) = 2.0  | Actual = " + axis.normalizeDimension(12.0));
		System.out.println("Expected normalizeDimension(20.0) = 10.0 | Actual = " + axis.normalizeDimension(20.0));

		assertEquals(1.0, axis.normalizeDimension(1.0), EPS);
		assertEquals(5.0, axis.normalizeDimension(5.0), EPS);
		assertEquals(10.0, axis.normalizeDimension(10.0), EPS);
		assertEquals(1.5, axis.normalizeDimension(11.5), EPS);
		assertEquals(2.0, axis.normalizeDimension(12.0), EPS);
		assertEquals(10.0, axis.normalizeDimension(20.0), EPS);
	}

	@Test
	void normalizeDoubleDimensionWithoutTorus() {
		Axis axis = new Axis(false, 10.0);

		System.out.println("\n=== Axis normalizeDimension(double) without torus ===");
		System.out.println("Situation : non-torus dimension should not wrap");
		System.out.println("Expected normalizeDimension(1.0)  = 1.0  | Actual = " + axis.normalizeDimension(1.0));
		System.out.println("Expected normalizeDimension(10.0) = 10.0 | Actual = " + axis.normalizeDimension(10.0));
		System.out.println("Expected normalizeDimension(12.0) = 12.0 | Actual = " + axis.normalizeDimension(12.0));
		System.out.println("Expected normalizeDimension(20.0) = 20.0 | Actual = " + axis.normalizeDimension(20.0));

		assertEquals(1.0, axis.normalizeDimension(1.0), EPS);
		assertEquals(10.0, axis.normalizeDimension(10.0), EPS);
		assertEquals(12.0, axis.normalizeDimension(12.0), EPS);
		assertEquals(20.0, axis.normalizeDimension(20.0), EPS);
	}

	// =========================
	// DISTANCE
	// =========================

	@Test
	void distanceOnTorusTakesShortestPath() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis distance on torus ===");
		System.out.println("Situation : distance uses the shortest path on the torus");
		System.out.println("Expected distance(1.0, 1.0) = 0.0 | Actual = " + axis.distance(1.0, 1.0));
		System.out.println("Expected distance(1.0, 9.0) = 2.0 | Actual = " + axis.distance(1.0, 9.0));
		System.out.println("Expected distance(9.0, 1.0) = 2.0 | Actual = " + axis.distance(9.0, 1.0));
		System.out.println("Expected distance(1.0, 5.0) = 4.0 | Actual = " + axis.distance(1.0, 5.0));
		System.out.println("Expected distance(0.0, 5.0) = 5.0 | Actual = " + axis.distance(0.0, 5.0));

		assertEquals(0.0, axis.distance(1.0, 1.0), EPS);
		assertEquals(2.0, axis.distance(1.0, 9.0), EPS);
		assertEquals(2.0, axis.distance(9.0, 1.0), EPS);
		assertEquals(4.0, axis.distance(1.0, 5.0), EPS);
		assertEquals(5.0, axis.distance(0.0, 5.0), EPS);
	}

	@Test
	void distanceWithoutTorusIsDirectDistance() {
		Axis axis = new Axis(false, 10.0);

		System.out.println("\n=== Axis distance without torus ===");
		System.out.println("Situation : distance is the direct absolute difference");
		System.out.println("Expected distance(1.0, 1.0) = 0.0 | Actual = " + axis.distance(1.0, 1.0));
		System.out.println("Expected distance(1.0, 9.0) = 8.0 | Actual = " + axis.distance(1.0, 9.0));
		System.out.println("Expected distance(9.0, 1.0) = 8.0 | Actual = " + axis.distance(9.0, 1.0));
		System.out.println("Expected distance(1.0, 5.0) = 4.0 | Actual = " + axis.distance(1.0, 5.0));

		assertEquals(0.0, axis.distance(1.0, 1.0), EPS);
		assertEquals(8.0, axis.distance(1.0, 9.0), EPS);
		assertEquals(8.0, axis.distance(9.0, 1.0), EPS);
		assertEquals(4.0, axis.distance(1.0, 5.0), EPS);
	}

	// =========================
	// EUCLIDEAN
	// =========================

	@Test
	void euclideanOnTorusDoesNotChangeWhenXmaxAfterXmin() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis euclidean on torus without crossing ===");
		System.out.println("Situation : xmax is already after xmin");
		System.out.println("Expected euclidean(2.0, 8.0) = 8.0 | Actual = " + axis.euclidean(2.0, 8.0));
		System.out.println("Expected euclidean(1.0, 5.0) = 5.0 | Actual = " + axis.euclidean(1.0, 5.0));

		assertEquals(8.0, axis.euclidean(2.0, 8.0), EPS);
		assertEquals(5.0, axis.euclidean(1.0, 5.0), EPS);
	}

	@Test
	void euclideanOnTorusAddsPerimeterWhenXmaxBeforeXmin() {
		Axis axis = new Axis(true, 10.0);

		System.out.println("\n=== Axis euclidean on torus with crossing ===");
		System.out.println("Situation : xmax is before xmin, so we add perimeter");
		System.out.println("Expected euclidean(8.0, 2.0) = 12.0 | Actual = " + axis.euclidean(8.0, 2.0));
		System.out.println("Expected euclidean(9.0, 5.0) = 15.0 | Actual = " + axis.euclidean(9.0, 5.0));

		assertEquals(12.0, axis.euclidean(8.0, 2.0), EPS);
		assertEquals(15.0, axis.euclidean(9.0, 5.0), EPS);
	}

	@Test
	void euclideanWithoutTorusReturnsXmaxDirectly() {
		Axis axis = new Axis(false, 10.0);

		System.out.println("\n=== Axis euclidean without torus ===");
		System.out.println("Situation : no torus, so xmax is returned directly");
		System.out.println("Expected euclidean(8.0, 2.0)  = 2.0  | Actual = " + axis.euclidean(8.0, 2.0));
		System.out.println("Expected euclidean(8.0, 12.0) = 12.0 | Actual = " + axis.euclidean(8.0, 12.0));

		assertEquals(2.0, axis.euclidean(8.0, 2.0), EPS);
		assertEquals(12.0, axis.euclidean(8.0, 12.0), EPS);
	}
}