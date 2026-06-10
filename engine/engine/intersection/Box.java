package engine.intersection;

import engine.model.ISU;

public class Box {

	private final ISU isu;

	private final double minX;
	private final double minY;
	private final double maxX;
	private final double maxY;

	public Box(ISU isu, double minX, double minY, double maxX, double maxY) {
		assert minX <= maxX : "minX must be <= maxX";
		assert minY <= maxY : "minY must be <= maxY";

		this.isu = isu;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public ISU isu() {
		return this.isu;
	}

	public double minX() {
		return this.minX;
	}

	public double minY() {
		return this.minY;
	}

	public double maxX() {
		return this.maxX;
	}

	public double maxY() {
		return this.maxY;
	}

	public double width() {
		return this.maxX - this.minX;
	}

	public double height() {
		return this.maxY - this.minY;
	}

	public boolean intersects(Box other) {

		return this.maxX >= other.minX && other.maxX >= this.minX && this.maxY >= other.minY && other.maxY >= this.minY;
	}

	public Box union(Box other) {

		return new Box(this.isu, Math.min(this.minX, other.minX), Math.min(this.minY, other.minY),
				Math.max(this.maxX, other.maxX), Math.max(this.maxY, other.maxY));
	}

	public boolean contains(ISU.Coord p) {

		return p.x() >= this.minX && p.x() <= this.maxX && p.y() >= this.minY && p.y() <= this.maxY;
	}
}