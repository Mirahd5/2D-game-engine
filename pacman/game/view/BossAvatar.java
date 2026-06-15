package game.view;

import engine.model.Entity;
import engine.view.RectAvatar;
import engine.view.View;
import oop.graphics.Graphics;

public class BossAvatar extends RectAvatar {

	public BossAvatar(View view, Entity entity) {
		super(view, entity);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Graphics.Colors.magenta);
		super.paint(g);
	}
}