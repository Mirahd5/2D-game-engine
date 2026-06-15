package game.view;

import engine.model.Entity;
import engine.view.CircleAvatar;
import engine.view.View;
import oop.graphics.Graphics;

public final class GhostAvatar extends CircleAvatar {

	public GhostAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		graphics.setColor(Graphics.Colors.red);

		super.paint(graphics);
	}
}