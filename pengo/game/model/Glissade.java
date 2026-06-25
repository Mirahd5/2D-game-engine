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

public abstract class Glissade extends Entity implements Pushable {

	protected static final double INITIAL_SPEED_NCELL_PER_SECOND = 4.2;
	protected static final double COLLISION_SIZE_FACTOR = 0.86;
	protected static final double MAX_TRAVEL_FACTOR = 1.5;
	protected static final double EPS = 0.000001;

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

	private final List<Enemies> carriedEnemies;

	private Enemies smoothPowerStopEnemy;
	private Grid.Position smoothPowerStopBlockPosition;
	private Grid.Position smoothPowerStopEnemyPosition;

	protected Glissade(Model model, String name, int zOrder, int sOrder) {
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
		this.smoothPowerStopEnemy = null;
		this.smoothPowerStopBlockPosition = null;
		this.smoothPowerStopEnemyPosition = null;

		setZOrder(zOrder);
		setSOrder(sOrder);

		updateBounding();
	}

	public void setRunState(PengoRunState runState) {
		this.runState = runState;
	}

	protected final PengoRunState runState() {
		return this.runState;
	}

	protected final long gameTimeMs() {
		if (this.runState == null) {
			return System.currentTimeMillis();
		}

		return this.runState.nowMs();
	}

	protected final boolean pausedByRunState() {
		return this.runState != null && this.runState.paused();
	}

	protected final boolean stoppedByRunState() {
		return this.runState != null && this.runState.stopped();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();
		result.add(new Rect(center, size, orientationDegree));
		return result;
	}

	@Override
	public final boolean push(int directionDegree) {
		if (!alive() || !installed() || this.sliding) {
			return false;
		}

		int direction = Orientation.normalizeCardinal(directionDegree);
		Grid.Position target = neighbor(position(), direction);

		if (target == null) {
			return false;
		}

		if (isBlockedAt(target)) {
			return onPushBlocked(direction, target);
		}

		this.slideDirection = direction;
		this.traveledDistance_cm = 0.0;
		this.maximumDistance_cm = axisLength(direction) * MAX_TRAVEL_FACTOR;
		this.previousCenter = center();
		this.lastStablePosition = position().copy();
		this.lastUpdate_ms = gameTimeMs();
		clearSmoothPowerStop();
		this.carriedEnemies.clear();

		double initialSpeed = model().isu().cmPerCell() * INITIAL_SPEED_NCELL_PER_SECOND;

		this.currentSpeed_cmPerSecond = initialSpeed;
		this.deceleration_cmPerSecond2 = (initialSpeed * initialSpeed) / (2.0 * this.maximumDistance_cm);

		setOrientation(direction);
		setLinearSpeed(speedVector(direction, this.currentSpeed_cmPerSecond));

		this.sliding = true;
		return true;
	}

	protected boolean onPushBlocked(int direction, Grid.Position target) {
		return false;
	}

	public final void updateSliding() {
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

		AgainstCrossingEnemies();

		if (this.smoothPowerStopEnemy != null) {
			updateSmoothPowerStop();
		} else {
			carryEnemies();
		}
	}

	public final void stopSliding() {
		stopSlidingAndSnapToCell();
	}

	private void stopSlidingAndSnapToCell() {
		stopSlidingAt(safeSnapPosition());
	}

	private void stopSlidingAt(Grid.Position snapPosition) {
		boolean wasSliding = this.sliding;

		clearSmoothPowerStop();

		this.sliding = false;
		this.currentSpeed_cmPerSecond = 0.0;
		this.deceleration_cmPerSecond2 = 0.0;
		this.previousCenter = null;
		this.lastUpdate_ms = 0L;

		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);

		releaseCarriedEnemies();

		if (wasSliding && alive() && installed() && snapPosition != null) {
			model().place(this, snapPosition);
			this.lastStablePosition = snapPosition.copy();
		}
	}

	@Override
	public final void collision(Entity other) {
		assert other != null;

		if (this.smoothPowerStopEnemy != null) {
			updateSmoothPowerStop();

			if (this.sliding && this.smoothPowerStopEnemy != null) {
				setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
				setAngularSpeed(0.0);
			}

			return;
		}

		boolean capturedEnemy = handleSlidingContact(other);

		if (capturedEnemy) {
			setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
			setAngularSpeed(0.0);
			carryEnemies();
			return;
		}

		if (this.sliding && other instanceof Power && !this.carriedEnemies.isEmpty() && isPowerAfterTrain(other)) {
			Enemies lastEnemy = this.carriedEnemies.get(this.carriedEnemies.size() - 1);
			startSmoothPowerStop(lastEnemy, blockStopBeforePower(other.position()), other.position().copy());
			setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
			setAngularSpeed(0.0);
			return;
		}

		if (this.sliding && isStaticObstacle(other) && isInFrontOfSlidingBlock(other)) {
			if (isCrushingObstacle(other)) {
				crushCarriedEnemies(other);
			}

			stopSliding();
			return;
		}

		if (isHardBlockForThisBlock(other)) {
			if (isCrushingObstacle(other)) {
				crushCarriedEnemies(other);
			}

			stopSliding();
			return;
		}

		carryEnemies();

		super.collision(other);
	}

	@Override
	public final void collision(List<Entity> entities) {
		assert entities != null;

		if (this.smoothPowerStopEnemy != null) {
			updateSmoothPowerStop();

			if (this.sliding && this.smoothPowerStopEnemy != null) {
				setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
				setAngularSpeed(0.0);
			}

			return;
		}

		boolean capturedEnemy = false;
		boolean mustStop = false;
		Entity crushingObstacle = null;

		for (Entity other : entities) {
			if (other == this || !other.alive()) {
				continue;
			}

			if (handleSlidingContact(other)) {
				capturedEnemy = true;
				continue;
			}

			if (this.sliding && other instanceof Power && !this.carriedEnemies.isEmpty() && isPowerAfterTrain(other)) {
				Enemies lastEnemy = this.carriedEnemies.get(this.carriedEnemies.size() - 1);
				startSmoothPowerStop(lastEnemy, blockStopBeforePower(other.position()), other.position().copy());
				capturedEnemy = true;
				continue;
			}

			if (this.sliding && isStaticObstacle(other) && isInFrontOfSlidingBlock(other)) {
				mustStop = true;

				if (isCrushingObstacle(other)) {
					crushingObstacle = other;
				}

				continue;
			}

			if (isHardBlockForThisBlock(other)) {
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

		if (capturedEnemy) {
			setLinearSpeed(speedVector(this.slideDirection, this.currentSpeed_cmPerSecond));
			setAngularSpeed(0.0);
			carryEnemies();
			return;
		}

		carryEnemies();

		super.collision(entities);
	}

	@Override
	public final boolean blocks(Entity other) {
		assert other != null;

		if (other instanceof Enemies) {
			Enemies enemy = (Enemies) other;

			if (!this.sliding) {
				return !sameGridCell(other);
			}

			return blocksEnemyWhileSliding(enemy);
		}

		if (this.sliding && this.smoothPowerStopEnemy != null && isSmoothPowerTarget(other)) {
			return false;
		}

		return isHardBlockForThisBlock(other);
	}

	private boolean blocksEnemyWhileSliding(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (this.carriedEnemies.contains(enemy)) {
			return false;
		}

		return isEnemyInSolidTrainZone(enemy);
	}

	private void AgainstCrossingEnemies() {
		if (!this.sliding || !alive() || !installed()) {
			return;
		}

		List<Entity> snapshot = model().entities();

		for (Entity entity : snapshot) {
			if (!(entity instanceof Enemies)) {
				continue;
			}

			Enemies enemy = (Enemies) entity;

			if (!enemy.alive() || enemy.isCrushed() || this.carriedEnemies.contains(enemy)) {
				continue;
			}
			int expectedIndex = this.carriedEnemies.size() + 1;

			if (this.smoothPowerStopEnemy == null && isCarryCandidateAtIndex(enemy, expectedIndex)) {
				prepareCarriedEnemy(enemy);
				continue;
			}

			if (isEnemyCrossingSlidingBody(enemy) || isEnemyInSolidTrainZone(enemy)) {
				stopSliding();
				return;
			}
		}
	}

	private boolean isEnemyCrossingSlidingBody(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (sameGridCell(enemy) || enemy.bounding().intersects(bounding())) {
			return true;
		}

		return isEnemyInBlockBody(enemy);
	}

	private boolean isEnemyInBlockBody(Enemies enemy) {
		if (enemy == null || !this.sliding) {
			return false;
		}

		ISU.Vector towardEnemy = center().mkVectorToward(enemy.center());
		double forward = forwardProjection(towardEnemy, this.slideDirection);
		double lateral = Math.abs(lateralProjection(towardEnemy, this.slideDirection));
		double cell = model().isu().cmPerCell();

		/*
		 * Zone stricte du corps du bloc, pas toute la trajectoire.
		 * Un poisson juste au-dessus/dessous du train ne rentre donc pas ici.
		 */
		return forward >= -cell * 0.72 && forward <= cell * 0.86 && lateral <= cell * 0.48;
	}

	private boolean isEnemyInSolidTrainZone(Enemies enemy) {
		if (enemy == null || !this.sliding || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		ISU.Vector towardEnemy = center().mkVectorToward(enemy.center());
		double forward = forwardProjection(towardEnemy, this.slideDirection);
		double lateral = Math.abs(lateralProjection(towardEnemy, this.slideDirection));
		double cell = model().isu().cmPerCell();

		/*
		 * Le train solide va du corps du bloc jusqu'au dernier poisson porté.
		 * Si aucun poisson n'est porté, la zone ne couvre que le corps du bloc.
		 *
		 * lateral <= 0.32 cell évite de bloquer un poisson vraiment sur le côté.
		 */
		double rearLimit = -cell * 0.72;
		double frontLimit = cell * (Math.max(0, this.carriedEnemies.size()) + 0.92);

		return forward >= rearLimit && forward <= frontLimit && lateral <= cell * 0.48;
	}
	
	public final boolean isSliding() {
		return this.sliding;
	}

	public final int slideDirection() {
		return this.slideDirection;
	}

	public final double currentSpeedCmPerSecond() {
		return this.currentSpeed_cmPerSecond;
	}

	public final double traveledDistanceCm() {
		return this.traveledDistance_cm;
	}

	public final double maximumDistanceCm() {
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

		if (cellCanReceiveStoppedBlock(current)) {
			return current.copy();
		}

		if (this.lastStablePosition != null && cellCanReceiveStoppedBlock(this.lastStablePosition)) {
			return this.lastStablePosition.copy();
		}

		Grid.Position behind = neighbor(current, (this.slideDirection + 180) % 360);

		if (behind != null && cellCanReceiveStoppedBlock(behind)) {
			return behind;
		}

		return current;
	}

	private boolean cellCanReceiveStoppedBlock(Grid.Position position) {
		if (position == null) {
			return false;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other == this || !other.alive()) {
				continue;
			}

			if (other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
					|| other instanceof Spawner || other instanceof Power) {
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

			if (isHardBlockForThisBlock(other)) {
				return true;
			}
		}

		return false;
	}

	private boolean handleSlidingContact(Entity other) {
		if (!this.sliding || this.smoothPowerStopEnemy != null || !(other instanceof Enemies) || !other.alive()) {
			return false;
		}

		Enemies enemy = (Enemies) other;

		if (enemy.isCrushed()) {
			return false;
		}

		if (!this.carriedEnemies.contains(enemy)) {
			int expectedIndex = this.carriedEnemies.size() + 1;

			if (!isCarryCandidateAtIndex(enemy, expectedIndex)) {
				return false;
			}
		}

		prepareCarriedEnemy(enemy);
		return true;
	}

	private void carryEnemies() {
		catchEnemiesInFrontOfTrain();

		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (Enemies enemy : copy) {
			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
			}
		}

		if (this.carriedEnemies.isEmpty()) {
			stopBeforePowerIfNeeded();
			return;
		}

		Grid.Position afterTrain = positionInFront(this.carriedEnemies.size() + 1);

		if (afterTrain == null) {
			crushCarriedEnemiesWithoutObstacle();
			return;
		}

		Entity obstacleAfterTrain = enemyStoppingObstacleAt(afterTrain);

		if (obstacleAfterTrain != null) {
			if (isCrushingObstacle(obstacleAfterTrain)) {
				crushCarriedEnemies(obstacleAfterTrain);
			} else {
				Enemies lastEnemy = this.carriedEnemies.get(this.carriedEnemies.size() - 1);
				startSmoothPowerStop(lastEnemy, blockStopBeforePower(afterTrain), afterTrain.copy());
			}
			return;
		}

		placeCarriedEnemiesAsTrain();
	}

	private boolean stopBeforePowerIfNeeded() {
		Grid.Position next = positionInFront(1);

		if (next == null) {
			return false;
		}

		Entity obstacle = powerAt(next);

		if (obstacle == null) {
			return false;
		}

		stopSlidingAt(position().copy());
		return true;
	}

	private Entity powerAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other instanceof Power && other.alive()) {
				return other;
			}
		}

		return null;
	}

	private void catchEnemiesInFrontOfTrain() {
		boolean added;

		do {
			added = false;

			Grid.Position nextEnemyPosition = positionInFront(this.carriedEnemies.size() + 1);
			Enemies enemy = firstCarryableEnemyAt(nextEnemyPosition);

			if (enemy != null) {
				prepareCarriedEnemy(enemy);
				added = true;
			}
		} while (added);
	}

	private void prepareCarriedEnemy(Enemies enemy) {
		assert enemy != null;

		if (!this.carriedEnemies.contains(enemy)) {
			this.carriedEnemies.add(enemy);
		}

		enemy.freeze();
		enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		enemy.setAngularSpeed(0.0);
	}

	private void placeCarriedEnemiesAsTrain() {
		for (int i = 0; i < this.carriedEnemies.size(); i++) {
			Enemies enemy = this.carriedEnemies.get(i);

			if (enemy.alive() && enemy.installed()) {
				enemy.freeze();
				enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
				enemy.setAngularSpeed(0.0);
				model().place(enemy, carriedEnemyCenter(i + 1));
			}
		}
	}

	private boolean isCarryCandidateAtIndex(Enemies enemy, int expectedIndex) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed() || expectedIndex <= 0) {
			return false;
		}

		Grid.Position expectedPosition = positionInFront(expectedIndex);

		if (expectedPosition == null) {
			return false;
		}

		ISU.Coord expectedCenter = expectedPosition.toISUCoordCentered();
		ISU.Vector towardExpected = center().mkVectorToward(expectedCenter);
		ISU.Vector towardEnemy = center().mkVectorToward(enemy.center());

		double expectedForward = forwardProjection(towardExpected, this.slideDirection);
		double enemyForward = forwardProjection(towardEnemy, this.slideDirection);
		double enemyLateral = Math.abs(lateralProjection(towardEnemy, this.slideDirection));

		double cell = model().isu().cmPerCell();

		return Math.abs(enemyForward - expectedForward) <= cell * 0.78
				&& enemyLateral <= cell * 0.46;
	}

	private Enemies firstCarryableEnemyAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		int expectedIndex = this.carriedEnemies.size() + 1;
		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other instanceof Enemies && other.alive()) {
				Enemies enemy = (Enemies) other;

				if (!enemy.isCrushed()
						&& !this.carriedEnemies.contains(enemy)
						&& isCarryCandidateAtIndex(enemy, expectedIndex)) {
					return enemy;
				}
			}
		}

		return null;
	}

	private Grid.Position positionInFront(int distanceInCells) {
		Grid.Position result = position().copy();

		for (int i = 0; i < distanceInCells; i++) {
			result = neighbor(result, this.slideDirection);

			if (result == null) {
				return null;
			}
		}

		return result;
	}

	private Grid.Position positionInFrontFrom(Grid.Position start, int distanceInCells) {
		if (start == null) {
			return null;
		}

		Grid.Position result = start.copy();

		for (int i = 0; i < distanceInCells; i++) {
			result = neighbor(result, this.slideDirection);

			if (result == null) {
				return null;
			}
		}

		return result;
	}

	private Grid.Position blockStopBeforePower(Grid.Position powerPosition) {
		if (powerPosition == null) {
			return positionInFront(1);
		}

		Grid.Position result = powerPosition.copy();
		int backward = (this.slideDirection + 180) % 360;
		int distance = Math.max(1, this.carriedEnemies.size());

		for (int i = 0; i < distance; i++) {
			result = neighbor(result, backward);

			if (result == null) {
				return positionInFront(1);
			}
		}

		return result;
	}

	private void startSmoothPowerStop(Enemies enemy, Grid.Position blockStopPosition, Grid.Position enemyStopPosition) {
		assert enemy != null;
		assert blockStopPosition != null;
		assert enemyStopPosition != null;

		this.smoothPowerStopEnemy = enemy;
		this.smoothPowerStopBlockPosition = blockStopPosition.copy();
		this.smoothPowerStopEnemyPosition = enemyStopPosition.copy();

		if (!this.carriedEnemies.contains(enemy)) {
			this.carriedEnemies.add(enemy);
		}

		for (Enemies carriedEnemy : this.carriedEnemies) {
			if (carriedEnemy.alive() && carriedEnemy.installed()) {
				carriedEnemy.freeze();
				carriedEnemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
				carriedEnemy.setAngularSpeed(0.0);
			}
		}
	}

	private void updateSmoothPowerStop() {
		if (this.smoothPowerStopEnemy == null || this.smoothPowerStopBlockPosition == null) {
			clearSmoothPowerStop();
			stopSliding();
			return;
		}

		removeInvalidCarriedEnemies();

		if (this.carriedEnemies.isEmpty()) {
			clearSmoothPowerStop();
			stopSliding();
			return;
		}

		placeCarriedEnemiesAsTrain();

		if (!hasReachedSmoothPowerStopPosition()) {
			return;
		}

		Grid.Position blockStop = this.smoothPowerStopBlockPosition.copy();

		releaseCarriedEnemiesAfterSmoothPowerStop(blockStop);

		clearSmoothPowerStop();
		stopSlidingAt(blockStop);
	}

	private void removeInvalidCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (Enemies enemy : copy) {
			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
			}
		}
	}

	private void releaseCarriedEnemiesAfterSmoothPowerStop(Grid.Position blockStopPosition) {
		assert blockStopPosition != null;

		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
				continue;
			}

			Grid.Position finalPosition = positionInFrontFrom(blockStopPosition, i + 1);
			ISU.Coord safeCenter = finalPosition == null ? enemy.center() : finalPosition.toISUCoordCentered();

			releaseCarriedEnemy(enemy, safeCenter);
		}
	}

	private boolean hasReachedSmoothPowerStopPosition() {
		if (this.smoothPowerStopBlockPosition == null) {
			return true;
		}

		ISU.Coord targetCenter = this.smoothPowerStopBlockPosition.toISUCoordCentered();
		ISU.Vector towardTarget = center().mkVectorToward(targetCenter);

		double remaining = forwardProjection(towardTarget, this.slideDirection);
		double tolerance = model().isu().cmPerCell() * 0.06;

		return remaining <= tolerance;
	}

	private void clearSmoothPowerStop() {
		this.smoothPowerStopEnemy = null;
		this.smoothPowerStopBlockPosition = null;
		this.smoothPowerStopEnemyPosition = null;
	}

	private ISU.Coord carriedEnemyCenter(int distanceInCells) {
		return center().mkTranslated(forwardOffset(this.slideDirection, model().isu().cmPerCell() * distanceInCells));
	}

	private void crushCarriedEnemiesWithoutObstacle() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushAt(this.slideDirection, carriedEnemyCenter(i + 1));
			}
		}
	}

	private void crushCarriedEnemies(Entity obstacle) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushBetweenAt(this, obstacle, this.slideDirection, carriedEnemyCenter(i + 1));
			}
		}
	}

	private void releaseCarriedEnemy(Enemies enemy, ISU.Coord safeCenter) {
		assert enemy != null;

		this.carriedEnemies.remove(enemy);

		if (!enemy.alive()) {
			return;
		}

		if (safeCenter != null && enemy.installed()) {
			model().place(enemy, safeCenter);
		}

		enemy.releaseAfterBeingCarried();
	}

	private void releaseCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (Enemies enemy : copy) {
			if (enemy.alive()) {
				enemy.releaseAfterBeingCarried();
			}
		}
	}

	private boolean isPowerAfterTrain(Entity other) {
		if (!(other instanceof Power) || this.carriedEnemies.isEmpty()) {
			return false;
		}

		Grid.Position expected = positionInFront(this.carriedEnemies.size() + 1);
		return expected != null && expected.equals(other.position());
	}

	private boolean isSmoothPowerTarget(Entity other) {
		return other instanceof Power
				&& this.smoothPowerStopEnemyPosition != null
				&& this.smoothPowerStopEnemyPosition.equals(other.position());
	}

	private Entity enemyStoppingObstacleAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		Grid.Cell cell = model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this && other.alive() && isEnemyStoppingObstacle(other)) {
				return other;
			}
		}

		return null;
	}

	private boolean isEnemyStoppingObstacle(Entity other) {
		return isCrushingObstacle(other) || other instanceof Power;
	}

	private boolean isStaticObstacle(Entity other) {
		return other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
				|| other instanceof Spawner || other instanceof Power;
	}

	private boolean isCrushingObstacle(Entity other) {
		return other instanceof Mur || other instanceof Glacon || other instanceof Spawner;
	}

	private boolean isHardBlockForThisBlock(Entity other) {
		if (other instanceof Enemies) {
			return false;
		}

		if (this.sliding && this.smoothPowerStopEnemy != null && isSmoothPowerTarget(other)) {
			return false;
		}

		if (other instanceof Pengo || other instanceof Friend) {
		    if (!this.sliding) {
		        return !sameGridCell(other);
		    }

		    return !sameGridCell(other) && isInFrontOfSlidingBlock(other);
		}

		boolean hardType = other instanceof Obstacle || other instanceof Mur || other instanceof Glacon
				|| other instanceof Spawner || other instanceof Power;

		if (!hardType) {
			return false;
		}

		if (!this.sliding) {
			return true;
		}

		return isInFrontOfSlidingBlock(other);
	}

	private boolean sameGridCell(Entity other) {
		return other != null && position().equals(other.position());
	}

	private boolean isInFrontOfSlidingBlock(Entity other) {
		if (other == null || !this.sliding) {
			return false;
		}

		ISU.Vector towardOther = center().mkVectorToward(other.center());
		double forward = forwardProjection(towardOther, this.slideDirection);
		double lateral = Math.abs(lateralProjection(towardOther, this.slideDirection));
		double cell = model().isu().cmPerCell();

		return forward > cell * 0.10 && lateral <= cell * 0.55;
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
		if (from == null) {
			return null;
		}

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

	protected final String slideDebugLabel(String name) {
		if (this.sliding) {
			return name + " SLIDE d=" + this.slideDirection + " v=" + Math.round(this.currentSpeed_cmPerSecond);
		}

		return name + " REST";
	}
}