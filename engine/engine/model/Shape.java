package engine.model;

/**
 * Classe de base des formes géométriques utilisées dans un Bounding.
 */
public abstract class Shape implements IShape {

	protected final ISU isu;
	protected final ISU.Coord center;

	public Shape(ISU.Coord center) {
		this.isu = center.isu();
		this.center = center.mkCopy();
	}

	public ISU.Coord center() {
		return this.center.mkCopy();
	}

	@Override
	public ISU isu() {
		return this.isu;
	}

	public void translate(ISU.Vector vector) {
		assert vector.isu() == this.isu;

		this.center.translate(vector);
	}

	protected final void checkToricDimensions(double width, double height) {
		assert width > 0.0 : width;
		assert height > 0.0 : height;

		double smallestHalfPerimeter = Double.POSITIVE_INFINITY;

		if (this.isu.xAxis().onTorus()) {
			smallestHalfPerimeter = Math.min(smallestHalfPerimeter, this.isu.xAxis().halfPerimeter());
		}

		if (this.isu.yAxis().onTorus()) {
			smallestHalfPerimeter = Math.min(smallestHalfPerimeter, this.isu.yAxis().halfPerimeter());
		}

		if (Double.isInfinite(smallestHalfPerimeter)) {
			return;
		}

		assert width < smallestHalfPerimeter : "shape width must be smaller than the smallest toric half-perimeter";

		assert height < smallestHalfPerimeter : "shape height must be smaller than the smallest toric half-perimeter";
	}
}