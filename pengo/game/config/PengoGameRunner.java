package game.config;

import java.io.IOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import oop.tasks.Runnable;
import game.model.Power;
import game.model.PowerType;
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
import game.view.PengoOverlay;
import game.view.PowerAvatar;
import oop.graphics.Canvas;
import oop.tasks.Runtime;

public final class PengoGameRunner {

	private static final double PLAYER_SPEED = 5.0;
	private static final double FRIEND_SPEED = 4.0;
	private static final double ENEMY_SPEED = 3.0;
	private static final double ICE_STUNT_SPEED = 5.0;

	private static final String ENEMY_GAL_PATH = "pengo/game/gal/EnemyChase.gal";
	private static final String FRIEND_GAL_PATH = "pengo/game/gal/FriendRandom.gal";

	private static int spawnedEnemyCounter = 0;
	private static int totalSpawnedEnemies = 0;
	private static final int POWER_MAX_SPAWN_PER_TYPE = 2;
	private static final int POWER_MIN_DELAY_MS = 4000;
	private static final int POWER_MAX_DELAY_MS = 9000;

	private static final Random RANDOM = new Random();
	private static final Map<PowerType, Integer> spawnedPowers =
			new EnumMap<PowerType, Integer>(PowerType.class);

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
		ISU isu = model.isu();

		ViewPort viewPort = new ViewPort(model, isu.new Coord(0.0, 0.0), isu.new Dimension(
				config.viewportWidthCells() * isu.cmPerCell(), config.viewportHeightCells() * isu.cmPerCell()));

		model.setViewPort(viewPort);

		Brain brain = new Brain();
		View view = new View(model, config.windowWidthPixel(), config.windowHeightPixel());
		Painter painter = new Painter(view);

		BuildMap builder;

		try {
			builder = new BuildMap(game, view, config.mapPath());
			builder.build();

			totalSpawnedEnemies = builder.enemies().size();
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to load Pengo map: " + config.mapPath(), exception);
		}

		Pengo pengo = builder.pengo();
		assert pengo != null;

		PengoGameState gameState = new PengoGameState(model, pengo, builder.enemies().size(), builder.spawners().size(),
				config.maxTotalSpawnedEnemies());

		view.setOverlay(new PengoOverlay(gameState));

		Automaton enemyAutomaton = loadGalAutomaton(ENEMY_GAL_PATH);
		Automaton friendAutomaton = loadGalAutomaton(FRIEND_GAL_PATH);

		CurrentPlayer currentPlayer = new CurrentPlayer(pengo);

		viewPort.centerOn(pengo);

		Ticker ticker = new Ticker(model, brain, view, config.tickDelayMs(), pengo);

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

		resetPowerSpawnCounters();
		installRandomPowerSpawner(model, view, gameState);

		painter.visible(canvas);

		PainterTask painterTask = new PainterTask(painter, config.paintDelayMs());

		Runtime.post(ticker);
		Runtime.post(painterTask);

		return new PengoGameSession(painter, controller, ticker, painterTask, view);
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
		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
			enemy.setStunt(enemyStunt);

			GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);

			brain.add(enemyBot);
		}
	}

	private static void installSpawnerBots(Brain brain, Model model, View view, BuildMap builder,
			Automaton enemyAutomaton, CurrentPlayer currentPlayer, PengoConfig config, PengoGameState gameState) {
		for (Spawner spawner : builder.spawners()) {
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
		int count = 0;

		for (Entity entity : model.entities()) {
			if (entity instanceof Enemies && entity.alive()) {
				count++;
			}
		}

		return count;
	}

	private static boolean spawnCellIsFree(Spawner spawner) {
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

	private static void installRandomPowerSpawner(final Model model, final View view, final PengoGameState gameState) {
		assert model != null;
		assert view != null;
		assert gameState != null;

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				gameState.update();

				if (gameState.finished()) {
					return;
				}

				spawnOneRandomPowerIfPossible(model, view);

				Runtime.post(this, randomPowerDelay());
			}
		}, randomPowerDelay());
	}

	private static int randomPowerDelay() {
		return POWER_MIN_DELAY_MS + RANDOM.nextInt(POWER_MAX_DELAY_MS - POWER_MIN_DELAY_MS + 1);
	}

	private static void spawnOneRandomPowerIfPossible(Model model, View view) {
		List<PowerType> possibleTypes = possiblePowerTypes(model);

		if (possibleTypes.isEmpty()) {
			return;
		}

		PowerType type = possibleTypes.get(RANDOM.nextInt(possibleTypes.size()));
		Grid.Position position = randomFreePowerPosition(model);

		if (position == null) {
			return;
		}

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

	private static Grid.Position randomFreePowerPosition(Model model) {
		List<Grid.Position> freePositions = new ArrayList<Grid.Position>();
		Grid grid = model.grid();

		for (int y = 0; y < grid.height(); y++) {
			for (int x = 0; x < grid.width(); x++) {
				Grid.Position position = grid.new Position(x, y);

				if (isFreeForPower(model, position)) {
					freePositions.add(position);
				}
			}
		}

		if (freePositions.isEmpty()) {
			return null;
		}

		return freePositions.get(RANDOM.nextInt(freePositions.size()));
	}

	private static boolean isFreeForPower(Model model, Grid.Position position) {
		Grid.Cell cell = model.grid().cellAt(position);

		for (Entity entity : cell.entities()) {
			if (!entity.alive()) {
				continue;
			}

			if (entity instanceof Mur || entity instanceof Glacon || entity instanceof Spawner || entity instanceof Pengo
					|| entity instanceof Friend || entity instanceof Enemies || entity instanceof Power) {
				return false;
			}
		}

		return true;
	}
}