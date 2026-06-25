package engine.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import engine.model.Entity;

/**
 * Animation d'intro : les entités apparaissent ligne par ligne.
 */
public final class StartupReveal {

	private static final long DEFAULT_DELAY_BETWEEN_ENTITIES_MS = 25L;
	private static final long DEFAULT_ENTITY_REVEAL_DURATION_MS = 260L;

	private final Map<Entity, Long> startTimes;

	private final long delayBetweenEntitiesMs;
	private final long entityRevealDurationMs;

	private long elapsedMs;
	private long totalDurationMs;

	public StartupReveal(List<Entity> entities) {
		this(entities, DEFAULT_DELAY_BETWEEN_ENTITIES_MS, DEFAULT_ENTITY_REVEAL_DURATION_MS);
	}

	public StartupReveal(List<Entity> entities, long delayBetweenEntitiesMs, long entityRevealDurationMs) {
		assert delayBetweenEntitiesMs >= 0L : delayBetweenEntitiesMs;
		assert entityRevealDurationMs > 0L : entityRevealDurationMs;

		this.delayBetweenEntitiesMs = delayBetweenEntitiesMs;
		this.entityRevealDurationMs = entityRevealDurationMs;

		this.startTimes = new IdentityHashMap<Entity, Long>();

		buildOrder(entities);
	}

	public long elapsedMs() {
		return this.elapsedMs;
	}

	public long totalDurationMs() {
		return this.totalDurationMs;
	}

	public boolean finished() {
		return this.elapsedMs >= this.totalDurationMs;
	}

	public boolean active() {
		return !finished();
	}

	public void tick(long elapsed) {
		assert elapsed >= 0L : elapsed;

		if (finished()) {
			return;
		}

		this.elapsedMs += elapsed;

		if (this.elapsedMs > this.totalDurationMs) {
			this.elapsedMs = this.totalDurationMs;
		}
	}

	public boolean visible(Entity entity) {
		return scale(entity) > 0.0;
	}

	public double scale(Entity entity) {
		Long start = this.startTimes.get(entity);

		if (start == null) {
			return 1.0;
		}

		long localTime = this.elapsedMs - start.longValue();

		if (localTime <= 0L) {
			return 0.0;
		}

		if (localTime >= this.entityRevealDurationMs) {
			return 1.0;
		}

		double progress = (double) localTime / (double) this.entityRevealDurationMs;

		return easeOut(progress);
	}

	public void reset() {
		this.elapsedMs = 0L;
	}

	private void buildOrder(List<Entity> entities) {
		List<Entity> ordered = new ArrayList<Entity>();

		for (Entity entity : entities) {
			if (entity.alive() && entity.installed()) {
				ordered.add(entity);
			}
		}

		ordered.sort(new Comparator<Entity>() {
			@Override
			public int compare(Entity first, Entity second) {
				int result = Integer.compare(first.position().y(), second.position().y());

				if (result != 0) {
					return result;
				}

				result = Integer.compare(first.position().x(), second.position().x());

				if (result != 0) {
					return result;
				}

				result = Integer.compare(first.zOrder(), second.zOrder());

				if (result != 0) {
					return result;
				}

				return Integer.compare(first.sOrder(), second.sOrder());
			}
		});

		if (ordered.isEmpty()) {
			this.totalDurationMs = 0L;
			return;
		}

		long start = 0L;

		for (Entity entity : ordered) {
			this.startTimes.put(entity, start);
			start += this.delayBetweenEntitiesMs;
		}

		this.totalDurationMs = start - this.delayBetweenEntitiesMs + this.entityRevealDurationMs;
	}

	private static double easeOut(double progress) {
		progress = clamp01(progress);

		return 1.0 - Math.pow(1.0 - progress, 3.0);
	}

	private static double clamp01(double value) {
		if (value < 0.0) {
			return 0.0;
		}

		if (value > 1.0) {
			return 1.0;
		}

		return value;
	}
}