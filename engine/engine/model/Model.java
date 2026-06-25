package engine.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * État du monde simulé : entités, mouvements, collisions et viewport.
 */
public class Model {

	private static final Logger LOGGER = Logger.getLogger(Model.class.getName());

	private final Game game;
	private final Grid grid;
	private final ISU isu;

	private final List<Entity> entities;

	private ViewPort viewPort;

	public Model(Game game) {
		this.game = game;
		this.grid = game.getGrid();
		this.isu = game.getIsu();

		this.entities = new ArrayList<Entity>();

		double defaultViewWidth = defaultViewPortLength(this.isu.xAxis(), this.isu.width());
		double defaultViewHeight = defaultViewPortLength(this.isu.yAxis(), this.isu.height());

		this.viewPort = new ViewPort(this, this.isu.new Coord(0.0, 0.0),
				this.isu.new Dimension(defaultViewWidth, defaultViewHeight));

		LOGGER.log(Level.INFO, "Model created");
	}

	private double defaultViewPortLength(Axis axis, double mapLength) {
		assert mapLength > 0.0 : mapLength;

		if (!axis.onTorus()) {
			return mapLength;
		}

		double result = mapLength - this.isu.cmPerCell();

		assert result > 0.0 : "a toric map must contain at least two cells";

		return result;
	}

	public Game game() {
		return this.game;
	}

	public Grid grid() {
		return this.grid;
	}

	public ISU isu() {
		return this.isu;
	}

	public List<Entity> entities() {
		return new ArrayList<Entity>(this.entities);
	}

	public ViewPort viewPort() {
		return this.viewPort;
	}

	public void setViewPort(ViewPort viewPort) {
		assert viewPort.model() == this;

		this.viewPort = viewPort;
	}

	// ENTITY MANAGEMENT

	public void add(Entity entity) {
		add(entity, entity.position());
	}

	public void add(Entity entity, Grid.Position initialPosition) {
		assert entity.model() == this;
		assert initialPosition.grid() == this.grid;
		assert !this.entities.contains(entity);

		entity.prepareInitialPosition(initialPosition);

		this.entities.add(entity);

		entity.install();

		LOGGER.log(Level.INFO, "Entity added to model: {0}", entity.name());
	}

	public void remove(Entity entity) {
		assert entity.model() == this;

		if (!this.entities.contains(entity)) {
			return;
		}

		entity.removeStunt();
		entity.uninstall();

		this.entities.remove(entity);

		LOGGER.log(Level.INFO, "Entity removed from model: {0}", entity.name());
	}

	// COLLISIONS DIRECTES

	public List<Entity> collisions(Entity entity) {
		assertOwned(entity);

		List<Entity> result = new ArrayList<Entity>();

		for (Entity other : this.entities) {
			if (other != entity && other.alive() && entity.intersects(other)) {
				result.add(other);
			}
		}

		return result;
	}

	// TICK

	public void tick(long elapsed) {
		assert elapsed >= 0 : elapsed;

		List<Entity> copy = new ArrayList<Entity>(this.entities);

		for (Entity entity : copy) {
			if (this.entities.contains(entity) && entity.alive() && entity.installed() && entity.isMoving()) {
				move(entity, elapsed);
			}
		}

		cleanupDeadEntities();
	}

	// PLACEMENT DIRECT

	public void place(Entity entity, Grid.Position position) {
		assert position.grid() == this.grid;

		assertOwned(entity);

		entity.applyPoseFromModel(position.toISUCoordCentered(), entity.orientation(), true);
	}

	public void place(Entity entity, ISU.Coord center) {
		assert center.isu() == this.isu;

		assertOwned(entity);

		entity.applyPoseFromModel(center, entity.orientation(), true);
	}

	void placeSilently(Entity entity, ISU.Coord center) {
		assert center.isu() == this.isu;

		assertOwned(entity);

		entity.applyPoseFromModel(center, entity.orientation(), false);
	}

	public void translate(Entity entity, Grid.Vector vector) {
		assert vector.grid() == this.grid;

		assertOwned(entity);

		Grid.Position target = entity.position();
		target.translate(vector);

		entity.applyPoseFromModel(target.toISUCoordCentered(), entity.orientation(), true);
	}

	public void translate(Entity entity, ISU.Vector vector) {
		assert vector.isu() == this.isu;

		assertOwned(entity);

		ISU.Coord target = entity.center();
		target.translate(vector);

		entity.applyPoseFromModel(target, entity.orientation(), true);
	}

	public void rotate(Entity entity, int angleDegree) {
		assertOwned(entity);

		entity.applyPoseFromModel(entity.center(), entity.orientation() + angleDegree, true);
	}

	public void setOrientation(Entity entity, int orientationDegree) {
		assertOwned(entity);

		entity.applyPoseFromModel(entity.center(), orientationDegree, true);
	}

	// MOVE

	public boolean move(Entity entity, long elapsed) {
		assert elapsed >= 0 : elapsed;

		assertOwned(entity);

		Motion motion = new Motion(entity, elapsed);

		List<Entity> candidates = motionCandidates(motion);

		MotionResult result = resolveMotion(motion.entity, motion.oldCenter, motion.movement, motion.oldOrientation,
				motion.targetOrientation, candidates);

		applyResolvedMotion(entity, result);

		if (entity.alive() && entity.installed()) {
			entity.commit();
		}

		return !result.blocked();
	}

	private final class Motion {

		final Entity entity;

		final ISU.Coord oldCenter;
		final int oldOrientation;
		final int targetOrientation;

		final ISU.Vector movement;

		Motion(Entity entity, long elapsed) {
			assert elapsed >= 0 : elapsed;

			this.entity = entity;

			double dt = elapsed / 1000.0;

			this.oldCenter = entity.center();
			this.oldOrientation = entity.orientation();

			ISU.Vector speed = entity.linearSpeed();

			this.movement = Model.this.isu.new Vector(speed.x() * dt, speed.y() * dt);

			this.targetOrientation = normalizeOrientation(
					this.oldOrientation + (int) Math.round(entity.angularSpeed() * dt));
		}
	}

	private List<Entity> motionCandidates(Motion motion) {
		List<Entity> candidates = new ArrayList<Entity>();

		List<Entity> pathCandidates = collisionsAlongMotion(motion.entity, motion.oldCenter, motion.movement);

		List<Entity> hitboxCandidates = potentialCollisionsByMotionHitbox(motion.entity, motion.oldCenter,
				motion.movement, motion.oldOrientation, motion.targetOrientation);

		addAllWithoutDuplicates(candidates, pathCandidates);
		addAllWithoutDuplicates(candidates, hitboxCandidates);

		return candidates;
	}

	public List<Entity> collisionsAlongMotion(Entity entity, ISU.Coord from, ISU.Vector movement) {
		assert entity.model() == this;
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		Set<Entity> result = new LinkedHashSet<Entity>();

		double maxStep = this.isu.cmPerCell() / 4.0;
		int nsteps = Math.max(1, (int) Math.ceil(movement.norm() / maxStep));

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			Grid.Position position = testedCenter.toGridPosition();
			Grid.Cell cell = this.grid.cellAt(position);

			for (Entity other : cell.entities()) {
				if (other != entity && other.alive()) {
					result.add(other);
				}
			}
		}

		return new ArrayList<Entity>(result);
	}

	public List<Entity> potentialCollisionsByMotionHitbox(Entity entity, ISU.Coord from, ISU.Coord to) {
		assert entity.model() == this;
		assert from.isu() == this.isu;
		assert to.isu() == this.isu;

		ISU.Vector movement = from.mkVectorToward(to);

		return potentialCollisionsByMotionHitbox(entity, from, movement, entity.orientation(), entity.orientation());
	}

	private List<Entity> potentialCollisionsByMotionHitbox(Entity entity, ISU.Coord from, ISU.Vector movement,
			int fromOrientation, int toOrientation) {
		assert entity.model() == this;
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		int nsteps = motionStepCount(movement, fromOrientation, toOrientation);

		Hitbox motionHitbox = entity.motionHitboxBetween(from, movement, fromOrientation, toOrientation, nsteps);

		List<Entity> result = new ArrayList<Entity>();

		for (Entity other : this.entities) {
			if (other == entity || !other.alive()) {
				continue;
			}

			boolean possible = motionHitbox.intersects(other.bounding().hitbox(), this.isu.xAxis().onTorus(),
					this.isu.yAxis().onTorus(), this.isu.width(), this.isu.height());

			if (possible) {
				result.add(other);
			}
		}

		return result;
	}

	private MotionResult resolveMotion(Entity entity, ISU.Coord from, ISU.Vector movement, int fromOrientation,
			int toOrientation, List<Entity> candidates) {
		assert entity.model() == this;
		assert from.isu() == this.isu;
		assert movement.isu() == this.isu;

		int nsteps = motionStepCount(movement, fromOrientation, toOrientation);
		int angleDelta = shortestAngleDelta(fromOrientation, toOrientation);

		ISU.Coord acceptedCenter = from.mkCopy();
		int acceptedOrientation = normalizeOrientation(fromOrientation);

		Set<Entity> allContacts = new LinkedHashSet<Entity>();

		for (int i = 1; i <= nsteps; i++) {
			double ratio = (double) i / nsteps;

			ISU.Coord testedCenter = this.isu.new Coord(from.x() + movement.x() * ratio,
					from.y() + movement.y() * ratio);

			int testedOrientation = normalizeOrientation(fromOrientation + (int) Math.round(angleDelta * ratio));

			Bounding testedBounding = entity.boundingAt(testedCenter, testedOrientation);

			List<Entity> currentBlockers = new ArrayList<Entity>();

			for (Entity other : candidates) {
				if (other == entity || !other.alive()) {
					continue;
				}

				if (!testedBounding.intersects(other.bounding())) {
					continue;
				}

				allContacts.add(other);

				if (isBlocking(entity, other)) {
					currentBlockers.add(other);
				}
			}

			if (!currentBlockers.isEmpty()) {
				return new MotionResult(acceptedCenter, acceptedOrientation, new ArrayList<Entity>(allContacts),
						currentBlockers);
			}

			acceptedCenter = testedCenter;
			acceptedOrientation = testedOrientation;
		}

		return new MotionResult(acceptedCenter, acceptedOrientation, new ArrayList<Entity>(allContacts),
				new ArrayList<Entity>());
	}

	private void applyResolvedMotion(Entity entity, MotionResult result) {
		entity.applyPoseFromModel(result.acceptedCenter, result.acceptedOrientation, true);

		if (result.blocked()) {
			entity.setLinearSpeed(this.isu.new Vector(0.0, 0.0));
			entity.setAngularSpeed(0.0);

			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.log(Level.FINER, "Motion blocked: entity={0}, blockers={1}",
						new Object[] { entity.name(), Integer.valueOf(result.blockers.size()) });
			}
		}

		notifyContacts(entity, result.contacts);
	}

	private void notifyContacts(Entity moving, List<Entity> contacts) {
		if (contacts.isEmpty()) {
			return;
		}

		moving.collision(contacts);
	}

	private static final class MotionResult {

		final ISU.Coord acceptedCenter;
		final int acceptedOrientation;

		final List<Entity> contacts;
		final List<Entity> blockers;

		MotionResult(ISU.Coord acceptedCenter, int acceptedOrientation, List<Entity> contacts, List<Entity> blockers) {
			this.acceptedCenter = acceptedCenter;
			this.acceptedOrientation = acceptedOrientation;
			this.contacts = contacts;
			this.blockers = blockers;
		}

		boolean blocked() {
			return !this.blockers.isEmpty();
		}
	}

	boolean isBlocking(Entity moving, Entity other) {
		assert moving.model() == this;
		assert other.model() == this;
		assert moving != other;

		return moving.blocks(other) || other.blocks(moving);
	}

	private int motionStepCount(ISU.Vector movement, int fromOrientation, int toOrientation) {
		assert movement.isu() == this.isu;

		double maxLinearStep = this.isu.cmPerCell() / 4.0;

		int linearSteps = (int) Math.ceil(movement.norm() / maxLinearStep);

		int angleDelta = Math.abs(shortestAngleDelta(fromOrientation, toOrientation));
		int angularSteps = (int) Math.ceil(angleDelta / 5.0);

		return Math.max(1, Math.max(linearSteps, angularSteps));
	}

	// VIEWPORT

	public List<Entity> visibleEntities() {
		return visibleEntities(this.viewPort);
	}

	public List<Entity> visibleEntities(ViewPort viewPort) {
		assert viewPort.model() == this;

		Set<Entity> result = new LinkedHashSet<Entity>();

		List<Integer> xs = coveredCellIndices(viewPort.x(), viewPort.width(), this.isu.width(), this.grid.width());

		List<Integer> ys = coveredCellIndices(viewPort.y(), viewPort.height(), this.isu.height(), this.grid.height());

		for (int x : xs) {
			for (int y : ys) {
				Grid.Cell cell = this.grid.cellAt(this.grid.new Position(x, y));

				for (Entity entity : cell.entities()) {
					if (entity.alive() && entity.installed()) {
						result.add(entity);
					}
				}
			}
		}

		return new ArrayList<Entity>(result);
	}

	private List<Integer> coveredCellIndices(double start, double length, double mapLength, int cellCount) {
		assert length > 0.0 : length;
		assert length <= mapLength : length;

		List<Integer> result = new ArrayList<Integer>();

		double end = start + length;

		if (end <= mapLength) {
			addCellRange(result, start, end, cellCount);
		} else {
			addCellRange(result, start, mapLength, cellCount);
			addCellRange(result, 0.0, end - mapLength, cellCount);
		}

		return result;
	}

	private void addCellRange(List<Integer> result, double start, double end, int cellCount) {
		if (end <= start) {
			return;
		}

		double cellSize = this.isu.cmPerCell();
		double epsilon = 1e-6;

		int first = (int) Math.floor(start / cellSize);
		int last = (int) Math.floor((end - epsilon) / cellSize);

		if (first < 0) {
			first = 0;
		}

		if (last >= cellCount) {
			last = cellCount - 1;
		}

		for (int i = first; i <= last; i++) {
			if (!result.contains(i)) {
				result.add(i);
			}
		}
	}

	// UTILS

	private void assertOwned(Entity entity) {
		assert entity.model() == this;
		assert this.entities.contains(entity);
		assert entity.installed();
	}

	private void addAllWithoutDuplicates(List<Entity> destination, List<Entity> source) {
		for (Entity entity : source) {
			if (!destination.contains(entity)) {
				destination.add(entity);
			}
		}
	}

	private void cleanupDeadEntities() {
		List<Entity> deadEntities = new ArrayList<Entity>();

		for (Entity entity : this.entities) {
			if (!entity.alive()) {
				deadEntities.add(entity);
			}
		}

		if (!deadEntities.isEmpty()) {
			LOGGER.log(Level.FINE, "Cleaning dead entities: count={0}", Integer.valueOf(deadEntities.size()));
		}

		for (Entity entity : deadEntities) {
			remove(entity);
		}
	}

	private int normalizeOrientation(int angleDegree) {
		return ((angleDegree % 360) + 360) % 360;
	}

	private int shortestAngleDelta(int fromOrientation, int toOrientation) {
		return ((toOrientation - fromOrientation + 540) % 360) - 180;
	}
}