package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.model.Orientation;

//classe parent de pengo et de friend comme ils peuvent etre controles
public abstract class Player extends Entity {

	// variables globales
	private final Grid grid;
	private final boolean torusOnXaxis;
	private final boolean torusOnYaxis;
	private final int gridWidth;
	private final int gridHeight;

	// champs
	private int lives;
	private Grid.Position respawnPosition;

	// constructeur dun player
	protected Player(Model model, String name, int initialLives) {
		super(model, name);

		this.grid = model.grid();
		this.torusOnXaxis = model.game().isTorusOnXaxis();
		this.torusOnYaxis = model.game().isTorusOnYaxis();
		this.gridWidth = this.grid.width();
		this.gridHeight = this.grid.height();

		this.lives = initialLives;
		this.respawnPosition = null;
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
			loseLifeIfNeeded();
			return;
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		List<Entity> physicalContacts = new ArrayList<Entity>();

		for (Entity entity : entities) {
			if (entity instanceof Enemies && entity.alive()) {
				loseLifeIfNeeded();
			} else {
				physicalContacts.add(entity);
			}
		}
		if (!physicalContacts.isEmpty()) {
			super.collision(physicalContacts);
		}
	}

	@Override
	protected void deploy() {
		super.deploy();

		if (this.respawnPosition == null) {
			this.respawnPosition = position();
		}
	}

	private void respawnAtStart() {
		if (this.respawnPosition == null) {
			return;
		}

		setPosition(this.respawnPosition.copy());
		setOrientation(0);
	}

	private void loseLifeIfNeeded() {
		boolean mustLoseLife = true;

		if (this instanceof Friend) {
			Friend friend = (Friend) this;
			mustLoseLife = friend.isControlled();
		}

		if (mustLoseLife && !hasEnemyProtection()) {
			loseLife();

			if (isGameOver()) {
				death();
			} else {
				respawnAtStart();
			}
		}
	}

}