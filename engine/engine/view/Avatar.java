package engine.view;

import engine.model.Entity;
import oop.graphics.Graphics;

/**
 * Représentation graphique d'une entité.
 */
public abstract class Avatar {

	protected final View view;
	protected final Entity entity;

	public Avatar(View view, Entity entity) {
		assert entity.model() == view.model();

		this.view = view;
		this.entity = entity;
	}

	public final View view() {
		return this.view;
	}

	public final Entity entity() {
		return this.entity;
	}

	public abstract void paint(Graphics graphics);

	public void tick(long elapsed) {
		assert elapsed >= 0L : elapsed;
	}
}