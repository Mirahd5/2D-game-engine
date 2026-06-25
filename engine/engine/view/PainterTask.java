package engine.view;

import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

/**
 * Tâche périodique qui demande le repaint du Canvas.
 */
public final class PainterTask implements Runnable {

	private final Painter painter;
	private final int delay;

	private long lastTime;
	private boolean stopped;

	public PainterTask(Painter painter, int delay) {
		assert delay > 0 : delay;

		this.painter = painter;
		this.delay = delay;
		this.lastTime = System.currentTimeMillis();
	}

	public int delay() {
		return this.delay;
	}

	public boolean stopped() {
		return this.stopped;
	}

	public void stop() {
		this.stopped = true;
	}

	@Override
	public void run() {
		if (this.stopped) {
			return;
		}

		long now = System.currentTimeMillis();
		long elapsed = now - this.lastTime;

		this.lastTime = now;

		repaintCanvas();
		postNextRun(elapsed);
	}

	private void repaintCanvas() {
		Canvas canvas = this.painter.canvas();

		if (canvas != null) {
			canvas.repaint();
		}
	}

	private void postNextRun(long elapsed) {
		if (this.stopped) {
			return;
		}

		int nextDelay = correctedDelay(elapsed);

		Runtime.post(this, nextDelay);
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