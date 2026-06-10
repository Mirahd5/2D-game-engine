// == STUNT ==
package engine.model;

import java.util.List;

public abstract class Stunt {

	// FIELDS

	protected Entity entity;

	// CONSTRUCTOR

	protected Stunt(Entity entity) {
		assert entity != null;
		this.entity = entity;
	}

	// GETTER

	public Entity entity() {
		return entity;
	}

	public abstract void tick(long elapsed);

	// SETTERS

	public void set(int orientation_degree) {
		entity.turn(orientation_degree - entity.orientation());
	}

	public void set(Grid.Position position) {
		entity.setPosition(position);
	}

	public void set(double x_cm, double y_cm) {
		entity.setCoord(entity.isu().new Coord(x_cm, y_cm));
	}

	public abstract void collision(Entity other);

	public abstract void collision(List<Entity> others);
}