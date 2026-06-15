package game.view;

import engine.model.Entity;
import engine.view.ImageAvatar;
import engine.view.View;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class GumSpriteAvatar extends ImageAvatar {

	private static final String[] PATHS = {
		"assets/items/gum.png"
	};

	public GumSpriteAvatar(View view, Entity entity) {
		super(view, entity, PATHS);
	}

	@Override
	protected BufferedImage image(Graphics g) {
		return super.image(g);
	}

	protected double visualScale() {
		return 0.8;
	}
}