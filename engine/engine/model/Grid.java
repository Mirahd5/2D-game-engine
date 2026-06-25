package engine.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import engine.view.Picture;

/**
 * Grille du monde exprimée en nombre de cellules.
 */
public class Grid {

	private final Game game;
	private final ISU isu;

	private final Axis xAxis;
	private final Axis yAxis;

	private final int width_ncell;
	private final int height_ncell;

	private final Cell[][] grid;

	public Grid(Game game) {
		this.game = game;
		this.isu = game.getIsu();

		this.width_ncell = game.getWidthCell();
		this.height_ncell = game.getHeightCell();

		this.xAxis = new Axis(game.getTorusOnXaxis(), this.width_ncell);
		this.yAxis = new Axis(game.getTorusOnYaxis(), this.height_ncell);

		this.grid = new Cell[this.width_ncell][this.height_ncell];

		init();
	}

	private void init() {
		for (int x = 0; x < this.width_ncell; x++) {
			for (int y = 0; y < this.height_ncell; y++) {
				this.grid[x][y] = new Cell(new Position(x, y));
			}
		}
	}

	public int width() {
		return this.width_ncell;
	}

	public int height() {
		return this.height_ncell;
	}

	public ISU isu() {
		return this.isu;
	}

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public Grid.Cell cellAt(Grid.Position position) {
		Grid.Position normalized = position.copy();
		normalized.normalize();

		assert contains(normalized) : "position outside non-toric grid";

		return this.grid[normalized.x()][normalized.y()];
	}

	public boolean contains(Grid.Position position) {
		return position.x() >= 0 && position.x() < this.width_ncell && position.y() >= 0
				&& position.y() < this.height_ncell;
	}

	public void show(PrintStream ps) {
		ps.println("Grid:");
		ps.println("width  = " + this.width_ncell + " ncell");
		ps.println("height = " + this.height_ncell + " ncell");
	}

	// == DIMENSION ==

	public class Dimension {

		private int x_ncell;
		private int y_ncell;

		public Dimension(int x_ncell, int y_ncell) {
			assert x_ncell > 0 : x_ncell;
			assert y_ncell > 0 : y_ncell;

			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;

			normalize();

			assert this.x_ncell > 0;
			assert this.y_ncell > 0;
			assert this.x_ncell <= Grid.this.width();
			assert this.y_ncell <= Grid.this.height();
		}

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		public Grid grid() {
			return Grid.this;
		}

		public void normalize() {
			this.x_ncell = Grid.this.xAxis.normalizeDimension(this.x_ncell);
			this.y_ncell = Grid.this.yAxis.normalizeDimension(this.y_ncell);
		}

		public Dimension copy() {
			return new Dimension(this.x_ncell, this.y_ncell);
		}

		public boolean equiv(Dimension dimension) {
			return dimension.grid() == Grid.this && this.x_ncell == dimension.x_ncell
					&& this.y_ncell == dimension.y_ncell;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Grid.Dimension)) {
				return false;
			}

			return equiv((Dimension) object);
		}

		@Override
		public int hashCode() {
			int result = System.identityHashCode(Grid.this);
			result = 31 * result + this.x_ncell;
			result = 31 * result + this.y_ncell;
			return result;
		}

		public ISU.Dimension toISUDimension() {
			double x_cm = this.x_ncell * Grid.this.isu.cmPerCell();
			double y_cm = this.y_ncell * Grid.this.isu.cmPerCell();

			return Grid.this.isu.new Dimension(x_cm, y_cm);
		}

		public void show(PrintStream ps) {
			ps.println("Dimension(" + this.x_ncell + ", " + this.y_ncell + ") ncell");
		}
	}

	// == VECTOR ==

	public class Vector {

		private int x_ncell;
		private int y_ncell;

		public Vector(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		public Grid grid() {
			return Grid.this;
		}

		public void add(Vector vector) {
			assert vector.grid() == Grid.this;

			this.x_ncell += vector.x_ncell;
			this.y_ncell += vector.y_ncell;
		}

		public Vector copy() {
			return new Vector(this.x_ncell, this.y_ncell);
		}

		public void show(PrintStream ps) {
			ps.println("Vector(" + this.x_ncell + ", " + this.y_ncell + ") ncell");
		}
	}

	// == POSITION ==

	public class Position {

		private int x_ncell;
		private int y_ncell;

		public Position(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;

			normalize();
		}

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		public Grid grid() {
			return Grid.this;
		}

		public void normalize() {
			this.x_ncell = Grid.this.xAxis.normalize(this.x_ncell);
			this.y_ncell = Grid.this.yAxis.normalize(this.y_ncell);
		}

		public Grid.Position copy() {
			return new Position(this.x_ncell, this.y_ncell);
		}

		public void translate(Vector vector) {
			assert vector.grid() == Grid.this;

			this.x_ncell += vector.x_ncell;
			this.y_ncell += vector.y_ncell;

			normalize();
		}

		public void moveNorth(int n_ncell) {
			assert n_ncell >= 0 : n_ncell;

			this.y_ncell -= n_ncell;
			normalize();
		}

		public void moveSouth(int n_ncell) {
			assert n_ncell >= 0 : n_ncell;

			this.y_ncell += n_ncell;
			normalize();
		}

		public void moveEast(int n_ncell) {
			assert n_ncell >= 0 : n_ncell;

			this.x_ncell += n_ncell;
			normalize();
		}

		public void moveWest(int n_ncell) {
			assert n_ncell >= 0 : n_ncell;

			this.x_ncell -= n_ncell;
			normalize();
		}

		public void rotateAround(Grid.Position center, int angleDegree) {
			assert center.grid() == Grid.this;

			int dx = Grid.this.xAxis.delta(center.x_ncell, this.x_ncell);
			int dy = Grid.this.yAxis.delta(center.y_ncell, this.y_ncell);

			int newDx;
			int newDy;

			int angle = normalizeAngle(angleDegree);

			switch (angle) {
			case 0:
				newDx = dx;
				newDy = dy;
				break;

			case 90:
				newDx = -dy;
				newDy = dx;
				break;

			case 180:
				newDx = -dx;
				newDy = -dy;
				break;

			case 270:
				newDx = dy;
				newDy = -dx;
				break;

			default:
				throw new IllegalArgumentException("Only 0, 90, 180 and 270 degrees are supported on Grid");
			}

			this.x_ncell = center.x_ncell + newDx;
			this.y_ncell = center.y_ncell + newDy;

			normalize();
		}

		public double distanceTo(Position position) {
			assert position.grid() == Grid.this;

			double dx = Grid.this.xAxis.distance(this.x_ncell, position.x_ncell);
			double dy = Grid.this.yAxis.distance(this.y_ncell, position.y_ncell);

			return Math.sqrt(dx * dx + dy * dy);
		}

		public ISU.Coord toISUCoord() {
			double x_cm = this.x_ncell * Grid.this.isu.cmPerCell();
			double y_cm = this.y_ncell * Grid.this.isu.cmPerCell();

			return Grid.this.isu.new Coord(x_cm, y_cm);
		}

		public ISU.Coord toISUCoordCentered() {
			double x_cm = (this.x_ncell + 0.5) * Grid.this.isu.cmPerCell();
			double y_cm = (this.y_ncell + 0.5) * Grid.this.isu.cmPerCell();

			return Grid.this.isu.new Coord(x_cm, y_cm);
		}

		public Picture.Pixel toPicturePixel() {
			return Grid.this.game.getPicture().from(this);
		}

		public Picture.Pixel toPicturePixelCentered() {
			return Grid.this.game.getPicture().from(toISUCoordCentered());
		}

		public boolean equiv(Position position) {
			return position.grid() == Grid.this && this.x_ncell == position.x_ncell && this.y_ncell == position.y_ncell;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Grid.Position)) {
				return false;
			}

			return equiv((Position) object);
		}

		@Override
		public int hashCode() {
			int result = System.identityHashCode(Grid.this);
			result = 31 * result + this.x_ncell;
			result = 31 * result + this.y_ncell;
			return result;
		}

		public void show(PrintStream ps) {
			ps.println("Position(" + this.x_ncell + ", " + this.y_ncell + ")");
		}
	}

	// == CELL ==

	public class Cell {

		private final Grid.Dimension size;
		private final Grid.Position position;
		private final List<Entity> entities;

		public Cell(Position position) {
			this.size = new Dimension(1, 1);
			this.position = position.copy();
			this.entities = new ArrayList<Entity>();
		}

		public Grid.Dimension size() {
			return this.size.copy();
		}

		public Grid.Position position() {
			return this.position.copy();
		}

		public List<Entity> entities() {
			return new ArrayList<Entity>(this.entities);
		}

		public void add(Entity entity) {
			if (!this.entities.contains(entity)) {
				this.entities.add(entity);
			}
		}

		public void remove(Entity entity) {
			this.entities.remove(entity);
		}

		public boolean contains(Entity entity) {
			return this.entities.contains(entity);
		}

		public void show(PrintStream ps) {
			ps.println("Cell at (" + this.position.x() + ", " + this.position.y() + ")");
			ps.println("Number of entities: " + this.entities.size());
		}
	}

	private static int normalizeAngle(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}
}