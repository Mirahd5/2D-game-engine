package engine.brain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.model.RotationStunt;
import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;
import java.util.List;
import engine.model.Model;

@DisplayName("PlayerBot - commandes du joueur")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestPlayerBot {

	@Test
	@Order(1)
	@DisplayName("01 - wish tente immédiatement de marcher")
	void wishImmediat() {
		System.out.println("PlayerBot/01 : appui clavier sans attendre le prochain tick");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);
		player.setStunt(stunt);
		PlayerBot bot = new PlayerBot(brain, stunt);

		bot.wish(5);

		assertEquals(PlayerBot.State.WALKING, bot.state());
		assertNull(bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
		assertEquals(0, stunt.derniereDirection());
	}

	@Test
	@Order(2)
	@DisplayName("02 - une direction refusée reste en attente")
	void directionEnAttente() {
		System.out.println("PlayerBot/02 : commande mémorisée si refusée");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);
		stunt.accepte(false);
		player.setStunt(stunt);
		PlayerBot bot = new PlayerBot(brain, stunt);

		bot.wish(90);

		assertEquals(PlayerBot.State.WAITING_FOR_DIRECTION, bot.state());
		assertEquals(Integer.valueOf(90), bot.wishedDirection());
	}

	@Test
	@Order(3)
	@DisplayName("03 - stopWishing remet le joueur au repos")
	void stopWishing() {
		System.out.println("PlayerBot/03 : relâcher la commande");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire stunt = new OutilsBrain.StuntMemoire(model, player);
		player.setStunt(stunt);
		PlayerBot bot = new PlayerBot(brain, stunt);

		bot.wish(0);
		bot.stopWishing();

		assertEquals(PlayerBot.State.IDLE, bot.state());
		assertNull(bot.currentDirection());
		assertNull(bot.wishedDirection());
	}

	@Test
	@Order(4)
	@DisplayName("04 - avec RotationStunt, PlayerBot relaie les commandes souris")
	void rotationCommandes() {
		System.out.println("PlayerBot/04 : relais vers RotationStunt");
		Brain brain = new Brain();
		var model = new JeuTest().model();
		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntMemoire walk = new OutilsBrain.StuntMemoire(model, player);
		RotationStunt rot = new RotationStunt(model, player, 90.0);
		player.setStunt(walk);
		PlayerBot bot = new PlayerBot(brain, walk, rot);

		bot.startTurnRight();
		assertTrue(rot.isTurningRight());
		bot.stopTurnRight();
		assertFalse(rot.isRotating());
	}
	@Test
	@Order(13)
	@DisplayName("13 - PlayerBot normalise les directions quelconques")
	void playerNormalizeDirections() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(10);
		assertEquals(Integer.valueOf(0), bot.currentDirection());

		bot.wish(80);
		assertEquals(Integer.valueOf(90), bot.currentDirection());

		bot.wish(170);
		assertEquals(Integer.valueOf(180), bot.currentDirection());

		bot.wish(260);
		assertEquals(Integer.valueOf(270), bot.currentDirection());

		bot.wish(-20);
		assertEquals(Integer.valueOf(0), bot.currentDirection());
	}

	@Test
	@Order(14)
	@DisplayName("14 - PlayerBot garde une direction souhaitée en attente")
	void playerDirectionEnAttente() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		stunt.accepteSeulement(0);

		bot.wish(0);

		assertEquals(PlayerBot.State.WALKING, bot.state());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
		assertNull(bot.wishedDirection());

		stunt.refuseTout();

		bot.wish(90);

		assertEquals(PlayerBot.State.WAITING_FOR_DIRECTION, bot.state());
		assertEquals(Integer.valueOf(90), bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());

		stunt.accepteSeulement(0);
		bot.think();

		assertEquals(PlayerBot.State.WAITING_FOR_DIRECTION, bot.state());
		assertEquals(Integer.valueOf(90), bot.wishedDirection());
		assertEquals(Integer.valueOf(0), bot.currentDirection());
	}

	@Test
	@Order(15)
	@DisplayName("15 - PlayerBot done/collision/stopWishing couvrent les transitions")
	void playerDoneCollisionStop() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		bot.wish(0);
		assertEquals(PlayerBot.State.WALKING, bot.state());

		bot.done();
		assertEquals(PlayerBot.State.WALKING, bot.state());

		bot.collision(player);
		bot.collision(List.of(player));

		assertTrue(stunt.nbCommandes() >= 3);

		bot.stopWishing();

		assertEquals(PlayerBot.State.IDLE, bot.state());
		assertNull(bot.wishedDirection());
		assertNull(bot.currentDirection());
	}

	@Test
	@Order(16)
	@DisplayName("16 - PlayerBot sans RotationStunt refuse les commandes de rotation")
	void playerSansRotation() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);
		OutilsBrain.StuntChoix stunt = OutilsBrain.stunt(model, player);

		PlayerBot bot = new PlayerBot(new Brain(), stunt);

		assertFalse(bot.canRotate());

		assertThrows(IllegalStateException.class, () -> bot.startTurnLeft());
		assertThrows(IllegalStateException.class, () -> bot.stopTurnLeft());
		assertThrows(IllegalStateException.class, () -> bot.startTurnRight());
		assertThrows(IllegalStateException.class, () -> bot.stopTurnRight());
	}

	@Test
	@Order(17)
	@DisplayName("17 - PlayerBot avec RotationStunt transmet les commandes de rotation")
	void playerAvecRotation() {
		Model model = new JeuTest().model();

		EntiteTest player = OutilsMoteur.ajoute(model, "player", 1, 1);

		OutilsBrain.StuntChoix walking = OutilsBrain.stunt(model, player);
		RotationStunt rotation = new RotationStunt(model, player, 90.0);

		PlayerBot bot = new PlayerBot(new Brain(), walking, rotation);

		assertTrue(bot.canRotate());

		bot.startTurnLeft();
		assertTrue(rotation.isTurningLeft());

		bot.stopTurnLeft();
		assertFalse(rotation.isRotating());

		bot.startTurnRight();
		assertTrue(rotation.isTurningRight());

		bot.stopTurnRight();
		assertFalse(rotation.isRotating());
	}

}
