package engine.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.brain.Brain;
import engine.brain.PlayerBot;
import engine.model.BasicStunt;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;
import oop.graphics.VirtualKeyCodes;
import java.util.ArrayList;
import java.util.List;
import engine.model.Entity;
import engine.model.Model;
import engine.model.Stunt;

@DisplayName("KeyboardController - clavier vers PlayerBot")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestKeyboardController {

	private static final class StuntClavier extends Stunt {
		private final List<Integer> directions = new ArrayList<Integer>();

		StuntClavier(Model model, Entity entity) {
			super(model, entity);
		}

		@Override
		public boolean walk(int degree) {
			this.directions.add(Integer.valueOf(degree));
			return valid();
		}

		List<Integer> directions() {
			return new ArrayList<Integer>(this.directions);
		}

		int nbCommandes() {
			return this.directions.size();
		}

		int derniereDirection() {
			return this.directions.get(this.directions.size() - 1).intValue();
		}
	}

	private PlayerBot nouveauPlayerBot(StuntClavier stunt) {
		return new PlayerBot(new Brain(), stunt);
	}


	@Test
	@Order(1)
	@DisplayName("01 - flèche droite donne une commande Est")
	void flecheDroite() {
		System.out.println("KeyboardController/01 : VK_RIGHT => Est");
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, player);
		PlayerBot bot = new PlayerBot(new Brain(), stunt);
		KeyboardController controller = new KeyboardController(bot);

		controller.pressed(null, VirtualKeyCodes.VK_RIGHT, ' ');

		assertEquals(PlayerBot.State.WALKING, bot.state());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
	}

	@Test
	@Order(2)
	@DisplayName("02 - touche Z donne une commande Nord")
	void toucheZ() {
		System.out.println("KeyboardController/02 : Z => Nord");
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		BasicStunt stunt = OutilsMoteur.stuntDeMarche(model, player);
		PlayerBot bot = new PlayerBot(new Brain(), stunt);
		KeyboardController controller = new KeyboardController(bot);

		controller.pressed(null, VirtualKeyCodes.VK_Z, 'z');

		assertEquals(PlayerBot.State.WALKING, bot.state());
		assertEquals(Integer.valueOf(270), bot.currentDirection());
	}
	@Test
	@Order(2)
	@DisplayName("02 - KeyboardController couvre toutes les flèches")
	void clavierFleches() {
		Model model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);

		StuntClavier stunt = new StuntClavier(model, player);
		PlayerBot bot = nouveauPlayerBot(stunt);

		KeyboardController controller = new KeyboardController(bot);

		controller.pressed(null, VirtualKeyCodes.VK_RIGHT, ' ');
		assertEquals(0, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_DOWN, ' ');
		assertEquals(90, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_LEFT, ' ');
		assertEquals(180, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_UP, ' ');
		assertEquals(270, stunt.derniereDirection());

		assertEquals(4, stunt.nbCommandes());
	}

	@Test
	@Order(3)
	@DisplayName("03 - KeyboardController couvre Z/Q/S/D")
	void clavierZQSD() {
		Model model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);

		StuntClavier stunt = new StuntClavier(model, player);
		PlayerBot bot = nouveauPlayerBot(stunt);

		KeyboardController controller = new KeyboardController(bot);

		controller.pressed(null, VirtualKeyCodes.VK_D, 'd');
		assertEquals(0, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_S, 's');
		assertEquals(90, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_Q, 'q');
		assertEquals(180, stunt.derniereDirection());

		controller.pressed(null, VirtualKeyCodes.VK_Z, 'z');
		assertEquals(270, stunt.derniereDirection());

		assertEquals(4, stunt.nbCommandes());
	}

	@Test
	@Order(4)
	@DisplayName("04 - KeyboardController ignore les touches inconnues, released et typed")
	void clavierIgnoreLeReste() {
		Model model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);

		StuntClavier stunt = new StuntClavier(model, player);
		PlayerBot bot = nouveauPlayerBot(stunt);

		KeyboardController controller = new KeyboardController(bot);

		controller.pressed(null, 9999, '?');
		controller.released(null, VirtualKeyCodes.VK_RIGHT, ' ');
		controller.typed(null, 'a');

		assertEquals(0, stunt.nbCommandes());
	}

}
