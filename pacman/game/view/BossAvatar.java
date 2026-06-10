package game.view;

import engine.model.Entity;
import engine.model.Picture;
import engine.view.Avatar;
import oop.graphics.Color;
import oop.graphics.Graphics;

public class BossAvatar extends Avatar {

	public BossAvatar(Entity entity) {
		super(entity);
	}

	@Override
	public void paint(Graphics g, Picture picture) {

		double cell_cm = entity.isu().cmPerCell();

		int cx = picture.toPixelX(entity.center().x());
		int cy = picture.toPixelY(entity.center().y());

		int cell_pix = picture.toPixelLength(cell_cm);

		Color purple = g.getColor(150, 0, 255, 0);
		g.setColor(purple);

		int verticalWidth = cell_pix;
		int verticalHeight = 4 * cell_pix;

		int verticalCenterX = cx;
		int verticalCenterY = cy + cell_pix / 2;

		g.fillRect(verticalCenterX - verticalWidth / 2, verticalCenterY - verticalHeight / 2, verticalWidth,
				verticalHeight);

		int horizontalWidth = 3 * cell_pix;
		int horizontalHeight = cell_pix;

		int horizontalCenterX = cx + cell_pix;
		int horizontalCenterY = cy;

		g.fillRect(horizontalCenterX - horizontalWidth / 2, horizontalCenterY - horizontalHeight / 2, horizontalWidth,
				horizontalHeight);
	}
}