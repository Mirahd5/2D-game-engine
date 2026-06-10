// = Rect =
package engine.intersection;

import engine.model.ISU;
import engine.model.ISU.Coord;


public class Rect extends Shape {

	// FIELDS

	protected double halfWidth, halfHeight;
	protected int angle_degree;
	private double width;
	private double height;

	// CONSTRUCTOR

	public Rect(ISU.Coord center, ISU.Dimension size, int angle_degree) {
		super(center);
		this.angle_degree = angle_degree;
		this.halfWidth = size.x() / 2;
		this.halfHeight = size.y() / 2;
		this.width = isu.width_cm();
		this.height = isu.height_cm();
	}

	// GETTERS

	public double angle_degree() {
		return angle_degree;
	}

	public double halfWidth() {
		return halfWidth;
	}

	public double halfHeight() {
		return halfHeight;
	}

	public Coord center() {
		return center;
	}

	// ROTATION

	void rotate(int angle_degree) {
		this.angle_degree += angle_degree;
		this.angle_degree = this.angle_degree % 360;

		if (this.angle_degree < 0) {
			this.angle_degree += 360;
		}
	}

	// == INTERSECTION ==
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	// =================================Rect/CircleIntersection===================================

	public boolean intersects(Circle circle) {
		// on depasse pas et on teste la collisition
		if (interCircleAt(circle, 0, 0, 0, 0)) {
			return true;
		}
		// le cercle depasse on copie
		if (intersectsCircleCopies(circle)) {
			return true;
		}
		// le rect depasse on copie
		if (intersectsRectCopies(circle)) {
			return true;
		}
		return false;
	}

	private boolean intersectsCircleCopies(Circle circle) {
		boolean g = circle.center.x() - circle.radius < 0;
		boolean d = circle.center.x() + circle.radius > isu.width_cm();
		boolean h = circle.center.y() - circle.radius < 0;
		boolean b = circle.center.y() + circle.radius > isu.height_cm();

		// on depasse a gauche on test collision avec la copie a droite
		if (g && interCircleAt(circle, width, 0, 0, 0)) {
			return true;
		}
		// on depasse a droite on test collision avec la copie a gauche
		if (d && interCircleAt(circle, -width, 0, 0, 0)) {
			return true;
		}
		// on depasse en haut on test collision avec la copie a en bas
		if (h && interCircleAt(circle, 0, height, 0, 0)) {
			return true;
		}
		// on depasse en bas on test collision avec la copie en haut
		if (b && interCircleAt(circle, 0, -height, 0, 0)) {
			return true;
		}
		// coin depassement en haut et a gauche
		if (g && h && interCircleAt(circle, width, height, 0, 0)) {
			return true;
		}
		// coin depassement en bas et a gauche
		if (g && b && interCircleAt(circle, width, -height, 0, 0)) {
			return true;
		}
		// coin depassement en haut et a droite
		if (d && h && interCircleAt(circle, -width, height, 0, 0)) {
			return true;
		}
		// coin deppassement en bas et a droite
		if (d && b && interCircleAt(circle, -width, -height, 0, 0)) {
			return true;
		}
		// si aucun cas de collision
		return false;
	}

	private boolean intersectsRectCopies(Circle circle) {
		boolean left = RectGeometry.rectTouchesLeft(this);
		boolean right = RectGeometry.rectTouchesRight(this);
		boolean top = RectGeometry.rectTouchesTop(this);
		boolean bottom = RectGeometry.rectTouchesBottom(this);

		if (left && interCircleAt(circle, 0, 0, width, 0)) {
			return true;
		}

		if (right && interCircleAt(circle, 0, 0, -width, 0)) {
			return true;
		}

		if (top && interCircleAt(circle, 0, 0, 0, height)) {
			return true;
		}

		if (bottom && interCircleAt(circle, 0, 0, 0, -height)) {
			return true;
		}

		if (left && top && interCircleAt(circle, 0, 0, width, height)) {
			return true;
		}

		if (left && bottom && interCircleAt(circle, 0, 0, width, height)) {
			return true;
		}

		if (right && top && interCircleAt(circle, 0, 0, -width, height)) {
			return true;
		}

		if (right && bottom && interCircleAt(circle, 0, 0, width, -height)) {
			return true;
		}

		return false;
	}

	private boolean interCircleAt(Circle circle, double cdx, double cdy, double rdx, double rdy) {

		Point virtualCircleCenter = new Point(circle.center.x() + cdx, circle.center.y() + cdy);

		return new RectCircleIntersection(this, virtualCircleCenter, circle.radius, rdx, rdy).intersects();
	}

	// ===========================Rect/RectIntersection=====================================================

	public boolean intersects(Rect rect) {
		if (intersectsRectAt(this, rect, 0, 0)) {
			return true;
		}
		if (intersectsRectWithVirtualCopies(this, rect)) {
			return true;
		}
		if (intersectsRectWithVirtualCopies(rect, this)) {
			return true;
		}
		// on teste les 2 sens car si b est au bord mais a non
		// sa ne suffit pas de copier juste a car il reste a sa place
		return false;
	}

	private boolean intersectsRectAt(Rect fixed, Rect moved, double dx, double dy) {
		return new RectRectIntersection(fixed, moved, dx, dy).intersects();
	}

	private boolean intersectsRectWithVirtualCopies(Rect fixed, Rect moved) {
		boolean left = RectGeometry.rectTouchesLeft(moved);
		boolean right = RectGeometry.rectTouchesRight(moved);
		boolean top = RectGeometry.rectTouchesTop(moved);
		boolean bottom = RectGeometry.rectTouchesBottom(moved);

		if (left && intersectsRectAt(fixed, moved, isu.width_cm(), 0)) {
			return true;
		}

		if (right && intersectsRectAt(fixed, moved, -isu.width_cm(), 0)) {
			return true;
		}

		if (top && intersectsRectAt(fixed, moved, 0, isu.height_cm())) {
			return true;
		}

		if (bottom && intersectsRectAt(fixed, moved, 0, -isu.height_cm())) {
			return true;
		}

		if (left && top && intersectsRectAt(fixed, moved, isu.width_cm(), isu.height_cm())) {
			return true;
		}

		if (left && bottom && intersectsRectAt(fixed, moved, isu.width_cm(), -isu.height_cm())) {
			return true;
		}

		if (right && top && intersectsRectAt(fixed, moved, -isu.width_cm(), isu.height_cm())) {
			return true;
		}

		if (right && bottom && intersectsRectAt(fixed, moved, -isu.width_cm(), -isu.height_cm())) {
			return true;
		}

		return false;
	}

	// BOX

	@Override
	public Box box() {
		Point[] corners = RectGeometry.cornersAt(this, 0, 0);
		double minX = RectGeometry.minX(corners);
		double minY = RectGeometry.minY(corners);
		double maxX = RectGeometry.maxX(corners);
		double maxY = RectGeometry.maxY(corners);
		return new Box(isu,minX, minY, maxX, maxY);

	}

	public Box box_360() {
		// Box qui englobe les 360 degrés
		double radius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
		return new Box(isu,center.x() - radius, center.y() - radius, center.x() + radius, center.y() + radius);
	}

}
