// engine/engine/view/Ticker.java
package engine.view;

import engine.brain.Brain;
import engine.model.Entity;
import engine.model.Model;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

/**
 * Ticker est la tâche périodique qui fait avancer le jeu.
 *
 * Il ne dessine rien. Il fait seulement évoluer la simulation : - le Brain
 * choisit les actions des bots ; - le Model applique les mouvements et
 * collisions ; - la View met à jour les animations.
 *
 * Correction Part IV slides 7-8 : Le délai de re-post est corrigé par le skew
 * pour maintenir la fréquence cible (ex: 60 ticks/s pour delay=16ms). Sans
 * cette correction, les ticks dérivent progressivement.
 */
public class Ticker implements Runnable {

	private final Model model;
	private final Brain brain;
	private final View view;

	private boolean stopped;

	private Entity followedEntity;

	private final int delay; // délai théorique en ms (ex: 16 pour ~60 FPS)
	private long lastTime; // timestamp du dernier run()

	public Ticker(Model model, Brain brain, View view, int delay) {
		this(model, brain, view, delay, null);
	}

	public Ticker(Model model, Brain brain, View view, int delay, Entity followedEntity) {
		assert model != null;
		assert brain != null;
		assert view != null;
		assert delay > 0;

		if (followedEntity != null) {
			assert followedEntity.model() == model;
		}

		this.model = model;
		this.brain = brain;
		this.view = view;
		this.delay = delay;
		this.followedEntity = followedEntity;

		this.lastTime = System.currentTimeMillis();

		this.stopped = false;
	}

	public void stop() {
		this.stopped = true;
	}

	public void setFollowedEntity(Entity followedEntity) {
		if (followedEntity != null) {
			assert followedEntity.model() == this.model;
		}

		this.followedEntity = followedEntity;
	}

	@Override
	public void run() {

		if (this.stopped) {
			return;
		}

		long now = System.currentTimeMillis();
		long elapsed = now - this.lastTime;
		this.lastTime = now;

		// Mise à jour de la simulation avec le temps réel écoulé.
		this.brain.think();

		this.model.tick(elapsed);

		/*
		 * Le viewport est repositionné après le déplacement de l'entité suivie.
		 */
		if (this.followedEntity != null && this.followedEntity.alive() && this.followedEntity.installed()) {

			this.model.viewPort().follow(this.followedEntity);
		}

		this.view.tick(elapsed);

		// --- Correction du skew (Part IV slides 7-8) ---
		// skew = combien de ms de retard/avance par rapport au délai théorique.
		// Si elapsed = 20ms et delay = 16ms → skew = 4ms → on re-poste dans 12ms.
		// Si elapsed = 10ms et delay = 16ms → skew = -6ms → on re-poste dans 22ms.
		long skew = elapsed - this.delay;
		int nextDelay = (int) (this.delay - skew);

		// Plancher à 1ms : on ne poste jamais avec un délai nul ou négatif.
		if (nextDelay < 1) {
			nextDelay = 1;
		}

		if (!this.stopped) {
			Runtime.post(this, nextDelay);
		}
	}
}