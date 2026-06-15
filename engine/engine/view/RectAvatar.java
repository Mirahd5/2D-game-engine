package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.Graphics;

/*
 * Avatar générique représentant une entité
 * par un rectangle orienté.
 */
public class RectAvatar extends Avatar {

	public RectAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		ISU.Dimension size = this.entity.size();

		ISU.Coord center = this.entity.center();

		double halfWidth = size.x() / 2.0;

		double halfHeight = size.y() / 2.0;

		int centerX = this.view.toPixelX(center.x());

		int centerY = this.view.toPixelY(center.y());

		int x = this.view.toPixelX(center.x() - halfWidth);

		int y = this.view.toPixelY(center.y() - halfHeight);

		int width = this.view.toPixelWidth(size.x());

		int height = this.view.toPixelHeight(size.y());

		Object savedTransform = graphics.getTransform();

		graphics.translate(centerX, centerY);

		graphics.rotate(Math.toRadians(this.entity.orientation()));

		graphics.translate(-centerX, -centerY);

		graphics.fillRect(x, y, width, height);

		graphics.setTransform(savedTransform);
	}
}