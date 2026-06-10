// = ISU =
package engine.model;

import java.io.PrintStream;

public class ISU {

	private Axis xAxis;
	private Axis yAxis;
	private Grid grid;
	private final double cmPerCell;

	private final Game game;

	public ISU(Game game) {
		this.game = game;
		this.cmPerCell = game.cmPerCell();
		assert this.cmPerCell > 0.0;
		this.xAxis = new Axis(game.torusOnXaxis(), game.width_cm());
		this.yAxis = new Axis(game.torusOnYaxis(), game.height_cm());
	}

	public void set(Grid grid) {
		assert grid != null;
		this.grid = grid;
	}

	public Grid grid() {
		return this.grid;
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

	public Game game() {
		return this.game;
	}

	public double width_cm() {
		return game.width_cm();
	}

	public double height_cm() {
		return game.height_cm();
	}

	public class Dimension {

		private double x_cm;
		private double y_cm;

		public Dimension(double x_cm, double y_cm) {
			setxy(x_cm, y_cm);
		}

		public void normalize() {
			assert this.x_cm > 0.0;
			assert this.y_cm > 0.0;
		}

		public void setxy(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;

			normalize();
		}

		public ISU isu() {
			return ISU.this;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ISU.Dimension)) {
				return false;
			}

			Dimension d = (Dimension) o;
			return equiv(d);
		}

		public boolean equiv(Dimension d) {
			return d != null && Double.compare(this.x_cm, d.x_cm) == 0 && Double.compare(this.y_cm, d.y_cm) == 0;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public ISU.Vector mkScaledVector(double factor) {
			return new Vector(this.x_cm * factor, this.y_cm * factor);
		}

		public ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return new Vector(this.x_cm * xFactor, this.y_cm * yFactor);
		}

		public ISU.Vector mkVector() {
			return new Vector(this.x_cm, this.y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("ISU.Dimension(" + this.x_cm + ", " + this.y_cm + ") cm");
		}
	}

	public class Coord {

		private double x_cm;
		private double y_cm;

		public Coord(double x_cm, double y_cm) {
			setxy(x_cm, y_cm);
		}

		private void setxy(double x_cm, double y_cm) {
			this.x_cm = xAxis.normalize(x_cm);
			this.y_cm = yAxis.normalize(y_cm);
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public void show(PrintStream ps) {
			ps.println("ISU.Coord(" + this.x_cm + ", " + this.y_cm + ") cm");
		}

		public ISU isu() {
			return ISU.this;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ISU.Coord)) {
				return false;
			}

			Coord c = (Coord) o;

			return Double.compare(this.x_cm, c.x_cm) == 0 && Double.compare(this.y_cm, c.y_cm) == 0;
		}

		public ISU.Vector mkVectorToward(Coord target) {
			assert target != null;

			double dx = xAxis.normalizeDelta(target.x_cm - this.x_cm);
			double dy = yAxis.normalizeDelta(target.y_cm - this.y_cm);

			return new Vector(dx, dy);
		}

		public Grid.Position toGridPosition() {
			assert grid != null;

			int x_ncell = (int) Math.round(this.x_cm / cmPerCell);
			int y_ncell = (int) Math.round(this.y_cm / cmPerCell);

			return grid.new Position(x_ncell, y_ncell);
		}

		public void translate(ISU.Vector v) {
			assert v != null;

			setxy(this.x_cm + v.x(), this.y_cm + v.y());
		}

		public ISU.Coord mkTranslated(ISU.Vector v) {
			assert v != null;

			return new Coord(this.x_cm + v.x(), this.y_cm + v.y());
		}

		public ISU.Coord mkCopy() {
			return new Coord(this.x_cm, this.y_cm);
		}

		public void rotation(int angle_degree) {
			double angle_rad = Math.toRadians(angle_degree);

			double newX = this.x_cm * Math.cos(angle_rad) - this.y_cm * Math.sin(angle_rad);

			double newY = this.x_cm * Math.sin(angle_rad) + this.y_cm * Math.cos(angle_rad);

			setxy(newX, newY);
		}

		public void rotateAround(Coord center, int angle_degree) {
			assert center != null;

			double angle_rad = Math.toRadians(angle_degree);

			double dx = this.x_cm - center.x_cm;
			double dy = this.y_cm - center.y_cm;

			double newX = center.x_cm + dx * Math.cos(angle_rad) - dy * Math.sin(angle_rad);

			double newY = center.y_cm + dx * Math.sin(angle_rad) + dy * Math.cos(angle_rad);

			setxy(newX, newY);
		}

		public double distanceTo(Coord pt) {
			assert pt != null;

			double dx = xAxis.distance(this.x_cm, pt.x_cm);
			double dy = yAxis.distance(this.y_cm, pt.y_cm);

			return Math.sqrt(dx * dx + dy * dy);
		}
	}

	public class Vector {

		private double targetX_cm;
		private double targetY_cm;

		public Vector(double targetX_cm, double targetY_cm) {
			this.targetX_cm = targetX_cm;
			this.targetY_cm = targetY_cm;
		}

		public double x() {
			return this.targetX_cm;
		}

		public double y() {
			return this.targetY_cm;
		}

		public void add(Vector v) {
			this.targetX_cm += v.targetX_cm;
			this.targetY_cm += v.targetY_cm;
		}

		public void scale(double factor) {
			this.targetX_cm *= factor;
			this.targetY_cm *= factor;
		}

		public void scale(double xFactor, double yFactor) {
			this.targetX_cm *= xFactor;
			this.targetY_cm *= yFactor;
		}

		double dot(ISU.Vector v) {
			assert v != null;

			return this.targetX_cm * v.targetX_cm + this.targetY_cm * v.targetY_cm;
		}

		public double norm() {
			return Math.sqrt(this.targetX_cm * this.targetX_cm + this.targetY_cm * this.targetY_cm);
		}

		public void unity() {
			double n = norm();
			assert n != 0.0;
			this.targetX_cm /= n;
			this.targetY_cm /= n;
		}

		public void turn(int angle_degree) {
			double angle_rad = Math.toRadians(angle_degree);
			double newX = this.targetX_cm * Math.cos(angle_rad) - this.targetY_cm * Math.sin(angle_rad);
			double newY = this.targetX_cm * Math.sin(angle_rad) + this.targetY_cm * Math.cos(angle_rad);
			this.targetX_cm = newX;
			this.targetY_cm = newY;
		}
	}

}