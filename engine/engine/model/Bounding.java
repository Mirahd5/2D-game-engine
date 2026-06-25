package engine.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Union de formes simples qui recouvrent une entité.
 */
public final class Bounding {

	private final Set<IShape> shapes;

	private ISU isu;

	public Bounding() {
		this.shapes = new LinkedHashSet<IShape>();
	}

	public void add(IShape shape) {
		if (this.isu == null) {
			this.isu = shape.isu();
		} else {
			assert shape.isu() == this.isu : "All shapes of a Bounding must use the same ISU";
		}

		this.shapes.add(shape);
	}

	public boolean isEmpty() {
		return this.shapes.isEmpty();
	}

	public int size() {
		return this.shapes.size();
	}

	public Set<IShape> shapes() {
		return new LinkedHashSet<IShape>(this.shapes);
	}

	public ISU isu() {
		return this.isu;
	}

	public Hitbox hitbox() {
		assert !this.shapes.isEmpty() : "An empty Bounding has no Hitbox";

		Hitbox result = null;

		for (IShape shape : this.shapes) {
			if (result == null) {
				result = shape.hitbox();
			} else {
				result = result.union(shape.hitbox());
			}
		}

		return result;
	}

	public boolean intersects(IShape shape) {
		if (this.shapes.isEmpty()) {
			return false;
		}

		assert shape.isu() == this.isu : "Shape and Bounding must use the same ISU";

		for (IShape current : this.shapes) {
			if (current.intersects(shape)) {
				return true;
			}
		}

		return false;
	}

	public boolean intersects(Bounding other) {
		if (this.shapes.isEmpty() || other.shapes.isEmpty()) {
			return false;
		}

		assert other.isu() == this.isu : "Bounding objects must use the same ISU";

		Hitbox firstHitbox = this.hitbox();
		Hitbox secondHitbox = other.hitbox();

		if (!firstHitbox.intersects(secondHitbox, this.isu.xAxis().onTorus(), this.isu.yAxis().onTorus(),
				this.isu.width(), this.isu.height())) {
			return false;
		}

		for (IShape first : this.shapes) {
			for (IShape second : other.shapes) {
				if (first.intersects(second)) {
					return true;
				}
			}
		}

		return false;
	}
}