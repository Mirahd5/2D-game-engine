package game.model;

import engine.intersection.Circle;
import engine.model.Entity;
import engine.model.Game;

public class Cherry extends Entity {

	public static final int SCORE_VALUE = 100;

	public Cherry(Game game) {
		super("Cherry");

		double size = game.cmPerCell() * 0.70;
		setSize(game.isu().new Dimension(size, size));
	}

	public int scoreValue() {
		return SCORE_VALUE;
	}

	@Override
	public void setBounding() {
		super.setBounding();

		assert center() != null : "center must be set before setBounding";

		double radius = size().x() / 2.0;
		bounding().add(new Circle(center(), radius));
	}
}