package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.Graphics;

/*
 * Avatar générique représentant une entité
 * par un cercle rempli.
 */
public class CircleAvatar extends Avatar {

	public CircleAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		ISU.Dimension size = this.entity.size();

		ISU.Coord center = this.entity.center();

		double radius = Math.min(size.x(), size.y()) / 2.0;

		int x = this.view.toPixelX(center.x() - radius);

		int y = this.view.toPixelY(center.y() - radius);

		int width = this.view.toPixelWidth(2.0 * radius);

		int height = this.view.toPixelHeight(2.0 * radius);

		graphics.fillOval(x, y, width, height);
	}
}