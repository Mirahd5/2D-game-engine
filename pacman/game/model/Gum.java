// == GUM ==
package game.model;

import engine.intersection.Circle;
import engine.model.Entity;
import engine.model.Game;

public class Gum extends Entity {

	public static final double RADIUS_FACTOR = 1.0 / 8.0;

	private final boolean power;

	public Gum(Game game) {
		this(game, false);
	}

	public Gum(Game game, boolean power) {
		super(power ? "PowerGum" : "Gum");
		this.power = power;

		double radius = game.cmPerCell() * (power ? RADIUS_FACTOR * 1.8 : RADIUS_FACTOR);
		setSize(game.isu().new Dimension(radius * 2.0, radius * 2.0));
	}

	public boolean power() {
		return this.power;
	}

	public int scoreValue() {
		if (this.power) {
			return 50;
		} else {
			return 10;
		}
	}

	@Override
	public void setBounding() {
		super.setBounding();
		double radius = size().x() / 2.0;
		assert radius < center().isu().xAxis().halfPerimeter();
		assert radius < center().isu().yAxis().halfPerimeter();
		bounding().add(new Circle(center(), radius));
	}
}