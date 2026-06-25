package engine.view;

import engine.model.Entity;
import engine.model.ISU;
import oop.graphics.Graphics;

/**
 * Avatar générique pour dessiner une entité avec une forme simple.
 */
public abstract class ShapeAvatar extends Avatar {

	public ShapeAvatar(View view, Entity entity) {
		super(view, entity);
	}

	protected boolean rotateWithEntity() {
		return false;
	}

	@Override
	public void paint(Graphics graphics) {
		if (!this.entity.alive() || !this.entity.installed()) {
			return;
		}

		ISU.Dimension size = this.entity.size();
		ISU.Coord center = this.entity.center();

		double scale = this.view.revealScale(this.entity);

		if (scale <= 0.0) {
			return;
		}

		int width = this.view.toPixelWidth(size.x() * scale);
		int height = this.view.toPixelHeight(size.y() * scale);

		if (width <= 0 || height <= 0) {
			return;
		}

		int centerX = this.view.toPixelX(center.x());
		int centerY = this.view.toPixelY(center.y());

		int x = centerX - width / 2;
		int y = centerY - height / 2;

		Object savedTransform = graphics.getTransform();

		if (rotateWithEntity()) {
			graphics.translate(centerX, centerY);
			graphics.rotate(Math.toRadians(this.entity.orientation()));
			graphics.translate(-centerX, -centerY);
		}

		paintShape(graphics, x, y, width, height);

		graphics.setTransform(savedTransform);
	}

	protected abstract void paintShape(Graphics graphics, int x, int y, int width, int height);
}