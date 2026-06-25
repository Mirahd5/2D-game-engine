package engine.model;

import java.util.List;

/**
 * Stunt de marche cardinale d'une cellule à la suivante.
 */
public class BasicStunt extends Stunt {

	private static final double EPSILON = 1e-6;
	private static final double TURN_SNAP_RATIO = 0.30;
	private static final double DEFAULT_WALK_SPEED = 5.0;

	private double walkSpeed;

	private boolean walking;
	private boolean pendingDone;

	private ISU.Coord startCenter;
	private ISU.Coord targetCenter;

	public BasicStunt(Model model, Entity entity) {
		this(model, entity, DEFAULT_WALK_SPEED);
	}

	public BasicStunt(Model model, Entity entity, double walkSpeed) {
		super(model, entity);

		assert walkSpeed > 0.0 : walkSpeed;

		this.walkSpeed = walkSpeed;
		resetAction();
	}

	public boolean walking() {
		return this.walking;
	}

	public double walkSpeed() {
		return this.walkSpeed;
	}

	public void setWalkSpeed(double walkSpeed) {
		if (walkSpeed <= 0.0) {
			return;
		}

		this.walkSpeed = walkSpeed;
	}

	public void cancelCurrentAction() {
		if (!valid()) {
			return;
		}

		stopEntity();
		resetAction();

		if (entity.installed()) {
			model.placeSilently(entity, entity.position().toISUCoordCentered());
		}
	}

	@Override
	public boolean walk(int degree) {
		if (!valid() || this.pendingDone) {
			return false;
		}

		int orientation = Orientation.normalize(degree);

		if (this.walking) {
			return changeDirectionWhileWalking(orientation);
		}

		return startWalkFromCurrentCell(orientation);
	}

	@Override
	public void set(double x, double y) {
		if (!valid() || !this.walking || this.pendingDone) {
			return;
		}

		ISU.Coord current = model.isu().new Coord(x, y);

		double walked = this.startCenter.distanceTo(current);
		double expected = this.startCenter.distanceTo(this.targetCenter);

		if (walked + EPSILON < expected) {
			return;
		}

		this.pendingDone = true;
		stopEntity();
	}

	@Override
	public void commit() {
		if (!valid() || !this.pendingDone) {
			return;
		}

		model.placeSilently(entity, this.targetCenter.mkCopy());

		resetAction();
		notifyDone();
	}

	@Override
	protected void onInvalidate() {
		resetAction();
	}

	@Override
	public void collision(Entity other) {
		if (model.isBlocking(entity, other)) {
			stopBecauseOfCollision();
		}

		notifyCollision(other);
	}

	@Override
	public void collision(List<Entity> entities) {
		boolean blocked = false;

		for (Entity other : entities) {
			if (other != entity && model.isBlocking(entity, other)) {
				blocked = true;
				break;
			}
		}

		if (blocked) {
			stopBecauseOfCollision();
		}

		notifyCollision(entities);
	}

	@Override
	public void set(int orientation) {
	}

	@Override
	public void set(Grid.Cell cell) {
	}

	private boolean startWalkFromCurrentCell(int orientation) {
		Grid.Position currentPosition = entity.position();
		Grid.Position targetPosition = Orientation.neighbor(model.grid(), currentPosition, orientation);

		if (!canWalkTo(targetPosition)) {
			return false;
		}

		entity.turn(orientation - entity.orientation());

		this.startCenter = currentPosition.toISUCoordCentered();
		this.targetCenter = targetPosition.toISUCoordCentered();

		model.placeSilently(entity, this.startCenter);

		this.walking = true;
		this.pendingDone = false;

		entity.setLinearSpeed(speedForOrientation(orientation));
		entity.setAngularSpeed(0.0);

		return true;
	}

	private boolean changeDirectionWhileWalking(int newOrientation) {
		int currentOrientation = entity.orientation();

		if (newOrientation == currentOrientation) {
			return true;
		}

		if (Orientation.opposite(currentOrientation, newOrientation)) {
			reverseImmediately(newOrientation);
			return true;
		}

		if (!isCloseToCellCenter()) {
			return false;
		}

		Grid.Position currentPosition = entity.position();
		Grid.Position targetPosition = Orientation.neighbor(model.grid(), currentPosition, newOrientation);

		if (!canWalkTo(targetPosition)) {
			return false;
		}

		ISU.Coord cellCenter = currentPosition.toISUCoordCentered();

		model.placeSilently(entity, cellCenter);
		entity.turn(newOrientation - entity.orientation());

		this.startCenter = cellCenter;
		this.targetCenter = targetPosition.toISUCoordCentered();

		entity.setLinearSpeed(speedForOrientation(newOrientation));
		entity.setAngularSpeed(0.0);

		this.walking = true;
		this.pendingDone = false;

		return true;
	}

	private void reverseImmediately(int newOrientation) {
		ISU.Coord currentCenter = entity.center().mkCopy();
		ISU.Coord oldStart = this.startCenter.mkCopy();

		entity.turn(newOrientation - entity.orientation());

		this.startCenter = currentCenter;
		this.targetCenter = oldStart;
		this.pendingDone = false;

		entity.setLinearSpeed(speedForOrientation(newOrientation));
		entity.setAngularSpeed(0.0);
	}

	private void stopBecauseOfCollision() {
		stopEntity();
		resetAction();
	}

	private void stopEntity() {
		entity.setLinearSpeed(model.isu().new Vector(0.0, 0.0));
		entity.setAngularSpeed(0.0);
	}

	private void resetAction() {
		this.walking = false;
		this.pendingDone = false;
		this.startCenter = null;
		this.targetCenter = null;
	}

	private ISU.Vector speedForOrientation(int orientation) {
		switch (orientation) {
		case Orientation.EAST:
			return model.isu().new Vector(entity.step().x() * this.walkSpeed, 0.0);

		case Orientation.SOUTH:
			return model.isu().new Vector(0.0, entity.step().y() * this.walkSpeed);

		case Orientation.WEST:
			return model.isu().new Vector(-entity.step().x() * this.walkSpeed, 0.0);

		case Orientation.NORTH:
			return model.isu().new Vector(0.0, -entity.step().y() * this.walkSpeed);

		default:
			throw new AssertionError("Direction must be cardinal: " + orientation);
		}
	}

	private boolean canWalkTo(Grid.Position targetPosition) {
		if (targetPosition == null) {
			return false;
		}

		if (!model.grid().contains(targetPosition)) {
			return false;
		}

		Grid.Cell targetCell = model.grid().cellAt(targetPosition);

		for (Entity other : targetCell.entities()) {
			if (other != entity && model.isBlocking(entity, other)) {
				return false;
			}
		}

		return true;
	}

	private boolean isCloseToCellCenter() {
		Grid.Position currentPosition = entity.position();
		ISU.Coord cellCenter = currentPosition.toISUCoordCentered();

		double distance = entity.center().distanceTo(cellCenter);

		return distance <= model.isu().cmPerCell() * TURN_SNAP_RATIO;
	}
}