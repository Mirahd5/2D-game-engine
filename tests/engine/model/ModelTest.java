package engine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_METHOD)
public class ModelTest {

	private static final double EPS = 0.000001;

	static class TestGame extends Game {
		TestGame() {
			super(10, 10, true, true, 3.7, 2);
		}
	}

	/*
	 * Petite entité de test générique. On évite d'utiliser PacMan/Ghost/Gum ici,
	 * car ce fichier teste engine.model.Model, pas les classes spécifiques du jeu.
	 */
	static class TestEntity extends Entity {

		TestEntity(Model model, String name) {
			super(model, name);
			updateBounding();
		}

		@Override
		protected Bounding createBounding(
			engine.model.ISU.Coord center,
			engine.model.ISU.Dimension size,
			int orientationDegree
		) {
			Bounding result = new Bounding();
			result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
			return result;
		}

		@Override
		public boolean blocks(Entity other) {
			return true;
		}
	}

	private Game game;
	private Model model;

	@BeforeEach
	void setup() {
		this.game = new TestGame();
		this.model = game.model();
	}

	@Test
	void modelInitialStateIsCorrect() {
		/*
		 * Situation : Un nouveau Game crée automatiquement son Model.
		 *
		 * On vérifie que le Model utilise bien la Grid et l'ISU du Game, et que la
		 * liste des entités est vide au départ.
		 */
		assertSame(game, model.game());
		assertSame(game.getGrid(), model.grid());
		assertSame(game.getIsu(), model.isu());

		assertNotNull(model.entities());
		assertTrue(model.entities().isEmpty());
	}

	@Test
	void modelCanAddEntity() {
		/*
		 * Situation : On ajoute une entité dans le Model.
		 *
		 * Résultat attendu : La liste contient exactement cette entité.
		 */
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		assertEquals(1, model.entities().size());
		assertTrue(model.entities().contains(entity));
	}

	@Test
	void addingSameEntityTwiceIsRejected() {
		/*
		 * Une entité ne peut être installée qu'une seule fois dans un Model.
		 * Le second ajout est donc une erreur de programmation attendue.
		 */
		Entity entity = new TestEntity(model, "e");

		model.add(entity);

		assertThrows(AssertionError.class, () -> model.add(entity));
		assertEquals(1, model.entities().size());
		assertTrue(model.entities().contains(entity));
	}

	@Test
	void modelCanRemoveEntity() {
		/*
		 * Situation : On ajoute puis on retire une entité.
		 *
		 * Résultat attendu : Elle n'est plus dans la liste du Model.
		 */
		Entity entity = new TestEntity(model, "e");

		model.add(entity);
		model.remove(entity);

		assertTrue(model.entities().isEmpty());
		assertFalse(model.entities().contains(entity));
	}

	@Test
	void removeNonExistingEntityDoesNothing() {
		/*
		 * Situation : On retire une entité qui n'a jamais été ajoutée.
		 *
		 * Résultat attendu : Le Model reste vide, sans erreur.
		 */
		Entity entity = new TestEntity(model, "e");

		model.remove(entity);

		assertTrue(model.entities().isEmpty());
	}

	@Test
	void modelCanContainSeveralEntities() {
		/*
		 * Situation : On ajoute plusieurs entités différentes.
		 *
		 * Résultat attendu : Le Model les contient toutes.
		 */
		Entity e1 = new TestEntity(model, "e1");
		Entity e2 = new TestEntity(model, "e2");
		Entity e3 = new TestEntity(model, "e3");

		model.add(e1);
		model.add(e2);
		model.add(e3);

		assertEquals(3, model.entities().size());
		assertTrue(model.entities().contains(e1));
		assertTrue(model.entities().contains(e2));
		assertTrue(model.entities().contains(e3));
	}

	@Test
	void collisionsReturnsEntitiesThatIntersect() {
		/*
		 * Situation : Deux entités sont placées au même endroit.
		 *
		 * Résultat attendu : Model.collisions(e1) doit retourner e2.
		 */
		Entity e1 = new TestEntity(model, "e1");
		model.add(e1);
		Entity e2 = new TestEntity(model, "e2");
		model.add(e2);

		e1.setPosition(game.getGrid().new Position(2, 2));
		e2.setPosition(game.getGrid().new Position(2, 2));


		assertEquals(1, model.collisions(e1).size());
		assertTrue(model.collisions(e1).contains(e2));
	}

	@Test
	void collisionsReturnsEmptyListWhenNoEntityIntersects() {
		/*
		 * Situation : Deux entités sont loin l'une de l'autre.
		 *
		 * Résultat attendu : Model.collisions(e1) doit être vide.
		 */
		Entity e1 = new TestEntity(model, "e1");
		model.add(e1);
		Entity e2 = new TestEntity(model, "e2");
		model.add(e2);

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(5, 5));


		assertTrue(model.collisions(e1).isEmpty());
	}

	@Test
	void moveSucceedsWhenNoCollision() {
		/*
		 * Situation : Une entité avance vers une cellule libre.
		 *
		 * Rappel : La vitesse est en cm/s. Si speed.x = cmPerCell et elapsed = 1000 ms,
		 * l'entité avance d'une cellule vers l'est.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		entity.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		boolean moved = model.move(entity, 1000L);

		assertTrue(moved);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());

		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void tickMovesEntityAccordingToLinearSpeed() {
		/*
		 * Situation : Une entité a une vitesse linéaire vers l'est.
		 *
		 * Résultat attendu : model.tick(1000) applique cette vitesse pendant 1 seconde.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		entity.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		model.tick(1000L);

		assertEquals(1, entity.position().x());
		assertEquals(0, entity.position().y());

		assertEquals((1 + 0.5) * game.getCmPerCell(), entity.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), entity.center().y(), EPS);
	}

	@Test
	void tickMovesEntityAccordingToAngularSpeed() {
		/*
		 * Situation : Une entité a une vitesse angulaire.
		 *
		 * Ici on utilise 90 degrés/seconde. Avec elapsed = 1000 ms, elle doit tourner
		 * de 90 degrés.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		entity.setAngularSpeed(90.0);

		model.tick(1000L);

		assertEquals(90, entity.orientation());
	}

	@Test
	void collisionDuringMoveFailsAndRestoresOldPosition() {
		/*
		 * Situation : e1 est en (0,0), e2 est en (1,0). e1 avance vers l'est.
		 *
		 * Résultat attendu : Le déplacement échoue, car e1 arrive sur e2. e1 revient à
		 * son ancienne position.
		 */
		Entity e1 = new TestEntity(model, "e1");
		model.add(e1);
		Entity e2 = new TestEntity(model, "e2");
		model.add(e2);

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(1, 0));


		e1.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		boolean moved = model.move(e1, 1000L);

		assertFalse(moved);

		assertEquals(0, e1.position().x());
		assertEquals(0, e1.position().y());

		assertEquals((0 + 0.5) * game.getCmPerCell(), e1.center().x(), EPS);
		assertEquals((0 + 0.5) * game.getCmPerCell(), e1.center().y(), EPS);
	}

	@Test
	void collisionDuringMoveResetsSpeed() {
		/*
		 * Situation : e1 avance vers e2 et collisionne.
		 *
		 * Résultat attendu : Une collision remet la vitesse linéaire et angulaire à
		 * zéro.
		 */
		Entity e1 = new TestEntity(model, "e1");
		model.add(e1);
		Entity e2 = new TestEntity(model, "e2");
		model.add(e2);

		e1.setPosition(game.getGrid().new Position(0, 0));
		e2.setPosition(game.getGrid().new Position(1, 0));


		e1.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));
		e1.setAngularSpeed(90.0);

		boolean moved = model.move(e1, 1000L);

		assertFalse(moved);

		assertEquals(0.0, e1.linearSpeed().x(), EPS);
		assertEquals(0.0, e1.linearSpeed().y(), EPS);
		assertEquals(0.0, e1.angularSpeed(), EPS);
	}

	@Test
	void tickUsesCurrentSpeedEachTime() {
		/*
		 * Situation : On appelle tick deux fois.
		 *
		 * Résultat attendu : Comme la vitesse reste active, l'entité continue à
		 * avancer.
		 *
		 * Ce test montre que BasicStunt devra plus tard arrêter la vitesse quand un
		 * walk d'une cellule est terminé.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		entity.setLinearSpeed(game.getIsu().new Vector(game.getCmPerCell(), 0.0));

		model.tick(1000L);
		assertEquals(1, entity.position().x());

		model.tick(1000L);
		assertEquals(2, entity.position().x());
	}

	@Test
	void deathRemovesEntityFromModel() {
		/*
		 * Situation : Une entité meurt.
		 *
		 * Résultat attendu : Elle est retirée du Model.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		assertTrue(model.entities().contains(entity));
		assertTrue(entity.alive());

		entity.death();
		model.tick(0L);

		assertFalse(entity.alive());
		assertFalse(model.entities().contains(entity));
	}

	@Test
	void deathRetractsEntityFromOccupiedCells() {
		/*
		 * Situation : Une entité occupe la cellule (0,0), puis meurt.
		 *
		 * Résultat attendu : La cellule ne contient plus cette entité.
		 */
		Entity entity = new TestEntity(model, "e");
		model.add(entity);

		Grid.Cell cell = game.getGrid().cellAt(game.getGrid().new Position(0, 0));

		assertTrue(cell.contains(entity));
		assertTrue(entity.occupied().contains(cell));

		entity.death();
		model.tick(0L);

		assertFalse(cell.contains(entity));
		assertTrue(entity.occupied().isEmpty());
	}

	@Test
	void visibleEntitiesReturnsOnlyEntitiesInsideViewPort() {
		Game game = new TestGame();
		Model model = game.model();

		Entity e1 = new TestEntity(model, "visible");
		model.add(e1);
		Entity e2 = new TestEntity(model, "hidden");
		model.add(e2);

		e1.setPosition(game.getGrid().new Position(1, 1));
		e2.setPosition(game.getGrid().new Position(8, 8));


		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(0.0, 0.0), game.getIsu().new Dimension(18.5, 18.5));

		model.setViewPort(vp);

		assertTrue(model.visibleEntities().contains(e1));
		assertFalse(model.visibleEntities().contains(e2));
	}

	@Test
	void visibleEntitiesReturnsOnlyEntitiesInViewPortCells() {
		/*
		 * Situation : Le ViewPort couvre les cellules proches du coin haut gauche. Une
		 * entité est dans cette zone, l'autre est loin.
		 *
		 * Résultat attendu : Le Model retourne seulement l'entité située dans les
		 * cellules couvertes par le ViewPort.
		 */
		Entity visible = new TestEntity(model, "visible");
		model.add(visible);
		Entity hidden = new TestEntity(model, "hidden");
		model.add(hidden);

		visible.setPosition(game.getGrid().new Position(1, 1));
		hidden.setPosition(game.getGrid().new Position(8, 8));


		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(0.0, 0.0), game.getIsu().new Dimension(18.5, 18.5));

		model.setViewPort(vp);

		assertTrue(model.visibleEntities().contains(visible));
		assertFalse(model.visibleEntities().contains(hidden));
	}

	@Test
	void visibleEntitiesUsesGridCellsNotOnlyEntityCenter() {
		/*
		 * Situation : L'entité est dans la cellule (2,0).
		 *
		 * Cellule (2,0) en cm : x commence à 2 * 3.7 = 7.4 x finit à 3 * 3.7 = 11.1
		 *
		 * Le ViewPort couvre seulement une petite partie de cette cellule : x = [7.4 ;
		 * 7.9]
		 *
		 * Le centre de l'entité est à x = 9.25, donc il est en dehors du ViewPort. Mais
		 * la cellule de l'entité intersecte bien le ViewPort.
		 *
		 * Résultat attendu : L'entité est visible, parce que CM2 demande de
		 * sélectionner les entités depuis les cellules couvertes par le ViewPort.
		 */
		Entity entity = new TestEntity(model, "entity");
		model.add(entity);

		entity.setPosition(game.getGrid().new Position(2, 0));

		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(2 * game.getCmPerCell(), 0.0),
				game.getIsu().new Dimension(0.5, game.getCmPerCell()));

		model.setViewPort(vp);

		assertFalse(entity.center().x() >= vp.x() && entity.center().x() <= vp.x() + vp.width());
		assertTrue(model.visibleEntities().contains(entity));
	}

	@Test
	void visibleEntitiesDoesNotReturnDuplicates() {
		/*
		 * Situation : Une entité peut occuper plusieurs cellules. Le ViewPort couvre
		 * plusieurs de ces cellules.
		 *
		 * Résultat attendu : L'entité ne doit apparaître qu'une seule fois dans la
		 * liste visible.
		 */
		Entity entity = new TestEntity(model, "big");
		model.add(entity);

		entity.setSize(game.getGrid().new Dimension(2, 2));
		entity.setPosition(game.getGrid().new Position(1, 1));


		ViewPort vp = new ViewPort(model, game.getIsu().new Coord(0.0, 0.0), game.getIsu().new Dimension(18.5, 18.5));

		model.setViewPort(vp);

		int count = 0;
		for (Entity e : model.visibleEntities()) {
			if (e == entity) {
				count++;
			}
		}

		assertEquals(1, count);
	}

	@Test
	void collisionsAlongMotionReturnsEntityInCrossedCell() {
		/*
		 * Situation : moving part de la cellule (0,0). blocker est dans la cellule
		 * (1,0). Le mouvement va de la cellule (0,0) vers la cellule (2,0).
		 *
		 * Résultat attendu : Même si la destination est plus loin, le Model détecte
		 * blocker parce que sa cellule est traversée.
		 */
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		moving.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(1, 0));


		ISU.Coord from = moving.center().mkCopy();

		ISU.Coord to = game.getIsu().new Coord((2 + 0.5) * game.getCmPerCell(), (0 + 0.5) * game.getCmPerCell());

		assertTrue(model.collisionsAlongMotion(moving, from, from.mkVectorToward(to)).contains(blocker));
	}

	@Test
	void moveDetectsCollisionOnPathNotOnlyAtDestination() {
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		moving.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(1, 0));


		moving.setLinearSpeed(game.getIsu().new Vector(2 * game.getCmPerCell(), 0.0));

		boolean moved = model.move(moving, 1000L);

		assertFalse(moved);

		assertEquals(0, moving.position().x());
		assertEquals(0, moving.position().y());

		assertEquals(0.0, moving.linearSpeed().x(), EPS);
		assertEquals(0.0, moving.linearSpeed().y(), EPS);
	}

	@Test
	void motionHitboxDetectsPotentialCollisionAlongPath() {
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		moving.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(2, 0));


		ISU.Coord from = moving.center().mkCopy();

		ISU.Coord to = game.getIsu().new Coord((2 + 0.5) * game.getCmPerCell(), (0 + 0.5) * game.getCmPerCell());

		assertTrue(model.potentialCollisionsByMotionHitbox(moving, from, to).contains(blocker));
	}

	@Test
	void motionHitboxDetectsPotentialCollisionButDoesNotBlockAlone() {
		/*
		 * Situation : moving part de (0,0) et avance vers (3,0). blocker est sur le
		 * trajet.
		 *
		 * La motion hitbox doit détecter blocker comme candidat potentiel. Mais ce test
		 * ne décide pas à lui seul si le mouvement est bloqué.
		 */
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		moving.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(2, 0));


		ISU.Coord from = moving.center().mkCopy();

		ISU.Coord to = game.getIsu().new Coord((3 + 0.5) * game.getCmPerCell(), (0 + 0.5) * game.getCmPerCell());

		assertTrue(model.potentialCollisionsByMotionHitbox(moving, from, to).contains(blocker));
	}

	@Test
	void preciseMotionCollisionDetectsRealCollisionBetweenTicks() {
		/*
		 * Situation : moving part de (0,0). blocker est en (1,0). moving va directement
		 * vers (2,0) en un tick.
		 *
		 * Résultat attendu : Même si on ne regarde pas seulement la destination finale,
		 * l'interpolation détecte que les formes se touchent pendant le mouvement.
		 */
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity blocker = new TestEntity(model, "blocker");
		model.add(blocker);

		moving.setPosition(game.getGrid().new Position(0, 0));
		blocker.setPosition(game.getGrid().new Position(1, 0));


		moving.setLinearSpeed(game.getIsu().new Vector(2 * game.getCmPerCell(), 0.0));

		boolean moved = model.move(moving, 1000L);

		assertFalse(moved);

		assertEquals(0, moving.position().x());
		assertEquals(0, moving.position().y());

		assertEquals(0.0, moving.linearSpeed().x(), EPS);
		assertEquals(0.0, moving.linearSpeed().y(), EPS);
	}

	@Test
	void motionHitboxCandidateDoesNotAutomaticallyBlockMovement() {
		/*
		 * Situation : La motion hitbox peut détecter un candidat potentiel. Mais s'il
		 * n'y a pas de vraie intersection des shapes interpolées, le mouvement ne doit
		 * pas être bloqué.
		 */
		Entity moving = new TestEntity(model, "moving");
		model.add(moving);
		Entity candidate = new TestEntity(model, "candidate");
		model.add(candidate);

		moving.setPosition(game.getGrid().new Position(0, 0));
		candidate.setPosition(game.getGrid().new Position(2, 2));


		moving.setLinearSpeed(game.getIsu().new Vector(2 * game.getCmPerCell(), 0.0));

		boolean moved = model.move(moving, 1000L);

		assertTrue(moved);
	}

	@Test
	void moveInterpolatesOrientationDuringMotion() {
		Entity entity = new TestEntity(model, "rotating");
		model.add(entity);

		entity.setAngularSpeed(90.0);

		model.move(entity, 1000L);

		assertEquals(90, entity.orientation());
	}

	static class RectEntity extends Entity {

		RectEntity(Model model, String name) {
			super(model, name);

			setSize(model.isu().new Dimension(2 * model.isu().cmPerCell(), model.isu().cmPerCell()));

			updateBounding();
		}

		@Override
		protected Bounding createBounding(
			ISU.Coord center,
			ISU.Dimension size,
			int orientationDegree
		) {
			Bounding result = new Bounding();
			result.add(new Rect(center, size, orientationDegree));
			return result;
		}
	}

	@Test
	void motionHitboxTakesRotationIntoAccount() {
		/*
		 * Situation : On crée une entité rectangulaire longue. Elle ne se déplace pas,
		 * mais elle tourne de 90 degrés.
		 *
		 * Résultat attendu : La motion hitbox avec rotation doit couvrir la hitbox
		 * initiale et couvrir aussi la position tournée.
		 */
		Entity entity = new RectEntity(model, "rotating");
		model.add(entity);

		entity.setPosition(game.getGrid().new Position(4, 4));


		ISU.Coord from = entity.center().mkCopy();
		ISU.Coord to = entity.center().mkCopy();

		Hitbox initial = entity.bounding().hitbox();

		Hitbox motion = entity.motionHitboxBetween(from, from.mkVectorToward(to), 0, 90, 8);

		assertTrue(motion.xmin() <= initial.xmin());
		assertTrue(motion.xmax() >= initial.xmax());
		assertTrue(motion.ymin() <= initial.ymin());
		assertTrue(motion.ymax() >= initial.ymax());

		/*
		 * Comme le rectangle passe de horizontal à vertical, la hauteur de la hitbox du
		 * mouvement doit devenir plus grande que la hauteur initiale.
		 */
		assertTrue((motion.ymax() - motion.ymin()) > (initial.ymax() - initial.ymin()));
	}
}
