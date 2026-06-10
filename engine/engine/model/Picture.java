package engine.model;

public class Picture {

	private final Game game;
	private final int pixelPerCm;
	private final int width_pix;
	private final int height_pix;

	public Picture(Game game) {

		this.game = game;
		this.pixelPerCm = game.pixelPerCm();
		this.width_pix = toPixelLength(game.width_cm());
		this.height_pix = toPixelLength(game.height_cm());
	}

	public int toPixelLength(double length_cm) {
		return (int) Math.round(length_cm * this.pixelPerCm);
	}

	public int toPixelX(double x_cm) {
		return toPixelLength(x_cm);
	}

	public int toPixelY(double y_cm) {
		return toPixelLength(y_cm);
	}

	public Pixel toPixel(ISU.Coord coord) {
		return new Pixel(toPixelX(coord.x()), toPixelY(coord.y()));
	}

	public Game game() {
		return this.game;
	}

	public int pixelPerCm() {
		return this.pixelPerCm;
	}

	public int width() {
		return this.width_pix;
	}

	public int height() {
		return this.height_pix;
	}

	public class Pixel {

		private final int x_pix;
		private final int y_pix;

		public Pixel(int x_pix, int y_pix) {
			this.x_pix = x_pix;
			this.y_pix = y_pix;
		}

		public int x() {
			return this.x_pix;
		}

		public int y() {
			return this.y_pix;
		}
	}
}