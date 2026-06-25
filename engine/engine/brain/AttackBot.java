package engine.brain;

import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Orientation;
import engine.model.Stunt;

/**
 * Bot qui poursuit une cible en choisissant une direction cardinale.
 */
public final class AttackBot extends Bot {

	private static final double EPSILON = 1e-9;

	private final Entity target;

	private Integer lastDirection;

	public AttackBot(Brain brain, Stunt stunt, Entity target) {
		super(brain, stunt);

		assert target != stunt.entity();
		assert target.model() == stunt.entity().model();

		this.target = target;
	}

	public Entity target() {
		return this.target;
	}

	@Override
	public void think() {
		if (!valid()) {
			return;
		}

		Entity self = this.stunt.entity();

		if (!self.alive() || !self.installed()) {
			return;
		}

		if (!this.target.alive() || !this.target.installed()) {
			return;
		}

		if (self.isMoving()) {
			return;
		}

		walkTowardTarget();
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity other) {
		think();
	}

	@Override
	public void collision(List<Entity> others) {
		think();
	}

	private void walkTowardTarget() {
		Entity self = this.stunt.entity();

		ISU.Vector toward = self.center().mkVectorToward(this.target.center());

		double dx = toward.x();
		double dy = toward.y();

		if (samePosition(dx, dy)) {
			return;
		}

		int horizontal = dx >= 0.0 ? Orientation.EAST : Orientation.WEST;
		int vertical = dy >= 0.0 ? Orientation.SOUTH : Orientation.NORTH;

		int[] directions = orderedDirections(dx, dy, horizontal, vertical);

		if (tryWalkWithoutReverse(directions)) {
			return;
		}

		tryWalk(directions);
	}

	private boolean samePosition(double dx, double dy) {
		return Math.abs(dx) <= EPSILON && Math.abs(dy) <= EPSILON;
	}

	private int[] orderedDirections(double dx, double dy, int horizontal, int vertical) {
		if (Math.abs(dx) >= Math.abs(dy)) {
			return new int[] { horizontal, vertical, Orientation.backOf(vertical), Orientation.backOf(horizontal) };
		}

		return new int[] { vertical, horizontal, Orientation.backOf(horizontal), Orientation.backOf(vertical) };
	}

	private boolean tryWalkWithoutReverse(int[] directions) {
		if (this.lastDirection == null) {
			return false;
		}

		int reverse = Orientation.backOf(this.lastDirection);

		for (int direction : directions) {
			if (direction != reverse && walk(direction)) {
				return true;
			}
		}

		return false;
	}

	private boolean tryWalk(int[] directions) {
		for (int direction : directions) {
			if (walk(direction)) {
				return true;
			}
		}

		return false;
	}

	private boolean walk(int direction) {
		if (!this.stunt.walk(direction)) {
			return false;
		}

		this.lastDirection = direction;
		return true;
	}
}