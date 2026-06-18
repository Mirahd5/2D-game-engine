package game.config;

import java.io.IOException;
import java.util.List;

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
import game.BuildMap;
import game.brain.EnemiesBot;
import game.brain.FriendBot;
import game.controller.PengoController;
import game.model.Enemies;
import game.model.Friend;
import game.model.Glacon;
import game.model.Mur;
import game.model.Pengo;
import game.model.Spawner;
import game.view.EnemiesAvatar;
import oop.graphics.Canvas;
import oop.tasks.Runtime;

/**
 * Lance une partie Pengo à partir d'une configuration déjà choisie dans le
 * menu.
 */
public final class PengoGameRunner {

	private static int spawnedEnemyCounter = 0;

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
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to load Pengo map: " + config.mapPath(), exception);
		}

		Pengo pengo = builder.pengo();
		assert pengo != null;

		viewPort.centerOn(pengo);

		BasicStunt pengoStunt = new BasicStunt(model, pengo);
		pengo.setStunt(pengoStunt);

		installFriendBots(brain, model, builder);
		installEnemyBots(brain, model, builder, pengo);
		installSpawnerBots(brain, model, view, builder, pengo, config);
		installIceBots(brain, model, builder);

		PengoController controller = new PengoController(
		        pengoStunt,
		        pengo,
		        config.continuousWalk()
		);

		installControllerBot(brain, controller);

		painter.visible(canvas);

		Ticker ticker = new Ticker(model, brain, view, config.tickDelayMs(), pengo);
		PainterTask painterTask = new PainterTask(painter, config.paintDelayMs());

		Runtime.post(ticker);
		Runtime.post(painterTask);

		return new PengoGameSession(painter, controller, ticker, painterTask, view);
	}

	private static void installFriendBots(Brain brain, Model model, BuildMap builder) {
		assert brain != null;
		assert model != null;
		assert builder != null;

		for (Friend friend : builder.friends()) {
			BasicStunt friendStunt = new BasicStunt(model, friend);
			friend.setStunt(friendStunt);

			FriendBot friendBot = new FriendBot(brain, friendStunt, friend);
			brain.add(friendBot);
		}
	}

	private static void installEnemyBots(Brain brain, Model model, BuildMap builder, Pengo pengo) {
		assert brain != null;
		assert model != null;
		assert builder != null;
		assert pengo != null;

		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy);
			enemy.setStunt(enemyStunt);

			EnemiesBot enemyBot = new EnemiesBot(brain, enemyStunt, enemy, pengo);
			brain.add(enemyBot);
		}
	}

	private static void installSpawnerBots(Brain brain, Model model, View view, BuildMap builder, Pengo pengo,
			PengoConfig config) {

		assert brain != null;
		assert model != null;
		assert view != null;
		assert builder != null;
		assert pengo != null;
		assert config != null;

		for (Spawner spawner : builder.spawners()) {
			BasicStunt spawnerStunt = new BasicStunt(model, spawner);
			spawner.setStunt(spawnerStunt);

			Bot spawnerBot = new Bot(brain, spawnerStunt) {
				private long lastSpawnMs = System.currentTimeMillis();

				@Override
				public void think() {
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

					if (!spawnCellIsFree(spawner)) {
						return;
					}

					createSpawnedEnemy(model, view, brain, pengo, spawner.position());
				}
			};

			brain.add(spawnerBot);
		}
	}

	private static void installIceBots(Brain brain, Model model, BuildMap builder) {
		assert brain != null;
		assert model != null;
		assert builder != null;

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
	}

	private static void installControllerBot(Brain brain, PengoController controller) {
	    assert brain != null;
	    assert controller != null;

	    BasicStunt pengoStunt = controller.stunt();

	    Bot controllerBot = new Bot(brain, pengoStunt) {
	        @Override
	        public void think() {
	            controller.updateContinuousWalk();
	        }

	        @Override
	        public void done() {
	            controller.updateContinuousWalk();
	        }

	        @Override
	        public void collision(Entity entity) {
	            controller.updateContinuousWalk();
	        }

	        @Override
	        public void collision(List<Entity> entities) {
	            controller.updateContinuousWalk();
	        }
	    };

	    brain.add(controllerBot);
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

	private static void createSpawnedEnemy(Model model, View view, Brain brain, Pengo pengo, Grid.Position position) {
		Enemies enemy = new Enemies(model, "spawned-enemy-" + spawnedEnemyCounter++);
		enemy.setAvatar(new EnemiesAvatar(view, enemy));

		model.add(enemy, position);

		BasicStunt enemyStunt = new BasicStunt(model, enemy);
		enemy.setStunt(enemyStunt);

		EnemiesBot enemyBot = new EnemiesBot(brain, enemyStunt, enemy, pengo);
		brain.add(enemyBot);
	}
}