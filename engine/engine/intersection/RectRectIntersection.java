package engine.intersection;



class RectRectIntersection {
	private Rect r1;
	private Rect r2;

	private double r2dx; // déplacement virtuel en x de r2
	private double r2dy; // déplacement virtuel en y de r2

	public RectRectIntersection(Rect r1, Rect r2, double r2dx, double r2dy) {
		this.r1 = r1;
		this.r2 = r2;
		this.r2dx = r2dx;
		this.r2dy = r2dy;
	}

	void remedy() {
		// rien a faire on fait les projection avec vecteurs et ps
	}

	public boolean intersects() {
		double[][] axes = new double[4][2];// 4 axes

		double[] Ar1 = getAxes(r1);
		double[] Ar2 = getAxes(r2);
		axes[0] = new double[] { Ar1[0], Ar1[1] };
		axes[1] = new double[] { Ar1[2], Ar1[3] };
		axes[2] = new double[] { Ar2[0], Ar2[1] };
		axes[3] = new double[] { Ar2[2], Ar2[3] };

		for (int i = 0; i < axes.length; i++) {
			double[] projR1 = project(r1, axes[i], 0, 0);
			double[] projR2 = project(r2, axes[i], r2dx, r2dy);
			if (!overlaps(projR1, projR2))
				return false;// pas de collistion existe droite qui separe
		}
		return true;// collision aucune droite
	}

	private boolean overlaps(double[] projR1, double[] projR2) {
		return projR1[1] >= projR2[0] && projR2[1] >= projR1[0];
	}

	private double[] project(Rect r, double[] axes, double dx, double dy) {
		Point[] corners = RectGeometry.cornersAt(r,dx, dy);

		double min = dot(axes, corners[0]);
		double max = min;

		for (int i = 1; i < corners.length; i++) {
			double p = dot(axes, corners[i]);

			if (p < min) {
				min = p;
			}

			if (p > max) {
				max = p;
			}
		}

		return new double[] { min, max };
	}

	private double[] getAxes(Rect r) {
		double cos = Math.cos(Math.toRadians(r.angle_degree));
		double sin = Math.sin(Math.toRadians(r.angle_degree));

		return new double[] { cos, sin, -sin, cos };// les 2 axes du rect dans le monde
	}

	private double dot(double[] axis, Point p) {
		// Ps entre un axe et un point vu comme vecteur depuis l'origine.
		return axis[0] * p.x() + axis[1] * p.y();
	}

}
