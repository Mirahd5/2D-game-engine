package game.view;

import engine.model.Entity;
import engine.model.ISU;
import engine.view.CircleAvatar;
import engine.view.View;
import oop.graphics.Graphics;

/*
 * Avatar spécifique à Pac-Man.
 *
 * Le cercle principal est dessiné par CircleAvatar.
 * Cette classe ajoute seulement l'animation de la bouche.
 */
public final class PacManAvatar extends CircleAvatar {

	private static final long MOUTH_FRAME_MS = 150L;

	private static final double SPEED_EPSILON = 0.000001;

	private long animationTime;
	private boolean mouthOpen;
	private int animatedOrientation;

	public PacManAvatar(View view, Entity entity) {
		super(view, entity);

		this.animationTime = 0L;
		this.mouthOpen = false;
		this.animatedOrientation = entity.orientation();
	}

	private void resetAnimation() {
		this.animationTime = 0L;
		this.mouthOpen = false;
	}

	@Override
	public void tick(long elapsed) {
		assert elapsed >= 0L;

		if (!this.entity.alive() || !this.entity.installed()) {

			resetAnimation();
			return;
		}

		int orientation = this.entity.orientation();

		if (orientation != this.animatedOrientation) {

			this.animatedOrientation = orientation;

			resetAnimation();
		}

		/*
		 * La bouche dépend uniquement du déplacement linéaire de Pac-Man.
		 *
		 * Une rotation immobile ne doit pas faire mâcher Pac-Man.
		 */
		if (this.entity.linearSpeed().norm() <= SPEED_EPSILON) {

			resetAnimation();
			return;
		}

		this.animationTime += elapsed;

		while (this.animationTime >= MOUTH_FRAME_MS) {

			this.animationTime -= MOUTH_FRAME_MS;

			this.mouthOpen = !this.mouthOpen;
		}
	}

	@Override
	public void paint(Graphics graphics) {
		assert graphics != null;

		graphics.setColor(Graphics.Colors.yellow);

		/*
		 * Dessin du corps circulaire.
		 */
		super.paint(graphics);

		if (!this.mouthOpen) {
			return;
		}

		ISU.Dimension size = this.entity.size();

		ISU.Coord center = this.entity.center();

		double radius = Math.min(size.x(), size.y()) / 2.0;

		int width = this.view.toPixelWidth(2.0 * radius);

		int height = this.view.toPixelHeight(2.0 * radius);

		int centerX = this.view.toPixelX(center.x());

		int centerY = this.view.toPixelY(center.y());

		int mouthLength = width / 2;

		int mouthThickness = Math.max(1, height / 4);

		/*
		 * La bouche doit idéalement avoir la couleur du fond de la carte.
		 */
		graphics.setColor(Graphics.Colors.gray);

		switch (this.entity.orientation()) {
		case 0:
			graphics.fillRect(centerX, centerY - mouthThickness / 2, mouthLength, mouthThickness);
			break;

		case 90:
			graphics.fillRect(centerX - mouthThickness / 2, centerY, mouthThickness, mouthLength);
			break;

		case 180:
			graphics.fillRect(centerX - mouthLength, centerY - mouthThickness / 2, mouthLength, mouthThickness);
			break;

		case 270:
			graphics.fillRect(centerX - mouthThickness / 2, centerY - mouthLength, mouthThickness, mouthLength);
			break;

		default:
			throw new AssertionError("PacMan orientation must be cardinal");
		}
	}
}