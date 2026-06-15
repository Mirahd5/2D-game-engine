package game.view;

import engine.model.Entity;
import engine.view.AnimatedImageAvatar;
import engine.view.View;

public class GhostSpriteAvatar extends AnimatedImageAvatar {

	public GhostSpriteAvatar(View view, Entity entity) {
		this(view, entity, "red");
	}

	public GhostSpriteAvatar(View view, Entity entity, String color) {
		super(view, entity, pathsFor(color), 120L);
	}

	private static String[] pathsFor(String color) {
		return new String[] { "assets/ghosts/" + color + "/" + color + "_01.png",
				"assets/ghosts/" + color + "/" + color + "_02.png", "assets/ghosts/" + color + "/" + color + "_03.png",
				"assets/ghosts/" + color + "/" + color + "_04.png", "assets/ghosts/" + color + "/" + color + "_05.png",
				"assets/ghosts/" + color + "/" + color + "_06.png", "assets/ghosts/" + color + "/" + color + "_07.png",
				"assets/ghosts/" + color + "/" + color + "_08.png" };
	}

	@Override
	protected boolean rotateWithEntity() {
		return false;
	}

	@Override
	protected double visualScale() {
		return 1.8;
	}
}
