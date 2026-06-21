package engine.controller;

import oop.graphics.Canvas;

/*
 * Contrôleur générique du moteur.
 *
 * Il reçoit les événements clavier et souris de la plateforme.
 * Par défaut, il ne fait rien.
 *
 * Les jeux spécifiques héritent de cette classe et redéfinissent
 * seulement les événements dont ils ont besoin.
 */
public abstract class Controller implements Canvas.KeyListener, Canvas.MouseListener {

	private boolean enabled;

	protected Controller() {
		this.enabled = true;
	}

	public boolean enabled() {
		return this.enabled;
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	// =========================
	// Keyboard events
	// =========================

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		// Rien par défaut.
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		// Rien par défaut.
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		// Rien par défaut.
	}

	// =========================
	// Mouse events
	// =========================

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// Rien par défaut.
	}

	@Override
	public void pressed(Canvas canvas, int button, int x, int y) {
		// Rien par défaut.
	}

	@Override
	public void released(Canvas canvas, int x, int y, int button) {
		// Rien par défaut.
	}
}