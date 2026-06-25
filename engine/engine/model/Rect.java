package engine.model;

/**
 * Rectangle de collision pouvant être tourné autour de son centre.
 */
public final class Rect extends Shape {

	private final double halfWidth;
	private final double halfHeight;

	private int angle_degree;

	public Rect(ISU.Coord center, ISU.Dimension size, int angleDegree) {
		super(center);

		assert size.isu() == this.isu;

		checkToricDimensions(size.x(), size.y());

		this.halfWidth = size.x() / 2.0;
		this.halfHeight = size.y() / 2.0;
		this.angle_degree = normalizeAngle(angleDegree);
	}

	public double halfWidth() {
		return this.halfWidth;
	}

	public double halfHeight() {
		return this.halfHeight;
	}

	public int angle() {
		return this.angle_degree;
	}

	public void rotate(int angleDegree) {
		this.angle_degree = normalizeAngle(this.angle_degree + angleDegree);
	}

	@Override
	public boolean intersects(IShape shape) {
		assert shape.isu() == this.isu;

		return shape.intersects(this);
	}

	@Override
	public boolean intersects(Circle circle) {
		assert circle.isu() == this.isu;

		return new RectCircleIntersection(this, circle).intersects();
	}

	@Override
	public boolean intersects(Rect rect) {
		assert rect.isu() == this.isu;

		return new RectRectIntersection(this, rect).intersects();
	}

	@Override
	public Hitbox hitbox() {
		Vec2[] corners = cornersOf(this, 0.0, 0.0);

		double xmin = Double.POSITIVE_INFINITY;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;

		for (Vec2 corner : corners) {
			double x = this.center.x() + corner.x;
			double y = this.center.y() + corner.y;

			xmin = Math.min(xmin, x);
			xmax = Math.max(xmax, x);
			ymin = Math.min(ymin, y);
			ymax = Math.max(ymax, y);
		}

		return new Hitbox(xmin, xmax, ymin, ymax);
	}

	private static final class RectCircleIntersection {

		private final Rect rect;
		private final Circle circle;

		RectCircleIntersection(Rect rect, Circle circle) {
			this.rect = rect;
			this.circle = circle;
		}

		boolean intersects() {
			Vec2 localCircle = circleCenterInRectFrame();

			double closestX = clamp(localCircle.x, -this.rect.halfWidth, this.rect.halfWidth);
			double closestY = clamp(localCircle.y, -this.rect.halfHeight, this.rect.halfHeight);

			double dx = localCircle.x - closestX;
			double dy = localCircle.y - closestY;

			double radius = this.circle.radius();

			return dx * dx + dy * dy <= radius * radius;
		}

		private Vec2 circleCenterInRectFrame() {
			ISU.Vector delta = this.rect.center.mkVectorToward(this.circle.center());

			double angle = Math.toRadians(-this.rect.angle_degree);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double x = delta.x() * cos - delta.y() * sin;
			double y = delta.x() * sin + delta.y() * cos;

			return new Vec2(x, y);
		}

		private double clamp(double value, double min, double max) {
			if (value < min) {
				return min;
			}

			if (value > max) {
				return max;
			}

			return value;
		}
	}

	private static final class RectRectIntersection {

		private final Rect first;
		private final Rect second;

		RectRectIntersection(Rect first, Rect second) {
			this.first = first;
			this.second = second;
		}

		boolean intersects() {
			ISU.Vector centerDelta = this.first.center.mkVectorToward(this.second.center);

			Vec2[] firstCorners = cornersOf(this.first, 0.0, 0.0);
			Vec2[] secondCorners = cornersOf(this.second, centerDelta.x(), centerDelta.y());

			Vec2[] firstAxes = axesOf(this.first);
			Vec2[] secondAxes = axesOf(this.second);

			Vec2[] axes = new Vec2[] { firstAxes[0], firstAxes[1], secondAxes[0], secondAxes[1] };

			for (Vec2 axis : axes) {
				Interval firstProjection = project(firstCorners, axis);
				Interval secondProjection = project(secondCorners, axis);

				if (!firstProjection.overlaps(secondProjection)) {
					return false;
				}
			}

			return true;
		}

		private Interval project(Vec2[] corners, Vec2 axis) {
			double min = corners[0].dot(axis);
			double max = min;

			for (int i = 1; i < corners.length; i++) {
				double value = corners[i].dot(axis);

				min = Math.min(min, value);
				max = Math.max(max, value);
			}

			return new Interval(min, max);
		}
	}

	private static final class Vec2 {

		final double x;
		final double y;

		Vec2(double x, double y) {
			this.x = x;
			this.y = y;
		}

		double dot(Vec2 other) {
			return this.x * other.x + this.y * other.y;
		}
	}

	private static final class Interval {

		final double min;
		final double max;

		Interval(double min, double max) {
			this.min = min;
			this.max = max;
		}

		boolean overlaps(Interval other) {
			return this.max >= other.min && other.max >= this.min;
		}
	}

	private static Vec2[] axesOf(Rect rect) {
		double angle = Math.toRadians(rect.angle_degree);

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		return new Vec2[] { new Vec2(cos, sin), new Vec2(-sin, cos) };
	}

	private static Vec2[] cornersOf(Rect rect, double centerX, double centerY) {
		Vec2[] axes = axesOf(rect);

		Vec2 xAxis = axes[0];
		Vec2 yAxis = axes[1];

		double hw = rect.halfWidth;
		double hh = rect.halfHeight;

		return new Vec2[] { new Vec2(centerX + xAxis.x * hw + yAxis.x * hh, centerY + xAxis.y * hw + yAxis.y * hh),
				new Vec2(centerX - xAxis.x * hw + yAxis.x * hh, centerY - xAxis.y * hw + yAxis.y * hh),
				new Vec2(centerX - xAxis.x * hw - yAxis.x * hh, centerY - xAxis.y * hw - yAxis.y * hh),
				new Vec2(centerX + xAxis.x * hw - yAxis.x * hh, centerY + xAxis.y * hw - yAxis.y * hh) };
	}

	private static int normalizeAngle(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}
}