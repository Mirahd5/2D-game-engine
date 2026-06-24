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
	private final PengoRunState runState;

	private Canvas canvas;
	private boolean stopped;

	public PengoGameSession(Painter painter, PengoController controller, Ticker ticker, PainterTask painterTask,
			View view, PengoRunState runState) {

		assert painter != null;
		assert controller != null;
		assert ticker != null;
		assert painterTask != null;
		assert view != null;
		assert runState != null;

		this.painter = painter;
		this.controller = controller;
		this.ticker = ticker;
		this.painterTask = painterTask;
		this.view = view;
		this.runState = runState;
		this.canvas = null;
		this.stopped = false;
	}

	public Painter painter() {
		return this.painter;
	}

	public PengoController controller() {
		return this.controller;
	}

	public View view() {
		return this.view;
	}

	public boolean paused() {
		return this.runState.paused();
	}
	
	public boolean finished() {
		return this.controller.gameFinished();
	}

	public boolean won() {
		return this.controller.gameWon();
	}

	public boolean lost() {
		return this.controller.gameLost();
	}

	public boolean stopped() {
		return this.stopped || this.runState.stopped();
	}

	public void pause() {
		if (stopped() || paused()) {
			return;
		}

		this.runState.pause();
		this.ticker.pause();
		this.controller.clearInputs();
		this.controller.disable();
		repaint();
	}

	public void resume() {
		if (stopped() || !paused()) {
			return;
		}

		this.runState.resume();
		this.controller.clearInputs();
		this.controller.enable();
		this.ticker.resume();
		repaint();
	}

	public void togglePause() {
		if (paused()) {
			resume();
		} else {
			pause();
		}
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

		int overlayWidth = this.view.overlayWidthPixel();
		int overlayHeight = this.view.overlayHeightPixel();

		int worldWidth = width - overlayWidth;
		int worldHeight = height - overlayHeight;

		if (worldWidth < 240) {
			worldWidth = Math.max(1, width);
		}

		if (worldHeight < 240) {
			worldHeight = Math.max(1, height);
		}

		if (this.view.widthPixel() == worldWidth && this.view.heightPixel() == worldHeight) {
			return;
		}

		this.view.resize(worldWidth, worldHeight);
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
		this.runState.stop();
		this.controller.clearInputs();
		this.controller.disable();
		this.ticker.stop();
		this.painterTask.stop();

		if (this.canvas != null) {
			this.painter.revoked(this.canvas);
			this.canvas = null;
		}
	}

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