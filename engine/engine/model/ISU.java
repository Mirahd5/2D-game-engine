package engine.model;

import java.io.PrintStream;

/**
 * Espace continu du monde, exprimé en centimètres.
 */
public class ISU {

	private static final double EPSILON = 1e-9;

	private final Axis xAxis;
	private final Axis yAxis;

	private Grid grid;

	private final double width_cm;
	private final double height_cm;

	private final double cmPerCell;

	public ISU(Game game) {
		this.width_cm = game.getWidthCm();
		this.height_cm = game.getHeightCm();

		this.cmPerCell = game.getCmPerCell();

		this.xAxis = new Axis(game.getTorusOnXaxis(), this.width_cm);
		this.yAxis = new Axis(game.getTorusOnYaxis(), this.height_cm);
	}

	public void set(Grid grid) {
		this.grid = grid;
	}

	public double width() {
		return this.width_cm;
	}

	public double height() {
		return this.height_cm;
	}

	public double cmPerCell() {
		return this.cmPerCell;
	}

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public Grid grid() {
		return this.grid;
	}

	// == DIMENSION ==

	public class Dimension {

		private double x_cm;
		private double y_cm;

		public Dimension(double x_cm, double y_cm) {
			assert x_cm > 0.0 : x_cm;
			assert y_cm > 0.0 : y_cm;

			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();

			assert this.x_cm > 0.0;
			assert this.y_cm > 0.0;
			assert this.x_cm <= ISU.this.width_cm;
			assert this.y_cm <= ISU.this.height_cm;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		public void normalize() {
			this.x_cm = ISU.this.xAxis.normalizeDimension(this.x_cm);
			this.y_cm = ISU.this.yAxis.normalizeDimension(this.y_cm);
		}

		public void setxy(double x_cm, double y_cm) {
			assert x_cm > 0.0 : x_cm;
			assert y_cm > 0.0 : y_cm;

			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();

			assert this.x_cm > 0.0;
			assert this.y_cm > 0.0;
			assert this.x_cm <= ISU.this.width_cm;
			assert this.y_cm <= ISU.this.height_cm;
		}

		public Dimension copy() {
			return new Dimension(this.x_cm, this.y_cm);
		}

		public boolean equiv(Dimension dimension) {
			return dimension != null && Double.compare(this.x_cm, dimension.x_cm) == 0
					&& Double.compare(this.y_cm, dimension.y_cm) == 0;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof ISU.Dimension)) {
				return false;
			}

			Dimension other = (Dimension) object;

			return other.isu() == ISU.this && this.equiv(other);
		}

		public ISU.Vector mkScaledVector(double factor) {
			return ISU.this.new Vector(this.x_cm * factor, this.y_cm * factor);
		}

		public ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return ISU.this.new Vector(this.x_cm * xFactor, this.y_cm * yFactor);
		}

		public ISU.Vector mkVector() {
			return ISU.this.new Vector(this.x_cm, this.y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("Dimension(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}

	// == COORD ==

	public class Coord {

		private double x_cm;
		private double y_cm;

		public Coord(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		private void normalize() {
			this.x_cm = ISU.this.xAxis.normalize(this.x_cm);
			this.y_cm = ISU.this.yAxis.normalize(this.y_cm);

			check();
		}

		private void check() {
			if (!ISU.this.xAxis.onTorus()) {
				assert this.x_cm >= 0.0;
				assert this.x_cm < ISU.this.width_cm;
			}

			if (!ISU.this.yAxis.onTorus()) {
				assert this.y_cm >= 0.0;
				assert this.y_cm < ISU.this.height_cm;
			}
		}

		public ISU.Coord mkCopy() {
			return ISU.this.new Coord(this.x_cm, this.y_cm);
		}

		public ISU.Coord mkTranslated(ISU.Vector vector) {
			assert vector.isu() == ISU.this;

			return ISU.this.new Coord(this.x_cm + vector.x(), this.y_cm + vector.y());
		}

		public void translate(ISU.Vector vector) {
			assert vector.isu() == ISU.this;

			this.x_cm += vector.x();
			this.y_cm += vector.y();

			normalize();
		}

		public ISU.Vector mkVectorToward(Coord target) {
			assert target.isu() == ISU.this;

			double dx = ISU.this.xAxis.delta(this.x_cm, target.x_cm);
			double dy = ISU.this.yAxis.delta(this.y_cm, target.y_cm);

			return ISU.this.new Vector(dx, dy);
		}

		public Grid.Position toGridPosition() {
			int x_ncell = (int) Math.floor(this.x_cm / ISU.this.cmPerCell);
			int y_ncell = (int) Math.floor(this.y_cm / ISU.this.cmPerCell);

			return ISU.this.grid.new Position(x_ncell, y_ncell);
		}

		public void rotation(int angleDegree) {
			double angle = Math.toRadians(angleDegree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double newX = this.x_cm * cos - this.y_cm * sin;
			double newY = this.x_cm * sin + this.y_cm * cos;

			this.x_cm = newX;
			this.y_cm = newY;

			normalize();
		}

		public void rotateAround(Coord center, int angleDegree) {
			assert center.isu() == ISU.this;

			double dx = ISU.this.xAxis.delta(center.x_cm, this.x_cm);
			double dy = ISU.this.yAxis.delta(center.y_cm, this.y_cm);

			double angle = Math.toRadians(angleDegree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double rotatedX = dx * cos - dy * sin;
			double rotatedY = dx * sin + dy * cos;

			this.x_cm = center.x_cm + rotatedX;
			this.y_cm = center.y_cm + rotatedY;

			normalize();
		}

		public double distanceTo(Coord coord) {
			assert coord.isu() == ISU.this;

			double dx = ISU.this.xAxis.distance(this.x_cm, coord.x_cm);
			double dy = ISU.this.yAxis.distance(this.y_cm, coord.y_cm);

			return Math.sqrt(dx * dx + dy * dy);
		}

		public boolean equiv(Coord coord) {
			return coord != null && Double.compare(this.x_cm, coord.x_cm) == 0
					&& Double.compare(this.y_cm, coord.y_cm) == 0;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof ISU.Coord)) {
				return false;
			}

			Coord other = (Coord) object;

			return other.isu() == ISU.this && this.equiv(other);
		}

		public void show(PrintStream ps) {
			ps.println("Coord(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}

	// == VECTOR ==

	public class Vector {

		private double x_cm;
		private double y_cm;

		public Vector(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		public Vector copy() {
			return ISU.this.new Vector(this.x_cm, this.y_cm);
		}

		public void add(Vector vector) {
			assert vector.isu() == ISU.this;

			this.x_cm += vector.x_cm;
			this.y_cm += vector.y_cm;
		}

		public void scale(double factor) {
			this.x_cm *= factor;
			this.y_cm *= factor;
		}

		public void scale(double xFactor, double yFactor) {
			this.x_cm *= xFactor;
			this.y_cm *= yFactor;
		}

		public double dot(ISU.Vector vector) {
			assert vector.isu() == ISU.this;

			return this.x_cm * vector.x_cm + this.y_cm * vector.y_cm;
		}

		public double norm() {
			return Math.sqrt(this.x_cm * this.x_cm + this.y_cm * this.y_cm);
		}

		public void unity() {
			double norm = norm();

			assert norm > EPSILON : "cannot normalize a zero vector";

			this.x_cm /= norm;
			this.y_cm /= norm;
		}

		public void turn(int angleDegree) {
			double angle = Math.toRadians(angleDegree);

			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			double newX = this.x_cm * cos - this.y_cm * sin;
			double newY = this.x_cm * sin + this.y_cm * cos;

			this.x_cm = newX;
			this.y_cm = newY;
		}

		public void show(PrintStream ps) {
			ps.println("Vector(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}
}