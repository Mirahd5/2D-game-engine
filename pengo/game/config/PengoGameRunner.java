package game.config;

import java.io.IOException;

import oop.tasks.Runnable;

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
import game.BuildMap;
import game.controller.PengoController;
import game.controller.Switch;
import game.gal.CurrentPlayer;
import game.gal.GalBot;
import game.model.Enemies;
import game.model.Friend;
import game.model.Glacon;
import game.model.Mur;
import game.model.Pengo;
import game.model.PengoGameState;
import game.model.Spawner;
import game.view.EnemiesAvatar;
import game.view.PengoClassicOverlay;
import oop.graphics.Canvas;
import oop.tasks.Runtime;

public final class PengoGameRunner {

	private static final double FRIEND_SPEED = 4.0;
	private static final double ENEMY_SPEED = 3.0;

	private static final String ENEMY_GAL_PATH = "pengo/game/gal/EnemyChase.gal";
	private static final String FRIEND_GAL_PATH = "pengo/game/gal/FriendRandom.gal";

	private static int spawnedEnemyCounter = 0;
	private static int totalSpawnedEnemies = 0;

	private PengoGameRunner() {
	}

	public static PengoGameSession start(Canvas canvas, PengoConfig config) {
		assert canvas != null;
		assert config != null;

		MapInfo mapInfo;

		try {
			mapInfo = MapInfo.read(config.mapPath());
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to read Pengo map: " + config.mapPath(), exception);
		}

		Game game = new Game(mapInfo.width(), mapInfo.height(), config.torusX(), config.torusY(), config.cmPerCell(),
				config.pixelPerCm()) {
		};

		Model model = game.model();

		/*
		 * Pengo normal : - pas de viewport qui suit le joueur - toute la map est
		 * visible
		 *
		 * Démo tore : - viewport plus petit - viewport centré sur Pengo
		 */
		boolean viewportDemo = useViewportDemo(config);

		ViewPort viewPort = createViewPort(model, config, viewportDemo);

		model.setViewPort(viewPort);

		Brain brain = new Brain();

		View view = new View(model, config.windowWidthPixel(), config.windowHeightPixel());

		Painter painter = new Painter(view);

		BuildMap builder;

		try {
			builder = new BuildMap(game, view, config.mapPath());
			builder.build();

			spawnedEnemyCounter = 0;
			totalSpawnedEnemies = builder.enemies().size();
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to load Pengo map: " + config.mapPath(), exception);
		}

		Pengo pengo = builder.pengo();

		assert pengo != null;

		PengoGameState gameState = new PengoGameState(model, pengo, builder.enemies().size(), builder.spawners().size(),
				config.maxTotalSpawnedEnemies());

		/*
		 * Nouveau HUD arcade compact.
		 */
		view.setOverlay(new PengoClassicOverlay(gameState));
		view.startStartupReveal();

		Automaton enemyAutomaton = loadGalAutomaton(ENEMY_GAL_PATH);
		Automaton friendAutomaton = loadGalAutomaton(FRIEND_GAL_PATH);

		CurrentPlayer currentPlayer = new CurrentPlayer(pengo);

		if (viewportDemo) {
			viewPort.centerOn(pengo);
		}

		Ticker ticker = new Ticker(model, brain, view, config.tickDelayMs(), viewportDemo ? pengo : null);

		Switch controlSwitcher = new Switch(brain, model, ticker, pengo, builder.friends(), friendAutomaton,
				currentPlayer);

		installFriendGalBots(brain, model, builder, friendAutomaton, currentPlayer, controlSwitcher);

		installEnemyGalBots(brain, model, builder, enemyAutomaton, currentPlayer);

		installSpawnerBots(brain, model, view, builder, enemyAutomaton, currentPlayer, config, gameState);

		installIceBots(brain, model, builder, gameState);

		installGameStateBot(brain, model, gameState);

		PengoController controller = new PengoController(controlSwitcher, view, config.continuousWalk());

		controller.setGameState(gameState);

		if (controller.continuousWalk()) {
			installContinuousWalkTask(controller, config.tickDelayMs());
		}

		installPengoPowerTimer(pengo, config.tickDelayMs());

		painter.visible(canvas);

		PainterTask painterTask = new PainterTask(painter, config.paintDelayMs());

		Runtime.post(ticker);
		Runtime.post(painterTask);

		PengoGameSession session = new PengoGameSession(painter, controller, ticker, painterTask, view);

		session.attach(canvas);

		return session;
	}

	private static boolean useViewportDemo(PengoConfig config) {
		assert config != null;

		return config.torusX() || config.torusY();
	}

	private static ViewPort createViewPort(Model model, PengoConfig config, boolean viewportDemo) {
		assert model != null;
		assert config != null;

		ISU isu = model.isu();

		if (!viewportDemo) {
			return new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(isu.width(), isu.height()));
		}

		double width = Math.min(config.viewportWidthCells() * isu.cmPerCell(), isu.width() - isu.cmPerCell());

		double height = Math.min(config.viewportHeightCells() * isu.cmPerCell(), isu.height() - isu.cmPerCell());

		if (width <= 0.0) {
			width = isu.width();
		}

		if (height <= 0.0) {
			height = isu.height();
		}

		return new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(width, height));
	}

	private static void installContinuousWalkTask(final PengoController controller, final int delayMs) {
		assert controller != null;
		assert delayMs > 0;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				controller.updateContinuousWalk();
				Runtime.post(this, delayMs);
			}
		}, delayMs);
	}

	private static void installPengoPowerTimer(final Pengo pengo, final int delayMs) {
		assert pengo != null;
		assert delayMs > 0;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				pengo.updateTimedPowers();
				Runtime.post(this, delayMs);
			}
		}, delayMs);
	}

	private static void installFriendGalBots(Brain brain, Model model, BuildMap builder, Automaton friendAutomaton,
			CurrentPlayer currentPlayer, Switch controlSwitcher) {
		assert brain != null;
		assert model != null;
		assert builder != null;
		assert friendAutomaton != null;
		assert currentPlayer != null;
		assert controlSwitcher != null;

		for (Friend friend : builder.friends()) {
			if (friend == controlSwitcher.controlledEntity()) {
				continue;
			}

			BasicStunt friendStunt = new BasicStunt(model, friend, FRIEND_SPEED);
			friend.setStunt(friendStunt);

			GalBot friendBot = new GalBot(brain, friendStunt, friend, friendAutomaton, currentPlayer);

			brain.add(friendBot);
		}
	}

	private static void installEnemyGalBots(Brain brain, Model model, BuildMap builder, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer) {
		assert brain != null;
		assert model != null;
		assert builder != null;
		assert enemyAutomaton != null;
		assert currentPlayer != null;

		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
			enemy.setStunt(enemyStunt);

			GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);

			brain.add(enemyBot);
		}
	}

	private static void installSpawnerBots(Brain brain, Model model, View view, BuildMap builder,
			Automaton enemyAutomaton, CurrentPlayer currentPlayer, PengoConfig config, PengoGameState gameState) {
		assert brain != null;
		assert model != null;
		assert view != null;
		assert builder != null;
		assert enemyAutomaton != null;
		assert currentPlayer != null;
		assert config != null;
		assert gameState != null;

		for (final Spawner spawner : builder.spawners()) {
			BasicStunt spawnerStunt = new BasicStunt(model, spawner);
			spawner.setStunt(spawnerStunt);

			Bot spawnerBot = new Bot(brain, spawnerStunt) {
				private long lastSpawnMs = System.currentTimeMillis();

				@Override
				public void think() {
					gameState.update();

					if (gameState.finished()) {
						gameState.stopAllEntities();
						return;
					}

					spawner.updateSliding();

					if (spawner.isSliding()) {
						return;
					}

					long now = System.currentTimeMillis();

					if (now - this.lastSpawnMs < config.spawnDelayMs()) {
						return;
					}

					this.lastSpawnMs = now;

					if (livingEnemies(model) >= config.maxLivingEnemies()) {
						return;
					}

					if (totalSpawnedEnemies >= config.maxTotalSpawnedEnemies()) {
						return;
					}

					if (!spawnCellIsFree(spawner)) {
						return;
					}

					createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, spawner.position(),
							gameState);
				}
			};

			brain.add(spawnerBot);
		}
	}

	private static void installIceBots(Brain brain, Model model, BuildMap builder, PengoGameState gameState) {
		assert brain != null;
		assert model != null;
		assert builder != null;
		assert gameState != null;

		for (final Glacon ice : builder.iceBlocks()) {
			BasicStunt stunt = new BasicStunt(model, ice);

			Bot bot = new Bot(brain, stunt) {
				@Override
				public void think() {
					gameState.update();

					if (gameState.finished()) {
						gameState.stopAllEntities();
						return;
					}

					ice.updateSliding();
				}
			};

			brain.add(bot);
		}
	}

	private static int livingEnemies(Model model) {
		assert model != null;

		int count = 0;

		for (Entity entity : model.entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	private static boolean spawnCellIsFree(Spawner spawner) {
		assert spawner != null;

		Grid.Cell cell = spawner.model().grid().cellAt(spawner.position());

		for (Entity entity : cell.entities()) {
			if (entity != spawner && entity.alive()) {
				if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Pengo
						|| entity instanceof Friend || entity instanceof Enemies) {
					return false;
				}
			}
		}

		return true;
	}

	private static void createSpawnedEnemy(Model model, View view, Brain brain, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer, Grid.Position position, PengoGameState gameState) {
		assert model != null;
		assert view != null;
		assert brain != null;
		assert enemyAutomaton != null;
		assert currentPlayer != null;
		assert position != null;
		assert gameState != null;

		Enemies enemy = new Enemies(model, "spawned-enemy-" + spawnedEnemyCounter++);
		enemy.setAvatar(new EnemiesAvatar(view, enemy));

		model.add(enemy, position);

		totalSpawnedEnemies++;

		gameState.enemySpawned();

		BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
		enemy.setStunt(enemyStunt);

		GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);

		brain.add(enemyBot);
	}

	private static Automaton loadGalAutomaton(String path) {
		assert path != null;
		assert !path.isEmpty();

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

	private static void installGameStateBot(Brain brain, Model model, PengoGameState gameState) {
		assert brain != null;
		assert model != null;
		assert gameState != null;

		Entity dummy = null;

		for (Entity entity : model.entities()) {
			if (entity.alive()) {
				dummy = entity;
				break;
			}
		}

		if (dummy == null) {
			return;
		}

		BasicStunt dummyStunt = new BasicStunt(model, dummy);

		Bot stateBot = new Bot(brain, dummyStunt) {
			@Override
			public void think() {
				gameState.update();

				if (gameState.finished()) {
					gameState.stopAllEntities();
				}
			}
		};

		brain.add(stateBot);
	}
}