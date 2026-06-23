package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.Bounding;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Orientation;
import engine.model.Rect;
import game.config.PengoRunState;
import oop.tasks.Runnable;

public final class Glacon extends Entity implements Pushable {

	private static final double INITIAL_SPEED_NCELL_PER_SECOND = 6.0;
	private static final double COLLISION_SIZE_FACTOR = 0.86;
	private static final double MAX_TRAVEL_FACTOR = 1.5;
	private static final double EPS = 0.000001;

	private boolean sliding;
	private int slideDirection;
	private boolean breaking;

	private double currentSpeed_cmPerSecond;
	private double deceleration_cmPerSecond2;
	private double traveledDistance_cm;
	private double maximumDistance_cm;

	private ISU.Coord previousCenter;
	private Grid.Position lastStablePosition;
	private long lastUpdate_ms;
	private PengoRunState runState;

	private final List<Enemies> carriedEnemies;

	public Glacon(Model model, String name) {
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
		this.carriedEnemies = new ArrayList<Enemies>();

		setZOrder(1);
		setSOrder(1);

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

	private boolean pausedByRunState() {
		return this.runState != null && this.runState.paused();
	}

	private boolean stoppedByRunState() {
		return this.runState != null && this.runState.stopped();
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

		int direction = Orientation.normalizeCardinal(directionDegree);
		Grid.Position target = neighbor(position(), direction);

		if (target == null) {
			return false;
		}

		if (isBlockedAt(target)) {
			breakIce();
			return true;
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

	/**
	 * Mise à jour appelée depuis le Brain de Pengo, sans hook ajouté au moteur.
	 */
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
		carryEnemiesWithIce();
	}

	/** Arrête immédiatement le glaçon et le recale au centre d'une cellule. */
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

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		releaseCarriedEnemies();

		if (wasSliding && alive() && installed()) {
			Grid.Position snapPosition = safeSnapPosition();
			model().place(this, snapPosition);
			this.lastStablePosition = snapPosition.copy();
		}
	}

	/** Détruit le glaçon lorsque Pengo appuie sur Espace contre un obstacle. */
	public void breakIce() {
		if (!alive() || this.breaking) {
			return;
		}

		stopSliding();

		this.breaking = true;

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		oop.tasks.Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (stoppedByRunState()) {
					return;
				}

				if (pausedByRunState()) {
					oop.tasks.Runtime.post(this, 50);
					return;
				}

				if (alive()) {
					death();
				}
			}
		}, (int) 250L);
	}

	public boolean isBreaking() {
		return this.breaking;
	}

	private boolean isStaticObstacle(Entity other) {
		return other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
				|| other instanceof Spawner || other instanceof Power;
	}

	@Override
	public void collision(Entity other) {
		assert other != null;

		handleSlidingContact(other);

		if (this.sliding && isStaticObstacle(other)) {
			if (isCrushingObstacle(other)) {
				crushCarriedEnemies(other);
			}

			stopSliding();
			return;
		}

		if (isHardBlockForThisIce(other)) {
			if (isCrushingObstacle(other)) {
				crushCarriedEnemies(other);
			}

			stopSliding();
			return;
		}

		carryEnemiesWithIce();

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		assert entities != null;

		boolean mustStop = false;
		Entity crushingObstacle = null;

		for (Entity other : entities) {
			if (other == this || !other.alive()) {
				continue;
			}

			handleSlidingContact(other);

			if (this.sliding && isStaticObstacle(other)) {
				mustStop = true;

				if (isCrushingObstacle(other)) {
					crushingObstacle = other;
				}

				continue;
			}

			if (isHardBlockForThisIce(other)) {
				mustStop = true;

				if (isCrushingObstacle(other)) {
					crushingObstacle = other;
				}
			}
		}

		if (crushingObstacle != null) {
			crushCarriedEnemies(crushingObstacle);
		}

		if (mustStop) {
			stopSliding();
			return;
		}

		carryEnemiesWithIce();

		super.collision(entities);
	}

	@Override
	public boolean blocks(Entity other) {
		assert other != null;

		if (other instanceof Enemies) {
			return !this.sliding && !sameGridCell(other);
		}

		return isHardBlockForThisIce(other);
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

	public double traveledDistanceCm() {
		return this.traveledDistance_cm;
	}

	public double maximumDistanceCm() {
		return this.maximumDistance_cm;
	}

	public static double collisionSizeFactor() {
		return COLLISION_SIZE_FACTOR;
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
		Grid.Position current = position();

		if (cellCanReceiveStoppedIce(current)) {
			return current.copy();
		}

		if (this.lastStablePosition != null && cellCanReceiveStoppedIce(this.lastStablePosition)) {
			return this.lastStablePosition.copy();
		}

		Grid.Position behind = neighbor(current, (this.slideDirection + 180) % 360);

		if (behind != null && cellCanReceiveStoppedIce(behind)) {
			return behind;
		}

		return current;
	}

	private boolean cellCanReceiveStoppedIce(Grid.Position position) {
		if (position == null) {
			return false;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this && other.alive()) {
				if (other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
						|| other instanceof Spawner || other instanceof Power) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean isBlockedAt(Grid.Position target) {
		Grid.Cell targetCell = model().grid().cellAt(target);

		for (Entity other : targetCell.entities()) {
			if (other != this && other.alive()) {
				if (isHardBlockForThisIce(other)) {
					return true;
				}
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

		if (!isOnSlidingPath(enemy)) {
			return;
		}

		if (!this.carriedEnemies.contains(enemy)) {
			this.carriedEnemies.add(enemy);
		}

		enemy.freeze();
		enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		enemy.setAngularSpeed(0.0);

		carryEnemyWithIce(enemy);
	}

	private boolean isOnSlidingPath(Entity other) {
		if (other == null || !this.sliding) {
			return false;
		}

		ISU.Vector towardOther = center().mkVectorToward(other.center());
		double forward = forwardProjection(towardOther, this.slideDirection);
		double lateral = Math.abs(lateralProjection(towardOther, this.slideDirection));
		double cell = model().isu().cmPerCell();

		return forward > -cell * 0.75 && lateral <= cell * 0.45;
	}

	private void carryEnemiesWithIce() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (Enemies enemy : copy) {
			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
			} else {
				carryEnemyWithIce(enemy);
			}
		}
	}

	private void carryEnemyWithIce(Enemies enemy) {
		assert enemy != null;

		if (!alive() || !installed() || !enemy.alive() || !enemy.installed()) {
			return;
		}
		Grid.Position enemyPosition = neighbor(position(), this.slideDirection);

		if (enemyPosition == null) {
			crushCarriedEnemyWithoutObstacle(enemy);
			return;
		}

		Entity obstacleOnEnemyCell = crushingObstacleAt(enemyPosition);

		if (obstacleOnEnemyCell != null) {
			killCarriedEnemy(enemy, obstacleOnEnemyCell);
			return;
		}

		Grid.Position afterEnemy = neighbor(enemyPosition, this.slideDirection);

		if (afterEnemy == null) {
			crushCarriedEnemyWithoutObstacle(enemy);
			return;
		}

		Entity obstacleAfterEnemy = crushingObstacleAt(afterEnemy);

		if (obstacleAfterEnemy != null) {
			killCarriedEnemy(enemy, obstacleAfterEnemy);
			return;
		}

		enemy.freeze();
		enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		enemy.setAngularSpeed(0.0);
		model().place(enemy, carriedEnemyCenter());
	}

	private ISU.Coord carriedEnemyCenter() {
		return center().mkTranslated(forwardOffset(this.slideDirection, model().isu().cmPerCell()));
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

	private void killCarriedEnemy(Enemies enemy, Entity obstacle) {
		assert enemy != null;

		this.carriedEnemies.remove(enemy);

		if (enemy.alive()) {
			enemy.crushBetweenAt(this, obstacle, this.slideDirection, carriedEnemyCenter());
		}
	}

	private void crushCarriedEnemyWithoutObstacle(Enemies enemy) {
		assert enemy != null;

		this.carriedEnemies.remove(enemy);

		if (enemy.alive()) {
			enemy.crushAt(this.slideDirection, carriedEnemyCenter());
		}
	}

	private Entity crushingObstacleAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this && other.alive() && isCrushingObstacle(other)) {
				return other;
			}
		}

		return null;
	}

	private boolean containsCrushingObstacle(Grid.Position position) {
		if (position == null) {
			return true;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this && other.alive() && isCrushingObstacle(other)) {
				return true;
			}
		}

		return false;
	}

	private void crushCarriedEnemies(Entity obstacle) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (Enemies enemy : copy) {
			if (enemy.alive()) {
				enemy.crushBetweenAt(this, obstacle, this.slideDirection, carriedEnemyCenter());
			}
		}
	}

	/**
	 * Centre final de l'ennemi ecrase : une case devant la position finale du
	 * glaçon. Ainsi l'image est placee dans la case entre le glaçon pousseur et
	 * le mur/glaçon/spawner qui arrete le mouvement.
	 */
	private ISU.Coord crushCenterForCarriedEnemy() {
		Grid.Position iceStopPosition = safeSnapPosition();

		if (iceStopPosition != null) {
			Grid.Position enemyPosition = neighbor(iceStopPosition, this.slideDirection);

			if (enemyPosition != null && !containsCrushingObstacle(enemyPosition)) {
				return enemyPosition.toISUCoordCentered();
			}
		}

		return carriedEnemyCenter();
	}

	private void releaseCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (Enemies enemy : copy) {
			if (enemy.alive()) {
				enemy.unfreeze();
				enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
				enemy.setAngularSpeed(0.0);
			}
		}
	}

	private boolean isHardBlockForThisIce(Entity other) {
		if (other instanceof Enemies) {
			return false;
		}

		if (other instanceof Pengo || other instanceof Friend) {
			return !sameGridCell(other);
		}

		boolean hardType = other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
				|| other instanceof Spawner || other instanceof Power;

		if (!hardType) {
			return false;
		}

		if (!this.sliding) {
			return true;
		}

		return isInFrontOfSlidingIce(other);
	}

	private boolean sameGridCell(Entity other) {
		return other != null && position().equals(other.position());
	}

	private boolean isInFrontOfSlidingIce(Entity other) {
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

	private double lateralProjection(ISU.Vector vector, int direction) {
		switch (direction) {
		case 0:
		case 180:
			return vector.y();

		case 90:
		case 270:
			return vector.x();

		default:
			throw new AssertionError("Not a cardinal direction: " + direction);
		}
	}

	private boolean isCrushingObstacle(Entity other) {
		return other instanceof Mur || other instanceof Glacon || other instanceof Spawner;
	}

	private double axisLength(int direction) {
		if (direction == 0 || direction == 180) {
			return model().isu().width();
		}

		return model().isu().height();
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

		if (model().game().isTorusOnXaxis()) {
			x = wrapCoordinate(x, model().grid().width());
		}

		if (model().game().isTorusOnYaxis()) {
			y = wrapCoordinate(y, model().grid().height());
		}

		return model().grid().new Position(x, y);
	}

	private int wrapCoordinate(int value, int size) {
		return ((value % size) + size) % size;
	}

	@Override
	public String debugLabel() {
		if (this.breaking) {
			return "Ice BREAK";
		}

		if (this.sliding) {
			return "Ice SLIDE d=" + this.slideDirection + " v=" + Math.round(this.currentSpeed_cmPerSecond);
		}

		return "Ice REST";
	}

}