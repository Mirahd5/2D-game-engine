package engine.model;

/**
 * Représente un axe normal ou torique du monde.
 */
public final class Axis {

	private static final double EPSILON = 1e-9;

	private final boolean onTorus;
	private final double perimeter;

	public Axis(boolean onTorus, double perimeter) {
		assert perimeter > 0.0 : perimeter;

		this.onTorus = onTorus;
		this.perimeter = perimeter;
	}

	public boolean onTorus() {
		return this.onTorus;
	}

	public double perimeter() {
		return this.perimeter;
	}

	public double halfPerimeter() {
		return this.perimeter / 2.0;
	}

	public int normalize(int value) {
		if (!this.onTorus) {
			return value;
		}

		return modp(value, integerPerimeter());
	}

	public double normalize(double value) {
		if (!this.onTorus) {
			return value;
		}

		return modp(value, this.perimeter);
	}

	public int normalizeDimension(int length) {
		assert length > 0 : length;

		int perimeter = integerPerimeter();

		if (!this.onTorus) {
			assert length <= perimeter : "dimension larger than axis";

			return length;
		}

		if (length <= perimeter) {
			return length;
		}

		int normalized = modp(length, perimeter);

		if (normalized == 0) {
			return perimeter;
		}

		return normalized;
	}

	public double normalizeDimension(double length) {
		assert length > 0.0 : length;

		if (!this.onTorus) {
			assert length <= this.perimeter : "dimension larger than axis";

			return length;
		}

		if (length <= this.perimeter) {
			return length;
		}

		double normalized = modp(length, this.perimeter);

		if (Math.abs(normalized) <= EPSILON) {
			return this.perimeter;
		}

		return normalized;
	}

	public int modp(int value, int perimeter) {
		assert perimeter > 0 : perimeter;

		int result = value % perimeter;

		if (result < 0) {
			result += perimeter;
		}

		return result;
	}

	public double modp(double value, double perimeter) {
		assert perimeter > 0.0 : perimeter;

		double result = value % perimeter;

		if (result < 0.0) {
			result += perimeter;
		}

		return result;
	}

	public int delta(int from, int to) {
		int delta = to - from;

		if (!this.onTorus) {
			return delta;
		}

		int perimeter = integerPerimeter();

		delta = modp(delta, perimeter);

		if (delta > perimeter / 2) {
			delta -= perimeter;
		}

		return delta;
	}

	public double delta(double from, double to) {
		double delta = to - from;

		if (!this.onTorus) {
			return delta;
		}

		delta = modp(delta, this.perimeter);

		if (delta > this.halfPerimeter()) {
			delta -= this.perimeter;
		}

		return delta;
	}

	public double distance(double position1, double position2) {
		return Math.abs(delta(position1, position2));
	}

	private int integerPerimeter() {
		double rounded = Math.rint(this.perimeter);

		assert Math.abs(this.perimeter - rounded) <= EPSILON : "integer normalization requires an integer perimeter";

		return (int) rounded;
	}
}