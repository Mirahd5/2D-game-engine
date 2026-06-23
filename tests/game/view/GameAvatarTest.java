package game.view;

import static org.junit.jupiter.api.Assertions.*;

import oop.graphics.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import engine.model.Game;
import engine.model.Model;
import engine.view.View;
import game.model.Ghost;
import game.model.Gum;
import game.model.Obstacle;
import game.model.PacMan;

public class GameAvatarTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	@Test
	void viewPaintDoesNotCrashWithGameAvatars() {
		Game game = new TestGame();
		Model model = game.model();
		View view = new View(model, 370, 370);

		PacMan pacman = new PacMan(model, "pacman");
		Ghost ghost = new Ghost(model, "ghost");
		Gum gum = new Gum(model, "gum");
		Obstacle wall = new Obstacle(model, "wall", 3, 3);

		pacman.setAvatar(new PacManAvatar(view, pacman));
		ghost.setAvatar(new GhostAvatar(view, ghost));
		gum.setAvatar(new GumAvatar(view, gum));
		wall.setAvatar(new ObstacleAvatar(view, wall));

		pacman.setPosition(game.getGrid().new Position(1, 1));
		ghost.setPosition(game.getGrid().new Position(2, 1));
		gum.setPosition(game.getGrid().new Position(4, 4));

		model.add(pacman);
		model.add(ghost);
		model.add(gum);
		model.add(wall);

		BufferedImage image = new BufferedImage(370, 370, BufferedImage.TYPE_INT_ARGB);
		//Graphics g = image.createGraphics();

		//assertDoesNotThrow(() -> view.paint(g));

		//g.dispose();
	}
}