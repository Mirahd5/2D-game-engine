package engine.view;

import java.io.PrintStream;

import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;

/**
 * Repère graphique du jeu, exprimé en pixels.
 */
public final class Picture {

	private final Game game;
	private final ISU isu;

	private final int pixelPerCm;

	private final int widthPixel;
	private final int heightPixel;

	public Picture(Game game) {
		assert game.getPixelPerCm() > 0 : game.getPixelPerCm();

		this.game = game;
		this.isu = game.getIsu();

		this.pixelPerCm = game.getPixelPerCm();

		this.widthPixel = (int) Math.ceil(game.getWidthCm() * this.pixelPerCm);
		this.heightPixel = (int) Math.ceil(game.getHeightCm() * this.pixelPerCm);

		assert this.widthPixel > 0 : this.widthPixel;
		assert this.heightPixel > 0 : this.heightPixel;
	}

	public Game game() {
		return this.game;
	}

	public ISU isu() {
		return this.isu;
	}

	public int pixelPerCm() {
		return this.pixelPerCm;
	}

	public int width() {
		return this.widthPixel;
	}

	public int height() {
		return this.heightPixel;
	}

	public int toPixel(double valueCm) {
		assert valueCm >= 0.0 : valueCm;

		return (int) Math.floor(valueCm * this.pixelPerCm);
	}

	public int toPixelLength(double lengthCm) {
		assert lengthCm >= 0.0 : lengthCm;

		return (int) Math.round(lengthCm * this.pixelPerCm);
	}

	public double toCm(int valuePixel) {
		assert valuePixel >= 0 : valuePixel;

		return (double) valuePixel / this.pixelPerCm;
	}

	public Pixel from(ISU.Coord coord) {
		assert coord.isu() == this.isu;

		return new Pixel(toPixel(coord.x()), toPixel(coord.y()));
	}

	public Pixel from(Grid.Position position) {
		assert position.grid() == this.game.getGrid();

		return from(position.toISUCoord());
	}

	public void show(PrintStream ps) {
		ps.println("Picture:");
		ps.println("width = " + this.widthPixel + " pixels");
		ps.println("height = " + this.heightPixel + " pixels");
		ps.println("pixelPerCm = " + this.pixelPerCm);
	}

	public final class Pixel {

		private final int xPixel;
		private final int yPixel;

		public Pixel(int xPixel, int yPixel) {
			assert xPixel >= 0 : xPixel;
			assert yPixel >= 0 : yPixel;
			assert xPixel < Picture.this.widthPixel : "x pixel outside picture";
			assert yPixel < Picture.this.heightPixel : "y pixel outside picture";

			this.xPixel = xPixel;
			this.yPixel = yPixel;
		}

		public Picture picture() {
			return Picture.this;
		}

		public int x() {
			return this.xPixel;
		}

		public int y() {
			return this.yPixel;
		}

		public ISU.Coord toISUCoord() {
			double xCm = Picture.this.toCm(this.xPixel);
			double yCm = Picture.this.toCm(this.yPixel);

			return Picture.this.isu.new Coord(xCm, yCm);
		}

		public Pixel copy() {
			return new Pixel(this.xPixel, this.yPixel);
		}

		public boolean equiv(Pixel pixel) {
			return pixel.picture() == Picture.this && this.xPixel == pixel.xPixel && this.yPixel == pixel.yPixel;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof Picture.Pixel)) {
				return false;
			}

			return equiv((Pixel) object);
		}

		@Override
		public int hashCode() {
			int result = System.identityHashCode(Picture.this);
			result = 31 * result + this.xPixel;
			result = 31 * result + this.yPixel;
			return result;
		}

		public void show(PrintStream ps) {
			ps.println("Pixel(" + this.xPixel + ", " + this.yPixel + ")");
		}
	}
}