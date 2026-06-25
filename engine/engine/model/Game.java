package engine.model;

import java.io.PrintStream;

import engine.view.Picture;

/**
 * Configuration générale utilisée pour construire les objets du moteur.
 */
public abstract class Game {

	private static final double EPSILON = 1e-6;

	private final boolean torusOnXaxis;
	private final boolean torusOnYaxis;

	private final double cmPerCell;
	private final int pixelPerCm;

	private final int width_ncell;
	private final int height_ncell;

	private final double width_cm;
	private final double height_cm;

	private final Grid grid;
	private final ISU isu;
	private final Picture picture;
	private final Model model;

	public Game(int width_ncell, int height_ncell, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell,
			int pixelPerCm) {
		this(width_ncell, height_ncell, width_ncell * cmPerCell, height_ncell * cmPerCell, torusOnXaxis, torusOnYaxis,
				cmPerCell, pixelPerCm);
	}

	public Game(double width_cm, double height_cm, boolean torusOnXaxis, boolean torusOnYaxis, double cmPerCell,
			int pixelPerCm) {
		this(cellCount(width_cm, cmPerCell), cellCount(height_cm, cmPerCell), width_cm, height_cm, torusOnXaxis,
				torusOnYaxis, cmPerCell, pixelPerCm);
	}

	private Game(int width_ncell, int height_ncell, double width_cm, double height_cm, boolean torusOnXaxis,
			boolean torusOnYaxis, double cmPerCell, int pixelPerCm) {
		assert width_ncell > 0 : width_ncell;
		assert height_ncell > 0 : height_ncell;
		assert width_cm > 0.0 : width_cm;
		assert height_cm > 0.0 : height_cm;
		assert cmPerCell > 0.0 : cmPerCell;
		assert pixelPerCm > 0 : pixelPerCm;

		assert Math.abs(width_ncell * cmPerCell - width_cm) < EPSILON;
		assert Math.abs(height_ncell * cmPerCell - height_cm) < EPSILON;

		this.torusOnXaxis = torusOnXaxis;
		this.torusOnYaxis = torusOnYaxis;

		this.cmPerCell = cmPerCell;
		this.pixelPerCm = pixelPerCm;

		this.width_ncell = width_ncell;
		this.height_ncell = height_ncell;

		this.width_cm = width_cm;
		this.height_cm = height_cm;

		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.isu.set(this.grid);

		this.picture = new Picture(this);
		this.model = new Model(this);
	}

	private static int cellCount(double length_cm, double cmPerCell) {
		assert length_cm > 0.0 : length_cm;
		assert cmPerCell > 0.0 : cmPerCell;

		double count = length_cm / cmPerCell;

		assert Math.abs(count - Math.round(count)) < EPSILON : "dimension must contain an integer number of cells";

		return (int) Math.round(count);
	}

	public Game game() {
		return this;
	}

	public double getWidthCm() {
		return this.width_cm;
	}

	public double getHeightCm() {
		return this.height_cm;
	}

	public int getWidthCell() {
		return this.width_ncell;
	}

	public int getHeightCell() {
		return this.height_ncell;
	}

	public double getWidthPixel() {
		return this.width_cm * this.pixelPerCm;
	}

	public double getHeightPixel() {
		return this.height_cm * this.pixelPerCm;
	}

	public double getCmPerCell() {
		return this.cmPerCell;
	}

	public int getPixelPerCm() {
		return this.pixelPerCm;
	}

	public boolean isTorusOnXaxis() {
		return this.torusOnXaxis;
	}

	public boolean isTorusOnYaxis() {
		return this.torusOnYaxis;
	}

	public boolean getTorusOnXaxis() {
		return this.torusOnXaxis;
	}

	public boolean getTorusOnYaxis() {
		return this.torusOnYaxis;
	}

	public ISU getIsu() {
		return this.isu;
	}

	public Grid getGrid() {
		return this.grid;
	}

	public Picture getPicture() {
		return this.picture;
	}

	public Model model() {
		return this.model;
	}

	public void show(PrintStream ps) {
		ps.println("Game:");
		ps.println("width_ncell = " + this.width_ncell);
		ps.println("height_ncell = " + this.height_ncell);
		ps.println("width_cm = " + this.width_cm);
		ps.println("height_cm = " + this.height_cm);
		ps.println("cmPerCell = " + this.cmPerCell);
		ps.println("pixelPerCm = " + this.pixelPerCm);
		ps.println("torusOnXaxis = " + this.torusOnXaxis);
		ps.println("torusOnYaxis = " + this.torusOnYaxis);
	}
}