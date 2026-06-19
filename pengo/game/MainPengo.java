package game;

import java.awt.Dimension;
import java.io.IOException;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Game;
import engine.model.Grid;
import engine.model.ISU;
import engine.model.Model;
import engine.model.ViewPort;
import engine.view.Painter;
import engine.view.PainterTask;
import engine.view.Ticker;
import engine.view.View;
import gal.ast.AST;
import gal.ast.Automaton;
import gal.parser.Parser;
import game.controller.PengoController;
import game.controller.Switch;
import game.gal.CurrentPlayer;
import game.gal.GalBot;
import game.model.Enemies;
import game.model.Friend;
import game.model.Glacon;
import game.model.Mur;
import game.model.Pengo;
import game.model.Spawner;
import game.view.EnemiesAvatar;
import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Runnable;
import oop.tasks.Task;

/** Point d'entrée du jeu Pengo utilisant le moteur générique. */
public final class MainPengo {

	private static final int MAP_WIDTH_CELLS = 12;
	private static final int MAP_HEIGHT_CELLS = 10;

	private static final double CM_PER_CELL = 4.0;
	private static final int PIXEL_PER_CM = 12;

	private static final int WINDOW_WIDTH_PIXEL = 720;
	private static final int WINDOW_HEIGHT_PIXEL = 600;

	private static final int VIEWPORT_WIDTH_CELLS = 10;
	private static final int VIEWPORT_HEIGHT_CELLS = 8;

	private static final int TICK_DELAY_MS = 16;
	private static final int PAINT_DELAY_MS = 33;

	private static final long SPAWN_DELAY_MS = 3500L;

	/*
	 * Nombre total maximum d'ennemis créés pendant toute la partie.
	 *
	 * Important : - on ne veut pas "refill" les ennemis quand il en reste 1 ; -
	 * sinon l'état Angry ne peut jamais vraiment arriver ; - donc on limite le
	 * total créé, pas le nombre vivant.
	 */
	private static final int MAX_TOTAL_ENEMIES = 3;

	private static final String MAP_PATH = "pengo/game/Maps.txt";
	private static final String ENEMY_GAL_PATH = "pengo/game/gal/EnemyChase.gal";
	private static final String FRIEND_GAL_PATH = "pengo/game/gal/FriendRandom.gal";

	private static int spawnedEnemyCounter = 0;
	private static int totalEnemiesCreated = 0;

	private MainPengo() {
	}

	public static void main(String[] args) {
		Runtime.boot(new Dimension(WINDOW_WIDTH_PIXEL, WINDOW_HEIGHT_PIXEL), new Runnable() {
			@Override
			public void run() {
				bootGame();
			}
		});
	}

	private static void bootGame() {
		Game game = new Game(MAP_WIDTH_CELLS, MAP_HEIGHT_CELLS, true, true, CM_PER_CELL, PIXEL_PER_CM) {
		};

		Model model = game.model();
		ISU isu = model.isu();

		ViewPort viewPort = new ViewPort(model, isu.new Coord(0.0, 0.0),
				isu.new Dimension(VIEWPORT_WIDTH_CELLS * isu.cmPerCell(), VIEWPORT_HEIGHT_CELLS * isu.cmPerCell()));
		model.setViewPort(viewPort);

		Brain brain = new Brain();
		View view = new View(model, WINDOW_WIDTH_PIXEL, WINDOW_HEIGHT_PIXEL);
		Painter painter = new Painter(view);

		BuildMap builder;

		try {
			builder = new BuildMap(game, view, MAP_PATH);
			builder.build();

			/*
			 * Les ennemis déjà présents dans la map comptent dans le total.
			 *
			 * Exemple : - si la map contient déjà 3 ennemis : le spawner ne spawn jamais ;
			 * - si la map contient 2 ennemis : le spawner peut en créer 1 seul ; - si la
			 * map contient 1 ennemi : le spawner peut en créer 2.
			 */
			totalEnemiesCreated = builder.enemies().size();

		} catch (IOException exception) {
			throw new IllegalStateException("Unable to load Pengo map: " + MAP_PATH, exception);
		}

		Pengo pengo = builder.pengo();
		assert pengo != null;

		Automaton enemyAutomaton = loadGalAutomaton(ENEMY_GAL_PATH);
		Automaton friendAutomaton = loadGalAutomaton(FRIEND_GAL_PATH);

		CurrentPlayer currentPlayer = new CurrentPlayer(pengo);

		viewPort.centerOn(pengo);

		Ticker ticker = new Ticker(model, brain, view, TICK_DELAY_MS, pengo);

		Switch controlSwitcher = new Switch(brain, model, ticker, pengo, builder.friends(), friendAutomaton,
				currentPlayer);

		for (Friend friend : builder.friends()) {
			if (friend == controlSwitcher.controlledEntity()) {
				continue;
			}

			BasicStunt friendStunt = new BasicStunt(model, friend);
			friend.setStunt(friendStunt);

			GalBot friendBot = new GalBot(brain, friendStunt, friend, friendAutomaton, currentPlayer);
			brain.add(friendBot);
		}

		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy);
			enemy.setStunt(enemyStunt);

			GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
			brain.add(enemyBot);
		}

		for (Spawner spawner : builder.spawners()) {
			BasicStunt spawnerStunt = new BasicStunt(model, spawner);
			spawner.setStunt(spawnerStunt);

			Bot spawnerBot = new Bot(brain, spawnerStunt) {
				private long lastSpawn_ms = System.currentTimeMillis();

				@Override
				public void think() {
					spawner.updateSliding();

					if (spawner.isSliding()) {
						return;
					}

					long now = System.currentTimeMillis();

					if (now - this.lastSpawn_ms < SPAWN_DELAY_MS) {
						return;
					}

					this.lastSpawn_ms = now;

					/*
					 * Correction importante : on ne teste PAS livingEnemies(model) ici.
					 *
					 * Sinon, quand il reste 1 ennemi, le spawner recrée des ennemis, et l'état
					 * Angry disparaît.
					 */
					if (totalEnemiesCreated >= MAX_TOTAL_ENEMIES) {
						return;
					}

					if (!spawnCellIsFree(spawner)) {
						return;
					}

					createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, spawner.position());
				}
			};

			brain.add(spawnerBot);
		}

		for (Glacon ice : builder.iceBlocks()) {
			BasicStunt iceStunt = new BasicStunt(model, ice);
			ice.setStunt(iceStunt);

			Bot iceBot = new Bot(brain, iceStunt) {
				@Override
				public void think() {
					ice.updateSliding();
				}
			};

			brain.add(iceBot);
		}

		PengoController controller = new PengoController(controlSwitcher);

		Task task = Runtime.task();
		Canvas canvas = (Canvas) task.find("canvas");
		assert canvas != null;

		canvas.set(painter);
		canvas.set((Canvas.KeyListener) controller);

		Runtime.post(ticker);
		Runtime.post(new PainterTask(painter, PAINT_DELAY_MS));
	}

	private static boolean spawnCellIsFree(Spawner spawner) {
		Grid.Cell cell = spawner.model().grid().cellAt(spawner.position());

		for (Entity entity : cell.entities()) {
			if (entity == spawner || !entity.alive()) {
				continue;
			}

			if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Pengo || entity instanceof Friend
					|| entity instanceof Enemies) {
				return false;
			}
		}

		return true;
	}

	private static void createSpawnedEnemy(Model model, View view, Brain brain, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer, Grid.Position position) {
		Enemies enemy = new Enemies(model, "spawned-enemy-" + spawnedEnemyCounter++);
		enemy.setAvatar(new EnemiesAvatar(view, enemy));

		model.add(enemy, position);

		/*
		 * Chaque ennemi créé par le spawner augmente le total. Ce compteur ne diminue
		 * jamais quand un ennemi meurt.
		 */
		totalEnemiesCreated++;

		BasicStunt enemyStunt = new BasicStunt(model, enemy);
		enemy.setStunt(enemyStunt);

		GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
		brain.add(enemyBot);
	}

	private static Automaton loadGalAutomaton(String path) {
		try {
			AST ast = Parser.from_file(path);

			if (ast.aut_list.isEmpty()) {
				throw new IllegalStateException("No GAL automaton in " + path);
			}

			return ast.aut_list.get(0);

		} catch (Exception exception) {
			throw new IllegalStateException("Unable to load GAL file: " + path, exception);
		}
	}
}