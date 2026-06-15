package game.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;
import engine.view.View;
import game.model.Ghost;
import game.model.Gum;
import game.model.Obstacle;
import game.model.PacMan;
import oop.graphics.BufferedImage;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;

public class GameAvatarTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	/*
	 * Double de test minimal : il mémorise seulement un objet de transformation et
	 * ignore les opérations graphiques. Le test valide la coopération View/Avatar,
	 * sans ouvrir de fenêtre et sans dépendre de l'event pump.
	 */
	static final class NoOpGraphics implements Graphics {
		private Object transform = new Object();

		@Override public BufferedImage load(String path) { return null; }
		@Override public Color getColor(int a, int r, int g, int b) { return null; }
		@Override public Font getFont(String name, int style, int size) { return null; }
		@Override public Font getFont() { return null; }
		@Override public void setFont(Font font) { }
		@Override public void drawString(String text, int x, int y) { }
		@Override public Color getColor() { return null; }
		@Override public void setColor(Color color) { }
		@Override public void drawLine(int x1, int y1, int x2, int y2) { }
		@Override public void drawRect(int x, int y, int width, int height) { }
		@Override public void fillRect(int x, int y, int width, int height) { }
		@Override public void drawOval(int x, int y, int width, int height) { }
		@Override public void fillOval(int x, int y, int width, int height) { }
		@Override public void drawPolygon(int[] xs, int[] ys, int count) { }
		@Override public void fillPolygon(int[] xs, int[] ys, int count) { }
		@Override public void setClip(int x, int y, int width, int height) { }
		@Override public void drawImage(BufferedImage image, int x, int y) { }
		@Override public void drawImage(BufferedImage image, int x, int y, int width, int height) { }
		@Override public void rotate(double angle) { }
		@Override public void scale(double x, double y) { }
		@Override public void translate(int x, int y) { }
		@Override public void shear(double x, double y) { }
		@Override public Object getTransform() { return this.transform; }
		@Override public void setTransform(Object transform) { this.transform = transform; }
	}

	@Test
	void viewPaintDoesNotCrashWithGameAvatars() {
		Game game = new TestGame();
		Model model = game.model();
		View view = new View(model, 370, 370);

		PacMan pacman = new PacMan(model, "pacman");
		Ghost ghost = new Ghost(model, "ghost");
		Gum gum = new Gum(model, "gum");
		Obstacle wall = new Obstacle(model, "wall");

		model.add(pacman, model.grid().new Position(1, 1));
		model.add(ghost, model.grid().new Position(2, 1));
		model.add(gum, model.grid().new Position(4, 4));
		model.add(wall, model.grid().new Position(3, 3));

		pacman.setAvatar(new PacManAvatar(view, pacman));
		ghost.setAvatar(new GhostAvatar(view, ghost));
		gum.setAvatar(new GumAvatar(view, gum));
		wall.setAvatar(new ObstacleAvatar(view, wall));

		assertDoesNotThrow(() -> view.paint(new NoOpGraphics()));
	}
}
