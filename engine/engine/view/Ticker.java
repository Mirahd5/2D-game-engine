package engine.view;

import engine.brain.Brain;
import engine.model.Entity;
import engine.model.Model;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

/**
 * Tâche périodique qui met à jour la simulation du jeu.
 */
public final class Ticker implements Runnable {

	private final Model model;
	private final Brain brain;
	private final View view;

	private final int delay;
	private final PerformanceMonitor performanceMonitor;

	private boolean stopped;
	private boolean paused;

	private Entity followedEntity;

	private long lastTime;

	public Ticker(Model model, Brain brain, View view, int delay, Entity followedEntity) {
		this(model, brain, view, delay, followedEntity, null);
	}

	public Ticker(Model model, Brain brain, View view, int delay, Entity followedEntity,
			PerformanceMonitor performanceMonitor) {
		assert delay > 0 : delay;

		if (followedEntity != null) {
			assert followedEntity.model() == model;
		}

		this.model = model;
		this.brain = brain;
		this.view = view;

		this.delay = delay;
		this.performanceMonitor = performanceMonitor;

		this.followedEntity = followedEntity;

		this.lastTime = System.currentTimeMillis();
	}

	public int delay() {
		return this.delay;
	}

	public boolean stopped() {
		return this.stopped;
	}

	public boolean paused() {
		return this.paused;
	}

	public Entity followedEntity() {
		return this.followedEntity;
	}

	public void setFollowedEntity(Entity followedEntity) {
		if (followedEntity != null) {
			assert followedEntity.model() == this.model;
		}

		this.followedEntity = followedEntity;
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

	@Override
	public void run() {
		if (this.stopped) {
			return;
		}

		long elapsed = elapsedSinceLastTick();

		if (this.paused) {
			postNextRun(this.delay);
			return;
		}

		recordTick(elapsed);

		if (this.view.startupRevealActive()) {
			tickStartupReveal(elapsed);
			postNextRun(correctedDelay(elapsed));
			return;
		}

		tickGame(elapsed);

		postNextRun(correctedDelay(elapsed));
	}

	private long elapsedSinceLastTick() {
		long now = System.currentTimeMillis();
		long elapsed = now - this.lastTime;

		this.lastTime = now;

		if (elapsed < 0L) {
			return 0L;
		}

		return elapsed;
	}

	private void recordTick(long elapsed) {
		if (this.performanceMonitor != null) {
			this.performanceMonitor.recordTick(elapsed);
		}
	}

	private void tickStartupReveal(long elapsed) {
		this.view.tick(elapsed);
	}

	private void tickGame(long elapsed) {
		this.brain.think();

		this.model.tick(elapsed);

		followEntity();

		this.view.tick(elapsed);
	}

	private void followEntity() {
		if (this.followedEntity == null) {
			return;
		}

		if (!this.followedEntity.alive() || !this.followedEntity.installed()) {
			return;
		}

		this.model.viewPort().follow(this.followedEntity);
	}

	private void postNextRun(int nextDelay) {
		if (!this.stopped) {
			Runtime.post(this, nextDelay);
		}
	}

	private int correctedDelay(long elapsed) {
		long skew = elapsed - this.delay;
		int nextDelay = (int) (this.delay - skew);

		if (nextDelay < 1) {
			return 1;
		}

		return nextDelay;
	}
}