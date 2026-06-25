package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.Grid;
import engine.model.ISU;

final class EnemyTrain {

	// bloc glissant qui transporte les ennemis
	private final Glissade owner;

	// liste des ennemis actuellement pousses par le bloc
	private final List<Enemies> carriedEnemies;

	EnemyTrain(Glissade owner) {
		this.owner = owner;
		this.carriedEnemies = new ArrayList<Enemies>();
	}

	boolean isEmpty() {
		return this.carriedEnemies.isEmpty();
	}

	int size() {
		return this.carriedEnemies.size();
	}

	boolean contains(Enemies enemy) {
		return this.carriedEnemies.contains(enemy);
	}

	// retourne le dernier ennemi du train
	Enemies lastEnemy() {
		if (this.carriedEnemies.isEmpty()) {
			return null;
		}

		return this.carriedEnemies.get(this.carriedEnemies.size() - 1);
	}

	void clear() {
		this.carriedEnemies.clear();
	}

	// retire les ennemis qui ne peuvent plus etre transportes
	void removeInvalidCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (Enemies enemy : copy) {
			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
			}
		}
	}

	// gere le contact entre le bloc glissant et un ennemi
	boolean handleSlidingContact(Entity other) {
		if (!this.owner.isSliding() || this.owner.powerStop().active() || !(other instanceof Enemies)
				|| !other.alive()) {
			return false;
		}

		Enemies enemy = (Enemies) other;

		if (enemy.isCrushed()) {
			return false;
		}

		// lennemi doit etre a la bonne position dans le train
		if (!this.carriedEnemies.contains(enemy)) {
			int expectedIndex = this.carriedEnemies.size() + 1;

			if (!isCarryCandidateAtIndex(enemy, expectedIndex)) {
				return false;
			}
		}

		prepareCarriedEnemy(enemy);

		return true;
	}

	// met a jour les ennemis transportes pendant la glissade
	void carryEnemies() {
		catchEnemiesInFrontOfTrain();

		removeInvalidCarriedEnemies();

		if (this.carriedEnemies.isEmpty()) {
			this.owner.powerStop().stopBeforePowerIfNeeded();
			return;
		}

		// cellule situee juste apres le dernier ennemi transporte
		Grid.Position afterTrain = this.owner.positionInFront(this.carriedEnemies.size() + 1);

		if (afterTrain == null) {
			crushCarriedEnemiesWithoutObstacle();
			return;
		}

		// verifie si un obstacle se trouve apres le train
		Entity obstacleAfterTrain = enemyStoppingObstacleAt(afterTrain);

		if (obstacleAfterTrain != null) {
			if (this.owner.isCrushingObstacle(obstacleAfterTrain)) {
				crushCarriedEnemies(obstacleAfterTrain);
			} else {
				Enemies lastEnemy = lastEnemy();
				this.owner.powerStop().start(lastEnemy, this.owner.powerStop().blockStopBeforePower(afterTrain),
						afterTrain.copy());
			}

			return;
		}

		placeCarriedEnemiesAsTrain();
	}

	// verifie les ennemis qui croisent la trajectoire du bloc
	void againstCrossingEnemies() {
		if (!this.owner.isSliding() || !this.owner.alive() || !this.owner.installed()) {
			return;
		}

		List<Entity> snapshot = this.owner.model().entities();

		for (Entity entity : snapshot) {
			if (!(entity instanceof Enemies)) {
				continue;
			}

			Enemies enemy = (Enemies) entity;

			if (!enemy.alive() || enemy.isCrushed() || this.carriedEnemies.contains(enemy)) {
				continue;
			}

			int expectedIndex = this.carriedEnemies.size() + 1;

			// capture lennemi sil est dans la bonne position
			if (!this.owner.powerStop().active() && isCarryCandidateAtIndex(enemy, expectedIndex)) {
				prepareCarriedEnemy(enemy);
				continue;
			}

			// stoppe la glissade si un ennemi bloque la zone du train
			if (isEnemyCrossingSlidingBody(enemy) || isEnemyInSolidTrainZone(enemy)) {
				this.owner.stopSliding();
				return;
			}
		}
	}

	// indique si un ennemi bloque le bloc pendant la glissade
	boolean blocksEnemyWhileSliding(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (this.carriedEnemies.contains(enemy)) {
			return false;
		}

		return isEnemyInSolidTrainZone(enemy);
	}

	// ajoute un ennemi au train et le fige
	void prepareCarriedEnemy(Enemies enemy) {
		if (!this.carriedEnemies.contains(enemy)) {
			this.carriedEnemies.add(enemy);
		}

		enemy.freeze();
		enemy.setLinearSpeed(this.owner.model().isu().new Vector(0.0, 0.0));
		enemy.setAngularSpeed(0.0);
	}

	// place chaque ennemi devant le bloc pour former un train
	void placeCarriedEnemiesAsTrain() {
		for (int i = 0; i < this.carriedEnemies.size(); i++) {
			Enemies enemy = this.carriedEnemies.get(i);

			if (enemy.alive() && enemy.installed()) {
				enemy.freeze();
				enemy.setLinearSpeed(this.owner.model().isu().new Vector(0.0, 0.0));
				enemy.setAngularSpeed(0.0);
				this.owner.model().place(enemy, carriedEnemyCenter(i + 1));
			}
		}
	}

	// relache un ennemi a une position sure
	void releaseCarriedEnemy(Enemies enemy, ISU.Coord safeCenter) {
		this.carriedEnemies.remove(enemy);

		if (!enemy.alive()) {
			return;
		}

		if (safeCenter != null && enemy.installed()) {
			this.owner.model().place(enemy, safeCenter);
		}

		enemy.releaseAfterBeingCarried();
	}

	// relache tous les ennemis transportes
	void releaseCarriedEnemies() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (Enemies enemy : copy) {
			if (enemy.alive()) {
				enemy.releaseAfterBeingCarried();
			}
		}
	}

	// relache les ennemis apres larret special devant un power
	void releaseCarriedEnemiesAfterSmoothPowerStop(Grid.Position blockStopPosition) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (!enemy.alive() || !enemy.installed()) {
				this.carriedEnemies.remove(enemy);
				continue;
			}

			Grid.Position finalPosition = this.owner.positionInFrontFrom(blockStopPosition, i + 1);
			ISU.Coord safeCenter = finalPosition == null ? enemy.center() : finalPosition.toISUCoordCentered();

			releaseCarriedEnemy(enemy, safeCenter);
		}
	}

	// ecrase les ennemis si le train sort de la grille
	void crushCarriedEnemiesWithoutObstacle() {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushAt(this.owner.slideDirection(), carriedEnemyCenter(i + 1));
			}
		}
	}

	// ecrase les ennemis contre un obstacle
	void crushCarriedEnemies(Entity obstacle) {
		List<Enemies> copy = new ArrayList<Enemies>(this.carriedEnemies);
		this.carriedEnemies.clear();

		for (int i = 0; i < copy.size(); i++) {
			Enemies enemy = copy.get(i);

			if (enemy.alive()) {
				enemy.crushBetweenAt(this.owner, obstacle, this.owner.slideDirection(), carriedEnemyCenter(i + 1));
			}
		}
	}

	// capture les ennemis directement alignes devant le train
	private void catchEnemiesInFrontOfTrain() {
		boolean added;

		do {
			added = false;

			Grid.Position nextEnemyPosition = this.owner.positionInFront(this.carriedEnemies.size() + 1);
			Enemies enemy = firstCarryableEnemyAt(nextEnemyPosition);

			if (enemy != null) {
				prepareCarriedEnemy(enemy);
				added = true;
			}
		} while (added);
	}

	// cherche un ennemi transportable sur une cellule donnee
	private Enemies firstCarryableEnemyAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		int expectedIndex = this.carriedEnemies.size() + 1;
		Grid.Cell cell = this.owner.model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other instanceof Enemies && other.alive()) {
				Enemies enemy = (Enemies) other;

				if (!enemy.isCrushed() && !this.carriedEnemies.contains(enemy)
						&& isCarryCandidateAtIndex(enemy, expectedIndex)) {
					return enemy;
				}
			}
		}

		return null;
	}

	// verifie si lennemi est bien place pour etre ajoute au train
	private boolean isCarryCandidateAtIndex(Enemies enemy, int expectedIndex) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed() || expectedIndex <= 0) {
			return false;
		}

		Grid.Position expectedPosition = this.owner.positionInFront(expectedIndex);

		if (expectedPosition == null) {
			return false;
		}

		ISU.Coord expectedCenter = expectedPosition.toISUCoordCentered();
		ISU.Vector towardExpected = this.owner.center().mkVectorToward(expectedCenter);
		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());

		double expectedForward = this.owner.geometry().forwardProjection(towardExpected, this.owner.slideDirection());
		double enemyForward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double enemyLateral = Math
				.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));

		double cell = this.owner.model().isu().cmPerCell();

		return Math.abs(enemyForward - expectedForward) <= cell * 0.78 && enemyLateral <= cell * 0.46;
	}

	// verifie si un ennemi traverse le corps du bloc
	private boolean isEnemyCrossingSlidingBody(Enemies enemy) {
		if (enemy == null || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		if (this.owner.sameGridCell(enemy) || enemy.bounding().intersects(this.owner.bounding())) {
			return true;
		}

		return isEnemyInBlockBody(enemy);
	}

	// verifie si un ennemi est dans la zone physique du bloc
	private boolean isEnemyInBlockBody(Enemies enemy) {
		if (enemy == null || !this.owner.isSliding()) {
			return false;
		}

		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());
		double forward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double lateral = Math.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));
		double cell = this.owner.model().isu().cmPerCell();

		return forward >= -cell * 0.72 && forward <= cell * 0.86 && lateral <= cell * 0.48;
	}

	// verifie si un ennemi se trouve dans la zone occupee par le train
	private boolean isEnemyInSolidTrainZone(Enemies enemy) {
		if (enemy == null || !this.owner.isSliding() || !enemy.alive() || enemy.isCrushed()) {
			return false;
		}

		ISU.Vector towardEnemy = this.owner.center().mkVectorToward(enemy.center());
		double forward = this.owner.geometry().forwardProjection(towardEnemy, this.owner.slideDirection());
		double lateral = Math.abs(this.owner.geometry().lateralProjection(towardEnemy, this.owner.slideDirection()));
		double cell = this.owner.model().isu().cmPerCell();

		double rearLimit = -cell * 0.72;
		double frontLimit = cell * (Math.max(0, this.carriedEnemies.size()) + 0.92);

		return forward >= rearLimit && forward <= frontLimit && lateral <= cell * 0.48;
	}

	// calcule le centre dun ennemi transporte selon sa position dans le train
	private ISU.Coord carriedEnemyCenter(int distanceInCells) {
		return this.owner.center().mkTranslated(this.owner.geometry().forwardOffset(this.owner.slideDirection(),
				this.owner.model().isu().cmPerCell() * distanceInCells));
	}

	// cherche un obstacle qui doit arreter le train
	private Entity enemyStoppingObstacleAt(Grid.Position position) {
		if (position == null) {
			return null;
		}

		Grid.Cell cell = this.owner.model().grid().cellAt(position);

		for (Entity other : cell.entities()) {
			if (other != this.owner && other.alive() && isEnemyStoppingObstacle(other)) {
				return other;
			}
		}

		return null;
	}

	// obstacles qui arretent les ennemis transportes
	private boolean isEnemyStoppingObstacle(Entity other) {
		return this.owner.isCrushingObstacle(other) || other instanceof Power;
	}
}