package engine.view;

import oop.graphics.Canvas;
import oop.graphics.Graphics;
import oop.tasks.Runtime;

public class Painter implements Canvas.PaintListener, oop.tasks.Runnable {

	private final Canvas canvas;
	private final View view;
	private final int frameDelay_ms;
	private boolean running;

	public Painter(Canvas canvas, View view, int frameDelay_ms) {
		assert frameDelay_ms > 0 : "frameDelay_ms must be positive";
		this.canvas = canvas;
		this.view = view;
		this.frameDelay_ms = frameDelay_ms;
		this.running = false;
	}

	public void start() {
		if (this.running) {
			return;
		}
		this.running = true;
		this.canvas.set(this);
		Runtime.post(this);
	}

	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		if (!this.running) {
			return;
		}

		this.canvas.repaint();
		Runtime.post(this, this.frameDelay_ms);
	}

	@Override
	public void visible(Canvas canvas) {
	}

	@Override
	public void paint(Canvas canvas, Graphics g) {
		this.view.paint(g);
	}

	@Override
	public void revoked(Canvas canvas) {
		stop();
	}
}