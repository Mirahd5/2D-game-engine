package engine.view;

import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Runnable;

/*
 * Tâche périodique qui demande un repaint du Canvas.
 *
 * Elle ne dessine pas directement :
 * - PainterTask demande canvas.repaint()
 * - Painter reçoit ensuite paint(Graphics)
 * - Painter délègue à View.paint(...)
 */
public final class PainterTask implements Runnable {

	private final Painter painter;
	private final int delay;

	private long lastTime;
	private boolean stopped;

	public PainterTask(Painter painter, int delay) {
		assert painter != null;
		assert delay > 0;

		this.painter = painter;
		this.delay = delay;
		this.lastTime = System.currentTimeMillis();
		this.stopped = false;
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

		Canvas canvas = this.painter.canvas();

		if (canvas != null) {
			canvas.repaint();
		}

		/*
		 * Correction du retard.
		 *
		 * Si delay = 30 ms et que le repaint précédent est arrivé après 45 ms :
		 *
		 * skew = 45 - 30 = 15 nextDelay = 30 - 15 = 15
		 *
		 * Cela aide à maintenir la fréquence globale demandée par les slides.
		 */
		long skew = elapsed - this.delay;

		int nextDelay = (int) (this.delay - skew);

		if (nextDelay < 1) {
			nextDelay = 1;
		}

		if (!this.stopped) {
			Runtime.post(this, nextDelay);
		}
	}
}