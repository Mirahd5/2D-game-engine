package game.view;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class PacManAvatar extends Avatar {

	private static final long FRAME_MS = 120;

	private final BasicStunt stunt;

	private BufferedImage full;

	private BufferedImage rightOpen;
	private BufferedImage rightFullOpen;

	private BufferedImage leftOpen;
	private BufferedImage leftFullOpen;

	private BufferedImage upOpen;
	private BufferedImage upFullOpen;

	private BufferedImage downOpen;
	private BufferedImage downFullOpen;

	public PacManAvatar(Entity entity, BasicStunt stunt) {
		super(entity);
		assert stunt != null : "stunt must not be null";
		this.stunt = stunt;
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		loadImages(g);

		BufferedImage img = imageForOrientation();

		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());

		double cell = entity.isu().cmPerCell();

		int w = picture.toPixelLength(cell);
		int h = picture.toPixelLength(cell);

		g.drawImage(img, x - w / 2, y - h / 2, w, h);
	}

	private void loadImages(Graphics g) {
		if (full != null) {
			return;
		}

		full = g.load("pacman_rsrc/01_pacman_full.png");

		rightOpen = g.load("pacman_rsrc/02_pacman_open_right.png");
		rightFullOpen = g.load("pacman_rsrc/03_pacman_full_open_right.png");

		downFullOpen = g.load("pacman_rsrc/04_pacman_full_open_down.png");
		downOpen = g.load("pacman_rsrc/05_pacman_open_down.png");

		upFullOpen = g.load("pacman_rsrc/06_pacman_full_open_up.png");
		upOpen = g.load("pacman_rsrc/07_pacman_open_up.png");

		leftFullOpen = g.load("pacman_rsrc/08_pacman_full_open_left.png");
		leftOpen = g.load("pacman_rsrc/09_pacman_open_left.png");
	}

	private BufferedImage imageForOrientation() {
		if (!stunt.isMoving()) {
			return full;
		}

		int frame = animationFrame();
		int orientation = entity.orientation();

		if (orientation == BasicStunt.EAST) {
			return frameForDirection(frame, rightOpen, rightFullOpen);
		}

		if (orientation == BasicStunt.WEST) {
			return frameForDirection(frame, leftOpen, leftFullOpen);
		}

		if (orientation == BasicStunt.NORTH) {
			return frameForDirection(frame, upOpen, upFullOpen);
		}

		if (orientation == BasicStunt.SOUTH) {
			return frameForDirection(frame, downOpen, downFullOpen);
		}

		return full;
	}

	private int animationFrame() {
		return (int) ((System.currentTimeMillis() / FRAME_MS) % 4);
	}

	private BufferedImage frameForDirection(int frame, BufferedImage open, BufferedImage fullOpen) {
		switch (frame) {
		case 0:
			return full;

		case 1:
			return open;

		case 2:
			return fullOpen;

		case 3:
			return open;

		default:
			return full;
		}
	}
}