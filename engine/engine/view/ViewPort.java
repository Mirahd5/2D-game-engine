package engine.view;

import engine.model.ISU;

public class ViewPort {

	private final ISU isu;
	private int canvasWidth_pix;
	private int canvasHeight_pix;
	private double x_cm;
	private double y_cm;
	private double width_cm;
	private double height_cm;

	// CONSTRUCTOR
	public ViewPort(ISU isu, int canvasWidth_pix, int canvasHeight_pix, double x_cm, double y_cm, double width_cm,
			double height_cm) {
		assert canvasWidth_pix > 0 : "canvas width must be positive";
		assert canvasHeight_pix > 0 : "canvas height must be positive";
		assert width_cm > 0.0 : "viewport width must be positive";
		assert height_cm > 0.0 : "viewport height must be positive";

		this.isu = isu;

		this.canvasWidth_pix = canvasWidth_pix;
		this.canvasHeight_pix = canvasHeight_pix;

		this.x_cm = x_cm;
		this.y_cm = y_cm;

		this.width_cm = width_cm;
		this.height_cm = height_cm;
	}


	public int toPixelX(double worldX_cm) {
		double localX_cm = worldX_cm - this.x_cm;
		return (int) Math.round(localX_cm * this.canvasWidth_pix / this.width_cm);
	}

	public int toPixelY(double worldY_cm) {
		double localY_cm = worldY_cm - this.y_cm;
		return (int) Math.round(localY_cm * this.canvasHeight_pix / this.height_cm);
	}


	public int toPixelWidth(double width_cm) {
		return (int) Math.round(width_cm * this.canvasWidth_pix / this.width_cm);
	}

	public int toPixelHeight(double height_cm) {
		return (int) Math.round(height_cm * this.canvasHeight_pix / this.height_cm);
	}


	public void setOrigin(double x_cm, double y_cm) {
		this.x_cm = x_cm;
		this.y_cm = y_cm;
	}

	public void centerOn(ISU.Coord center) {
		assert center != null : "center must not be null";

		this.x_cm = center.x() - this.width_cm / 2.0;
		this.y_cm = center.y() - this.height_cm / 2.0;
	}


	public void setCanvasSize(int width_pix, int height_pix) {
		assert width_pix > 0;
		assert height_pix > 0;

		this.canvasWidth_pix = width_pix;
		this.canvasHeight_pix = height_pix;
	}


	public boolean contains(ISU.Coord coord) {
		assert coord != null : "coord must not be null";

		return coord.x() >= this.x_cm && coord.x() <= this.x_cm + this.width_cm && coord.y() >= this.y_cm
				&& coord.y() <= this.y_cm + this.height_cm;
	}



	public ISU isu() {
		return this.isu;
	}

	public double x() {
		return this.x_cm;
	}

	public double y() {
		return this.y_cm;
	}

	public double width() {
		return this.width_cm;
	}

	public double height() {
		return this.height_cm;
	}

	public int canvasWidth() {
		return this.canvasWidth_pix;
	}

	public int canvasHeight() {
		return this.canvasHeight_pix;
	}
}