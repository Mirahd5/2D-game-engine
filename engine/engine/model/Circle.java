package engine.model;

/**
 * Cercle utilisé comme forme de collision.
 */
public final class Circle extends Shape {

	private final double radius;

	public Circle(ISU.Coord center, double radius) {
		super(center);

		assert radius > 0.0 : radius;

		double diameter = 2.0 * radius;
		checkToricDimensions(diameter, diameter);

		this.radius = radius;
	}

	public double radius() {
		return this.radius;
	}

	@Override
	public boolean intersects(IShape shape) {
		assert shape.isu() == this.isu;

		return shape.intersects(this);
	}

	@Override
	public boolean intersects(Circle circle) {
		assert circle.isu() == this.isu;

		double distance = this.center.distanceTo(circle.center());
		double radiusSum = this.radius + circle.radius();

		return distance <= radiusSum;
	}

	@Override
	public boolean intersects(Rect rect) {
		assert rect.isu() == this.isu;

		return rect.intersects(this);
	}

	@Override
	public Hitbox hitbox() {
		double xmin = this.center.x() - this.radius;
		double xmax = this.center.x() + this.radius;
		double ymin = this.center.y() - this.radius;
		double ymax = this.center.y() + this.radius;

		return new Hitbox(xmin, xmax, ymin, ymax);
	}
}