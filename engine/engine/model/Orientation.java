package engine.model;

/**
 * Utilitaires pour les orientations cardinales du moteur.
 */
public final class Orientation {

	public static final int EAST = 0;
	public static final int SOUTH = 90;
	public static final int WEST = 180;
	public static final int NORTH = 270;

	private Orientation() {
	}

	public static int normalize(int degree) {
		int normalized = ((degree % 360) + 360) % 360;

		if (normalized < 45 || normalized >= 315) {
			return EAST;
		}

		if (normalized < 135) {
			return SOUTH;
		}

		if (normalized < 225) {
			return WEST;
		}

		return NORTH;
	}

	public static int leftOf(int orientation) {
		return normalize(orientation - 90);
	}

	public static int rightOf(int orientation) {
		return normalize(orientation + 90);
	}

	public static int backOf(int orientation) {
		return normalize(orientation + 180);
	}

	public static boolean opposite(int first, int second) {
		return backOf(first) == normalize(second);
	}

	public static Grid.Vector vector(Grid grid, int direction) {
		switch (normalize(direction)) {
		case EAST:
			return grid.new Vector(1, 0);

		case SOUTH:
			return grid.new Vector(0, 1);

		case WEST:
			return grid.new Vector(-1, 0);

		case NORTH:
			return grid.new Vector(0, -1);

		default:
			throw new AssertionError("Direction must be cardinal");
		}
	}

	public static Grid.Position neighbor(Grid grid, Grid.Position from, int direction) {
		Grid.Vector vector = vector(grid, direction);

		int x = from.x() + vector.x();
		int y = from.y() + vector.y();

		if (!grid.xAxis().onTorus() && (x < 0 || x >= grid.width())) {
			return null;
		}

		if (!grid.yAxis().onTorus() && (y < 0 || y >= grid.height())) {
			return null;
		}

		return grid.new Position(x, y);
	}
}