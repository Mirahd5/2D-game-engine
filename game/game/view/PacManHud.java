package game.view;

import engine.view.Overlay;
import engine.view.View;
import oop.graphics.Graphics;

/*
 * HUD spécifique à Pac-Man.
 *
 * Cette classe appartient au jeu et non au moteur.
 */
public class PacManHud implements Overlay {

	public static final int WIDTH_PIXEL = 220;

	private int score;
	private int lives;
	private String message;

	public PacManHud() {
		this.score = 0;
		this.lives = 3;
		this.message = "";
	}

	@Override
	public int widthPixel() {
		return WIDTH_PIXEL;
	}

	public int score() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int lives() {
		return this.lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public String message() {
		return this.message;
	}

	public void setMessage(String message) {
		assert message != null;

		this.message = message;
	}

	@Override
	public void paint(Graphics graphics, View view, int x, int y) {
		assert graphics != null;
		assert view != null;

		graphics.setColor(Graphics.Colors.blue);

		graphics.fillRect(x, y, widthPixel(), view.heightPixel());

		graphics.setColor(Graphics.Colors.white);

		int textX = x + 20;
		int textY = y + 40;

		graphics.drawString("PACMAN", textX, textY);

		textY += 40;

		graphics.drawString("Score : " + this.score, textX, textY);

		textY += 30;

		graphics.drawString("Lives : " + this.lives, textX, textY);

		if (!this.message.isEmpty()) {
			textY += 50;

			graphics.drawString(this.message, textX, textY);
		}
	}
}