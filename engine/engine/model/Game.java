// == GAME ==
package engine.model;

import java.io.PrintStream;

public class Game {

	// CONSTANT

	protected final boolean torusOnXaxis; // vrai si l'axe X est une boucle fermée
	protected final boolean torusOnYaxis; // vrai si l'axe Y est une boucle fermée

	protected final double cmPerCell; // échelle qui relie l'unité ncell à cm
	protected final int pixelPerCm; // échelle qui relie l'unité pixel à cm

	// FIELDS

	protected final int width_ncell; // largeur du monde en nombre de cellules
	protected final int height_ncell; // hauteur du monde en nombre de celluls

	protected final double width_cm; // largeur du monde en cm
	protected final double height_cm; // hauteur du monde en cm

	protected final Grid grid; // permet la création de coordonnées en unités ncell
	protected final ISU isu; // permet la création de coordonnées en unités cm
	protected Picture pict; // permet la création de coordonnées en unités pixel, ne sera utilisé qu'à
							// partir de Task2

	// CONSTRUCTORS

	protected Game(int w_ncell, int h_ncell, boolean torusOnX, boolean torusOnY, double cmParCell, int pixelParCm) {
		assert w_ncell > 0;
		assert h_ncell > 0;
		assert cmParCell > 0;
		assert pixelParCm > 0;

		this.width_ncell = w_ncell;
		this.height_ncell = h_ncell;

		this.torusOnXaxis = torusOnX;
		this.torusOnYaxis = torusOnY;

		this.cmPerCell = cmParCell;
		this.pixelPerCm = pixelParCm;

		this.width_cm = this.width_ncell * this.cmPerCell;
		this.height_cm = this.height_ncell * this.cmPerCell;

		assert this.width_cm == this.width_ncell * this.cmPerCell;
		assert this.height_cm == this.height_ncell * this.cmPerCell;

		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.isu.set(this.grid);

		this.pict = new Picture(this);
	}

	protected Game(double w_cm, double h_cm, boolean torusOnX, boolean torusOnY, double cmParCell, int pixelParCm) {
		assert w_cm > 0;
		assert h_cm > 0;
		assert cmParCell > 0;
		assert pixelParCm > 0;
		
		 this.torusOnXaxis = torusOnX;
	     this.torusOnYaxis = torusOnY;

	     this.cmPerCell = cmParCell;
	     this.pixelPerCm = pixelParCm;
	     
	     double w = w_cm / cmPerCell;
	     double h = h_cm / cmPerCell;
	     
	     assert w == Math.floor(w); //floor(7.9)=7
	     assert h == Math.floor(h);
	     
	     this.width_ncell = (int) w;
	     this.height_ncell = (int) h;
	     
	     this.width_cm = w_cm;
	     this.height_cm = h_cm;
	     
	     assert this.width_cm == this.width_ncell * this.cmPerCell;
	     assert this.height_cm == this.height_ncell * this.cmPerCell;
	     

	     this.isu = new ISU(this);
	     this.grid = new Grid(this);
	     this.isu.set(this.grid);

	     //this.pict = null;
	}

	// GETTER

	Game game;

	public Game game() {
		return this;
	}
	
	public boolean torusOnXaxis() {
        return torusOnXaxis;
    }

    public boolean torusOnYaxis() {
        return torusOnYaxis;
    }

    public double cmPerCell() {
        return cmPerCell;
    }

    public int pixelPerCm() {
        return pixelPerCm;
    }

    public int width_ncell() {
        return width_ncell;
    }

    public int height_ncell() {
        return height_ncell;
    }

    public double width_cm() {
        return width_cm;
    }

    public double height_cm() {
        return height_cm;
    }

    public Grid grid() {
        return grid;
    }

    public ISU isu() {
        return isu;
    }

    public Picture pict() {
        return pict;
    }

	// SHOW

	public void show(PrintStream ps) {
		ps.println("=== GAME ===");
        ps.println("torusOnXaxis = " + torusOnXaxis);
        ps.println("torusOnYaxis = " + torusOnYaxis);
        ps.println("cmPerCell    = " + cmPerCell);
        ps.println("pixelPerCm   = " + pixelPerCm);
        ps.println("width_ncell  = " + width_ncell);
        ps.println("height_ncell = " + height_ncell);
        ps.println("width_cm     = " + width_cm);
        ps.println("height_cm    = " + height_cm);
	}
}
