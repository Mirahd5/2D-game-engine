package engine.intersection;

public class Vector {

	private double x, y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public void add(Vector v) {
		x += v.x();
		y += v.y();
	}

	public Vector scaled(double factor) {
		return new Vector(x * factor, y * factor);
	}

	public double dot(Vector v) {
		return x * v.x() + y * v.y();
	}

	public double norm() {
		return Math.sqrt(x * x + y * y);
	}

	public void unity() {
		double n = norm();
		assert (n != 0);
		x = x / n;
		y = y / n;
	}

	public Vector turned(double angle_degree) {
		double angle = Math.toRadians(angle_degree);

		double newX = x * Math.cos(angle) - y * Math.sin(angle);
		double newY = x * Math.sin(angle) + y * Math.cos(angle);

		return new Vector(newX, newY);
	}
}