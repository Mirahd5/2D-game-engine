package game.gal;

import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Orientation;
import game.model.Enemies;
import game.model.Friend;
import game.model.Glacon;
import game.model.Mur;
import game.model.Pengo;
import game.model.Power;
import game.model.Spawner;

public final class GalGameContext {

	private final Entity self;
	private final BasicStunt stunt;
	private final CurrentPlayer currentPlayer;

	private final Grid grid;
	private final int gridWidth;
	private final int gridHeight;

	public GalGameContext(Entity self, BasicStunt stunt, CurrentPlayer currentPlayer) {
		assert stunt.entity() == self;

		this.self = self;
		this.stunt = stunt;
		this.currentPlayer = currentPlayer;

		this.grid = self.model().grid();
		this.gridWidth = this.grid.width();
		this.gridHeight = this.grid.height();
	}

	public void waitAction() {
	}

	public void moveAction(String direction) {
		this.stunt.walk(resolveDirection(direction));
	}

	public void turnAction(String direction) {
		this.self.setOrientation(resolveDirection(direction));
	}

	public boolean cellCondition(String directionName, String categoryName) {
		int direction = resolveDirection(directionName);

		Grid.Position target = Orientation.neighbor(this.grid, this.self.position(), direction);

		if (target == null) {
			return false;
		}

		Grid.Cell cell = this.grid.cellAt(target);
		List<Entity> entities = cell.entities();

		String category = normalizeName(categoryName);

		if (isVoidCategory(category)) {
			return free(cell);
		}

		if (isPlayerCategory(category)) {
			Entity player = this.currentPlayer.currentPlayer();
			return player != null && player.alive() && cell.contains(player);
		}

		if (isEnemy(category)) {
			return containsAlive(entities, Enemies.class);
		}

		if (isTeam(category)) {
			return containsAlive(entities, Friend.class) || containsAlive(entities, Pengo.class);
		}

		if (isObstacle(category)) {
			return containsAlive(entities, Mur.class) || containsAlive(entities, Glacon.class)
					|| containsAlive(entities, Spawner.class);
		}

		if (isPushable(category)) {
			return containsAlive(entities, Glacon.class) || containsAlive(entities, Spawner.class);
		}

		if (isPower(category)) {
			return containsAlive(entities, Power.class);
		}

		return false;
	}

	public boolean closestCondition(String targetName, String directionName) {
		if (!isPlayerCategory(normalizeName(targetName))) {
			return false;
		}

		Entity target = findPengo();

		if (target == null || !target.alive()) {
			return false;
		}

		int wantedDirection = resolveDirection(directionName);

		Grid.Position from = this.self.position();
		Grid.Position to = target.position();

		int dx = this.grid.xAxis().delta(from.x(), to.x());
		int dy = this.grid.yAxis().delta(from.y(), to.y());

		int closestDirection;

		if (Math.abs(dx) >= Math.abs(dy)) {
			closestDirection = dx >= 0 ? Orientation.EAST : Orientation.WEST;
		} else {
			closestDirection = dy >= 0 ? Orientation.SOUTH : Orientation.NORTH;
		}

		return closestDirection == wantedDirection;
	}

	public boolean faces(String directionName) {
		int orientation = Orientation.normalize(this.self.orientation());
		int direction = resolveDirection(directionName);

		return orientation == direction;
	}

	public boolean gotCondition(String categoryName) {
		String category = normalizeName(categoryName);

		if (isEnemy(category)) {
			return nEnemyLeft() == 1;
		}

		if (isDanger(category)) {
			Pengo pengo = findPengo();
			return pengo != null && pengo.nLives() <= 1;
		}

		return false;
	}

	private boolean free(Grid.Cell cell) {
		if (this.self instanceof Enemies) {
			Grid.Position center = this.grid.new Position(this.gridWidth / 2, this.gridHeight / 2);

			if (cell.position().equals(center)) {
				return false;
			}
		}

		for (Entity entity : cell.entities()) {
			if (entity == this.self || !entity.alive()) {
				continue;
			}

			if (entity instanceof Enemies) {
				return false;
			}

			if (this.self.blocks(entity) || entity.blocks(this.self)) {
				return false;
			}
		}

		return true;
	}

	private int nEnemyLeft() {
		int count = 0;

		for (Entity entity : this.self.model().entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	private Pengo findPengo() {
		for (Entity entity : this.self.model().entities()) {
			if (entity instanceof Pengo && entity.alive()) {
				return (Pengo) entity;
			}
		}

		return null;
	}

	private int resolveDirection(String directionName) {
		String direction = normalizeName(directionName);
		int orientation = Orientation.normalize(this.self.orientation());

		if (direction.length() == 0 || direction.equals("F") || direction.equals("FORWARD") || direction.equals("FRONT")
				|| direction.equals("H") || direction.equals("HERE")) {
			return orientation;
		}

		if (direction.equals("B") || direction.equals("BACK") || direction.equals("BACKWARD")) {
			return Orientation.backOf(orientation);
		}

		if (direction.equals("L") || direction.equals("LEFT")) {
			return Orientation.leftOf(orientation);
		}

		if (direction.equals("R") || direction.equals("RIGHT")) {
			return Orientation.rightOf(orientation);
		}

		if (direction.equals("N") || direction.equals("NORTH")) {
			return Orientation.NORTH;
		}

		if (direction.equals("S") || direction.equals("SOUTH")) {
			return Orientation.SOUTH;
		}

		if (direction.equals("E") || direction.equals("EAST")) {
			return Orientation.EAST;
		}

		if (direction.equals("W") || direction.equals("WEST") || direction.equals("O")) {
			return Orientation.WEST;
		}

		throw new UnsupportedOperationException("Direction GAL non supportee : " + directionName);
	}

	private static String normalizeName(String name) {
		if (name == null) {
			return "";
		}

		return name.trim().toUpperCase();
	}

	private static boolean isVoidCategory(String category) {
		return category.equals("V") || category.equals("VOID");
	}

	private static boolean isPlayerCategory(String category) {
		return category.equals("@") || category.equals("PLAYERT") || category.equals("PLAYER");
	}

	private static boolean isEnemy(String category) {
		return category.equals("A") || category.equals("ADVERSARY");
	}

	private static boolean isTeam(String category) {
		return category.equals("T") || category.equals("TEAM");
	}

	private static boolean isObstacle(String category) {
		return category.equals("O") || category.equals("OBSTACLE");
	}

	private static boolean isPushable(String category) {
		return category.equals("M") || category.equals("MOVEABLE");
	}

	private static boolean isPower(String category) {
		return category.equals("P") || category.equals("PICKABLE") || category.equals("POWER");
	}

	private static boolean isDanger(String category) {
		return category.equals("D") || category.equals("DANGER");
	}

	private static boolean containsAlive(List<Entity> entities, Class<?> type) {
		for (Entity entity : entities) {
			if (type.isInstance(entity) && entity.alive()) {
				return true;
			}
		}

		return false;
	}
}