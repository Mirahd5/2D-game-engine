package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;

public class Gum extends Entity {

	private int points;

	// CONSTRUCTOR

	public Gum(Model model, String name) {
		super(model, name);

		/*
		 * Une Gum est plus petite qu'une cellule. Ici on choisit une taille = 1/4 de
		 * cellule.
		 */
		double gumSize = model.game().getCmPerCell() / 4.0;

		setSize(model.game().getIsu().new Dimension(gumSize, gumSize));
		setZOrder(0);
		setSOrder(0);
		this.points = 10;

		updateBounding();
	}

	public int points() {
		return this.points;
	}

	// === Task COLLISION ===

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();

		double radius = Math.min(size.x(), size.y()) / 2.0;

		result.add(new Circle(center, radius));

		return result;
	}
}