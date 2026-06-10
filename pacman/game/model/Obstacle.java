// == OBSTACLE ==
package game.model;

import engine.intersection.Bounding;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.intersection.Rect;

public class Obstacle extends Entity {

	// CONSTRUCTOR

	public Obstacle(Game game, int x_ncell, int y_ncell) {
		super("Obstacle");
		double cmPerCell = game.cmPerCell();
		setSize(game.isu().new Dimension(cmPerCell, cmPerCell));
		// Place l'entité à la cellule donnée
		setPosition(game.grid().new Position(x_ncell, y_ncell));
	}

	// === Task COLLISION ===

	@Override
	public void setBounding() {
		super.setBounding();
		double cmPerCell = center().isu().cmPerCell();

		assert cmPerCell / 2.0 < center().isu().xAxis().halfPerimeter();
		assert cmPerCell / 2.0 < center().isu().yAxis().halfPerimeter();
		// Rect centré sur le centre de l'entité, taille = une cellule, angle = 0
		bounding().add(new Rect(center(), center().isu().new Dimension(cmPerCell, cmPerCell), 0));
	}
}