package engine.view;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Model;

public class ViewPaintTest {

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	static class TestEntity extends Entity {
		TestEntity(Model model, String name) {
			super(model, name);
			setBounding();
		}

		@Override
		protected void setBounding() {
			this.bounding = new Bounding();
			this.bounding.add(new Circle(this.center, Math.min(this.size.x(), this.size.y()) / 2.0));
		}
	}
	
	@Test
	void entitiesAreSortedByZOrderThenSOrder() {
		Game game = new TestGame();
		Model model = game.model();

		Entity gum = new TestEntity(model, "gum");
		Entity wall = new TestEntity(model, "wall");
		Entity pacman = new TestEntity(model, "pacman");
		Entity ghost = new TestEntity(model, "ghost");

		gum.setZOrder(0);
		gum.setSOrder(0);

		wall.setZOrder(1);
		wall.setSOrder(0);

		pacman.setZOrder(2);
		pacman.setSOrder(0);

		ghost.setZOrder(2);
		ghost.setSOrder(1);

		assertTrue(gum.zOrder() < wall.zOrder());
		assertTrue(wall.zOrder() < pacman.zOrder());
		assertTrue(pacman.zOrder() == ghost.zOrder());
		assertTrue(pacman.sOrder() < ghost.sOrder());
	}

	@Test
	void entitiesCanBeSortedByZOrderAndSOrder() {
		Game game = new TestGame();
		Model model = game.model();

		Entity low = new TestEntity(model, "low");
		Entity high = new TestEntity(model, "high");

		low.setZOrder(0);
		high.setZOrder(2);

		assertTrue(low.zOrder() < high.zOrder());

		low.setSOrder(0);
		high.setSOrder(1);

		assertTrue(low.sOrder() < high.sOrder());
	}
	
	@Test
	void avatarTickDoesNotCrash() {
		Game game = new TestGame();
		Model model = game.model();

		View view = new View(model, 370, 370);

		Entity entity = new TestEntity(model, "e");
		entity.setAvatar(new game.view.PacManAvatar(view, entity));

		model.add(entity);

		assertDoesNotThrow(() -> view.tick(150L));
		assertDoesNotThrow(() -> view.tick(150L));
	}
}