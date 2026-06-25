package engine.model;

/**
 * Boîte rectangulaire alignée sur les axes, utilisée comme filtre rapide de
 * collision.
 */
public final class Hitbox {

	private final double xmin;
	private final double xmax;
	private final double ymin;
	private final double ymax;

	public Hitbox(double xmin, double xmax, double ymin, double ymax) {
		assert xmin <= xmax : "invalid x interval";
		assert ymin <= ymax : "invalid y interval";

		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public double xmin() {
		return this.xmin;
	}

	public double xmax() {
		return this.xmax;
	}

	public double ymin() {
		return this.ymin;
	}

	public double ymax() {
		return this.ymax;
	}

	public double width() {
		return this.xmax - this.xmin;
	}

	public double height() {
		return this.ymax - this.ymin;
	}

	public boolean intersects(Hitbox hitbox) {
		return !(this.xmax < hitbox.xmin || hitbox.xmax < this.xmin || this.ymax < hitbox.ymin
				|| hitbox.ymax < this.ymin);
	}

	public boolean intersectsOnTorus(Hitbox hitbox, double worldWidth, double worldHeight) {
		return intersects(hitbox, true, true, worldWidth, worldHeight);
	}

	public boolean intersects(Hitbox hitbox, boolean torusX, boolean torusY, double worldWidth, double worldHeight) {
		assert worldWidth > 0.0 : worldWidth;
		assert worldHeight > 0.0 : worldHeight;

		double[] xOffsets = torusX ? new double[] { 0.0, -worldWidth, worldWidth } : new double[] { 0.0 };
		double[] yOffsets = torusY ? new double[] { 0.0, -worldHeight, worldHeight } : new double[] { 0.0 };

		for (double dx : xOffsets) {
			for (double dy : yOffsets) {
				if (intersectsShifted(hitbox, dx, dy)) {
					return true;
				}
			}
		}

		return false;
	}

	public Hitbox union(Hitbox hitbox) {
		return new Hitbox(Math.min(this.xmin, hitbox.xmin), Math.max(this.xmax, hitbox.xmax),
				Math.min(this.ymin, hitbox.ymin), Math.max(this.ymax, hitbox.ymax));
	}

	private boolean intersectsShifted(Hitbox hitbox, double dx, double dy) {
		double shiftedXmin = hitbox.xmin + dx;
		double shiftedXmax = hitbox.xmax + dx;
		double shiftedYmin = hitbox.ymin + dy;
		double shiftedYmax = hitbox.ymax + dy;

		return !(this.xmax < shiftedXmin || shiftedXmax < this.xmin || this.ymax < shiftedYmin
				|| shiftedYmax < this.ymin);
	}
}