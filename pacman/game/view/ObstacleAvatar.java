package game.view;

import engine.model.Entity;
import engine.view.RectAvatar;
import engine.view.View;
import oop.graphics.Graphics;

public class ObstacleAvatar extends RectAvatar {

	public ObstacleAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	public void paint(Graphics g) {
		assert g != null;

		g.setColor(Graphics.Colors.blue);
		super.paint(g);
	}
}