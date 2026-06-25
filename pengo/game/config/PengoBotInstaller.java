package game.config;

import engine.brain.Bot;
import engine.brain.Brain;
import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.view.DebugOptions;
import engine.view.View;
import gal.ast.Automaton;
import game.BuildMap;
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
import oop.tasks.Runnable;
import oop.tasks.Runtime;

public final class PengoBotInstaller {

	private static final double FRIEND_SPEED = 4.0;
	private static final double ENEMY_SPEED = 3.0;

	private static int spawnedEnemyCounter = 0;
	private static int totalSpawnedEnemies = 0;

	private PengoBotInstaller() {
	}

	public static void resetEnemyCounters(int initialEnemyCount) {
		spawnedEnemyCounter = 0;
		totalSpawnedEnemies = initialEnemyCount;
	}

	// GAL COMPORTEMENT

	// met le gal du bot non controlé
	public static void installFriendGalBot(Brain brain, Model model, Friend friend, Automaton friendAutomaton,
			CurrentPlayer currentPlayer, Switch controlSwitcher) {
		if (friend == null) {
			return;
		}
		if (friend == controlSwitcher.controlledEntity()) {
			return;
		}
		BasicStunt friendStunt = new BasicStunt(model, friend, FRIEND_SPEED);
		friend.setStunt(friendStunt);
		GalBot friendBot = new GalBot(brain, friendStunt, friend, friendAutomaton, currentPlayer);
		brain.add(friendBot);
	}

	// met les bots gal des ennemies
	public static void installEnemyGalBots(Brain brain, Model model, BuildMap builder, Automaton enemyAutomaton,
			CurrentPlayer currentPlayer) {
		for (Enemies enemy : builder.enemies()) {
			BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
			enemy.setStunt(enemyStunt);
			GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
			brain.add(enemyBot);
		}
	}

	// SPAWNER ET ICE

	public static void installSpawnerBots(Brain brain, Model model, View view, BuildMap builder,
			Automaton enemyAutomaton, CurrentPlayer currentPlayer, PengoConfig config, PengoGameState gameState,
			PengoRunState runState) {

		for (final Spawner spawner : builder.spawners()) {
			BasicStunt spawnerStunt = new BasicStunt(model, spawner);
			spawner.setStunt(spawnerStunt);
			Bot spawnerBot = new Bot(brain, spawnerStunt) {
				private long lastSpawnMs = runState.nowMs();

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

					long now = runState.nowMs();
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
					createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, spawner.position(), gameState,
							runState);
				}
			};
			brain.add(spawnerBot);
		}
	}

	public static void installIceBots(Brain brain, Model model, BuildMap builder, PengoGameState gameState) {
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

	public static void installGameStateBot(Brain brain, Model model, PengoGameState gameState) {
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
			CurrentPlayer currentPlayer, Grid.Position position, PengoGameState gameState, PengoRunState runState) {

		Enemies enemy = new Enemies(model, "spawned-enemy-" + spawnedEnemyCounter++);
		enemy.setRunState(runState);
		enemy.setAvatar(new EnemiesAvatar(view, enemy));
		model.add(enemy, position);
		totalSpawnedEnemies++;
		gameState.enemySpawned();
		BasicStunt enemyStunt = new BasicStunt(model, enemy, ENEMY_SPEED);
		enemy.setStunt(enemyStunt);
		GalBot enemyBot = new GalBot(brain, enemyStunt, enemy, enemyAutomaton, currentPlayer);
		brain.add(enemyBot);
	}

	// Debug : permet de faire spawn un ennemi avec une option debug.
	public static void installDebugLoadSpawner(final DebugOptions debugOptions, final Model model, final View view,
			final Brain brain, final Automaton enemyAutomaton, final CurrentPlayer currentPlayer,
			final BuildMap builder, final PengoGameState gameState, final PengoRunState runState) {

		Runtime.post(new Runnable() {
			@Override
			public void run() {
				if (runState.stopped()) {
					return;
				}
				if (!runState.paused() && debugOptions.loadTest()) {
					Grid.Position position = PengoPowerSpawner.randomFreePosition(model, builder);

					if (position != null) {
						createSpawnedEnemy(model, view, brain, enemyAutomaton, currentPlayer, position, gameState,
								runState);
					}
				}
				Runtime.post(this, 300);
			}
		}, 300);
	}
}