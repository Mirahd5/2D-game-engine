package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/**
 * Avatar qui dessine une entité sous forme de rectangle orienté.
 */
public class RectAvatar extends ShapeAvatar {

	public RectAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	protected boolean rotateWithEntity() {
		return true;
	}

	@Override
	protected void paintShape(Graphics graphics, int x, int y, int width, int height) {
		graphics.fillRect(x, y, width, height);
	}
}