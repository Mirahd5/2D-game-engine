package engine.model;

import java.util.List;

import engine.brain.Bot;

/**
 * Action attachée à une entité, pilotée éventuellement par un Bot.
 */
public abstract class Stunt {

	protected final Model model;
	protected final Entity entity;

	private Bot bot;
	private boolean enabled;

	protected Stunt(Model model, Entity entity) {
		assert entity.model() == model;

		this.model = model;
		this.entity = entity;
		this.bot = null;
		this.enabled = true;
	}

	public final Model model() {
		return this.model;
	}

	public final Entity entity() {
		return this.entity;
	}

	public final Bot bot() {
		return this.bot;
	}

	public final boolean enabled() {
		return this.enabled;
	}

	public final boolean valid() {
		return this.enabled && this.entity.alive() && this.entity.installed();
	}

	public final void setBot(Bot bot) {
		assert this.enabled;
		assert bot.stunt() == this;

		if (this.bot == bot) {
			return;
		}

		this.bot = bot;
	}

	public final void invalidate() {
		if (!this.enabled) {
			return;
		}

		this.enabled = false;

		onInvalidate();

		this.bot = null;
	}

	protected void onInvalidate() {
	}

	protected final void notifyDone() {
		if (valid() && this.bot != null) {
			this.bot.done();
		}
	}

	protected final void notifyCollision(Entity entity) {
		if (valid() && this.bot != null) {
			this.bot.collision(entity);
		}
	}

	protected final void notifyCollision(List<Entity> entities) {
		if (valid() && this.bot != null) {
			this.bot.collision(entities);
		}
	}

	public abstract boolean walk(int degree);

	public void set(int orientation) {
	}

	public void set(Grid.Cell cell) {
	}

	public void set(double x, double y) {
	}

	public void commit() {
	}

	public void collision(Entity entity) {
		notifyCollision(entity);
	}

	public void collision(List<Entity> entities) {
		notifyCollision(entities);
	}
}