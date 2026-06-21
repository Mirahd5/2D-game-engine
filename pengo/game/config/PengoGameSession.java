package game.config;

import engine.view.Painter;
import engine.view.PainterTask;
import engine.view.Ticker;
import engine.view.View;
import game.controller.PengoController;
import oop.graphics.Canvas;

public final class PengoGameSession {

	private final Painter painter;
	private final PengoController controller;
	private final Ticker ticker;
	private final PainterTask painterTask;
	private final View view;

	private Canvas canvas;
	private boolean stopped;

	public PengoGameSession(Painter painter, PengoController controller, Ticker ticker, PainterTask painterTask,
			View view) {

		assert painter != null;
		assert controller != null;
		assert ticker != null;
		assert painterTask != null;
		assert view != null;

		this.painter = painter;
		this.controller = controller;
		this.ticker = ticker;
		this.painterTask = painterTask;
		this.view = view;
		this.canvas = null;
		this.stopped = false;
	}

	public Painter painter() {
		return this.painter;
	}

	public PengoController controller() {
		return this.controller;
	}

	public void attach(Canvas canvas) {
		assert canvas != null;

		this.canvas = canvas;
		resizeTo(canvas);
		this.painter.visible(canvas);
	}

	public void resizeTo(Canvas canvas) {
		assert canvas != null;

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (width <= 0 || height <= 0) {
			return;
		}

		if (this.view.widthPixel() == width && this.view.heightPixel() == height) {
			return;
		}

		this.view.resize(width, height);
	}

	public void repaint() {
		if (this.canvas != null) {
			this.canvas.repaint();
		}
	}

	public void stop() {
		if (this.stopped) {
			return;
		}

		this.stopped = true;

		this.ticker.stop();
		this.painterTask.stop();

		if (this.canvas != null) {
			this.painter.revoked(this.canvas);
			this.canvas = null;
		}
	}
	
	// utile quand le canvas est supprime ou recreee
	public void detach(Canvas canvas) {
		if (canvas == null) {
			return;
		}

		if (this.canvas == canvas) {
			this.canvas = null;
		}

		this.painter.revoked(canvas);
	}
}