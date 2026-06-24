package engine.model;

import static engine.tests.OutilsMoteur.EPS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import engine.tests.OutilsMoteur;
import engine.tests.OutilsMoteur.EntiteTest;
import engine.tests.OutilsMoteur.JeuTest;

@DisplayName("RotationStunt - rotation continue")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class TestRotationStunt {

	private void forceBoolean(RotationStunt stunt, String fieldName, boolean value) throws Exception {
		Field field = RotationStunt.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.setBoolean(stunt, value);
	}

	@Test
	@Order(1)
	@DisplayName("01 - constructeur par défaut : rotation droite à 180 degrés/seconde")
	void constructeurParDefaut() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e);
		e.setStunt(stunt);

		assertFalse(stunt.isRotating());
		assertFalse(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertFalse(stunt.walk(0));

		stunt.startTurnRight();

		assertTrue(stunt.isRotating());
		assertTrue(stunt.isTurningRight());
		assertFalse(stunt.isTurningLeft());
		assertEquals(180.0, e.angularSpeed(), EPS);

		model.tick(500L);

		assertEquals(90, e.orientation());
	}

	@Test
	@Order(2)
	@DisplayName("02 - startTurnRight applique une vitesse angulaire positive")
	void rotationDroite() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.startTurnRight();

		assertTrue(stunt.isTurningRight());
		assertFalse(stunt.isTurningLeft());
		assertTrue(stunt.isRotating());
		assertEquals(90.0, e.angularSpeed(), EPS);

		model.tick(1000L);

		assertEquals(90, e.orientation());
	}

	@Test
	@Order(3)
	@DisplayName("03 - stopTurnRight coupe la rotation droite")
	void stopRotationDroite() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.startTurnRight();
		assertEquals(90.0, e.angularSpeed(), EPS);

		stunt.stopTurnRight();

		assertFalse(stunt.isTurningRight());
		assertFalse(stunt.isTurningLeft());
		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(4)
	@DisplayName("04 - startTurnLeft applique une vitesse angulaire négative")
	void rotationGauche() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.startTurnLeft();

		assertTrue(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertTrue(stunt.isRotating());
		assertEquals(-90.0, e.angularSpeed(), EPS);

		model.tick(1000L);

		assertEquals(270, e.orientation());
	}

	@Test
	@Order(5)
	@DisplayName("05 - stopTurnLeft coupe la rotation gauche")
	void stopRotationGauche() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.startTurnLeft();
		assertEquals(-90.0, e.angularSpeed(), EPS);

		stunt.stopTurnLeft();

		assertFalse(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(6)
	@DisplayName("06 - stopTurnLeft et stopTurnRight ne font rien si la rotation n'est pas active")
	void stopQuandDejaArrete() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);

		stunt.stopTurnLeft();
		stunt.stopTurnRight();

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(7)
	@DisplayName("07 - changer de direction remplace l'ancienne rotation")
	void changementDeDirection() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 60.0);
		e.setStunt(stunt);

		stunt.startTurnLeft();

		assertTrue(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertEquals(-60.0, e.angularSpeed(), EPS);

		stunt.startTurnRight();

		assertFalse(stunt.isTurningLeft());
		assertTrue(stunt.isTurningRight());
		assertEquals(60.0, e.angularSpeed(), EPS);

		stunt.startTurnLeft();

		assertTrue(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertEquals(-60.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(8)
	@DisplayName("08 - un stunt invalidé refuse de démarrer une rotation")
	void stuntInvalide() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.invalidate();

		assertFalse(stunt.isRotating());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.isRotating());
		assertFalse(stunt.isTurningRight());
		assertFalse(stunt.isTurningLeft());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(9)
	@DisplayName("09 - invalidate remet les états internes à false")
	void invalidateNettoieEtatInterne() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		stunt.startTurnRight();

		assertTrue(stunt.isRotating());

		stunt.invalidate();

		assertFalse(stunt.isRotating());
		assertFalse(stunt.isTurningRight());
		assertFalse(stunt.isTurningLeft());
	}

	@Test
	@Order(10)
	@DisplayName("10 - une entité non installée ne peut pas tourner")
	void entiteNonInstallee() {
		Model model = new JeuTest().model();

		EntiteTest e = new EntiteTest(model, "nonInstallee");

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		assertFalse(e.installed());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(11)
	@DisplayName("11 - une entité morte ne peut pas démarrer une rotation")
	void entiteMorte() {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		e.death();

		assertFalse(e.alive());

		stunt.startTurnRight();
		stunt.startTurnLeft();

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(12)
	@DisplayName("12 - stopTurnRight ne remet pas la vitesse à zéro si l'entité n'est plus installée")
	void stopRightEntiteRetiree() throws Exception {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		model.remove(e);

		assertFalse(e.installed());

		e.setAngularSpeed(90.0);
		forceBoolean(stunt, "turningRight", true);

		stunt.stopTurnRight();

		assertFalse(stunt.isTurningRight());
		assertEquals(90.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(13)
	@DisplayName("13 - stopTurnLeft ne remet pas la vitesse à zéro si l'entité est morte")
	void stopLeftEntiteMorte() throws Exception {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		e.death();

		assertFalse(e.alive());

		e.setAngularSpeed(-90.0);
		forceBoolean(stunt, "turningLeft", true);

		stunt.stopTurnLeft();

		assertFalse(stunt.isTurningLeft());
		assertEquals(-90.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(14)
	@DisplayName("14 - branche interne : stopLeft ne coupe pas si right est encore actif")
	void stopLeftAvecRightEncoreActif() throws Exception {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		e.setAngularSpeed(90.0);
		forceBoolean(stunt, "turningLeft", true);
		forceBoolean(stunt, "turningRight", true);

		stunt.stopTurnLeft();

		assertFalse(stunt.isTurningLeft());
		assertTrue(stunt.isTurningRight());
		assertTrue(stunt.isRotating());
		assertEquals(90.0, e.angularSpeed(), EPS);

		stunt.stopTurnRight();

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}

	@Test
	@Order(15)
	@DisplayName("15 - branche interne : stopRight ne coupe pas si left est encore actif")
	void stopRightAvecLeftEncoreActif() throws Exception {
		Model model = new JeuTest().model();
		EntiteTest e = OutilsMoteur.ajoute(model, "e", 2, 2);

		RotationStunt stunt = new RotationStunt(model, e, 90.0);
		e.setStunt(stunt);

		e.setAngularSpeed(-90.0);
		forceBoolean(stunt, "turningLeft", true);
		forceBoolean(stunt, "turningRight", true);

		stunt.stopTurnRight();

		assertTrue(stunt.isTurningLeft());
		assertFalse(stunt.isTurningRight());
		assertTrue(stunt.isRotating());
		assertEquals(-90.0, e.angularSpeed(), EPS);

		stunt.stopTurnLeft();

		assertFalse(stunt.isRotating());
		assertEquals(0.0, e.angularSpeed(), EPS);
	}
}