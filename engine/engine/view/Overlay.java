package engine.view;

import oop.graphics.Graphics;

/**
 * Élément graphique optionnel affiché autour de la zone de jeu.
 */
public interface Overlay {

	default int widthPixel() {
		return 0;
	}

	default int heightPixel() {
		return 0;
	}

	void paint(Graphics graphics, View view, int x, int y);

	default void tick(long elapsed) {
		assert elapsed >= 0L : elapsed;
	}
}