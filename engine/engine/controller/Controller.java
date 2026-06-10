package engine.controller;

import oop.graphics.Canvas;

public abstract class Controller implements Canvas.KeyListener, Canvas.MouseListener {

	// FIELDS
	protected final Canvas canvas;

	// CONSTRUCTOR
	public Controller(Canvas canvas) {
		assert canvas != null : "canvas must not be null";

		this.canvas = canvas;

		// On branche ce contrôleur au canvas.
		this.canvas.set((Canvas.KeyListener) this);
		this.canvas.set((Canvas.MouseListener) this);
	}

	// KEY LISTENER
	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
	}

	// MOUSE LISTENER

	@Override
	public void moved(Canvas canvas, int x, int y) {
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
	}

	@Override
	public void released(Canvas canvas, int button, int x, int y) {
	}
}