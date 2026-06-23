package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.model.Orientation;

//classe parent de pengo et de friend comme ils peuvent etre controles
public abstract class Player extends Entity {

	// variables globales
	private static final long HIT_COOLDOWN_MS = 900L;
	private final Grid grid;
	private final boolean torusOnXaxis;
	private final boolean torusOnYaxis;
	private final int gridWidth;
	private final int gridHeight;

	// champs
	private int lives;
	private long lastHitTime_ms;
	private boolean controlled;

	// constructeur dun player
	protected Player(Model model, String name, int initialLives) {
		super(model, name);

		this.grid = model.grid();
		this.torusOnXaxis = model.game().isTorusOnXaxis();
		this.torusOnYaxis = model.game().isTorusOnYaxis();
		this.gridHeight = grid.height();
		this.gridWidth = grid.width();

		this.lives = initialLives;
		this.controlled = false;
	}

	public final int nLives() {
		return this.lives;
	}

	public final void gainLife() {
		this.lives++;
	}

	public final void loseLife() {
		if (this.lives > 0) {
			this.lives--;
		}
	}

	public final boolean isGameOver() {
		return this.lives == 0;
	}

	public final boolean isControlled() {
		return this.controlled;
	}

	public final void setControlled(boolean controlled) {
		this.controlled = controlled;
	}

	protected boolean hasEnemyProtection() {
		return false;
	}

	public final boolean faceIceAhead(int directionDegree) {

		if (!alive() || !installed() || isMoving()) {
			return false;
		}
		int direction = Orientation.normalizeCardinal(directionDegree);
		Pushable pushable = pushableBlockAhead(direction);
		if (pushable == null) {
			return false;
		}
		setOrientation(direction);
		return true;
	}

	public final boolean useIceAhead() {

		if (!alive() || !installed() || isMoving()) {
			return false;
		}
		int direction = Orientation.normalizeCardinal(orientation());
		Pushable pushable = pushableBlockAhead(direction);
		if (pushable == null) {
			return false;
		}
		return pushable.push(direction);
	}

	// retourne le bloc poussable ahead de la direction sil existe
	private Pushable pushableBlockAhead(int direction) {
		Grid.Position target = Orientation.neighbor(grid, position(), direction, torusOnXaxis, torusOnYaxis);
		if (target == null) {
			return null;
		}

		Grid.Cell targetCell = grid.cellAt(target);
		List<Entity> entities = targetCell.entities();

		for (Entity entity : entities) {
			if (entity.alive() && entity instanceof Pushable) {
				Pushable pushable = (Pushable) entity;
				return pushable;
			}
		}
		return null;
	}

	@Override
	public void collision(Entity other) {
		if (other instanceof Enemies && other.alive()) {
			Enemies enemy = (Enemies) other;

			if (enemy.isCrushed()) {
				return;
			}

			if (this instanceof Pengo) {
				Pengo pengo = (Pengo) this;

				if (pengo.hasStar()) {
					enemy.kill();
					return;
				}
			}

			hitByEnemy();
			return;
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		List<Entity> physicalContacts = new ArrayList<Entity>();
		boolean touchedEnemy = false;

		for (Entity entity : entities) {
			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;

				if (enemy.isCrushed()) {
					continue;
				}

				if (this instanceof Pengo) {
					Pengo pengo = (Pengo) this;

					if (pengo.hasStar()) {
						enemy.kill();
						continue;
					}
				}

				touchedEnemy = true;
			} else {
				physicalContacts.add(entity);
			}
		}

		if (touchedEnemy) {
			hitByEnemy();
		}

		if (!physicalContacts.isEmpty()) {
			super.collision(physicalContacts);
		}
	}

	public final void forceLoseLife() {
		if (!alive() || !installed()) {
			return;
		}

		loseLife();

		if (isGameOver()) {
			setLinearSpeed(model().isu().new Vector(0.0, 0.0));
			setAngularSpeed(0.0);
			death();
			return;
		}

		respawnAtStart();
	}

	private void respawnAtStart() {
		Grid.Position respawn = centerRespawnPosition();

		cancelCurrentMovement();
		stopPlayer();

		model().place(this, respawn);
		setOrientation(0);
	}

	private void cancelCurrentMovement() {
		if (stunt() instanceof BasicStunt) {
			BasicStunt basicStunt = (BasicStunt) stunt();
			basicStunt.cancelCurrentAction();
		}
	}

	private void stopPlayer() {
		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);
	}

	public final void hitByEnemy() {

		if (!alive() || !installed()) {
			return;
		}

		long now = System.currentTimeMillis();

		if (now - this.lastHitTime_ms < HIT_COOLDOWN_MS) {
			return;
		}

		if (!isControlled() || hasEnemyProtection()) {
			return;
		}

		this.lastHitTime_ms = now;
		loseLife();

		if (isGameOver()) {
			setLinearSpeed(model().isu().new Vector(0.0, 0.0));
			setAngularSpeed(0.0);
			death();
			return;
		}

		respawnAtStart();
	}

	private Grid.Position centerRespawnPosition() {
		int centerX = this.gridWidth / 2;
		int centerY = this.gridHeight / 2;

		Grid.Position center = this.grid.new Position(centerX, centerY);

		if (canRespawnOn(center)) {
			return center;
		}

		int maxRadius = Math.max(this.gridWidth, this.gridHeight);

		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int y = centerY - radius; y <= centerY + radius; y++) {
				for (int x = centerX - radius; x <= centerX + radius; x++) {
					if (x < 0 || x >= this.gridWidth || y < 0 || y >= this.gridHeight) {
						continue;
					}

					Grid.Position position = this.grid.new Position(x, y);

					if (canRespawnOn(position)) {
						return position;
					}
				}
			}
		}

		return center;
	}

	private boolean canRespawnOn(Grid.Position position) {
		Grid.Cell cell = this.grid.cellAt(position);

		for (Entity entity : cell.entities()) {
			if (entity == this || !entity.alive()) {
				continue;
			}

			if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Spawner) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String debugLabel() {
		String action = isMoving() ? "WALK" : "REST";

		if (isControlled()) {
			action = "CONTROL " + action;
		}

		return name() + " " + action + " dir=" + orientation();
	}
}