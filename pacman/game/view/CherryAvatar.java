package game.view;

import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

/**
 * Avatar de la cerise bonus.
 */
public class CherryAvatar extends Avatar {

	private BufferedImage cherry;

	public CherryAvatar(Entity entity) {
		super(entity);
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		if (cherry == null) {
			cherry = g.load("pacman_rsrc/cherry.png");
		}

		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());

		double cell = entity.isu().cmPerCell();
		int w = picture.toPixelLength(cell);
		int h = picture.toPixelLength(cell);

		g.drawImage(cherry, x - w / 2, y - h / 2, w, h);
	}
}