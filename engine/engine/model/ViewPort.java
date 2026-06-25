package engine.model;

/**
 * Partie visible du monde, exprimée dans le repère ISU.
 */
public final class ViewPort {

	private final Model model;
	private final ISU isu;

	private ISU.Coord origin;
	private ISU.Dimension dimension;

	public ViewPort(Model model, ISU.Coord origin, ISU.Dimension dimension) {
		this.model = model;
		this.isu = model.isu();

		assert origin.isu() == this.isu;
		assert dimension.isu() == this.isu;

		ISU.Dimension dimensionCopy = dimension.copy();

		checkDimension(dimensionCopy);

		this.dimension = dimensionCopy;
		this.origin = origin.mkCopy();

		normalizeOrigin();
	}

	public Model model() {
		return this.model;
	}

	public ISU isu() {
		return this.isu;
	}

	public ISU.Coord origin() {
		return this.origin.mkCopy();
	}

	public ISU.Dimension dimension() {
		return this.dimension.copy();
	}

	public double x() {
		return this.origin.x();
	}

	public double y() {
		return this.origin.y();
	}

	public double width() {
		return this.dimension.x();
	}

	public double height() {
		return this.dimension.y();
	}

	public void setOrigin(ISU.Coord origin) {
		assert origin.isu() == this.isu;

		this.origin = origin.mkCopy();

		normalizeOrigin();
	}

	public void setDimension(ISU.Dimension dimension) {
		assert dimension.isu() == this.isu;

		ISU.Dimension copy = dimension.copy();

		checkDimension(copy);

		this.dimension = copy;

		normalizeOrigin();
	}

	public void centerOn(Entity entity) {
		assert entity.model() == this.model;
		assert entity.installed();

		ISU.Coord center = entity.center();

		double targetX = center.x() - width() / 2.0;
		double targetY = center.y() - height() / 2.0;

		targetX = legalOriginX(targetX);
		targetY = legalOriginY(targetY);

		setOrigin(this.isu.new Coord(targetX, targetY));
	}

	public void follow(Entity entity) {
		centerOn(entity);
	}

	public void follow(Entity entity, double elasticity) {
		assert entity.model() == this.model;
		assert entity.installed();

		assert elasticity >= 0.0 : elasticity;
		assert elasticity <= 1.0 : elasticity;

		ISU.Coord center = entity.center();

		double targetX = legalOriginX(center.x() - width() / 2.0);
		double targetY = legalOriginY(center.y() - height() / 2.0);

		double dx = deltaXTo(targetX);
		double dy = deltaYTo(targetY);

		double newX = this.origin.x() + dx * elasticity;
		double newY = this.origin.y() + dy * elasticity;

		setOrigin(this.isu.new Coord(newX, newY));
	}

	private void checkDimension(ISU.Dimension candidate) {
		assert candidate.isu() == this.isu;

		double width = candidate.x();
		double height = candidate.y();

		assert width > 0.0 : width;
		assert height > 0.0 : height;

		if (this.isu.xAxis().onTorus()) {
			assert width < this.isu.width() : "viewport width must be smaller than toric map width";
		} else {
			assert width <= this.isu.width() : "viewport width must be <= map width";
		}

		if (this.isu.yAxis().onTorus()) {
			assert height < this.isu.height() : "viewport height must be smaller than toric map height";
		} else {
			assert height <= this.isu.height() : "viewport height must be <= map height";
		}
	}

	private void normalizeOrigin() {
		double x = legalOriginX(this.origin.x());
		double y = legalOriginY(this.origin.y());

		this.origin = this.isu.new Coord(x, y);
	}

	private double legalOriginX(double requestedX) {
		if (this.isu.xAxis().onTorus()) {
			return this.isu.xAxis().normalize(requestedX);
		}

		double maximum = this.isu.width() - width();

		return clamp(requestedX, 0.0, maximum);
	}

	private double legalOriginY(double requestedY) {
		if (this.isu.yAxis().onTorus()) {
			return this.isu.yAxis().normalize(requestedY);
		}

		double maximum = this.isu.height() - height();

		return clamp(requestedY, 0.0, maximum);
	}

	private double deltaXTo(double targetX) {
		if (this.isu.xAxis().onTorus()) {
			return this.isu.xAxis().delta(this.origin.x(), targetX);
		}

		return targetX - this.origin.x();
	}

	private double deltaYTo(double targetY) {
		if (this.isu.yAxis().onTorus()) {
			return this.isu.yAxis().delta(this.origin.y(), targetY);
		}

		return targetY - this.origin.y();
	}

	private double clamp(double value, double minimum, double maximum) {
		if (value < minimum) {
			return minimum;
		}

		if (value > maximum) {
			return maximum;
		}

		return value;
	}
}