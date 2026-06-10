package game.view;

import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import game.model.Gum;
import oop.graphics.Color;
import oop.graphics.Graphics;

/**
 * Avatar d'une gomme.
 */
public class GumAvatar extends Avatar {

	public GumAvatar(Entity entity) {
		super(entity);
	}

	@Override
	public void paint(Graphics g, Picture picture) {
		int x = picture.toPixelX(entity.center().x());
		int y = picture.toPixelY(entity.center().y());
		boolean power = entity instanceof Gum && ((Gum) entity).power();
		int size;
		Color color;
		if (power) {
			size = 12;
			color = g.getColor(255, 170, 170, 0);
		} else {
			size = 5;
			color = g.getColor(255, 220, 120, 0);
		}
		g.setColor(color);
		g.fillOval(x - size / 2, y - size / 2, size, size);
	}
}