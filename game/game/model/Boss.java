package game.model;

import engine.model.Bounding;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;

/*
 * Boss ayant une forme de T.
 *
 * À l'orientation 0 degré, il occupe :
 *
 *     X
 *     X X X
 *     X
 *     X
 *
 * Cellules relatives :
 * (0,-1)
 * (0, 0), (1,0), (2,0)
 * (0, 1)
 * (0, 2)
 */
public class Boss extends Entity implements Damager {

	private final int damage;

	public Boss(Model model, String name) {
		super(model, name);

		this.damage = 2;

		double cell = model.isu().cmPerCell();

		/*
		 * En orientation initiale : largeur globale = 3 cellules ; hauteur globale = 4
		 * cellules.
		 */
		setSize(model.isu().new Dimension(3.0 * cell, 4.0 * cell));

		setZOrder(2);
		setSOrder(2);

		updateBounding();
	}

	@Override
	public int damage() {
		return this.damage;
	}

	/*
	 * Le Bounding est l'union de deux rectangles : - une barre verticale de quatre
	 * cellules ; - une barre horizontale de trois cellules.
	 */
	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		assert center != null;
		assert size != null;
		assert center.isu() == this.isu;
		assert size.isu() == this.isu;

		Bounding result = new Bounding();

		double cell = this.isu.cmPerCell();

		/*
		 * Barre verticale.
		 *
		 * Elle recouvre les cellules relatives : (0,-1), (0,0), (0,1), (0,2).
		 *
		 * Le centre géométrique de ces quatre cellules se trouve à +0.5 cellule sur
		 * l'axe vertical.
		 */
		ISU.Vector verticalOffset = this.isu.new Vector(0.0, cell / 2.0);

		verticalOffset.turn(orientationDegree);

		ISU.Coord verticalCenter = center.mkTranslated(verticalOffset);

		ISU.Dimension verticalSize = this.isu.new Dimension(cell, 4.0 * cell);

		result.add(new Rect(verticalCenter, verticalSize, orientationDegree));

		/*
		 * Barre horizontale.
		 *
		 * Elle recouvre : (0,0), (1,0), (2,0).
		 *
		 * Son centre est donc décalé d'une cellule vers la droite.
		 */
		ISU.Vector horizontalOffset = this.isu.new Vector(cell, 0.0);

		horizontalOffset.turn(orientationDegree);

		ISU.Coord horizontalCenter = center.mkTranslated(horizontalOffset);

		ISU.Dimension horizontalSize = this.isu.new Dimension(3.0 * cell, cell);

		result.add(new Rect(horizontalCenter, horizontalSize, orientationDegree));

		return result;
	}

	/*
	 * Installe le Boss dans ses six cellules.
	 *
	 * occupy() maintient simultanément : - l'ensemble occupied de l'entité ; - la
	 * liste d'entités de chaque cellule.
	 */
	@Override
	protected void deploy() {
		assert this.installed();

		occupyRotated(0, 0);

		occupyRotated(0, -1);
		occupyRotated(0, 1);
		occupyRotated(0, 2);

		occupyRotated(1, 0);
		occupyRotated(2, 0);
	}

	private void occupyRotated(int dx, int dy) {
		int[] rotated = rotateOffset(dx, dy);

		Grid.Position target = this.position.copy();

		target.translate(this.grid.new Vector(rotated[0], rotated[1]));

		occupy(target);
	}

	/*
	 * Rotation des coordonnées discrètes correspondant aux cellules occupées.
	 */
	private int[] rotateOffset(int dx, int dy) {
		int angle = ((this.orientation_degree % 360) + 360) % 360;

		switch (angle) {
		case 0:
			return new int[] { dx, dy };

		case 90:
			return new int[] { -dy, dx };

		case 180:
			return new int[] { -dx, -dy };

		case 270:
			return new int[] { dy, -dx };

		default:
			throw new IllegalStateException("Boss orientation must be cardinal: " + angle);
		}
	}

	@Override
	public boolean blocks(Entity other) {
		assert other != null;

		return true;
	}
}