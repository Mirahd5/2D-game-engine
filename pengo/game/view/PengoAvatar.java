package game.view;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class PengoAvatar extends Avatar {

	//120 ms donne environ 8 changements d'image par seconde	 
	private static final long FRAME_MS = 120;

	private final BasicStunt stunt;

	private BufferedImage down1;
	private BufferedImage down2;

	private BufferedImage up1;
	private BufferedImage up2;

	private BufferedImage right1;
	private BufferedImage right2;

	private BufferedImage left1;
	private BufferedImage left2;

	public PengoAvatar(Entity entity, BasicStunt stunt) {
		super(entity);
		this.stunt = stunt;
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		assert g != null;
		assert picture != null;

		loadImages(g);

		BufferedImage image = imageForOrientation();

		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());

		double cellSize_cm = entity.isu().cmPerCell();

		int width_pixel = picture.toPixelLength(cellSize_cm);
		int height_pixel = picture.toPixelLength(cellSize_cm);

		g.drawImage(
			image,
			x - width_pixel / 2,
			y - height_pixel / 2,
			width_pixel,
			height_pixel
		);
	}

	private void loadImages(Graphics g) {
		if (down1 != null) {
			return;
		}

		down1 = g.load("pengo_rsrc/pengo_down1.png");
		down2 = g.load("pengo_rsrc/pengo_down2.png");

		up1 = g.load("pengo_rsrc/pengo_up1.png");
		up2 = g.load("pengo_rsrc/pengo_up2.png");

		right1 = g.load("pengo_rsrc/pengo_right1.png");
		right2 = g.load("pengo_rsrc/pengo_right2.png");

		left1 = g.load("pengo_rsrc/pengo_left1.png");
		left2 = g.load("pengo_rsrc/pengo_left2.png");
	}

	private BufferedImage imageForOrientation() {
		int orientation = entity.orientation();
		if (!stunt.isMoving()) {
			return idleImageForOrientation(orientation);
		}

		int frame = animationFrame();

		if (orientation == BasicStunt.EAST) {
			return frameForDirection(frame, right1, right2);
		}

		if (orientation == BasicStunt.WEST) {
			return frameForDirection(frame, left1, left2);
		}

		if (orientation == BasicStunt.NORTH) {
			return frameForDirection(frame, up1, up2);
		}

		if (orientation == BasicStunt.SOUTH) {
			return frameForDirection(frame, down1, down2);
		}

		return down1;
	}
	
	//Image utilisée lorsque Pengo ne bouge pas.
	private BufferedImage idleImageForOrientation(int orientation) {
		if (orientation == BasicStunt.EAST) {
			return right1;
		}

		if (orientation == BasicStunt.WEST) {
			return left1;
		}

		if (orientation == BasicStunt.NORTH) {
			return up1;
		}

		if (orientation == BasicStunt.SOUTH) {
			return down1;
		}

		return down1;
	}

	/*
	 * frame 0 : premier pied en avant
	 * frame 1 : second pied en avant
	 */
	private BufferedImage frameForDirection(
		int frame,
		BufferedImage first,
		BufferedImage second
	) {
		if (frame == 0) {
			return first;
		}

		return second;
	}

	//Renvoie alternativement 0 puis 1 avec FRAME_MS = 120 :
	private int animationFrame() {
		return (int) ((System.currentTimeMillis() / FRAME_MS) % 2);
	}
}