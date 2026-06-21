package game.view;

import engine.view.Avatar;
import engine.view.View;
import game.model.Pengo;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;
import engine.model.Orientation;

/** Avatar animé de Pengo, synchronisé par View.tick(...). */
public final class PengoAvatar extends Avatar {

	private static final long FRAME_DURATION_MS = 120L;

	private final Pengo pengo;

	private BufferedImage down1;
	private BufferedImage down2;
	private BufferedImage up1;
	private BufferedImage up2;
	private BufferedImage right1;
	private BufferedImage right2;
	private BufferedImage left1;
	private BufferedImage left2;

	private long elapsedSinceFrame;
	private int frame;
	private int animatedOrientation;

	public PengoAvatar(View view, Pengo pengo) {
		super(view, pengo);

		this.pengo = pengo;
		this.elapsedSinceFrame = 0L;
		this.frame = 0;
		this.animatedOrientation = Orientation.normalizeCardinal(pengo.orientation());
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		if (!this.pengo.isMoving()) {
			this.elapsedSinceFrame = 0L;
			this.frame = 0;
			this.animatedOrientation = this.pengo.orientation();
			return;
		}

		if (this.animatedOrientation != this.pengo.orientation()) {
			this.animatedOrientation = this.pengo.orientation();
			this.elapsedSinceFrame = 0L;
			this.frame = 0;
		}

		this.elapsedSinceFrame += elapsed;

		while (this.elapsedSinceFrame >= FRAME_DURATION_MS) {
			this.elapsedSinceFrame -= FRAME_DURATION_MS;
			this.frame = 1 - this.frame;
		}
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		loadImages(graphics);

		BufferedImage image = imageForOrientation(Orientation.normalizeCardinal(this.pengo.orientation()));

		int centerX = this.view.toPixelX(this.pengo.center().x());
		int centerY = this.view.toPixelY(this.pengo.center().y());

		int width = this.view.toPixelWidth(this.pengo.size().x());
		int height = this.view.toPixelHeight(this.pengo.size().y());

		graphics.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
	}

	private void loadImages(Graphics graphics) {
		if (this.down1 != null) {
			return;
		}

		this.down1 = graphics.load("pengo_rsrc/pengo_down1.png");
		this.down2 = graphics.load("pengo_rsrc/pengo_down2.png");
		this.up1 = graphics.load("pengo_rsrc/pengo_up1.png");
		this.up2 = graphics.load("pengo_rsrc/pengo_up2.png");
		this.right1 = graphics.load("pengo_rsrc/pengo_right1.png");
		this.right2 = graphics.load("pengo_rsrc/pengo_right2.png");
		this.left1 = graphics.load("pengo_rsrc/pengo_left1.png");
		this.left2 = graphics.load("pengo_rsrc/pengo_left2.png");
	}

	private BufferedImage imageForOrientation(int orientation) {
		int activeFrame = this.pengo.isMoving() ? this.frame : 0;

		switch (orientation) {
		case 0:
			return activeFrame == 0 ? this.right1 : this.right2;
		case 90:
			return activeFrame == 0 ? this.down1 : this.down2;
		case 180:
			return activeFrame == 0 ? this.left1 : this.left2;
		case 270:
			return activeFrame == 0 ? this.up1 : this.up2;
		default:
			return this.down1;
		}
	}
}