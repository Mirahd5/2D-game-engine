// == PAC MAN ==
package game.model;

import engine.intersection.Circle;
import engine.model.Entity;
import engine.model.Game;

public class PacMan extends Entity {

	// CONSTRUCTOR

	public PacMan(Game game) {
		super("PacMan");
		double cmPerCell = game.cmPerCell();
		setSize(game.isu().new Dimension(cmPerCell / 2.0, cmPerCell / 2.0));
	}

	// === Task COLLISION ===

	@Override
	public void setBounding() {
		super.setBounding();
		double radius = size().x() / 2.0;
		assert radius < center().isu().xAxis().halfPerimeter();
		assert radius < center().isu().yAxis().halfPerimeter();
		bounding().add(new Circle(center(), radius));
	}
}