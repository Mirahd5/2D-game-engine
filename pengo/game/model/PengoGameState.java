package game.model;

import engine.model.Entity;
import engine.model.Model;

public class PengoGameState {

	private final Model model;
	private final Pengo pengo;

	private final int targetTotalEnemies;

	private int spawnedEnemies;

	private static final int ENEMY_KILL_SCORE = 100;

	private boolean won;
	private boolean lost;

	public PengoGameState(Model model, Pengo pengo, int initialEnemies, int spawnerCount, int maxTotalEnemies) {
		assert model != null;
		assert pengo != null;
		assert initialEnemies >= 0;
		assert spawnerCount >= 0;
		assert maxTotalEnemies >= 0;

		this.model = model;
		this.pengo = pengo;

		this.spawnedEnemies = initialEnemies;

		if (spawnerCount > 0) {
			this.targetTotalEnemies = Math.max(initialEnemies, maxTotalEnemies);
		} else {
			this.targetTotalEnemies = initialEnemies;
		}

		this.won = false;
		this.lost = false;

		update();
	}

	public Pengo pengo() {
		return this.pengo;
	}

	public PowerType activePowerType() {
		return this.pengo.activePowerTypeForHud();
	}

	public long activePowerRemainingMs() {
		return this.pengo.activePowerRemainingMsForHud();
	}

	public void enemySpawned() {
		this.spawnedEnemies++;
	}

	public void update() {
		if (this.won || this.lost) {
			return;
		}

		if (!this.pengo.alive() || this.pengo.isGameOver()) {
			this.lost = true;
			return;
		}

		if (this.targetTotalEnemies > 0 && this.spawnedEnemies >= this.targetTotalEnemies && livingEnemies() == 0) {
			this.won = true;
		}
	}

	public boolean finished() {
		return this.won || this.lost;
	}

	public boolean playing() {
		return !this.won && !this.lost;
	}

	public boolean won() {
		return this.won;
	}

	public boolean lost() {
		return this.lost;
	}

	public int lives() {
		return this.pengo.nLives();
	}

	public int score() {
		return killedEnemies() * ENEMY_KILL_SCORE;
	}

	public int killedEnemies() {
		int killed = this.spawnedEnemies - livingEnemies();

		if (killed < 0) {
			return 0;
		}

		return killed;
	}

	public int spawnedEnemies() {
		return this.spawnedEnemies;
	}

	public int targetTotalEnemies() {
		return this.targetTotalEnemies;
	}

	public int livingEnemies() {
		int count = 0;

		for (Entity entity : this.model.entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	public int livingSpawners() {
		int count = 0;

		for (Entity entity : this.model.entities()) {
			if (entity instanceof Spawner && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	public String status() {
		if (this.won) {
			return "YOU WIN";
		}

		if (this.lost) {
			return "GAME OVER";
		}

		return "PLAYING";
	}

	public void stopAllEntities() {
		for (Entity entity : this.model.entities()) {
			if (entity.alive()) {
				entity.setLinearSpeed(this.model.isu().new Vector(0.0, 0.0));
				entity.setAngularSpeed(0.0);
			}
		}
	}

	public boolean powerActive(PowerType type) {
		return this.pengo.powerActiveForHud(type);
	}

	public long powerRemainingMs(PowerType type) {
		return this.pengo.powerRemainingMsForHud(type);
	}
	
	public boolean respawnInvincible() {
		return this.pengo.hasRespawnInvincibility();
	}

	public long respawnInvincibleRemainingMs() {
		return this.pengo.respawnInvincibilityRemainingMs();
	}
}