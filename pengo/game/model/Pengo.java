package game.model;

import java.util.ArrayList;
import java.util.List;

import engine.model.BasicStunt;
import engine.model.Bounding;
import engine.model.Circle;
import engine.model.Entity;
import engine.model.ISU;
import engine.model.Model;

public final class Pengo extends Player {

	private static final int nlife = 3;

	private static final long SHIELD_DURATION_MS = 10000L;
	private static final long SPEED_DURATION_MS = 8000L;
	private static final long FREEZE_DURATION_MS = 5000L;
	private static final long STAR_DURATION_MS = 5000L;


	private long shieldEnd_ms;
	private long speedEnd_ms;
	private long freezeEnd_ms;
	private long starEnd_ms;
	
	private ISU.Dimension normalStep;
	private ISU.Dimension boostedStep;
	
	private boolean friendActivator;

	public Pengo(Model model) {
		this(model, "Pengo");
	}

	public Pengo(Model model, String name) {
		super(model, name, nlife);

		this.shieldEnd_ms = 0L;
		this.speedEnd_ms = 0L;
		this.freezeEnd_ms = 0L;
		this.starEnd_ms = 0L;
		
		this.friendActivator = false;

		double diameter = model.isu().cmPerCell() * 0.80;
		setSize(model.isu().new Dimension(diameter, diameter));
		this.normalStep = model.isu().new Dimension(model.isu().cmPerCell(), model.isu().cmPerCell());
		this.boostedStep = model.isu().new Dimension(model.isu().cmPerCell() * 2.0, model.isu().cmPerCell() * 2.0);

		setStep(this.normalStep);
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
 
	//shield
	public void activateShield() {
		this.shieldEnd_ms = System.currentTimeMillis() + SHIELD_DURATION_MS;
		System.out.println("protection activated");
	}

	public void deactivateShield() {
		this.shieldEnd_ms = 0L;
		System.out.println("protection desactivated");
	}

	public boolean hasShield() {
		return System.currentTimeMillis() < this.shieldEnd_ms;
	}

	//speed
	public void activateSpeed() {
		this.speedEnd_ms = System.currentTimeMillis() + SPEED_DURATION_MS;
		setStep(this.boostedStep);
		refreshCurrentMovementSpeed();
		System.out.println("Speed boost ON");
	}

	public void deactivateSpeed() {
		this.speedEnd_ms = 0L;
		setStep(this.normalStep);
		refreshCurrentMovementSpeed();
		System.out.println("Speed boost OFF");
	}

	public boolean hasSpeedBoost() {
		return System.currentTimeMillis() < this.speedEnd_ms;
	}
	
	private void refreshCurrentMovementSpeed() {
		if (!isMoving()) {
			return;
		}

		double walkSpeed = 5.0;

		switch (orientation()) {
		case 0:
			setLinearSpeed(model().isu().new Vector(step().x() * walkSpeed, 0.0));
			break;

		case 90:
			setLinearSpeed(model().isu().new Vector(0.0, step().y() * walkSpeed));
			break;

		case 180:
			setLinearSpeed(model().isu().new Vector(-step().x() * walkSpeed, 0.0));
			break;

		case 270:
			setLinearSpeed(model().isu().new Vector(0.0, -step().y() * walkSpeed));
			break;

		default:
			break;
		}
	}

	public void activateFreezePower() {
		this.freezeEnd_ms = System.currentTimeMillis() + FREEZE_DURATION_MS;
		freezeEnemies();
	}

	public void deactivateFreezePower() {
		this.freezeEnd_ms = 0L;
		unfreezeEnemies();
	}

	public boolean hasFreezePower() {
		return System.currentTimeMillis() < this.freezeEnd_ms;
	}

	public void freezeEnemies() {
		for (Entity entity : model().entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;

				enemy.freeze();

				if (enemy.stunt() instanceof BasicStunt) {
					BasicStunt stunt = (BasicStunt) enemy.stunt();
					stunt.cancelCurrentAction();
				} else {
					enemy.setLinearSpeed(model().isu().new Vector(0.0, 0.0));
					enemy.setAngularSpeed(0.0);
				}
			}
		}
	}

	private void unfreezeEnemies() {
		for (Entity entity : model().entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				Enemies enemy = (Enemies) entity;
				enemy.unfreeze();
			}
		}
	}
	public void activateStar() {
		this.starEnd_ms = System.currentTimeMillis() + STAR_DURATION_MS;
	}

	public void deactivateStar() {
		this.starEnd_ms = 0L;
	}

	public boolean hasStar() {
		return System.currentTimeMillis() < this.starEnd_ms;
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
	

	public void explode() {
		forceLoseLife();
	}

	// UPDATE TIMERS

	public void updateTimedPowers() {
		long now = System.currentTimeMillis();

		if (this.freezeEnd_ms > 0L) {
			if (now < this.freezeEnd_ms) {
				freezeEnemies();
			} else {
				deactivateFreezePower();
			}
		}

		if (this.shieldEnd_ms > 0L && now >= this.shieldEnd_ms) {
			deactivateShield();
		}

		if (this.speedEnd_ms > 0L && now >= this.speedEnd_ms) {
			deactivateSpeed();
		}

		if (this.starEnd_ms > 0L && now >= this.starEnd_ms) {
			deactivateStar();
		}
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