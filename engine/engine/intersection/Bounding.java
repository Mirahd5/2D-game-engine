package engine.intersection;

import java.util.HashSet;
import java.util.Set;

// = BOUNDING =

public class Bounding {

	// FIELDS

	private Set<iShape> boundings;

	// CONSTRUCTOR

	public Bounding() {
		this.boundings = new HashSet<>();
	}

	// BUILDER

	public void add(iShape shape) {
		this.boundings.add(shape);
	}

	// INTERSECTION

	public boolean intersects(iShape shape) {
		Box shapeBox = shape.box();

		for (iShape s : this.boundings) {
			if (s.box().intersects(shapeBox) && s.intersects(shape)) {
				return true;
			}
		}

		return false;
	}

	public boolean intersects(Bounding bounding) {
		if (!this.box().intersects(bounding.box())) {
			return false;
		}

		for (iShape s1 : this.boundings) {
			for (iShape s2 : bounding.boundings) {
				if (s1.box().intersects(s2.box()) && s1.intersects(s2)) {
					return true;
				}
			}
		}
		return false;
	}

	public Box box() {
		assert !this.boundings.isEmpty() : "bounding must contain at least one shape";

		Box result = null;

		for (iShape shape : this.boundings) {
			if (result == null) {
				result = shape.box();
			} else {
				result = result.union(shape.box());
			}
		}

		return result;
	}

	// GETTERS

	public int size() {
		return this.boundings.size();
	}

	public boolean isEmpty() {
		return this.boundings.isEmpty();
	}
}
