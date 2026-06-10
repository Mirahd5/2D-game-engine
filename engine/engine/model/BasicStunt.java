// == BASIC STUNT ==
package engine.model;

import java.util.List;

import engine.brain.Bot;

public class BasicStunt extends Stunt {

	public static final int EAST = 0;
	public static final int SOUTH = 90;
	public static final int WEST = 180;
	public static final int NORTH = 270;

	private Bot bot;
	private final double speed_cm_per_ms;
	private double remaining_cm;
	private int direction_degree;

	private int wanted_direction_degree;
	private boolean moving;
	private boolean stopRequested;
	private final boolean continuousWalk;

	public BasicStunt(Entity entity, double speed_cm_per_ms) {
		this(entity, speed_cm_per_ms, false);
	}

	public BasicStunt(Entity entity, double speed_cm_per_ms, boolean continuousWalk) {
		super(entity);
		assert speed_cm_per_ms > 0;

		this.speed_cm_per_ms = speed_cm_per_ms;
		this.remaining_cm = 0.0;
		this.direction_degree = EAST;
		this.wanted_direction_degree = EAST;

		this.moving = false;
		this.stopRequested = false;
		this.continuousWalk = continuousWalk;
	}

	public void setBot(Bot bot) {
		assert bot != null;

		this.bot = bot;
	}

	public boolean isMoving() {
		return this.moving;
	}

	public int direction() {
		return this.direction_degree;
	}

	public int wantedDirection() {
		return this.wanted_direction_degree;
	}

	public void walk(int degree) {

		int direction = closestCardinal(degree);

		this.wanted_direction_degree = direction;
		this.stopRequested = false;

		if (!this.moving) {
			startMove(direction);
			return;
		}

		if (isOpposite(this.direction_degree, direction)) {
			this.direction_degree = direction;
			set(this.direction_degree);
			double cellWidth_cm = entity.isu().game().cmPerCell();
			double alreadyMoved_cm = cellWidth_cm - this.remaining_cm;
			if (alreadyMoved_cm <= 0.0) {
				alreadyMoved_cm = cellWidth_cm;
			}

			this.remaining_cm = alreadyMoved_cm;
		}
	}

	public void stop() {
		this.stopRequested = true;
	}

	private boolean isOpposite(int d1, int d2) {
		return Math.abs(d1 - d2) == 180;
	}

	public void stopNow() {
		this.moving = false;
		this.remaining_cm = 0.0;
		this.stopRequested = true;
	}

	private void startMove(int direction) {

		this.direction_degree = closestCardinal(direction);
		set(this.direction_degree);
		double cellWidth_cm = entity.isu().game().cmPerCell();
		this.remaining_cm = cellWidth_cm;
		this.moving = true;
	}

	private int closestCardinal(int degree) {
		degree = ((degree % 360) + 360) % 360;
		if (degree < 45 || degree >= 315) {
			return EAST;
		}
		if (degree < 135) {
			return SOUTH;
		}
		if (degree < 225) {
			return WEST;
		}
		return NORTH;
	}

	@Override
	public void tick(long elapsed_ms) {

		// Si aucun mouvement n'est en cours, il n'y a rien à faire.
		if (!this.moving) {
			return;
		}

		// Distance que l'entité devrait parcourir pendant ce tick.
		double moved_cm = this.speed_cm_per_ms * elapsed_ms;

		// Cas 1 : la distance calculée dépasse ou atteint la distance restante.
		if (moved_cm >= this.remaining_cm) {
			moveStep(this.remaining_cm);

			this.remaining_cm = 0.0;
			this.moving = false;

			if (!this.stopRequested && this.continuousWalk) {
				walk(this.wanted_direction_degree);

			} else if (!this.stopRequested && this.bot != null) {
				this.bot.done();
			}

			// Cas 2 : le pas courant n'est pas encore terminé.

		} else {
			// On avance seulement de la distance possible pour ce tick.
			moveStep(moved_cm);
			this.remaining_cm -= moved_cm;
		}
	}

	private void moveStep(double distance_cm) {
		switch (this.direction_degree) {
		case EAST:
			entity.moveEast(distance_cm);
			break;

		case WEST:
			entity.moveWest(distance_cm);
			break;

		case SOUTH:
			entity.translate(entity.isu().new Vector(0.0, distance_cm));
			break;

		case NORTH:
			entity.translate(entity.isu().new Vector(0.0, -distance_cm));
			break;

		default:
			break;
		}
	}

	@Override
	public void collision(Entity other) {
		stopNow();
		if (!this.continuousWalk && this.bot != null) {
			this.bot.collision(other);
		}
	}

	@Override
	public void collision(List<Entity> others) {
		stopNow();
		if (!this.continuousWalk && this.bot != null) {
			this.bot.collision(others);
		}
	}
}