// = AXIS =
package engine.model;

public class Axis {

	// FIELDS
	private final boolean onTorus;
	private final double perimeter;
	private final double halfPerimeter;

	// CONSTRUCTOR

	public Axis(boolean onTorus, double perimeter) {
		assert (perimeter > 0);
		this.onTorus = onTorus;
		this.perimeter = perimeter;
		this.halfPerimeter = perimeter / 2.0;
	}

	// NORMALIZE INTEGER POSITION
	public int normalize(int length) {
		if (!this.onTorus) {
			return length;
		}
		return modp(length, (int) this.perimeter);
	}

	public int modp(int length, int perimeter) {
		assert (perimeter > 0);
		int r = length % perimeter;
		if (r < 0) {
			r += perimeter;
		}
		assert (r >= 0);
		assert (r < perimeter);
		return r;
	}

	// NORMALIZE REAL POSITION

	public double normalize(double length) {

		if (!this.onTorus) {
			return length;
		}
		return modp(length, this.perimeter);
	}

	public double modp(double length, double perimeter) {
		assert (perimeter > 0.0);
		double r = length % perimeter;
		if (r < 0.0) {
			r += perimeter;
		}
		if (r >= perimeter || Math.abs(r - perimeter) < 1e-9) {
			r = 0.0;
		}

		if (r < 0.0 && Math.abs(r) < 1e-9) {
			r = 0.0;
		}
		assert (r >= 0.0);
		assert (r < perimeter);
		return r;
	}

	// NORMALIZE REAL DELTA
	public double normalizeDelta(double length) {

		if (!this.onTorus) {
			return length;
		}
		double r = modp(length, this.perimeter);
		if (r >= this.halfPerimeter) {
			r -= this.perimeter;
		}

		return r;
	}

	// DISTANCE

	public double distance(double position1, double position2) {
		double d = position2 - position1;
		if (!this.onTorus) {
			return Math.abs(d);
		}
		return Math.abs(normalizeDelta(d));
	}

	// GETTERS

	public boolean onTorus() {
		return this.onTorus;
	}

	public double perimeter() {
		return this.perimeter;
	}

	public double halfPerimeter() {
		return this.halfPerimeter;
	}

	// UTILITY FOR BOX / TORUS CASES

	public double euclideanMax(double min, double max) {
		if (this.onTorus && max < min) {
			return max + this.perimeter;
		}
		return max;
	}
}