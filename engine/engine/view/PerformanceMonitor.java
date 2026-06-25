package engine.view;

/**
 * Mesure les temps de tick, de repaint et les FPS.
 */
public final class PerformanceMonitor {

	private final TimeStats tickStats;
	private final TimeStats paintStats;
	private final TimeStats fpsStats;

	private long lastPaintTime_ns;

	public PerformanceMonitor() {
		this.tickStats = new TimeStats();
		this.paintStats = new TimeStats();
		this.fpsStats = new TimeStats();

	}

	public void recordTick(long elapsedMs) {
		if (elapsedMs < 0L) {
			return;
		}

		this.tickStats.record(elapsedMs);
	}

	public void recordPaintEvent() {
		long now = System.nanoTime();

		if (this.lastPaintTime_ns != 0L) {
			double elapsedMs = (now - this.lastPaintTime_ns) / 1_000_000.0;

			this.paintStats.record(elapsedMs);

			if (elapsedMs > 0.0) {
				this.fpsStats.record(1000.0 / elapsedMs);
			}
		}

		this.lastPaintTime_ns = now;
	}

	public TimeStats tickStats() {
		return this.tickStats;
	}

	public TimeStats paintStats() {
		return this.paintStats;
	}

	public TimeStats fpsStats() {
		return this.fpsStats;
	}

	public void reset() {
		this.tickStats.reset();
		this.paintStats.reset();
		this.fpsStats.reset();

		this.lastPaintTime_ns = 0L;
	}
}