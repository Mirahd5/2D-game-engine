// = Circle =
package engine.intersection;

import engine.model.ISU;

public class Circle extends Shape {

	double radius;

	// CONSTRUCTOR

	public Circle(ISU.Coord center, double radius) {
		super(center);
		this.radius = radius;
	}

	// INTERSECTION
	public boolean intersects(Rect rect) {
		return rect.intersects(this);
	}

	public boolean intersects(Circle circle) {
		double d = circle.center.distanceTo(this.center);
		return d <= this.radius + circle.radius;
	}

	@Override
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	// BOX
	@Override
	public Box box() {
		double cx = center.x();
		double cy = center.y();
		return new Box(isu, cx - radius, cy - radius, cx + radius, cy + radius);
	}

}
