package engine.brain;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.ISU;
import engine.model.Orientation;
import engine.model.Stunt;

/**
 * Bot qui éloigne son entité de l'ennemi vivant le plus proche.
 */
public final class FleeBot extends Bot {

	private final List<Entity> enemies;

	public FleeBot(Brain brain, Stunt stunt, Entity enemy) {
		super(brain, stunt);

		this.enemies = new ArrayList<Entity>();

		addEnemy(enemy);
	}

	public FleeBot(Brain brain, Stunt stunt, List<Entity> enemies) {
		super(brain, stunt);

		this.enemies = new ArrayList<Entity>();

		for (Entity enemy : enemies) {
			addEnemy(enemy);
		}
	}

	public void addEnemy(Entity enemy) {
		assert enemy != this.stunt.entity();
		assert enemy.model() == this.stunt.entity().model();

		if (!this.enemies.contains(enemy)) {
			this.enemies.add(enemy);
		}
	}

	public void removeEnemy(Entity enemy) {
		this.enemies.remove(enemy);
	}

	public List<Entity> enemies() {
		return new ArrayList<Entity>(this.enemies);
	}

	public Entity closestEnemy() {
		Entity self = this.stunt.entity();

		Entity closest = null;
		double closestDistance = Double.POSITIVE_INFINITY;

		for (Entity enemy : this.enemies) {
			if (!enemy.alive() || !enemy.installed()) {
				continue;
			}

			double distance = self.distanceCenterToCenter(enemy);

			if (distance < closestDistance) {
				closestDistance = distance;
				closest = enemy;
			}
		}

		return closest;
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

		if (self.isMoving()) {
			return;
		}

		Entity closest = closestEnemy();

		if (closest == null) {
			return;
		}

		walkAwayFrom(closest);
	}

	@Override
	public void done() {
		think();
	}

	@Override
	public void collision(Entity entity) {
		think();
	}

	@Override
	public void collision(List<Entity> entities) {
		think();
	}

	private void walkAwayFrom(Entity enemy) {
		Entity self = this.stunt.entity();

		ISU.Vector towardEnemy = self.center().mkVectorToward(enemy.center());

		double dx = towardEnemy.x();
		double dy = towardEnemy.y();

		int horizontalAway = dx >= 0.0 ? Orientation.WEST : Orientation.EAST;
		int verticalAway = dy >= 0.0 ? Orientation.NORTH : Orientation.SOUTH;

		int[] directions;

		if (Math.abs(dx) >= Math.abs(dy)) {
			directions = new int[] { horizontalAway, verticalAway, Orientation.backOf(verticalAway),
					Orientation.backOf(horizontalAway) };
		} else {
			directions = new int[] { verticalAway, horizontalAway, Orientation.backOf(horizontalAway),
					Orientation.backOf(verticalAway) };
		}

		for (int direction : directions) {
			if (this.stunt.walk(direction)) {
				return;
			}
		}
	}
}