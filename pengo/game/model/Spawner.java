package game.model;

import java.util.List;

import engine.model.Bounding;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;
import game.config.PengoRunState;

public final class Spawner extends Entity implements Pushable {

	private static final double INITIAL_SPEED_NCELL_PER_SECOND = 6.0;
	private static final double COLLISION_SIZE_FACTOR = 0.86;
	private static final double MAX_TRAVEL_FACTOR = 1.5;
	private static final double EPS = 0.000001;

	private boolean sliding;
	private int slideDirection;

	private double currentSpeed_cmPerSecond;
	private double deceleration_cmPerSecond2;
	private double traveledDistance_cm;
	private double maximumDistance_cm;

	private ISU.Coord previousCenter;
	private Grid.Position lastStablePosition;
	private long lastUpdate_ms;
	private PengoRunState runState;

	public Spawner(Model model, String name) {
		super(model, name);

		double cell = model.isu().cmPerCell();
		double collisionSize = cell * COLLISION_SIZE_FACTOR;
		setSize(model.isu().new Dimension(collisionSize, collisionSize));
		setStep(model.isu().new Dimension(cell, cell));

		this.sliding = false;
		this.slideDirection = 0;
		this.currentSpeed_cmPerSecond = 0.0;
		this.deceleration_cmPerSecond2 = 0.0;
		this.traveledDistance_cm = 0.0;
		this.maximumDistance_cm = 0.0;
		this.previousCenter = null;
		this.lastStablePosition = null;
		this.lastUpdate_ms = 0L;
		this.runState = null;

		setZOrder(1);
		setSOrder(2);

		updateBounding();
	}

	public void setRunState(PengoRunState runState) {
		this.runState = runState;
	}

	private long gameTimeMs() {
		if (this.runState == null) {
			return System.currentTimeMillis();
		}

		return this.runState.nowMs();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();
		result.add(new Rect(center, size, orientationDegree));
		return result;
	}

	@Override
	public boolean push(int directionDegree) {
		if (!alive() || !installed() || this.sliding) {
			return false;
		}

		int direction = normalizeCardinal(directionDegree);
		Grid.Position target = neighbor(position(), direction);

		if (target == null || isBlockedAt(target)) {
			return false;
		}

		this.slideDirection = direction;
		this.traveledDistance_cm = 0.0;
		this.maximumDistance_cm = axisLength(direction) * MAX_TRAVEL_FACTOR;
		this.previousCenter = center();
		this.lastStablePosition = position().copy();
		this.lastUpdate_ms = gameTimeMs();

		double initialSpeed = model().isu().cmPerCell() * INITIAL_SPEED_NCELL_PER_SECOND;
		this.currentSpeed_cmPerSecond = initialSpeed;
		this.deceleration_cmPerSecond2 = (initialSpeed * initialSpeed) / (2.0 * this.maximumDistance_cm);

		setOrientation(direction);
		setLinearSpeed(speedVector(direction, this.currentSpeed_cmPerSecond));

		this.sliding = true;
		return true;
	}

	/** Mise à jour appelée depuis le Bot du spawner, sans modifier le moteur. */
	public void updateSliding() {
		if (!this.sliding || !alive() || !installed()) {
			return;
		}

		if (linearSpeed().norm() <= EPS) {
			stopSlidingAndSnapToCell();
			return;
		}

		long now = gameTimeMs();
		long elapsed = this.lastUpdate_ms == 0L ? 0L : now - this.lastUpdate_ms;
		this.lastUpdate_ms = now;

		ISU.Coord currentCenter = center();

		if (this.previousCenter != null) {
			this.traveledDistance_cm += this.previousCenter.distanceTo(currentCenter);
		}

		this.previousCenter = currentCenter;
		rememberStableCellIfUseful();

		if (this.traveledDistance_cm + EPS >= this.maximumDistance_cm) {
			stopSliding();
			return;
		}

		double dt = Math.max(0.0, elapsed / 1000.0);
		this.currentSpeed_cmPerSecond = Math.max(0.0,
				this.currentSpeed_cmPerSecond - this.deceleration_cmPerSecond2 * dt);

		if (this.currentSpeed_cmPerSecond <= EPS) {
			stopSliding();
			return;
		}

		setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
	}

	public void stopSliding() {
		stopSlidingAndSnapToCell();
	}

	private void stopSlidingAndSnapToCell() {
		boolean wasSliding = this.sliding;

		this.sliding = false;
		this.currentSpeed_cmPerSecond = 0.0;
		this.deceleration_cmPerSecond2 = 0.0;
		this.previousCenter = null;
		this.lastUpdate_ms = 0L;
		this.runState = null;

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		if (wasSliding && alive() && installed()) {
			Grid.Position snapPosition = safeSnapPosition();
			model().place(this, snapPosition);
			this.lastStablePosition = snapPosition.copy();
		}
	}

	@Override
	public void collision(Entity other) {
		assert other != null;

		handleSlidingContact(other);

		if (isHardBlockForThisSpawner(other)) {
			stopSliding();
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		assert entities != null;

		boolean mustStop = false;

		for (Entity other : entities) {
			if (other == this || !other.alive()) {
				continue;
			}

			handleSlidingContact(other);

			if (isHardBlockForThisSpawner(other)) {
				mustStop = true;
			}
		}

		if (mustStop) {
			stopSliding();
		}

		super.collision(entities);
	}

	@Override
	public boolean blocks(Entity other) {
		assert other != null;

		if (other instanceof Enemies) {
			return !this.sliding && !sameGridCell(other);
		}

		return isHardBlockForThisSpawner(other);
	}

	public boolean isSliding() {
		return this.sliding;
	}

	public int slideDirection() {
		return this.slideDirection;
	}

	public double currentSpeedCmPerSecond() {
		return this.currentSpeed_cmPerSecond;
	}

	private void rememberStableCellIfUseful() {
		if (!alive() || !installed()) {
			return;
		}

		Grid.Position current = position();
		ISU.Coord cellCenter = current.toISUCoordCentered();

		if (center().distanceTo(cellCenter) <= model().isu().cmPerCell() * 0.20) {
			this.lastStablePosition = current.copy();
		}
	}

	private Grid.Position safeSnapPosition() {
		if (this.lastStablePosition != null && cellCanReceiveStoppedSpawner(this.lastStablePosition)) {
			return this.lastStablePosition.copy();
		}

		Grid.Position current = position();

		if (cellCanReceiveStoppedSpawner(current)) {
			return current.copy();
		}

		Grid.Position behind = neighbor(current, (this.slideDirection + 180) % 360);

		if (behind != null && cellCanReceiveStoppedSpawner(behind)) {
			return behind;
		}

		return current;
	}

	private boolean cellCanReceiveStoppedSpawner(Grid.Position position) {
		if (position == null) {
			return false;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other == this || !other.alive()) {
				continue;
			}

			if (other instanceof Mur || other instanceof Glacon || other instanceof Spawner) {
				return false;
			}
		}

		return true;
	}

	private boolean isBlockedAt(Grid.Position target) {
		Grid.Cell targetCell = model().grid().cellAt(target);

		for (Entity other : targetCell.entities()) {
			if (other == this || !other.alive()) {
				continue;
			}

			if (isHardBlockForThisSpawner(other)) {
				return true;
			}
		}

		return false;
	}

	private void handleSlidingContact(Entity other) {
		if (!this.sliding || !(other instanceof Enemies) || !other.alive()) {
			return;
		}

		Enemies enemy = (Enemies) other;

		if (enemy.isCrushed()) {
			return;
		}

		if (!isCrushedBySlidingSpawner(enemy)) {
			return;
		}

		enemy.crushAt(this.slideDirection, center().mkTranslated(forwardOffset(this.slideDirection, model().isu().cmPerCell())));
	}

	private boolean isCrushedBySlidingSpawner(Entity other) {
		if (other == null || !this.sliding) {
			return false;
		}

		ISU.Vector towardOther = center().mkVectorToward(other.center());
		double forward = forwardProjection(towardOther, this.slideDirection);

		/*
		 * Les contacts sont notifiés après le mouvement accepté. Si le spawner va vite,
		 * il peut déjà avoir légèrement dépassé l'ennemi au moment où collision(...)
		 * est appelée. On accepte donc une petite marge arrière.
		 */
		return forward > -model().isu().cmPerCell() * 0.75;
	}

	private boolean isHardBlockForThisSpawner(Entity other) {
		if (other instanceof Enemies) {
			return false;
		}

		boolean hardType = other instanceof Mur || other instanceof Glacon || other instanceof Spawner
				|| other instanceof Pengo || other instanceof Friend;

		if (!hardType) {
			return false;
		}

		if (!this.sliding) {
			if ((other instanceof Pengo || other instanceof Friend) && sameGridCell(other)) {
				return false;
			}

			return true;
		}

		return isInFrontOfSlidingSpawner(other);
	}

	private boolean sameGridCell(Entity other) {
		return other != null && position().equals(other.position());
	}

	private boolean isInFrontOfSlidingSpawner(Entity other) {
		if (other == null || !this.sliding) {
			return false;
		}

		ISU.Vector towardOther = center().mkVectorToward(other.center());
		double forward = forwardProjection(towardOther, this.slideDirection);

		return forward > model().isu().cmPerCell() * 0.10;
	}

	private double forwardProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case 0:
			return vector.x();
		case 90:
			return vector.y();
		case 180:
			return -vector.x();
		case 270:
			return -vector.y();
		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private double axisLength(int direction) {
		if (direction == 0 || direction == 180) {
			return model().isu().width();
		}

		return model().isu().height();
	}

	private ISU.Vector forwardOffset(int direction, double distance_cm) {
		switch (direction) {
		case 0:
			return model().isu().new Vector(distance_cm, 0.0);
		case 90:
			return model().isu().new Vector(0.0, distance_cm);
		case 180:
			return model().isu().new Vector(-distance_cm, 0.0);
		case 270:
			return model().isu().new Vector(0.0, -distance_cm);
		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private ISU.Vector speedVector(int direction, double speed) {
		switch (direction) {
		case 0:
			return model().isu().new Vector(speed, 0.0);
		case 90:
			return model().isu().new Vector(0.0, speed);
		case 180:
			return model().isu().new Vector(-speed, 0.0);
		case 270:
			return model().isu().new Vector(0.0, -speed);
		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private Grid.Position neighbor(Grid.Position from, int direction) {
		int dx = 0;
		int dy = 0;

		switch (direction) {
		case 0:
			dx = 1;
			break;
		case 90:
			dy = 1;
			break;
		case 180:
			dx = -1;
			break;
		case 270:
			dy = -1;
			break;
		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}

		int x = from.x() + dx;
		int y = from.y() + dy;

		if (!model().game().isTorusOnXaxis() && (x < 0 || x >= model().grid().width())) {
			return null;
		}

		if (!model().game().isTorusOnYaxis() && (y < 0 || y >= model().grid().height())) {
			return null;
		}

		return model().grid().new Position(x, y);
	}

	private int normalizeCardinal(int degree) {
		int d = ((degree % 360) + 360) % 360;

		if (d < 45 || d >= 315) {
			return 0;
		}
		if (d < 135) {
			return 90;
		}
		if (d < 225) {
			return 180;
		}
		return 270;
	}

	@Override
	public String debugLabel() {
		if (this.sliding) {
			return "Spawner SLIDE d=" + this.slideDirection + " v=" + Math.round(this.currentSpeed_cmPerSecond);
		}

		return "Spawner REST";
	}
}