package game.model;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;
import engine.model.Rect;
import java.util.List;

/**
 * Ennemi mobile du jeu Pengo.
 */
public final class Enemies extends Entity {
	private boolean frozen;
	private String galState;

	public Enemies(Model model, String name) {
		super(model, name);

		this.frozen = false;
		this.galState = "normal";

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

	public void kill() {
		death();
	}

	@Override
	public boolean blocks(Entity other) {
		return false;
	}

	@Override
	public void collision(Entity other) {
		if (other instanceof Player && other.alive()) {
			Player player = (Player) other;
			player.hitByEnemy();
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		Player touchedPlayer = null;

		for (Entity entity : entities) {
			if (entity instanceof Player && entity.alive()) {
				touchedPlayer = (Player) entity;
				break;
			}
		}

		if (touchedPlayer != null) {
			touchedPlayer.hitByEnemy();
		}

		super.collision(entities);
	}
}