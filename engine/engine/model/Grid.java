// = GRID =
package engine.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class Grid {

	// FIELDS
	private final ISU isu;

	private final Axis xAxis, yAxis;

	private final int width_ncell, height_ncell;

	private final double cmPerCell;

	private final Cell[][] grid;

	// CONSTRUCTOR

	public Grid(Game game) {

		this.isu = game.isu();
		this.width_ncell = game.width_ncell();
		this.height_ncell = game.height_ncell();
		this.cmPerCell = game.cmPerCell();
		assert this.width_ncell > 0;
		assert this.height_ncell > 0;
		assert this.cmPerCell > 0.0;
		this.xAxis = new Axis(game.torusOnXaxis(), this.width_ncell);
		this.yAxis = new Axis(game.torusOnYaxis(), this.height_ncell);
		this.grid = new Cell[this.width_ncell][this.height_ncell];
		init();
	}

	// INIT

	private void init() {
		for (int x = 0; x < this.width_ncell; x++) {
			for (int y = 0; y < this.height_ncell; y++) {
				Position p = new Position(x, y);
				this.grid[x][y] = new Cell(p);
				assert this.grid[x][y].position().equiv(p);
			}
		}
	}

	// GETTERS

	public int width() {
		return this.width_ncell;
	}

	public int height() {
		return this.height_ncell;
	}

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public ISU isu() {
		return this.isu;
	}

	public Grid.Cell cellAt(Grid.Position p) {

		int x = this.xAxis.normalize(p.x());
		int y = this.yAxis.normalize(p.y());

		assert x >= 0 && x < this.width_ncell;
		assert y >= 0 && y < this.height_ncell;

		Cell c = this.grid[x][y];

		assert c.position().equiv(new Position(x, y));

		return c;
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("===== GRID =====");
		ps.println("width_ncell  = " + this.width_ncell);
		ps.println("height_ncell = " + this.height_ncell);
		ps.println("cmPerCell    = " + this.cmPerCell);
	}

	// DIMENSION (nb cell)

	public class Dimension {

		protected int x_ncell;
		protected int y_ncell;

		// CONSTRUCTOR

		public Dimension(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
			normalize();
		}

		// GETTERS

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		// GEOMETRY
		public void normalize() {
			assert x_ncell > 0;
			assert y_ncell > 0;
		}

		// EQUALS / EQUIV

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Grid.Dimension)) {
				return false;
			}

			Dimension d = (Dimension) o;
			return equiv(d);
		}

		public boolean equiv(Dimension d) {
			return d != null && this.x_ncell == d.x_ncell && this.y_ncell == d.y_ncell;
		}

		// CONVERSION

		public ISU.Dimension toISUDimension() {
			return isu.new Dimension(this.x_ncell * Grid.this.cmPerCell, this.y_ncell * Grid.this.cmPerCell);
		}

		// SHOW

		public void show(PrintStream ps) {
			ps.println("Dimension(" + x_ncell + ", " + y_ncell + ") ncell");
		}
	}

	// VECTOR

	public class Vector {

		private int x_ncell;
		private int y_ncell;

		// CONSTRUCTOR
		public Vector(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		// GETTERS
		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		// OPERATION
		public void add(Vector v) {
			assert v != null;

			this.x_ncell += v.x_ncell;
			this.y_ncell += v.y_ncell;
		}

		// SHOW
		public void show(PrintStream ps) {
			ps.println("Vector(" + x_ncell + ", " + y_ncell + ") ncell");
		}
	}

	// == POINT / POSITION ==

	public class Position {

		private int x_ncell;
		private int y_ncell;

		// CONSTRUCTOR

		public Position(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
			normalize();
		}

		// GETTERS
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
			this.x_ncell = xAxis.normalize(this.x_ncell);
			this.y_ncell = yAxis.normalize(this.y_ncell);
		}

		// COPY
		public Grid.Position copy() {
			return new Position(this.x_ncell, this.y_ncell);
		}

		// EQUALS
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Grid.Position)) {
				return false;
			}

			Position p = (Position) o;
			return equiv(p);
		}

		public boolean equiv(Position p) {
			return p != null && this.x_ncell == p.x_ncell && this.y_ncell == p.y_ncell;
		}

		// TRANSLATION

		public void translate(Vector v) {
			this.x_ncell = xAxis.normalize(this.x_ncell + v.x());
			this.y_ncell = yAxis.normalize(this.y_ncell + v.y());
		}

		public void moveNorth(int n_ncell) {
			this.y_ncell = yAxis.normalize(this.y_ncell - n_ncell);
		}

		// ROTATION
		public void rotateAround(Grid.Position position, int angle_degree) {
			assert position != null;

			double angle_rad = Math.toRadians(angle_degree);

			int dx = this.x_ncell - position.x_ncell;
			int dy = this.y_ncell - position.y_ncell;

			int newX = position.x_ncell + (int) Math.round(dx * Math.cos(angle_rad) - dy * Math.sin(angle_rad));

			int newY = position.y_ncell + (int) Math.round(dx * Math.sin(angle_rad) + dy * Math.cos(angle_rad));

			this.x_ncell = newX;
			this.y_ncell = newY;

			normalize();
		}

		// DISTANCE
		public double distanceTo(Position p) {
			assert p != null;

			double dx = xAxis.distance(this.x_ncell, p.x_ncell);
			double dy = yAxis.distance(this.y_ncell, p.y_ncell);

			return Math.sqrt(dx * dx + dy * dy);
		}

		// CONVERSION

		public ISU.Coord toISUCoord() {
			return isu.new Coord((this.x_ncell - 0.5) * Grid.this.cmPerCell,
					(this.y_ncell - 0.5) * Grid.this.cmPerCell);
		}

		public ISU.Coord toISUCoordCentered() {
			return isu.new Coord(this.x_ncell * Grid.this.cmPerCell, this.y_ncell * Grid.this.cmPerCell);
		}

		// SHOW

		public void show(PrintStream ps) {
			ps.println("Position(" + x_ncell + ", " + y_ncell + ") ncell");
		}
	}

		// CELL
	public class Cell {

		private final Grid.Dimension size;
		private final Grid.Position position;
		private final List<Entity> entities;

		// CONSTRUCTOR
	
		public Cell(Position p) {
			this.size = new Dimension(1, 1);
			this.position = p.copy();
			this.entities = new ArrayList<>();
		}

		
		// GETTERS
		public Dimension size() {
			return this.size;
		}

	
		public Position position() {
			return this.position.copy();
		}
		
		// ADD
		public void add(Entity e) {

			if (!this.entities.contains(e)) {
				this.entities.add(e);
			}

			assert contains(e);
		}
		
		public void remove(Entity e) {
			assert e != null;

			this.entities.remove(e);

			assert !contains(e);
		}

		// PREDICATE
		public boolean contains(Entity e) {
			return this.entities.contains(e);
		}

		// SHOW
		public void show(PrintStream ps) {
			ps.print("Cell ");
			this.position.show(ps);
			ps.println("entities = " + this.entities.size());
		}
	}
}