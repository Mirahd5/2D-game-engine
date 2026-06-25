package engine.controller;

import oop.graphics.Canvas;

/**
 * Contrôleur générique qui transforme les événements clavier et souris en actions de jeu.
 */
public abstract class Controller implements Canvas.KeyListener, Canvas.MouseListener {

	private boolean enabled;

	protected Controller() {
		this.enabled = true;
	}

	public final boolean enabled() {
		return this.enabled;
	}

	public final void enable() {
		this.enabled = true;
	}

	public final void disable() {
		this.enabled = false;
	}

	@Override
	public final void pressed(Canvas canvas, int keyCode, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyPressed(canvas, keyCode, keyChar);
	}

	@Override
	public final void released(Canvas canvas, int keyCode, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyReleased(canvas, keyCode, keyChar);
	}

	@Override
	public final void typed(Canvas canvas, char keyChar) {
		if (!this.enabled) {
			return;
		}

		keyTyped(canvas, keyChar);
	}

	@Override
	public final void moved(Canvas canvas, int x, int y) {
		if (!this.enabled) {
			return;
		}

		mouseMoved(canvas, x, y);
	}

	@Override
	public final void pressed(Canvas canvas, int button, int x, int y) {
		if (!this.enabled) {
			return;
		}

		mousePressed(canvas, button, x, y);
	}

	@Override
	public final void released(Canvas canvas, int x, int y, int button) {
		if (!this.enabled) {
			return;
		}

		mouseReleased(canvas, x, y, button);
	}

	protected void keyPressed(Canvas canvas, int keyCode, char keyChar) {
	}

	protected void keyReleased(Canvas canvas, int keyCode, char keyChar) {
	}

	protected void keyTyped(Canvas canvas, char keyChar) {
	}

	protected void mouseMoved(Canvas canvas, int x, int y) {
	}

	protected void mousePressed(Canvas canvas, int button, int x, int y) {
	}

	protected void mouseReleased(Canvas canvas, int x, int y, int button) {
	}
}