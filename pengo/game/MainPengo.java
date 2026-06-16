package game;

import java.awt.Dimension;
import java.io.IOException;

import engine.brain.Brain;
import engine.brain.Bot;
import engine.brain.PlayerBot;
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
    private static final int MAX_LIVING_ENEMIES = 5;

    private static final String MAP_PATH = "pengo/game/Maps.txt";

    private static int spawnedEnemyCounter = 0;

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
        /*
         * Game n'a aucune méthode abstraite à spécialiser. L'instance anonyme évite de
         * créer une nouvelle classe PengoGame uniquement pour stocker la configuration.
         */
        Game game = new Game(MAP_WIDTH_CELLS, MAP_HEIGHT_CELLS, true, true, CM_PER_CELL, PIXEL_PER_CM) {
        };

        Model model = game.model();
        ISU isu = model.isu();

        ViewPort viewPort = new ViewPort(model, isu.new Coord(0.0, 0.0),
                isu.new Dimension(VIEWPORT_WIDTH_CELLS * isu.cmPerCell(),
                        VIEWPORT_HEIGHT_CELLS * isu.cmPerCell()));
        model.setViewPort(viewPort);

        Brain brain = new Brain();
        View view = new View(model, WINDOW_WIDTH_PIXEL, WINDOW_HEIGHT_PIXEL);
        Painter painter = new Painter(view);

        BuildMap builder;
        try {
            builder = new BuildMap(game, view, MAP_PATH);
            builder.build();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Pengo map: " + MAP_PATH, exception);
        }

        Pengo pengo = builder.pengo();
        assert pengo != null;

        viewPort.centerOn(pengo);

        BasicStunt pengoStunt = new BasicStunt(model, pengo);
        pengo.setStunt(pengoStunt);

        PlayerBot playerBot = new PlayerBot(brain, pengoStunt);
        brain.add(playerBot);

        for (Friend friend : builder.friends()) {
            BasicStunt friendStunt = new BasicStunt(model, friend);
            friend.setStunt(friendStunt);

            FriendBot friendBot = new FriendBot(brain, friendStunt, friend);
            brain.add(friendBot);
        }

        for (Enemies enemy : builder.enemies()) {
            BasicStunt enemyStunt = new BasicStunt(model, enemy);
            enemy.setStunt(enemyStunt);

            EnemiesBot enemyBot = new EnemiesBot(brain, enemyStunt, enemy, pengo);
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

                    if (livingEnemies(model) >= MAX_LIVING_ENEMIES) {
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

        PengoController controller = new PengoController(playerBot, pengo);

        Task task = Runtime.task();
        Canvas canvas = (Canvas) task.find("canvas");
        assert canvas != null;

        canvas.set(painter);
        canvas.set((Canvas.KeyListener) controller);

        Runtime.post(new Ticker(model, brain, view, TICK_DELAY_MS, pengo));
        Runtime.post(new PainterTask(painter, PAINT_DELAY_MS));
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

            if (entity instanceof Mur
                    || entity instanceof Glacon
                    || entity instanceof Pengo
                    || entity instanceof Friend
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