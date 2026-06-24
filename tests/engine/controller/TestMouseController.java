package engine.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@DisplayName("MouseController - FSM des deux boutons")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestMouseController {



	static class CommandesMemoire implements RotationCommands {
		int leftStart;
		int leftStop;
		int rightStart;
		int rightStop;

		@Override
		public void startTurnLeft() {
			leftStart++;
		}

		@Override
		public void stopTurnLeft() {
			leftStop++;
		}

		@Override
		public void startTurnRight() {
			rightStart++;
		}

		@Override
		public void stopTurnRight() {
			rightStop++;
		}
	}

	@Test
	@Order(1)
	@DisplayName("01 - bouton gauche démarre puis arrête la rotation gauche")
	void rotationGaucheSimple() {
		System.out.println("MouseController/01 : clic gauche maintenu");
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(1, commands.leftStop);
	}

	@Test
	@Order(2)
	@DisplayName("02 - le premier bouton reste prioritaire quand les deux sont pressés")
	void prioritePremierBouton() {
		System.out.println("MouseController/02 : gauche puis droite");
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT_RP, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 3, 0, 0);
		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		controller.released(null, 1, 0, 0);
		assertEquals(MouseController.State.NO_ROT, controller.state());
	}
	@Test
	@Order(5)
	@DisplayName("05 - MouseController couvre clic droit simple")
	void sourisDroiteSimple() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(1, commands.rightStart);
		assertEquals(0, commands.leftStart);

		controller.released(null, 3, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(6)
	@DisplayName("06 - MouseController couvre priorité droite puis gauche")
	void sourisPrioriteDroitePuisGauche() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 3, 0, 0);
		controller.pressed(null, 1, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT_LP, controller.state());
		assertEquals(1, commands.rightStart);
		assertEquals(0, commands.leftStart);

		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(0, commands.rightStop);

		controller.released(null, 3, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(7)
	@DisplayName("07 - MouseController couvre priorité gauche puis droite, relâchement prioritaire d'abord")
	void sourisPrioriteGaucheRelacheeAvantDroite() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT_RP, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
		assertEquals(0, commands.rightStart);

		controller.released(null, 3, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);
		assertEquals(0, commands.rightStop);
	}

	@Test
	@Order(8)
	@DisplayName("08 - MouseController ignore les boutons inconnus et moved")
	void sourisIgnoreBoutonsInconnus() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.moved(null, 10, 20);
		controller.pressed(null, 2, 0, 0);
		controller.released(null, 2, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(0, commands.leftStart);
		assertEquals(0, commands.leftStop);
		assertEquals(0, commands.rightStart);
		assertEquals(0, commands.rightStop);
	}

	@Test
	@Order(9)
	@DisplayName("09 - MouseController ignore les relâchements non cohérents")
	void sourisRelachementsIncoherents() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.released(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT, controller.state());
		assertEquals(0, commands.leftStop);

		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.leftStop);

		controller.pressed(null, 3, 0, 0);
		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.RIGHT_ROT, controller.state());
		assertEquals(0, commands.rightStop);

		controller.released(null, 3, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
		assertEquals(1, commands.rightStop);
	}

	@Test
	@Order(10)
	@DisplayName("10 - MouseController ignore les pressions quand les deux boutons sont déjà considérés pressés")
	void sourisDeuxBoutonsDejaPresses() {
		CommandesMemoire commands = new CommandesMemoire();
		MouseController controller = new MouseController(commands);

		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);
		controller.pressed(null, 1, 0, 0);
		controller.pressed(null, 3, 0, 0);

		assertEquals(MouseController.State.LEFT_ROT_RP, controller.state());
		assertEquals(1, commands.leftStart);
		assertEquals(0, commands.rightStart);

		controller.released(null, 3, 0, 0);
		controller.released(null, 1, 0, 0);

		assertEquals(MouseController.State.NO_ROT, controller.state());
	}

}
