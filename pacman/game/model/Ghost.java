package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;

public class Ghost extends Entity implements Damager {

	private int damage;

	// CONSTRUCTOR

	public Ghost(Model model, String name) {
		super(model, name);

		this.damage = 1;

		/*
		 * Un Ghost occupe une cellule, mais sa taille physique est d'une demi-cellule.
		 */
		double ghostSize = model.game().getCmPerCell() / 2.0;

		setSize(model.game().getIsu().new Dimension(ghostSize, ghostSize));

		setZOrder(2);
		setSOrder(1);

		updateBounding();
	}

	// === Task COLLISION ===

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		ISU isu = center.isu();

		Bounding result = new Bounding();

		double width = size.x();
		double height = size.y();

		/*
		 * Tête circulaire.
		 */
		double headRadius = width / 2.0;

		ISU.Vector headOffset = isu.new Vector(0.0, -height * 0.20);

		headOffset.turn(orientationDegree);

		ISU.Coord headCenter = center.mkTranslated(headOffset);

		result.add(new Circle(headCenter, headRadius));

		/*
		 * Corps rectangulaire.
		 */
		ISU.Dimension bodySize = isu.new Dimension(width, height * 0.65);

		ISU.Vector bodyOffset = isu.new Vector(0.0, height * 0.20);

		bodyOffset.turn(orientationDegree);

		ISU.Coord bodyCenter = center.mkTranslated(bodyOffset);

		result.add(new Rect(bodyCenter, bodySize, orientationDegree));

		return result;
	}

	@Override
	public int damage() {
		return this.damage;
	}

	@Override
	public void collision(Entity e) {
		if (e instanceof Gum) {
			return;
		}

		super.collision(e);

		if (e instanceof PacMan) {
			((PacMan) e).takeDamage(this.damage);
		}
	}

	@Override
	public void collision(java.util.List<Entity> entities) {
		List<Entity> realCollisions = new ArrayList<Entity>();

		for (Entity e : entities) {
			if (e instanceof Gum) {
				continue;
			}
			realCollisions.add(e);
			if (e instanceof PacMan) {
				((PacMan) e).takeDamage(this.damage);
			}
		}
		if (!realCollisions.isEmpty()) {
			super.collision(realCollisions);
		}
	}

	@Override
	public boolean blocks(Entity other) {
		assert other != null;

		/*
		 * Deux fantômes ne peuvent pas se traverser. Un fantôme touche Pac-Man, mais ne
		 * bloque pas nécessairement son mouvement.
		 */
		return other instanceof Ghost;
	}
}