// == FLEE BOT ==
package engine.brain;

import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.ISU;

public class FleeBot extends Bot {

	// FIELDS
	private List<Entity> enemies;

	// CONSTRUCTOR

	public FleeBot(BasicStunt stunt, List<Entity> enemies) {
		super(stunt);
		assert enemies != null;
		this.enemies = enemies;
	}

	// GETTER / SETTER

	public List<Entity> enemies() {
		return enemies;
	}

	public void setEnemies(List<Entity> enemies) {
		assert enemies != null;
		this.enemies = enemies;
	}

	// DECISION MAKING

	@Override
	public void think() {
		fleeFromClosestEnemy();
	}

	@Override
	public void done() {
		fleeFromClosestEnemy();
	}

	@Override
	public void collision(Entity other) {
		fleeFromClosestEnemy();
	}

	@Override
	public void collision(List<Entity> others) {
		fleeFromClosestEnemy();
	}

	// HELPERS

	private void fleeFromClosestEnemy() {
		if (stunt.isMoving())
			return;
		if (enemies == null || enemies.isEmpty())
			return;

		ISU.Coord myCenter = entity().center();
		if (myCenter == null)
			return;

		// Chercher l'ennemi le plus proche
		Entity closest = null;
		double minDist = Double.MAX_VALUE;

		for (Entity enemy : enemies) {
			if (enemy == null || enemy.center() == null)
				continue;
			double dist = entity().distanceCenterToCenter(enemy);
			if (dist < minDist) {
				minDist = dist;
				closest = enemy;
			}
		}

		if (closest == null || closest.center() == null)
			return;

		// Direction vers l'ennemi
		double dx = closest.center().x() - myCenter.x();
		double dy = closest.center().y() - myCenter.y();

		int fleeDirection;

		if (Math.abs(dx) >= Math.abs(dy)) {
			// L'ennemi est plutôt à l'horizontale : fuir à l'opposé
			fleeDirection = (dx >= 0) ? BasicStunt.WEST : BasicStunt.EAST;
		} else {
			// L'ennemi est plutôt à la verticale : fuir à l'opposé
			fleeDirection = (dy >= 0) ? BasicStunt.NORTH : BasicStunt.SOUTH;
		}
		stunt.walk(fleeDirection);
	}
}