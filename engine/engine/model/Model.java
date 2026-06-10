package engine.model;

import java.util.ArrayList;
import java.util.List;

public class Model {

	private final List<Entity> entities;
	private final List<Stunt> stunts;

	private final List<Entity> obstacles;
	private final List<Entity> collectibles;

	public Model() {
		this.entities = new ArrayList<>();
		this.stunts = new ArrayList<>();
		this.obstacles = new ArrayList<>();
		this.collectibles = new ArrayList<>();
	}

	public void addEntity(Entity entity) {
		this.entities.add(entity);
	}

	public void addStunt(Stunt stunt) {
		this.stunts.add(stunt);
	}

	public void addObstacle(Entity obstacle) {
		this.obstacles.add(obstacle);
		this.entities.add(obstacle);
	}

	public void addCollectible(Entity collectible) {
		this.collectibles.add(collectible);
		this.entities.add(collectible);
	}

	public void tick(long elapsed_ms) {

		// Sauvegarder les anciennes positions.
		List<Grid.Position> oldPositions = new ArrayList<>();
		for (Stunt stunt : stunts) {
			oldPositions.add(stunt.entity().position());
		}
		// Faire bouger les entités.
		for (Stunt stunt : stunts) {
			stunt.tick(elapsed_ms);
			stunt.entity().setBounding();
		}
		// Gérer collisions avec les murs.
		for (int i = 0; i < stunts.size(); i++) {
			Stunt stunt = stunts.get(i);
			Entity entity = stunt.entity();

			Entity wall = touchedObstacle(entity);

			if (wall != null) {
				entity.setPosition(oldPositions.get(i));
				entity.setBounding();
				stunt.collision(wall);
			}
		}
	}

	public Entity touchedObstacle(Entity entity) {
		for (Entity obstacle : obstacles) {
			if (entity.intersects(obstacle)) {
				return obstacle;
			}
		}
		return null;
	}

	public List<Entity> touchedCollectibles(Entity entity) {
		List<Entity> touched = new ArrayList<>();
		for (Entity collectible : collectibles) {
			if (entity.intersects(collectible)) {
				touched.add(collectible);
			}
		}
		return touched;
	}

	public void removeCollectible(Entity collectible) {
		this.collectibles.remove(collectible);
		this.entities.remove(collectible);
	}

	public void removeEntity(Entity entity) {
		this.entities.remove(entity);
		this.obstacles.remove(entity);
		this.collectibles.remove(entity);
	}

	public void removeStunt(Stunt stunt) {
		this.stunts.remove(stunt);
	}

	public boolean intersects(Entity e1, Entity e2) {
		return e1.intersects(e2);
	}

	public List<Entity> entities() {
		return this.entities;
	}

	public List<Entity> obstacles() {
		return this.obstacles;
	}

	public List<Entity> collectibles() {
		return this.collectibles;
	}

	public List<Stunt> stunts() {
		return this.stunts;
	}
}