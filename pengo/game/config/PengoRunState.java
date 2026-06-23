package game.config;

/**
 * Etat d'execution d'une partie Pengo.
 *
 * Il fournit aussi une horloge de jeu qui ne progresse pas pendant la pause.
 * Les timers de gameplay doivent utiliser nowMs() au lieu de
 * System.currentTimeMillis() pour etre vraiment bloques pendant la pause.
 */
public final class PengoRunState {

	private boolean paused;
	private boolean stopped;
	private long pauseStarted_ms;
	private long totalPaused_ms;

	public PengoRunState() {
		this.paused = false;
		this.stopped = false;
		this.pauseStarted_ms = 0L;
		this.totalPaused_ms = 0L;
	}

	public boolean paused() {
		return this.paused;
	}

	public boolean stopped() {
		return this.stopped;
	}

	public boolean running() {
		return !this.paused && !this.stopped;
	}

	public long nowMs() {
		long now = System.currentTimeMillis();
		long pausedNow = 0L;

		if (this.paused) {
			pausedNow = now - this.pauseStarted_ms;
		}

		return now - this.totalPaused_ms - pausedNow;
	}

	public void pause() {
		if (this.stopped || this.paused) {
			return;
		}

		this.paused = true;
		this.pauseStarted_ms = System.currentTimeMillis();
	}

	public long resume() {
		if (!this.paused) {
			return 0L;
		}

		long now = System.currentTimeMillis();
		long pauseDuration = now - this.pauseStarted_ms;

		if (pauseDuration < 0L) {
			pauseDuration = 0L;
		}

		this.totalPaused_ms += pauseDuration;
		this.pauseStarted_ms = 0L;
		this.paused = false;

		return pauseDuration;
	}

	public void stop() {
		this.stopped = true;
		this.paused = false;
		this.pauseStarted_ms = 0L;
	}
}