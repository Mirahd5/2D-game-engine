package engine.intersection;


class RectCircleIntersection {

	// FIELDS

	private Rect outer;
	private Point center;
	private double radius;
	private double rectDx;
	private double rectDy;

	// CONSTRUCTOR

	RectCircleIntersection(Rect outer, Point center, double radius, double rectDx, double rectDy) {
		this.outer = outer;
		this.center = center;
		this.radius = radius;
		this.rectDx = rectDx;
		this.rectDy = rectDy;

		remedy();
	}
	// REMEDY means `set right an undesirable situation`

	void remedy() {
		Point rectCenter = new Point(outer.center.x() + rectDx, outer.center.y() + rectDy);
		Vector fromRectToCircle = rectCenter.vectorToward(center);
		Vector local = fromRectToCircle.turned(-outer.angle_degree);
		center = new Point(local.x(), local.y());
	}
	
	// INTERSECTION in the easy case
	boolean intersects() {
		Point p = closestRectpoint();
		double d = p.vectorToward(center).norm();
		return d <= radius;
	}

	Point closestRectpoint() {
		double x = clamp(center.x(), -outer.halfWidth, outer.halfWidth);
		double y = clamp(center.y(), -outer.halfHeight, outer.halfHeight);
		return new Point(x, y);
	}

	double clamp(double p, double l, double r) {
		if (p < l) {
			return l;
		}
		if (r < p) {
			return r;
		}
		return p;
	}

}