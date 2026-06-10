package engine.brain;

import java.util.ArrayList;
import java.util.List;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;

public class AttackBot extends Bot {

	private Entity target;

	private List<Entity> walls;

	private int lastDirection;

	public AttackBot(BasicStunt stunt, Entity target) {
		this(stunt, target, new ArrayList<Entity>());
	}

	public AttackBot(BasicStunt stunt, Entity target, List<Entity> walls) {
		super(stunt);
		this.target = target;
		this.walls = new ArrayList<Entity>(walls);
		this.lastDirection = BasicStunt.EAST;
	}

	public void setWalls(List<Entity> walls) {
		this.walls = new ArrayList<Entity>(walls);
	}

	@Override
	public void think() {
		walkTowardTarget();
	}

	@Override
	public void done() {
		walkTowardTarget();
	}

	@Override
	public void collision(Entity other) {
		// Si on touche un mur, on retente une autre direction.
		walkTowardTarget();
	}

	@Override
	public void collision(List<Entity> others) {
		walkTowardTarget();
	}

	private void walkTowardTarget() {
		if (stunt.isMoving()) {
			return;
		}
		ISU.Coord myCenter = entity().center();
		ISU.Coord targetCenter = target.center();
		double dx = targetCenter.x() - myCenter.x();
		double dy = targetCenter.y() - myCenter.y();
		int horizontal = dx >= 0 ? BasicStunt.EAST : BasicStunt.WEST;
		int vertical = dy >= 0 ? BasicStunt.SOUTH : BasicStunt.NORTH;
		int[] directions;

		if (Math.abs(dx) >= Math.abs(dy)) {
			directions = new int[] { horizontal, vertical, opposite(vertical), opposite(horizontal) };
		} else {
			directions = new int[] { vertical, horizontal, opposite(horizontal), opposite(vertical) };
		}

		int reverse = opposite(lastDirection);
		for (int direction : directions) {
			if (direction != reverse && !isBlocked(direction)) {
				lastDirection = direction;
				stunt.walk(direction);
				return;
			}
		}

		for (int direction : directions) {
			if (!isBlocked(direction)) {
				lastDirection = direction;
				stunt.walk(direction);
				return;
			}
		}
	}

	private boolean isBlocked(int direction) {
		Grid.Position current = entity().position();
		Grid grid = current.grid();

		Grid.Position next = current.copy();
		next.translate(vectorFor(grid, direction));

		for (Entity wall : walls) {
			if (wall.position().equiv(next)) {
				return true;
			}
		}

		return false;
	}

	private Grid.Vector vectorFor(Grid grid, int direction) {
		switch (direction) {
		case BasicStunt.EAST:
			return grid.new Vector(1, 0);

		case BasicStunt.WEST:
			return grid.new Vector(-1, 0);

		case BasicStunt.SOUTH:
			return grid.new Vector(0, 1);

		case BasicStunt.NORTH:
			return grid.new Vector(0, -1);

		default:
			return grid.new Vector(0, 0);
		}
	}

	private int opposite(int direction) {
		switch (direction) {
		case BasicStunt.EAST:
			return BasicStunt.WEST;

		case BasicStunt.WEST:
			return BasicStunt.EAST;

		case BasicStunt.NORTH:
			return BasicStunt.SOUTH;

		case BasicStunt.SOUTH:
			return BasicStunt.NORTH;

		default:
			return BasicStunt.EAST;
		}
	}
}