package engine.brain;

import java.util.List;

import engine.model.Entity;
import engine.model.Orientation;
import engine.model.Stunt;

/**
 * Bot du joueur qui mémorise la dernière direction demandée.
 */
public final class PlayerBot extends Bot {

	private Integer wishedDirection;
	private Integer currentDirection;

	public PlayerBot(Brain brain, Stunt stunt) {
		super(brain, stunt);
	}

	public Integer wishedDirection() {
		return this.wishedDirection;
	}

	public Integer currentDirection() {
		return this.currentDirection;
	}

	public void wish(int direction) {
		this.wishedDirection = Orientation.normalize(direction);

		think();
	}

	public void stopWishing() {
		this.wishedDirection = null;
		this.currentDirection = null;
	}

	@Override
	public void think() {
		if (!valid()) {
			return;
		}

		Entity player = this.stunt.entity();

		if (!player.alive() || !player.installed()) {
			return;
		}

		if (player.isMoving()) {
			return;
		}

		tryToMove();
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

	private void tryToMove() {
		if (tryWishedDirection()) {
			return;
		}

		tryCurrentDirection();
	}

	private boolean tryWishedDirection() {
		if (this.wishedDirection == null) {
			return false;
		}

		if (!this.stunt.walk(this.wishedDirection)) {
			return false;
		}

		this.currentDirection = this.wishedDirection;
		this.wishedDirection = null;

		return true;
	}

	private boolean tryCurrentDirection() {
		if (this.currentDirection == null) {
			return false;
		}

		return this.stunt.walk(this.currentDirection);
	}
}