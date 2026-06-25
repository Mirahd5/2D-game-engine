package engine.brain;

import java.util.List;

import engine.model.Entity;
import engine.model.Stunt;

/**
 * Bot générique qui choisit les actions d'une entité via son Stunt.
 */
public abstract class Bot {

	protected final Brain brain;
	protected final Stunt stunt;

	protected Bot(Brain brain, Stunt stunt) {
		this.brain = brain;
		this.stunt = stunt;

		this.stunt.setBot(this);
	}

	public final Brain brain() {
		return this.brain;
	}

	public final Stunt stunt() {
		return this.stunt;
	}

	public final Entity entity() {
		return this.stunt.entity();
	}

	public abstract void think();

	/*
	 * Les réactions sont optionnelles : une sous-classe redéfinit seulement les
	 * événements utiles.
	 */
	public void done() {
	}

	public void collision(Entity entity) {
	}

	public void collision(List<Entity> entities) {
	}

	public final boolean valid() {
		return this.stunt.valid() && this.stunt.bot() == this;
	}
}