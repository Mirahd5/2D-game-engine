package engine.view;

public final class SpawnAnimation {

	private static final long DEFAULT_DURATION_MS = 450L;
	private static final double START_SCALE = 0.25;

	private final long durationMs;
	private long elapsedMs;

	public SpawnAnimation() {
		this(DEFAULT_DURATION_MS);
	}

	public SpawnAnimation(long durationMs) {
		assert durationMs > 0L;

		this.durationMs = durationMs;
		this.elapsedMs = 0L;
	}

	public void tick(long elapsed) {
		assert elapsed >= 0L;

		if (this.elapsedMs >= this.durationMs) {
			return;
		}

		this.elapsedMs += elapsed;

		if (this.elapsedMs > this.durationMs) {
			this.elapsedMs = this.durationMs;
		}
	}

	public double scale() {
		double progress = (double) this.elapsedMs / (double) this.durationMs;

		if (progress < 0.0) {
			progress = 0.0;
		}

		if (progress > 1.0) {
			progress = 1.0;
		}

		return START_SCALE + (1.0 - START_SCALE) * progress;
	}

	public boolean finished() {
		return this.elapsedMs >= this.durationMs;
	}

	public void reset() {
		this.elapsedMs = 0L;
	}
}