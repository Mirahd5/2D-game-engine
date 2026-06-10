package engine.intersection;

class RectGeometry {

	static boolean rectTouchesRight(Rect rect) {
		return maxX(cornersAt(rect, 0, 0)) > rect.isu.width_cm();
	}

	static boolean rectTouchesTop(Rect rect) {
		return minY(cornersAt(rect, 0, 0)) < 0;
	}

	static boolean rectTouchesBottom(Rect rect) {
		return maxY(cornersAt(rect, 0, 0)) > rect.isu.height_cm();
	}

	static boolean rectTouchesLeft(Rect rect) {
		return minX(cornersAt(rect, 0, 0)) < 0;
	}

	static double minX(Point[] points) {
		double min = points[0].x();

		for (int i = 1; i < points.length; i++) {
			if (points[i].x() < min) {
				min = points[i].x();
			}
		}

		return min;
	}

	static double minY(Point[] points) {
		double min = points[0].y();

		for (int i = 1; i < points.length; i++) {
			if (points[i].y() < min) {
				min = points[i].y();
			}
		}

		return min;
	}

	static double maxX(Point[] points) {
		double max = points[0].x();

		for (int i = 1; i < points.length; i++) {
			if (points[i].x() > max) {
				max = points[i].x();
			}
		}

		return max;
	}

	static double maxY(Point[] points) {
		double max = points[0].y();

		for (int i = 1; i < points.length; i++) {
			if (points[i].y() > max) {
				max = points[i].y();
			}
		}

		return max;
	}

	static Point[] cornersAt(Rect rect, double dx, double dy) {
		double cos = Math.cos(Math.toRadians(rect.angle_degree()));
		double sin = Math.sin(Math.toRadians(rect.angle_degree()));

		// centre virtuel euclidien du rectangle
		Point center = new Point(rect.center().x() + dx, rect.center().y() + dy);

		// axe X local mis à l'échelle par halfWidth
		Vector axisX = new Vector(cos * rect.halfWidth(), sin * rect.halfWidth());

		// axe Y local mis à l'échelle par halfHeight
		Vector axisY = new Vector(-sin * rect.halfHeight(), cos * rect.halfHeight());

		// Vecteurs du centre vers chacun des 4 corners
		Vector topRightV = new Vector(axisX.x() + axisY.x(), axisX.y() + axisY.y());
		Vector topLeftV = new Vector(-axisX.x() + axisY.x(), -axisX.y() + axisY.y());
		Vector bottomLeftV = new Vector(-axisX.x() - axisY.x(), -axisX.y() - axisY.y());
		Vector bottomRightV = new Vector(axisX.x() - axisY.x(), axisX.y() - axisY.y());

		// Les 4 corners du rectangle virtuel
		Point topRight = center.translated(topRightV);
		Point topLeft = center.translated(topLeftV);
		Point bottomLeft = center.translated(bottomLeftV);
		Point bottomRight = center.translated(bottomRightV);

		return new Point[] { topRight, topLeft, bottomLeft, bottomRight };

	}
}
