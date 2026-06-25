package engine.view;

import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

/**
 * Image contenant plusieurs sprites découpables.
 */
public final class SpriteSheet {

	private final String path;

	private BufferedImage sheet;

	public SpriteSheet(String path) {
		assert path.length() > 0 : "empty sprite sheet path";

		this.path = path;
	}

	public String path() {
		return this.path;
	}

	public boolean loaded() {
		return this.sheet != null;
	}

	public void load(Graphics graphics) {
		if (this.sheet == null) {
			this.sheet = graphics.load(this.path);
		}
	}

	public BufferedImage image(Graphics graphics) {
		load(graphics);

		return this.sheet;
	}

	public BufferedImage frame(Graphics graphics, int x, int y, int width, int height) {
		assert x >= 0 : x;
		assert y >= 0 : y;
		assert width > 0 : width;
		assert height > 0 : height;

		load(graphics);

		return this.sheet.getSubimage(x, y, width, height);
	}
}