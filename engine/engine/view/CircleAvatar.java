package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/**
 * Avatar qui dessine une entité sous forme de cercle.
 */
public class CircleAvatar extends ShapeAvatar {

	public CircleAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	protected void paintShape(Graphics graphics, int x, int y, int width, int height) {
		graphics.fillOval(x, y, width, height);
	}
}