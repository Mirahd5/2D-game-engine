// == GHOST BOT ==
package game.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.brain.Bot;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import game.model.Ghost;
import game.model.PacMan;

public class GhostBot extends Bot {

	private static final int EAST = BasicStunt.EAST;
	private static final int SOUTH = BasicStunt.SOUTH;
	private static final int WEST = BasicStunt.WEST;
	private static final int NORTH = BasicStunt.NORTH;

	private static final int[] DIRECTIONS = { EAST, SOUTH, WEST, NORTH };

	private final Ghost ghost;
	private final PacMan pacman;
	private final BasicStunt pacmanStunt;
	private final List<Entity> walls;
	private final Random random;

	private int lastDirection;
	private int ticks;

	public GhostBot(BasicStunt stunt, Ghost ghost, PacMan pacman, BasicStunt pacmanStunt,
			List<? extends Entity> walls) {

		super(stunt);
		this.ghost = ghost;
		this.pacman = pacman;
		this.pacmanStunt = pacmanStunt;
		this.walls = new ArrayList<Entity>(walls);
		this.random = new Random(ghost.type().ordinal() + 1L);
		this.lastDirection = EAST;
		this.ticks = 0;
	}

	@Override
	public void think() {
		chooseMove();
	}

	@Override
	public void done() {
		chooseMove();
	}

	@Override
	public void collision(Entity other) {
		chooseMove();
	}

	@Override
	public void collision(List<Entity> others) {
		chooseMove();
	}

	private void chooseMove() {
		if (!ghost.alive())
			return;
		if (stunt.isMoving())
			return;

		ticks++;

		switch (ghost.type()) {
		case BLINKY:
			moveToward(pacman.position(), false, 0);
			break;

		case SPEEDY:
			moveToward(positionAheadOfPacman(4), false, 0);
			break;

		case INKY:
			if (ticks % 3 == 0) {
				moveRandomly();
			} else {
				moveToward(pacman.position(), false, 2);
			}
			break;

		case CLYDE:
			if (cellDistance(ghost.position(), pacman.position()) <= 8) {
				moveToward(pacman.position(), true, 0);
			} else {
				moveToward(pacman.position(), false, 0);
			}
			break;

		default:
			moveRandomly();
			break;
		}
	}

	private void moveToward(Grid.Position target, boolean flee, int randomNoise) {
		int bestDirection = Integer.MIN_VALUE;
		double bestScore = Double.MAX_VALUE;

		int reverse = opposite(lastDirection);
		boolean foundWithoutReverse = false;

		for (int direction : DIRECTIONS) {
			if (direction == reverse)
				continue;
			if (isBlocked(direction))
				continue;

			double score = score(direction, target, flee, randomNoise);

			if (score < bestScore) {
				bestScore = score;
				bestDirection = direction;
				foundWithoutReverse = true;
			}
		}

		if (!foundWithoutReverse) {
			for (int direction : DIRECTIONS) {
				if (isBlocked(direction))
					continue;

				double score = score(direction, target, flee, randomNoise);

				if (score < bestScore) {
					bestScore = score;
					bestDirection = direction;
				}
			}
		}

		if (bestDirection != Integer.MIN_VALUE) {
			lastDirection = bestDirection;
			stunt.walk(bestDirection);
		}
	}

	private double score(Grid.Position nextDirection, Grid.Position target, boolean flee, int randomNoise) {
		return 0.0; // unused overload guard
	}

	private double score(int direction, Grid.Position target, boolean flee, int randomNoise) {
		Grid.Position next = nextPosition(direction);
		double distance = cellDistance(next, target);

		if (flee) {
			distance = -distance;
		}

		if (randomNoise > 0) {
			distance += random.nextInt(randomNoise + 1);
		}

		return distance;
	}

	private void moveRandomly() {
		List<Integer> possible = new ArrayList<Integer>();
		int reverse = opposite(lastDirection);

		for (int direction : DIRECTIONS) {
			if (direction != reverse && !isBlocked(direction)) {
				possible.add(direction);
			}
		}

		if (possible.isEmpty()) {
			for (int direction : DIRECTIONS) {
				if (!isBlocked(direction)) {
					possible.add(direction);
				}
			}
		}

		if (!possible.isEmpty()) {
			int direction = possible.get(random.nextInt(possible.size()));
			lastDirection = direction;
			stunt.walk(direction);
		}
	}

	private Grid.Position positionAheadOfPacman(int nCells) {
		Grid.Position target = pacman.position();
		Grid grid = target.grid();

		for (int i = 0; i < nCells; i++) {
			target.translate(vectorFor(grid, pacmanStunt.direction()));
		}

		return target;
	}

	private boolean isBlocked(int direction) {
		Grid.Position next = nextPosition(direction);

		for (Entity wall : walls) {
			if (wall.position().equiv(next)) {
				return true;
			}
		}

		return false;
	}

	private Grid.Position nextPosition(int direction) {
		Grid.Position current = ghost.position();
		Grid grid = current.grid();
		Grid.Position next = current.copy();
		next.translate(vectorFor(grid, direction));
		return next;
	}

	private Grid.Vector vectorFor(Grid grid, int direction) {
		switch (direction) {
		case EAST:
			return grid.new Vector(1, 0);
		case WEST:
			return grid.new Vector(-1, 0);
		case SOUTH:
			return grid.new Vector(0, 1);
		case NORTH:
			return grid.new Vector(0, -1);
		default:
			return grid.new Vector(0, 0);
		}
	}

	private int opposite(int direction) {
		switch (direction) {
		case EAST:
			return WEST;
		case WEST:
			return EAST;
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		default:
			return EAST;
		}
	}

	private double cellDistance(Grid.Position a, Grid.Position b) {
		int dx = Math.abs(a.x() - b.x());
		int dy = Math.abs(a.y() - b.y());
		Grid grid = a.grid();
		dx = Math.min(dx, grid.width() - dx);

		return dx + dy;
	}
}