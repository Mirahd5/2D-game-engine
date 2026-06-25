package engine.model;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import engine.view.Avatar;

/**
 * Classe de base de toutes les entités du modèle.
 */
public abstract class Entity {

	private static final double SPEED_EPSILON = 1e-6;

	protected final Model model;
	protected final Grid grid;
	protected final ISU isu;
	protected final String name;

	protected Bounding bounding;
	protected final Set<Grid.Cell> occupied;

	protected Stunt stunt;
	protected Avatar avatar;

	protected ISU.Vector lSpeed;
	protected double aSpeed;

	protected int zOrder;
	protected int sOrder;

	protected ISU.Dimension size;
	protected ISU.Dimension step;
	protected Grid.Position position;
	protected ISU.Coord center;

	private boolean installed;
	protected boolean alive;

	protected int orientation_degree;

	public Entity(Model model, String name) {
		this.model = model;
		this.grid = model.grid();
		this.isu = model.isu();
		this.name = name;

		this.occupied = new HashSet<Grid.Cell>();

		this.lSpeed = this.isu.new Vector(0.0, 0.0);

		this.size = this.isu.new Dimension(this.isu.cmPerCell(), this.isu.cmPerCell());
		this.step = this.isu.new Dimension(this.isu.cmPerCell(), this.isu.cmPerCell());

		this.position = this.grid.new Position(0, 0);
		this.center = this.position.toISUCoordCentered();

		this.alive = true;

	}

	// SETTERS

	public final void setPosition(Grid.Position position) {
		this.model.place(this, position);
	}

	public final void setCoord(ISU.Coord center) {
		this.model.place(this, center);
	}

	public final void translate(Grid.Vector vector) {
		this.model.translate(this, vector);
	}

	public final void translate(ISU.Vector vector) {
		this.model.translate(this, vector);
	}

	public final void turn(int angleDegree) {
		this.model.rotate(this, angleDegree);
	}

	public final void setOrientation(int orientationDegree) {
		this.model.setOrientation(this, orientationDegree);
	}

	protected final void setSize(Grid.Dimension dimension) {
		setSize(dimension.toISUDimension());
	}

	protected final void setSize(ISU.Dimension dimension) {
		assert dimension.isu() == this.isu;

		boolean wasInstalled = this.installed;

		if (wasInstalled) {
			retract();
		}

		this.size = this.isu.new Dimension(dimension.x(), dimension.y());

		if (this.bounding != null) {
			updateBounding();
		}

		if (wasInstalled) {
			deploy();
			checkGeometry();
		}
	}

	protected final void setStep(ISU.Dimension step) {
		assert step.isu() == this.isu;

		this.step = this.isu.new Dimension(step.x(), step.y());
	}

	public final void setStunt(Stunt stunt) {
		assert stunt.model() == this.model;
		assert stunt.entity() == this;

		if (this.stunt == stunt) {
			return;
		}

		if (this.stunt != null) {
			this.stunt.invalidate();
		}

		stopMotion();

		this.stunt = stunt;
	}

	public final void removeStunt() {
		if (this.stunt == null) {
			return;
		}

		this.stunt.invalidate();
		this.stunt = null;

		stopMotion();
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	public void removeAvatar() {
		this.avatar = null;
	}

	public void setZOrder(int zOrder) {
		this.zOrder = zOrder;
	}

	public void setSOrder(int sOrder) {
		this.sOrder = sOrder;
	}

	public void setLinearSpeed(ISU.Vector speed) {
		assert speed.isu() == this.isu;

		this.lSpeed = this.isu.new Vector(speed.x(), speed.y());
	}

	public void setAngularSpeed(double speed) {
		this.aSpeed = speed;
	}

	// GETTERS

	public Model model() {
		return this.model;
	}

	public Grid grid() {
		return this.grid;
	}

	public ISU isu() {
		return this.isu;
	}

	public String name() {
		return this.name;
	}

	public ISU.Coord center() {
		return this.center.mkCopy();
	}

	public Grid.Position position() {
		return this.position.copy();
	}

	public int orientation() {
		return this.orientation_degree;
	}

	public ISU.Dimension size() {
		return this.size.copy();
	}

	public ISU.Dimension step() {
		return this.step.copy();
	}

	public Bounding bounding() {
		return this.bounding;
	}

	public Set<Grid.Cell> occupied() {
		return new HashSet<Grid.Cell>(this.occupied);
	}

	public Stunt stunt() {
		return this.stunt;
	}

	public Avatar avatar() {
		return this.avatar;
	}

	public ISU.Vector linearSpeed() {
		return this.lSpeed.copy();
	}

	public double angularSpeed() {
		return this.aSpeed;
	}

	public int zOrder() {
		return this.zOrder;
	}

	public int sOrder() {
		return this.sOrder;
	}

	public boolean alive() {
		return this.alive;
	}

	public final boolean installed() {
		return this.installed;
	}

	public boolean isMoving() {
		return this.lSpeed.norm() > SPEED_EPSILON || Math.abs(this.aSpeed) > SPEED_EPSILON;
	}

	// DEBUG

	public String debugLabel() {
		String status = isMoving() ? "MOVING" : "IDLE";
		return this.name + " " + status;
	}

	public void show(PrintStream ps) {
		ps.println("Entity: " + this.name);
		ps.println("orientation = " + this.orientation_degree + " degree");

		ps.print("position = ");
		this.position.show(ps);

		ps.print("center = ");
		this.center.show(ps);

		ps.print("size = ");
		this.size.show(ps);

		ps.print("step = ");
		this.step.show(ps);
	}

	// MOVE DE COMPATIBILITÉ

	public void moveNorth(int nStep) {
		assert nStep >= 0 : nStep;

		ISU.Vector vector = this.isu.new Vector(0.0, -nStep * this.step.y());
		translate(vector);
	}

	public void moveSouth(int nStep) {
		assert nStep >= 0 : nStep;

		ISU.Vector vector = this.isu.new Vector(0.0, nStep * this.step.y());
		translate(vector);
	}

	public void moveEast(double lengthCm) {
		assert lengthCm >= 0.0 : lengthCm;

		ISU.Vector vector = this.isu.new Vector(lengthCm, 0.0);
		translate(vector);
	}

	public void moveWest(double lengthCm) {
		assert lengthCm >= 0.0 : lengthCm;

		ISU.Vector vector = this.isu.new Vector(-lengthCm, 0.0);
		translate(vector);
	}

	// COLLISION

	public boolean intersects(Entity entity) {
		assert this.bounding != null;
		assert entity.bounding != null;

		return this.bounding.intersects(entity.bounding);
	}

	public double distanceCenterToCenter(Entity entity) {
		return this.center.distanceTo(entity.center);
	}

	public void collision(Entity entity) {
		if (this.stunt != null) {
			this.stunt.collision(entity);
		}
	}

	public void collision(List<Entity> entities) {
		if (this.stunt != null) {
			this.stunt.collision(entities);
		}
	}

	public boolean blocks(Entity other) {
		return false;
	}

	protected abstract Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree);

	protected final void updateBounding() {
		this.bounding = createBounding(this.center, this.size, this.orientation_degree);

		assert this.bounding != null;
	}

	final Bounding boundingAt(ISU.Coord center, int orientationDegree) {
		assert center.isu() == this.isu;

		int normalizedOrientation = normalizeOrientation(orientationDegree);

		ISU.Coord centerCopy = this.isu.new Coord(center.x(), center.y());
		ISU.Dimension sizeCopy = this.isu.new Dimension(this.size.x(), this.size.y());

		return createBounding(centerCopy, sizeCopy, normalizedOrientation);
	}

	// VIE

	public final void death() {
		if (!this.alive) {
			return;
		}

		this.alive = false;

		stopMotion();

		if (this.stunt != null) {
			this.stunt.invalidate();
		}
	}

	// OCCUPATION DE LA GRILLE

	protected final void occupy(Grid.Position position) {
		assert this.installed;

		Grid.Cell cell = this.grid.cellAt(position);

		if (!cell.contains(this)) {
			cell.add(this);
		}

		this.occupied.add(cell);
	}

	protected final void retract() {
		Set<Grid.Cell> previousCells = new HashSet<Grid.Cell>(this.occupied);

		for (Grid.Cell cell : previousCells) {
			cell.remove(this);
		}

		this.occupied.clear();
	}

	protected void deploy() {
		assert this.installed;

		occupy(this.position);
	}

	final void prepareInitialPosition(Grid.Position initialPosition) {
		assert !this.installed;
		assert this.occupied.isEmpty();

		this.position = initialPosition.copy();
		this.position.normalize();

		this.center = this.position.toISUCoordCentered();

		updateBounding();
		checkGeometry();
	}

	final void install() {
		assert !this.installed;
		assert this.occupied.isEmpty();
		assert this.bounding != null;

		this.installed = true;

		deploy();
		checkGeometry();
	}

	final void uninstall() {
		if (!this.installed) {
			return;
		}

		retract();

		this.installed = false;

		checkGeometry();
	}

	// SYNCHRONISATION PAR LE MODEL

	final void applyPoseFromModel(ISU.Coord newCenter, int newOrientation, boolean notifyStunt) {
		assert newCenter.isu() == this.isu;

		ISU.Coord oldCenter = this.center.mkCopy();
		int oldOrientation = this.orientation_degree;

		Grid.Cell oldCenterCell = null;

		if (this.installed) {
			oldCenterCell = this.grid.cellAt(this.position);
			retract();
		}

		this.center = this.isu.new Coord(newCenter.x(), newCenter.y());
		this.position = this.center.toGridPosition();
		this.orientation_degree = normalizeOrientation(newOrientation);

		updateBounding();

		if (this.installed) {
			deploy();
		}

		if (notifyStunt && this.stunt != null) {
			notifyStunt(oldCenter, oldOrientation, oldCenterCell);
		}

		checkGeometry();
	}

	private void notifyStunt(ISU.Coord oldCenter, int oldOrientation, Grid.Cell oldCenterCell) {
		if (!oldCenter.equals(this.center)) {
			this.stunt.set(this.center.x(), this.center.y());
		}

		if (oldOrientation != this.orientation_degree) {
			this.stunt.set(this.orientation_degree);
		}

		if (this.installed) {
			Grid.Cell newCenterCell = this.grid.cellAt(this.position);

			if (oldCenterCell != newCenterCell) {
				this.stunt.set(newCenterCell);
			}
		}
	}

	void commit() {
		if (this.stunt != null) {
			this.stunt.commit();
		}
	}

	// HITBOX DU MOUVEMENT

	final Hitbox motionHitboxBetween(ISU.Coord from, ISU.Vector movement, int fromOrientation, int toOrientation,
			int nsteps) {
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;
		assert nsteps >= 1;

		int orientationDelta = shortestAngleDelta(fromOrientation, toOrientation);

		Bounding firstBounding = boundingAt(from, fromOrientation);
		Hitbox result = firstBounding.hitbox();

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			int testedOrientation = normalizeOrientation(fromOrientation + (int) Math.round(orientationDelta * ratio));

			Bounding testedBounding = boundingAt(testedCenter, testedOrientation);

			result = result.union(testedBounding.hitbox());
		}

		return result;
	}

	public Hitbox hitboxBetween(ISU.Coord from, ISU.Coord to) {
		assert from.isu() == this.isu;
		assert to.isu() == this.isu;

		Bounding startBounding = boundingAt(from, this.orientation_degree);
		Hitbox current = startBounding.hitbox();

		ISU.Vector delta = from.mkVectorToward(to);

		Hitbox moved = new Hitbox(current.xmin() + delta.x(), current.xmax() + delta.x(), current.ymin() + delta.y(),
				current.ymax() + delta.y());

		return current.union(moved);
	}

	// OUTILS PRIVÉS

	private void stopMotion() {
		this.lSpeed = this.isu.new Vector(0.0, 0.0);
		this.aSpeed = 0.0;
	}

	private int shortestAngleDelta(int fromOrientation, int toOrientation) {
		return ((toOrientation - fromOrientation + 540) % 360) - 180;
	}

	private int normalizeOrientation(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}

	private void checkGeometry() {
		assert this.center != null;
		assert this.position != null;
		assert this.size != null;
		assert this.step != null;
		assert this.bounding != null;

		assert this.position.equals(this.center.toGridPosition()) : "center and Grid position are inconsistent";

		if (!this.installed) {
			assert this.occupied.isEmpty() : "an uninstalled entity occupies cells";
			return;
		}

		Grid.Cell centerCell = this.grid.cellAt(this.position);

		assert this.occupied.contains(centerCell) : "the center cell is not occupied";

		for (Grid.Cell cell : this.occupied) {
			assert cell.contains(this) : "Entity/Cell occupation is inconsistent";
		}
	}
}