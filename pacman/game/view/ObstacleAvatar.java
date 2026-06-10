package game.view;

import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import oop.graphics.Color;
import oop.graphics.Graphics;

public class ObstacleAvatar extends Avatar {

	public ObstacleAvatar(Entity entity) {
		super(entity);
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());

		int w = picture.toPixelLength(entity.size().x());
		int h = picture.toPixelLength(entity.size().y());

		Color blue = g.getColor(0, 0, 255, 0);
		g.setColor(blue);

		g.fillRect(x - w / 2, y - h / 2, w, h);
	}
}