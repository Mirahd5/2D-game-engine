package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;

public final class Pengo extends Player {

	private static final int nlife = 3;

	private boolean shield;
	private boolean speedBoost;
	private boolean freezePower;
	private boolean star;
	private boolean friendActivator;

	public Pengo(Model model) {
		this(model, "Pengo");
	}

	public Pengo(Model model, String name) {
		super(model, name, nlife);

		this.shield = false;
		this.speedBoost = false;
		this.freezePower = false;
		this.star = false;
		this.friendActivator = false;

		double diameter = model.isu().cmPerCell() * 0.80;
		setSize(model.isu().new Dimension(diameter, diameter));
		setStep(model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell()));

		setZOrder(3);
		setSOrder(0);

		updateBounding();
	}

	@Override
	protected Bounding createBounding(ISU.Coord center, ISU.Dimension size, int orientationDegree) {
		Bounding result = new Bounding();
		result.add(new Circle(center, Math.min(size.x(), size.y()) / 2.0));
		return result;
	}

	public void activateShield() {
		this.shield = true;
	}

	public void deactivateShield() {
		this.shield = false;
	}

	public boolean hasShield() {
		return this.shield;
	}

	public void activateSpeed() {
		this.speedBoost = true;
	}

	public void deactivateSpeed() {
		this.speedBoost = false;
	}

	public boolean hasSpeedBoost() {
		return this.speedBoost;
	}

	public void activateFreezePower() {
		this.freezePower = true;
	}

	public void deactivateFreezePower() {
		this.freezePower = false;
	}

	public boolean hasFreezePower() {
		return this.freezePower;
	}

	public void activateStar() {
		this.star = true;
	}

	public void deactivateStar() {
		this.star = false;
	}

	public boolean hasStar() {
		return this.star;
	}

	public void activateFriendActivator() {
		this.friendActivator = true;
	}

	public void deactivateFriendActivator() {
		this.friendActivator = false;
	}

	public boolean hasFriendActivator() {
		return this.friendActivator;
	}

	@Override
	protected boolean hasEnemyProtection() {
		return hasShield();
	}

	@Override
	public void collision(Entity other) {
		if (other instanceof Power) {
			Power power = (Power) other;
			power.apply(this);
			power.death();
			return;
		}

		super.collision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		List<Entity> remainingContacts = new ArrayList<Entity>();

		for (Entity entity : entities) {
			if (entity instanceof Power) {
				Power power = (Power) entity;
				power.apply(this);
				power.death();
			} else {
				remainingContacts.add(entity);
			}
		}

		if (!remainingContacts.isEmpty()) {
			super.collision(remainingContacts);
		}
	}

}