package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;
import java.util.List;

import oop.tasks.Runnable;
import oop.tasks.Runtime;

/**
 * Ennemi mobile du jeu Pengo.
 */
public final class Enemies extends Entity {
	public static final long CRUSH_DISPLAY_MS = 550L;

	private boolean frozen;
	private String galState;
	private boolean crushed;
	/*
	 * Nom historique conserve pour ne pas casser l'avatar : true => image
	 * ecrasee_horizontal.png, false => image ecrasee_vertical.png.
	 */
	private boolean crushedHorizontal;
	private double crushedDisplayWidth_cm;
	private double crushedDisplayHeight_cm;
	private Entity crushPusher;
	private Entity crushObstacle;
	private int crushDirectionDegree;

	public Enemies(Model model, String name) {
		super(model, name);

		this.frozen = false;
		this.galState = "normal";
		this.crushed = false;
		this.crushedHorizontal = true;
		this.crushedDisplayWidth_cm = model.isu().cmPerCell();
		this.crushedDisplayHeight_cm = model.isu().cmPerCell();
		this.crushPusher = null;
		this.crushObstacle = null;
		this.crushDirectionDegree = 0;

		double size = model.isu().cmPerCell() * 0.78;
		setSize(model.isu().new Dimension(size, size));
		setStep(model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell()));

		setZOrder(3);
		setSOrder(2);

		updateBounding();
	}

	public String galState() {
		return this.galState;
	}

	public void setGalState(String galState) {
		if (galState == null || galState.trim().length() == 0) {
			this.galState = "normal";
			return;
		}

		this.galState = galState.trim();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();

		double headRadius = Math.min(size.x(), size.y()) * 0.30;
		ISU.Vector headOffset = center.isu().new Vector(0.0, -size.y() * 0.18);
		headOffset.turn(orientationDegree);
		result.add(new Circle(center.mkTranslated(headOffset), headRadius));

		ISU.Dimension bodySize = center.isu().new Dimension(size.x() * 0.80, size.y() * 0.55);
		ISU.Vector bodyOffset = center.isu().new Vector(0.0, size.y() * 0.18);
		bodyOffset.turn(orientationDegree);
		result.add(new Rect(center.mkTranslated(bodyOffset), bodySize, orientationDegree));

		return result;
	}

	public boolean isFrozen() {
		return this.frozen;
	}

	public void freeze() {
		/*
		 * On laisse finir le pas en cours. Le Bot n'en démarrera pas de nouveau tant
		 * que frozen reste vrai. Cela conserve la cohérence interne de BasicStunt.
		 */
		this.frozen = true;
	}

	public void unfreeze() {
		this.frozen = false;
	}

	public boolean isAlive() {
		return alive();
	}

	/**
	 * Un ennemi mort reste vivant quelques instants pour que son avatar puisse
	 * afficher l'image ecrasee_horizontal.png ou ecrasee_vertical.png.
	 */
	public void kill() {
		crush(orientation());
	}

	public void crush(int directionDegree) {
		crushAt(directionDegree, center());
	}

	/**
	 * Lance un ecrasement dynamique entre un bloc pousseur et un obstacle final.
	 * L'avatar calcule ensuite l'image ecrasee avec la distance bord-a-bord en ISU.
	 */
	public void crushBetween(Entity pusher, Entity obstacle, int directionDegree) {
		crushBetweenAt(pusher, obstacle, directionDegree, center());
	}

	public void crushBetweenAt(Entity pusher, Entity obstacle, int directionDegree, ISU.Coord fallbackCenter) {
		this.crushPusher = pusher;
		this.crushObstacle = obstacle;
		this.crushDirectionDegree = normalizeCardinal(directionDegree);
		crushAt(directionDegree, fallbackCenter);
	}

	/**
	 * Place l'ennemi a l'endroit exact ou il doit etre affiche ecrase, puis
	 * lance la mort differee.
	 */
	public void crushAt(int directionDegree, ISU.Coord crushCenter) {
		if (!alive() || this.crushed) {
			return;
		}

		int direction = normalizeCardinal(directionDegree);
		this.crushDirectionDegree = direction;

		if (crushCenter != null && installed()) {
			model().place(this, crushCenter);
		}

		/*
		 * true signifie : l'ecrasement arrive sur l'axe horizontal
		 * (pousseur a gauche/droite du recepteur). L'avatar utilise alors
		 * ecrasee_horizontal.png et le dessine dans l'espace ISU exact
		 * entre le pousseur et le recepteur.
		 */
		this.crushedHorizontal = direction == 0 || direction == 180;
		this.crushedDisplayWidth_cm = model().isu().cmPerCell() * 1.08;
		this.crushedDisplayHeight_cm = model().isu().cmPerCell() * 1.08;
		this.crushed = true;
		this.frozen = true;

		removeStunt();
		setOrientation(direction);
		setLinearSpeed(model().isu().new Vector(0.0, 0.0));
		setAngularSpeed(0.0);
		setSOrder(10);

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (isCrushed() && alive()) {
					finishCrushDeath();
				}
			}
		}, (int) CRUSH_DISPLAY_MS);
	}

	public boolean isCrushed() {
		return this.crushed;
	}

	public boolean crushedHorizontal() {
		return this.crushedHorizontal;
	}

	public double crushedDisplayWidthCm() {
		return this.crushedDisplayWidth_cm;
	}

	public double crushedDisplayHeightCm() {
		return this.crushedDisplayHeight_cm;
	}

	public Entity crushPusher() {
		return this.crushPusher;
	}

	public Entity crushObstacle() {
		return this.crushObstacle;
	}

	public int crushDirection() {
		return this.crushDirectionDegree;
	}

	public void finishCrushDeath() {
		if (alive()) {
			death();
		}
	}

	private static int normalizeCardinal(int degree) {
		int d = ((degree % 360) + 360) % 360;

		if (d < 45 || d >= 315) {
			return 0;
		}

		if (d < 135) {
			return 90;
		}

		if (d < 225) {
			return 180;
		}

		return 270;
	}

	@Override
	public boolean blocks(Entity other) {
		return false;
	}

	@Override
	public void collision(Entity other) {
		if (this.crushed) {
			return;
		}

		if (other instanceof Player && other.alive()) {
			Player player = (Player) other;
			if (player instanceof Pengo) {
				Pengo pengo = (Pengo) player;

				if (pengo.hasStar()) {
					kill();
					return;
				}
			}
			player.hitByEnemy();
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		if (this.crushed) {
			return;
		}

		Player touchedPlayer = null;

		for (Entity entity : entities) {
			if (entity instanceof Player && entity.alive()) {
				touchedPlayer = (Player) entity;
				break;
			}
		}

		if (touchedPlayer != null) {
			if (touchedPlayer instanceof Pengo) {
				Pengo pengo = (Pengo) touchedPlayer;

				if (pengo.hasStar()) {
					kill();
					return;
				}
			}

			touchedPlayer.hitByEnemy();
		}

		super.collision(entities);
	}

	@Override
	public String debugLabel() {
		if (this.frozen) {
			return "Enemy FROZEN " + this.galState;
		}

		if (isMoving()) {
			return "Enemy " + this.galState + " WALK";
		}

		return "Enemy " + this.galState;
	}
}