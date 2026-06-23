package game.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import game.model.Power;
import game.model.PowerType;
import game.model.Spawner;
import game.view.EnemiesAvatar;
import game.view.PengoClassicOverlay;
import game.view.PowerAvatar;
import oop.graphics.Canvas;
import oop.tasks.Runtime;
import engine.view.DebugOptions;
import engine.view.PerformanceMonitor;

public final class PengoGameRunner {

	private static final double FRIEND_SPEED = 4.0;
	private static final double ENEMY_SPEED = 3.0;

	private static final String ENEMY_GAL_PATH = "pengo/game/gal/EnemyChase.gal";
	private static final String FRIEND_GAL_PATH = "pengo/game/gal/FriendRandom.gal";

	private static final int POWER_MIN_DELAY_MS = 12000;
	private static final int POWER_MAX_DELAY_MS = 18000;
	private static final int POWER_MAX_SPAWN_PER_TYPE = 2;

	private static final Random RANDOM = new Random();
	private static final Map<PowerType, Integer> spawnedPowers = new EnumMap<PowerType, Integer>(PowerType.class);

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

		boolean viewportDemo = useViewportDemo(config);

		ViewPort viewPort = createViewPort(model, config, viewportDemo);

		model.setViewPort(viewPort);

		Brain brain = new Brain();

		DebugOptions debugOptions = new DebugOptions();
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();

		View view = new View(model, config.windowWidthPixel(), config.windowHeightPixel());
		view.setDebugOptions(debugOptions);
		view.setPerformanceMonitor(performanceMonitor);

		Painter painter = new Painter(view, performanceMonitor);
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

		view.setOverlay(new PengoClassicOverlay(gameState));
		view.startStartupReveal();

		Automaton enemyAutomaton = loadGalAutomaton(ENEMY_GAL_PATH);
		Automaton friendAutomaton = loadGalAutomaton(FRIEND_GAL_PATH);

		CurrentPlayer currentPlayer = new CurrentPlayer(pengo);

		if (viewportDemo) {
			viewPort.centerOn(pengo);
		}

		Ticker ticker = new Ticker(model, brain, view, config.tickDelayMs(), viewportDemo ? pengo : null,
				performanceMonitor);

		Switch controlSwitcher = new Switch(brain, model, ticker, pengo, builder.friends(), friendAutomaton,
				currentPlayer);

		installFriendGalBots(brain, model, builder, friendAutomaton, currentPlayer, controlSwitcher);

		installEnemyGalBots(brain, model, builder, enemyAutomaton, currentPlayer);

		installSpawnerBots(brain, model, view, builder, enemyAutomaton, currentPlayer, config, gameState);

		installDebugLoadSpawner(debugOptions, model, view, brain, enemyAutomaton, currentPlayer, builder, gameState);

		installIceBots(brain, model, builder, gameState);

		installGameStateBot(brain, model, gameState);

		PengoController controller = new PengoController(controlSwitcher, view, config.continuousWalk(), debugOptions);

		controller.setGameState(gameState);

		if (controller.continuousWalk()) {
			installContinuousWalkTask(controller, config.tickDelayMs());
		}

		installPengoPowerTimer(pengo, config.tickDelayMs());

		resetPowerSpawnCounters();
		installRandomPowerSpawner(model, view, gameState, builder);

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

	private static void resetPowerSpawnCounters() {
		spawnedPowers.clear();

		for (PowerType type : PowerType.values()) {
			spawnedPowers.put(type, Integer.valueOf(0));
		}
	}

	private static void installRandomPowerSpawner(final Model model, final View view, final PengoGameState gameState,
			final BuildMap builder) {
		assert model != null;
		assert view != null;
		assert gameState != null;
		assert builder != null;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				gameState.update();

				if (gameState.finished()) {
					return;
				}

				spawnOneRandomPowerIfPossible(model, view, builder);

				Runtime.post(this, randomPowerDelay());
			}
		}, randomPowerDelay());
	}

	private static int randomPowerDelay() {
		return POWER_MIN_DELAY_MS + RANDOM.nextInt(POWER_MAX_DELAY_MS - POWER_MIN_DELAY_MS + 1);
	}

	private static void spawnOneRandomPowerIfPossible(Model model, View view, BuildMap builder) {
		List<PowerType> possibleTypes = possiblePowerTypes(model);

		if (possibleTypes.isEmpty()) {
			return;
		}

		Grid.Position position = randomFreePowerPosition(model, builder);

		if (position == null) {
			System.out.println("No free position for random power.");
			return;
		}

		PowerType type = possibleTypes.get(RANDOM.nextInt(possibleTypes.size()));

		Power power = new Power(model, "random-power-" + type + "-" + System.currentTimeMillis(), type);

		power.setAvatar(new PowerAvatar(view, power));

		model.add(power, position);

		int currentCount = spawnedPowers.get(type).intValue();
		spawnedPowers.put(type, Integer.valueOf(currentCount + 1));

		System.out.println("Random power spawned: " + type + " at (" + position.x() + ", " + position.y() + ")");
	}

	private static List<PowerType> possiblePowerTypes(Model model) {
		List<PowerType> result = new ArrayList<PowerType>();

		for (PowerType type : PowerType.values()) {
			int count = spawnedPowers.get(type).intValue();

			if (count >= POWER_MAX_SPAWN_PER_TYPE) {
				continue;
			}

			if (hasActivePower(model, type)) {
				continue;
			}

			result.add(type);
		}

		return result;
	}

	private static boolean hasActivePower(Model model, PowerType type) {
		for (Entity entity : model.entities()) {
			if (entity instanceof Power && entity.alive()) {
				Power power = (Power) entity;

				if (power.getType() == type) {
					return true;
				}
			}
		}

		return false;
	}

	private static Grid.Position randomFreePowerPosition(Model model, BuildMap builder) {
		List<Grid.Position> freePositions = new ArrayList<Grid.Position>();
		Grid grid = model.grid();

		for (int y = 0; y < grid.height(); y++) {
			for (int x = 0; x < grid.width(); x++) {
				Grid.Position position = grid.new Position(x, y);

				if (isFreeForPower(model, builder, position)) {
					freePositions.add(position);
				}
			}
		}

		if (freePositions.isEmpty()) {
			return null;
		}

		return freePositions.get(RANDOM.nextInt(freePositions.size()));
	}

	private static boolean isFreeForPower(Model model, BuildMap builder, Grid.Position position) {
		char[][] map = builder.getMap();

		int x = position.x();
		int y = position.y();

		if (y < 0 || y >= map.length || x < 0 || x >= map[y].length) {
			return false;
		}

		if (map[y][x] != '.') {
			return false;
		}

		for (Entity entity : model.entities()) {
			if (entity == null) {
				continue;
			}

			if (!entity.alive() || !entity.installed()) {
				continue;
			}

			Grid.Position entityPosition = entity.position();

			if (entityPosition == null) {
				continue;
			}

			if (entityPosition.x() == x && entityPosition.y() == y) {
				return false;
			}
		}

		return true;
	}

	// debug
	private static void installDebugLoadSpawner(final DebugOptions debugOptions, final Model model, final View view,
			final Brain brain, final Automaton enemyAutomaton, final CurrentPlayer currentPlayer,
			final BuildMap builder, final PengoGameState gameState) {
		assert debugOptions != null;
		assert model != null;
		assert view != null;
		assert brain != null;
		assert enemyAutomaton != null;
		assert currentPlayer != null;
		assert builder != null;
		assert gameState != null;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (debugOptions.loadTest()) {
					Grid.Position position = randomFreePowerPosition(model, builder);

					if (position != null) {
						createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, position, gameState);
					}
				}

				Runtime.post(this, 300);
			}
		}, 300);
	}
}