package game.model;

import java.util.List;

import engine.model.Entity;

final class SlideCollisionHandler {

	// bloc glissant qui possede ce gestionnaire
	private final Glissade owner;

	// train dennemis transporte par le bloc
	private final EnemyTrain enemyTrain;

	// gestionnaire de larret special devant power
	private final PowerStopController powerStop;

	SlideCollisionHandler(Glissade owner, EnemyTrain enemyTrain, PowerStopController powerStop) {
		this.owner = owner;
		this.enemyTrain = enemyTrain;
		this.powerStop = powerStop;
	}

	// gere une collision avec une seule entite
	boolean handleCollision(Entity other) {
		// si un arret devant power est en cours on le met a jour
		if (this.powerStop.active()) {
			this.powerStop.update();

			// garde la vitesse de glissade tant que l arret special continue
			if (this.owner.isSliding() && this.powerStop.active()) {
				this.owner.restoreSlideSpeed();
			}

			return true;
		}

		// essaie de capturer un ennemi touche par le bloc
		boolean capturedEnemy = this.enemyTrain.handleSlidingContact(other);

		if (capturedEnemy) {
			this.owner.restoreSlideSpeed();
			this.enemyTrain.carryEnemies();
			return true;
		}

		// demarre larret special si un power est juste apres le train
		if (this.owner.isSliding() && other instanceof Power && !this.enemyTrain.isEmpty()
				&& this.powerStop.isPowerAfterTrain(other)) {
			Enemies lastEnemy = this.enemyTrain.lastEnemy();

			this.powerStop.start(lastEnemy, this.powerStop.blockStopBeforePower(other.position()),
					other.position().copy());

			this.owner.restoreSlideSpeed();
			return true;
		}

		// arrete le bloc contre un obstacle statique devant lui
		if (this.owner.isSliding() && this.owner.isStaticObstacle(other) && this.owner.isInFrontOfSlidingBlock(other)) {
			if (this.owner.isCrushingObstacle(other)) {
				this.enemyTrain.crushCarriedEnemies(other);
			}

			this.owner.stopSliding();
			return true;
		}

		// arrete le bloc contre une entite qui le bloque
		if (this.owner.isHardBlockForThisBlock(other)) {
			if (this.owner.isCrushingObstacle(other)) {
				this.enemyTrain.crushCarriedEnemies(other);
			}

			this.owner.stopSliding();
			return true;
		}

		// continue la gestion du train si rien n a arrete la glissade
		this.enemyTrain.carryEnemies();

		return false;
	}

	// gere une collision avec plusieurs entites en meme temps
	boolean handleCollision(List<Entity> entities) {
		// si un arret devant power est en cours on le met a jour
		if (this.powerStop.active()) {
			this.powerStop.update();
			// garde la vitesse de glissade tant que larret special continue
			if (this.owner.isSliding() && this.powerStop.active()) {
				this.owner.restoreSlideSpeed();
			}
			return true;
		}

		boolean capturedEnemy = false;
		boolean mustStop = false;
		Entity crushingObstacle = null;

		for (Entity other : entities) {
			if (other == this.owner || !other.alive()) {
				continue;
			}
			// capture les ennemis touches pendant la glissade
			if (this.enemyTrain.handleSlidingContact(other)) {
				capturedEnemy = true;
				continue;
			}
			// detecte un power place juste apres le train
			if (this.owner.isSliding() && other instanceof Power && !this.enemyTrain.isEmpty()
					&& this.powerStop.isPowerAfterTrain(other)) {
				Enemies lastEnemy = this.enemyTrain.lastEnemy();

				this.powerStop.start(lastEnemy, this.powerStop.blockStopBeforePower(other.position()),
						other.position().copy());

				capturedEnemy = true;
				continue;
			}
			// detecte un obstacle statique devant le bloc
			if (this.owner.isSliding() && this.owner.isStaticObstacle(other)
					&& this.owner.isInFrontOfSlidingBlock(other)) {
				mustStop = true;

				if (this.owner.isCrushingObstacle(other)) {
					crushingObstacle = other;
				}
				continue;
			}
			// detecte une entite qui bloque vraiment le bloc
			if (this.owner.isHardBlockForThisBlock(other)) {
				mustStop = true;
				if (this.owner.isCrushingObstacle(other)) {
					crushingObstacle = other;
				}
			}
		}
		// ecrase les ennemis si un obstacle ecrasant a ete trouve
		if (crushingObstacle != null) {
			this.enemyTrain.crushCarriedEnemies(crushingObstacle);
		}
		// stoppe la glissade si une collision l impose
		if (mustStop) {
			this.owner.stopSliding();
			return true;
		}
		// continue apres avoir capture au moins un ennemi
		if (capturedEnemy) {
			this.owner.restoreSlideSpeed();
			this.enemyTrain.carryEnemies();
			return true;
		}
		// continue la gestion du train si rien n a arrete la glissade
		this.enemyTrain.carryEnemies();

		return false;
	}

	// indique si le bloc bloque une entite donnee
	boolean blocks(Entity other) {
		if (other instanceof Enemies) {
			Enemies enemy = (Enemies) other;
			// un ennemi sur la meme case ne bloque pas si le bloc ne glisse pas
			if (!this.owner.isSliding()) {
				return !this.owner.sameGridCell(other);
			}
			return this.enemyTrain.blocksEnemyWhileSliding(enemy);
		}
		// le power cible ne bloque pas pendant l arret special
		if (this.owner.isSliding() && this.powerStop.active() && this.powerStop.isSmoothPowerTarget(other)) {
			return false;
		}

		return this.owner.isHardBlockForThisBlock(other);
	}
}