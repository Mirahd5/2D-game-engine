package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.model.Model;
import engine.view.View;
import game.model.Enemies;
import game.model.Friend;
import game.model.Glacon;
import game.model.Mur;
import game.model.Pengo;
import game.model.Spawner;
import game.view.SpawnerAvatar;
import game.view.EnemiesAvatar;
import game.view.FriendAvatar;
import game.view.GlaconAvatar;
import game.view.MurAvatar;
import game.view.PengoAvatar;

/**
 * Lit la carte textuelle de Pengo et installe les entités dans le Model.
 *
 * Symboles actuellement reconnus : # mur, . case libre, I bloc de glace, P
 * joueur, S spawner, E ennemi, F allié.
 */
public final class BuildMap {

	private final Game game;
	private final Model model;
	private final Grid grid;
	private final View view;
	private final String mapPath;

	private char[][] map;

	private Pengo pengo;
	private final List<Mur> walls;
	private final List<Glacon> iceBlocks;
	private final List<Spawner> spawners;
	private final List<Enemies> enemies;
	private final List<Friend> friends;

	private boolean built;

	public BuildMap(Game game, View view, String mapPath) throws IOException {
		assert game != null;
		assert view != null;
		assert mapPath != null;
		assert !mapPath.isEmpty();
		assert game.model() == view.model();

		this.game = game;
		this.model = game.model();
		this.grid = game.getGrid();
		this.view = view;
		this.mapPath = mapPath;

		this.walls = new ArrayList<Mur>();
		this.iceBlocks = new ArrayList<Glacon>();
		this.spawners = new ArrayList<Spawner>();
		this.enemies = new ArrayList<Enemies>();
		this.friends = new ArrayList<Friend>();

		this.pengo = null;
		this.built = false;

		loadMap();
	}

	private void loadMap() throws IOException {
		List<String> lines = new ArrayList<String>();

		try (BufferedReader reader = new BufferedReader(new FileReader(this.mapPath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		if (lines.isEmpty()) {
			throw new IllegalStateException("The Pengo map is empty: " + this.mapPath);
		}

		int expectedWidth = this.grid.width();
		int expectedHeight = this.grid.height();

		if (lines.size() != expectedHeight) {
			throw new IllegalStateException("Map height is " + lines.size() + " but Grid height is " + expectedHeight);
		}

		this.map = new char[expectedHeight][expectedWidth];

		for (int y = 0; y < expectedHeight; y++) {
			String line = lines.get(y);

			if (line.length() != expectedWidth) {
				throw new IllegalStateException(
						"Map line " + y + " has width " + line.length() + " but Grid width is " + expectedWidth);
			}

			for (int x = 0; x < expectedWidth; x++) {
				this.map[y][x] = line.charAt(x);
			}
		}
	}

	public void build() {
		if (this.built) {
			throw new IllegalStateException("This Pengo map has already been built");
		}

		for (int y = 0; y < this.map.length; y++) {
			for (int x = 0; x < this.map[y].length; x++) {
				buildCell(this.map[y][x], x, y);
			}
		}

		if (this.pengo == null) {
			throw new IllegalStateException("The map must contain exactly one Pengo symbol 'P'");
		}

		this.built = true;
	}

	private void buildCell(char symbol, int x, int y) {
		switch (symbol) {
		case '#':
			addWall(x, y);
			break;

		case '.':
			// Case de déplacement libre : aucune entité n'est installée.
			break;

		case 'I':
			addIceBlock(x, y);
			break;

		case 'P':
			addPengo(x, y);
			break;

		case 'S':
			addSpawner(x, y);
			break;

		case 'E':
			addEnemy(x, y);
			break;

		case 'F':
			addFriend(x, y);
			break;

		case ' ':
			break;

		default:
			throw new IllegalArgumentException(
					"Unknown map symbol '" + symbol + "' at position (" + x + ", " + y + ")");
		}
	}

	private <E extends Entity> E install(E entity, int x, int y) {
		this.model.add(entity, this.grid.new Position(x, y));
		return entity;
	}

	private void addWall(int x, int y) {
		Mur wall = new Mur(this.model, "wall-" + x + "-" + y);
		wall.setAvatar(new MurAvatar(this.view, wall));
		this.walls.add(install(wall, x, y));
	}

	private void addIceBlock(int x, int y) {
		Glacon ice = new Glacon(this.model, "ice-" + x + "-" + y);
		ice.setAvatar(new GlaconAvatar(this.view, ice));
		this.iceBlocks.add(install(ice, x, y));
	}

	private void addPengo(int x, int y) {
		if (this.pengo != null) {
			throw new IllegalStateException("The map contains more than one Pengo");
		}

		Pengo player = new Pengo(this.model);
		player.setAvatar(new PengoAvatar(this.view, player));
		this.pengo = install(player, x, y);
	}

	private void addSpawner(int x, int y) {
		Spawner spawner = new Spawner(this.model, "spawner-" + x + "-" + y);
		spawner.setAvatar(new SpawnerAvatar(this.view, spawner));
		this.spawners.add(install(spawner, x, y));
	}

	private void addEnemy(int x, int y) {
		Enemies enemy = new Enemies(this.model, "enemy-" + x + "-" + y);
		enemy.setAvatar(new EnemiesAvatar(this.view, enemy));
		this.enemies.add(install(enemy, x, y));
	}

	private void addFriend(int x, int y) {
		Friend friend = new Friend(this.model, "friend-" + x + "-" + y);
		friend.setAvatar(new FriendAvatar(this.view, friend));
		this.friends.add(install(friend, x, y));
	}

	public void printMap() {
		for (char[] line : this.map) {
			System.out.println(new String(line));
		}
	}

	public char[][] getMap() {
		char[][] copy = new char[this.map.length][];

		for (int y = 0; y < this.map.length; y++) {
			copy[y] = this.map[y].clone();
		}

		return copy;
	}

	public Pengo pengo() {
		return this.pengo;
	}

	public List<Mur> walls() {
		return new ArrayList<Mur>(this.walls);
	}

	public List<Glacon> iceBlocks() {
		return new ArrayList<Glacon>(this.iceBlocks);
	}

	public List<Spawner> spawners() {
		return new ArrayList<Spawner>(this.spawners);
	}

	public List<Enemies> enemies() {
		return new ArrayList<Enemies>(this.enemies);
	}

	public List<Friend> friends() {
		return new ArrayList<Friend>(this.friends);
	}
}