package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;

public class PacMan extends Entity {

	private int health;
	private int maxHealth;
	private int score;

	// CONSTRUCTOR

	public PacMan(Model model, String name) {
		super(model, name);

		this.maxHealth = 3;
		this.health = this.maxHealth;
		this.score = 0;

		/*
		 * PacMan occupe une cellule, mais sa taille physique est d'une demi-cellule.
		 */
		double pacManSize = model.game().getCmPerCell() / 2.0;

		setSize(model.game().getIsu().new Dimension(pacManSize, pacManSize));

		setZOrder(2);
		setSOrder(0);

		updateBounding();
	}

	// === Task COLLISION ===

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();

		double radius = Math.min(size.x(), size.y()) / 2.0;

		result.add(new Circle(center, radius));

		return result;
	}

	public int health() {
		return this.health;
	}

	public int maxHealth() {
		return this.maxHealth;
	}

	public boolean isDead() {
		return this.health <= 0;
	}

	public void takeDamage(int amount) {
		assert amount >= 0;

		this.health -= amount;

		if (this.health <= 0) {
			this.health = 0;
			death();
		}
	}

	@Override
	public void collision(Entity e) {
		assert e != null;

		/*
		 * Gum = contact non bloquant. On ne doit PAS appeler super.collision(e), sinon
		 * le Stunt croit qu'il y a une vraie collision et PacMan s'arrête brièvement.
		 */
		if (e instanceof Gum) {
			Gum gum = (Gum) e;

			addScore(gum.points());
			gum.death();

			return;
		}

		/*
		 * Damager / Ghost / Boss / Obstacle = vraie collision. Là on prévient le Stunt.
		 */
		super.collision(e);

		if (e instanceof Damager) {
			takeDamage(((Damager) e).damage());
		}
	}

	@Override
	public void collision(java.util.List<Entity> entities) {
		assert entities != null;

		java.util.List<Entity> blockingOrDangerous = new java.util.ArrayList<Entity>();

		for (Entity e : entities) {
			if (e instanceof Gum) {
				Gum gum = (Gum) e;

				addScore(gum.points());
				gum.death();
			} else {
				blockingOrDangerous.add(e);
			}

			if (e instanceof Damager) {
				takeDamage(((Damager) e).damage());
			}
		}

		/*
		 * On prévient le Stunt seulement s'il y a autre chose qu'une gum.
		 */
		if (!blockingOrDangerous.isEmpty()) {
			super.collision(blockingOrDangerous);
		}
	}

	public int score() {
		return this.score;
	}

	public void addScore(int points) {
		assert points >= 0;
		this.score += points;
	}
}