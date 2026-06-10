// == GHOST ==
package game.model;

import engine.intersection.Circle;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;

public class Ghost extends Entity {

	public enum Type {
		BLINKY, SPEEDY, INKY, CLYDE
	}

	public static final int MAX_SPAWNS = 3;

	private final Type type;
	private Grid.Position spawnPosition;
	private int remainingSpawns;
	private boolean alive;

	public Ghost(Game game) {
		this(game, Type.BLINKY);
	}

	public Ghost(Game game, Type type) {
		super("Ghost");
		assert type != null : "type must not be null";

		this.type = type;
		this.remainingSpawns = MAX_SPAWNS;
		this.alive = true;

		double cmPerCell = game.cmPerCell();
		setSize(game.isu().new Dimension(cmPerCell / 2.0, cmPerCell / 2.0));
	}

	public Type type() {
		return this.type;
	}

	public Grid.Position spawnPosition() {
		return this.spawnPosition.copy();
	}

	public int remainingSpawns() {
		return this.remainingSpawns;
	}

	public boolean alive() {
		return this.alive;
	}

	public void setSpawnPosition(Grid.Position position) {
		assert position != null : "position must not be null";
		this.spawnPosition = position.copy();
		setPosition(position);
		setBounding();
	}

	public void respawn() {
		if (this.spawnPosition == null)
			return;
		this.alive = true;
		setPosition(this.spawnPosition);
		setBounding();
	}

	public void eatenByPacman() {
		if (this.remainingSpawns > 0) {
			this.remainingSpawns--;
		}

		if (this.remainingSpawns == 0) {
			this.alive = false;
		} else {
			respawn();
		}
	}

	@Override
	public void setBounding() {
		super.setBounding();
		double radius = size().x() / 2.0;
		assert radius < center().isu().xAxis().halfPerimeter();
		assert radius < center().isu().yAxis().halfPerimeter();
		bounding().add(new Circle(center(), radius));
	}
}