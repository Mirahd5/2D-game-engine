package engine.intersection;

public class Point {

	private double x, y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public Point translated(Vector v) {
		return new Point(x + v.x(), y + v.y());
	}

	public Vector vectorToward(Point p) {
		return new Vector(p.x() - x, p.y() - y);
	}

	public void rotateAround(Point center, double angle_degree) {

		double angle = Math.toRadians(angle_degree);

		double dx = x - center.x();
		double dy = y - center.y();

		double newX = center.x() + dx * Math.cos(angle) - dy * Math.sin(angle);
		double newY = center.y() + dx * Math.sin(angle) + dy * Math.cos(angle);

		x = newX;
		y = newY;
	}
}