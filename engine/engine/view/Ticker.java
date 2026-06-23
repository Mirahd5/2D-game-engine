// engine/engine/view/Ticker.java
package engine.view;

import engine.brain.Brain;
import engine.model.Entity;
import engine.model.Model;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

public class Ticker implements Runnable {

	private final Model model;
	private final Brain brain;
	private final View view;

	private boolean stopped;
	private boolean paused;

	private Entity followedEntity;

	private final int delay; // délai théorique en ms (ex: 16 pour ~60 FPS)
	private long lastTime; // timestamp du dernier run()

	private final PerformanceMonitor performanceMonitor;

	public Ticker(Model model, Brain brain, View view, int delay, Entity followedEntity) {
		this(model, brain, view, delay, followedEntity, null);
	}

	public Ticker(Model model, Brain brain, View view, int delay, Entity followedEntity,
			PerformanceMonitor performanceMonitor) {
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
		this.performanceMonitor = performanceMonitor;

		this.lastTime = System.currentTimeMillis();
		this.stopped = false;
		this.paused = false;
	}

	public void stop() {
		this.stopped = true;
	}

	public void pause() {
		if (this.stopped) {
			return;
		}

		this.paused = true;
	}

	public void resume() {
		if (this.stopped) {
			return;
		}

		this.paused = false;
		this.lastTime = System.currentTimeMillis();
	}

	public boolean paused() {
		return this.paused;
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

		if (this.paused) {
			this.lastTime = System.currentTimeMillis();

			if (!this.stopped) {
				Runtime.post(this, this.delay);
			}

			return;
		}

		long now = System.currentTimeMillis();
		long elapsed = now - this.lastTime;
		this.lastTime = now;

		if (this.performanceMonitor != null) {
			this.performanceMonitor.recordTick(elapsed);
		}

		if (this.view.startupRevealActive()) {
			this.view.tick(elapsed);

			long skew = elapsed - this.delay;
			int nextDelay = (int) (this.delay - skew);

			if (nextDelay < 1) {
				nextDelay = 1;
			}

			if (!this.stopped) {
				Runtime.post(this, nextDelay);
			}

			return;
		}

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