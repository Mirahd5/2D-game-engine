package game.view;

import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import game.model.Ghost;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class GhostAvatar extends Avatar {

	private BufferedImage blinkyRight;
	private BufferedImage blinkyLeft;
	private BufferedImage blinkyUp;
	private BufferedImage blinkyDown;

	private BufferedImage speedyRight;
	private BufferedImage speedyLeft;
	private BufferedImage speedyUp;
	private BufferedImage speedyDown;

	private BufferedImage inkyRight;
	private BufferedImage inkyLeft;
	private BufferedImage inkyUp;
	private BufferedImage inkyDown;

	private BufferedImage clydeRight;
	private BufferedImage clydeLeft;
	private BufferedImage clydeUp;
	private BufferedImage clydeDown;

	public GhostAvatar(Entity entity) {
		super(entity);
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		loadImages(g);

		if (entity instanceof Ghost && !((Ghost) entity).alive()) {
			return;
		}

		BufferedImage image = imageForGhost();

		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());

		double cell = entity.isu().cmPerCell();

		int w = picture.toPixelLength(cell);
		int h = picture.toPixelLength(cell);

		g.drawImage(image, x - w / 2, y - h / 2, w, h);
	}

	private void loadImages(Graphics g) {
		if (blinkyRight != null) {
			return;
		}

		blinkyRight = g.load("pacman_rsrc/blinky_right.png");
		blinkyLeft = g.load("pacman_rsrc/blinky_left.png");
		blinkyUp = g.load("pacman_rsrc/blinky_up.png");
		blinkyDown = g.load("pacman_rsrc/blinky_down.png");

		speedyRight = g.load("pacman_rsrc/speedy_right.png");
		speedyLeft = g.load("pacman_rsrc/speedy_left.png");
		speedyUp = g.load("pacman_rsrc/speedy_up.png");
		speedyDown = g.load("pacman_rsrc/speedy_down.png");

		inkyRight = g.load("pacman_rsrc/inky_right.png");
		inkyLeft = g.load("pacman_rsrc/inky_left.png");
		inkyUp = g.load("pacman_rsrc/inky_up.png");
		inkyDown = g.load("pacman_rsrc/inky_down.png");

		clydeRight = g.load("pacman_rsrc/clyde_right.png");
		clydeLeft = g.load("pacman_rsrc/clyde_left.png");
		clydeUp = g.load("pacman_rsrc/clyde_up.png");
		clydeDown = g.load("pacman_rsrc/clyde_down.png");
	}

	private BufferedImage imageForGhost() {
		Ghost.Type type = Ghost.Type.BLINKY;

		if (entity instanceof Ghost) {
			type = ((Ghost) entity).type();
		}

		int orientation = entity.orientation();

		switch (type) {
		case SPEEDY:
			return imageForOrientation(orientation, speedyRight, speedyLeft, speedyUp, speedyDown);
		case INKY:
			return imageForOrientation(orientation, inkyRight, inkyLeft, inkyUp, inkyDown);
		case CLYDE:
			return imageForOrientation(orientation, clydeRight, clydeLeft, clydeUp, clydeDown);
		case BLINKY:
		default:
			return imageForOrientation(orientation, blinkyRight, blinkyLeft, blinkyUp, blinkyDown);
		}
	}

	private BufferedImage imageForOrientation(int orientation, BufferedImage right, BufferedImage left,
			BufferedImage up, BufferedImage down) {

		if (orientation == 180) {
			return left;
		}

		if (orientation == 270) {
			return up;
		}

		if (orientation == 90) {
			return down;
		}

		return right;
	}
}