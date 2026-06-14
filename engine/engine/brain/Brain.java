package engine.brain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.model.Entity;

public class Brain {

	private final List<Bot> bots;

	public Brain() {
		this.bots = new ArrayList<Bot>();
	}

	public void add(Bot bot) {
		assert bot != null;
		assert bot.brain() == this;

		if (!this.bots.contains(bot)) {
			this.bots.add(bot);
		}
	}

	public void remove(Bot bot) {
		assert bot != null;

		this.bots.remove(bot);
	}

	public void think() {
		List<Bot> copy = new ArrayList<Bot>(this.bots);

		for (Bot bot : copy) {
			if (bot.valid()) {
				bot.think();
			}
		}

		/*
		 * Les bots dont l'entité est morte ou retirée sont supprimés après l'itération.
		 */
		this.bots.removeIf(bot -> !bot.valid());
	}

	public List<Bot> bots() {
		return Collections.unmodifiableList(this.bots);
	}

	public void removeBotsOf(Entity entity) {
		assert entity != null;

		this.bots.removeIf(bot -> bot.stunt().entity() == entity);
	}
}