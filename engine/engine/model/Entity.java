// == ENTITY ==
package engine.model;

import java.io.PrintStream;

import engine.intersection.Bounding;

public class Entity {

	// FIELDS

	private Grid grid;
	private ISU isu;
	private final String name;
	private Bounding bounding;

	private ISU.Dimension size;
	private ISU.Dimension step;
	private Grid.Position position;
	private ISU.Coord center;

	private int orientation_degree;

	// CONSTRUCTOR

	public Entity(String name) {
		this.name = name;
		this.grid = null;
		this.isu = null;
		this.size = null;
		this.step = null;
		this.position = null;
		this.center = null;
		this.bounding = null;
		this.orientation_degree = 0;
	}

	// INTERNAL HELPERS
	private void detachFromCurrentCell() {
		if (this.grid != null && this.position != null) {
			this.grid.cellAt(this.position).remove(this);
		}
	}

	private void attachToCurrentCell() {
		if (this.grid != null && this.position != null) {
			this.grid.cellAt(this.position).add(this);
		}
	}

	private void ensureStep() {
		if (this.step == null) {
			this.step = this.isu.new Dimension(this.grid.isu().cmPerCell(), this.grid.isu().cmPerCell());
		}
	}

	private void checkCoherence() {

		assert this.position.equiv(this.center.toGridPosition());

		assert this.grid.cellAt(this.position).contains(this);
	}

	private int normalizeAngle(int angle_degree) {
		int a = angle_degree % 360;

		if (a < 0) {
			a += 360;
		}

		return a;
	}

	// SETTERS
	public void setPosition(Grid.Position position) {
		detachFromCurrentCell();
		this.grid = position.grid();
		this.isu = this.grid.isu();
		this.position = position.copy();
		this.center = this.position.toISUCoordCentered();
		ensureStep();
		attachToCurrentCell();
		checkCoherence();
	}

	public void setCoord(ISU.Coord center) {

		detachFromCurrentCell();
		this.isu = center.isu();
		this.grid = this.isu.grid();
		this.center = center.mkCopy();
		this.position = this.center.toGridPosition();
		ensureStep();
		attachToCurrentCell();
		checkCoherence();
	}

	public void setSize(Grid.Dimension dimension) {

		this.size = dimension.toISUDimension();
		if (this.isu == null) {
			this.isu = this.size.isu();
			this.grid = this.isu.grid();
		}
	}

	public void setSize(ISU.Dimension dimension) {
		this.isu = dimension.isu();
		if (this.grid == null) {
			this.grid = this.isu.grid();
		}
		this.size = this.isu.new Dimension(dimension.x(), dimension.y());
	}

	public void setBounding() {
		this.bounding = new Bounding();
	}

	// GETTERS

	public ISU.Coord center() {
		assert this.center != null;

		return this.center.mkCopy();
	}

	public Grid.Position position() {
		assert this.position != null;

		return this.position.copy();
	}

	public int orientation() {
		return this.orientation_degree;
	}

	public String name() {
		return this.name;
	}

	public ISU.Dimension size() {
		return this.size;
	}

	public ISU.Dimension step() {
		return this.step;
	}

	public Bounding bounding() {
		assert this.bounding != null : "bounding must not be null";
		return this.bounding;
	}

	public ISU isu() {
		return isu;
	}

	// TRANSLATION

	public void translate(Grid.Vector v) {

		detachFromCurrentCell();
		this.position.translate(v);
		this.center = this.position.toISUCoordCentered();
		attachToCurrentCell();
		updateBoundingIfNeeded();
		checkCoherence();
	}

	public void translate(ISU.Vector v) {
		detachFromCurrentCell();
		this.center.translate(v);
		this.position = this.center.toGridPosition();
		attachToCurrentCell();
		updateBoundingIfNeeded();
		checkCoherence();
	}

	// TURN
	public void turn(int angle_degree) {
		this.orientation_degree = normalizeAngle(this.orientation_degree + angle_degree);

		assert this.orientation_degree >= 0;
		assert this.orientation_degree < 360;
		updateBoundingIfNeeded();
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("name = " + this.name);
		ps.println("orientation = " + this.orientation_degree);

		if (this.position != null) {
			ps.print("position = ");
			this.position.show(ps);
		} else {
			ps.println("position = null");
		}

		if (this.center != null) {
			ps.print("center = ");
			this.center.show(ps);
		} else {
			ps.println("center = null");
		}

		if (this.size != null) {
			ps.print("size = ");
			this.size.show(ps);
		} else {
			ps.println("size = null");
		}

		if (this.step != null) {
			ps.print("step = ");
			this.step.show(ps);
		} else {
			ps.println("step = null");
		}
	}

	// MOVE

	public void moveNorth(int nStep) {
		assert nStep >= 0;
		assert this.grid != null;

		translate(this.grid.new Vector(0, -nStep));
	}

	public void moveSouth(int nStep) {
		assert nStep >= 0;
		assert this.grid != null;

		translate(this.grid.new Vector(0, nStep));
	}

	public void moveEast(double length_cm) {
		assert length_cm >= 0.0;

		translate(this.isu.new Vector(length_cm, 0.0));
	}

	public void moveWest(double length_cm) {
		assert length_cm >= 0.0;

		translate(this.isu.new Vector(-length_cm, 0.0));
	}

	public boolean intersects(Entity e) {
		return this.bounding.intersects(e.bounding);
	}

	public double distanceCenterToCenter(Entity e) {
		return this.center.distanceTo(e.center);
	}

	private void updateBoundingIfNeeded() {
		if (this.bounding != null) {
			setBounding();
		}
	}
}