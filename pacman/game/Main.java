package game;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.model.BasicStunt;
import engine.model.Entity;
import engine.model.Grid;
import engine.model.Model;
import engine.view.Painter;
import engine.view.View;

import game.controller.PacManController;
import game.brain.GhostBot;
import game.model.Cherry;
import game.model.Ghost;
import game.model.Gum;
import game.model.Obstacle;
import game.model.PacMan;
import game.model.Pacman;
import game.view.CherryAvatar;
import game.view.GhostAvatar;
import game.view.GumAvatar;
import game.view.ObstacleAvatar;
import game.view.PacManAvatar;

import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Task;

/**
 * Point d'entrée graphique du jeu PacMan.
 *
 * Cette classe assemble le jeu :
 * - configuration ;
 * - entités ;
 * - modèle ;
 * - vue ;
 * - contrôleur ;
 * - boucle principale.
 */
public class Main {

    private static final int HUD_WIDTH = 220;

    private static final int INITIAL_LIVES = 3;
    private static final int INVULNERABLE_MS = 3000;
    private static final int POWER_MS = 7000;
    private static final int GHOST_SCORE = 200;

    private static final int POWER_SPAWN_MS = 15000;
    private static final int CHERRY_SPAWN_MS = 30000;

    /*
     * Carte du niveau.
     *
     * # = mur
     * . = gomme
     * P = position initiale de PacMan
     * S = spawn de fantôme
     * espace = case vide / tunnel
     */
    private static final String[] MAP = {
        "#.###########################.#",
        "#..............#..............#",
        "#.#####.#####..#..#####.#####.#",
        "#.#####.#####..#..#####.#####.#",
        "#.............................#",
        "#.#####.###.#######.###.#####.#",
        "#.......###....#....###.......#",
        "#######.#####.###.#####.#######",
        "#######.#####.###.#####.#######",
        "#.............###.............#",
        "#.#####.#####.###.#####.#####.#",
        "#....##................##.....#",
        "###.###.###.#######.###.###.###",
        "#.....#.###....#....###.#.....#",
        "#.###.#.#####.###.#####.#.###.#",
        " .###.........P.........#.###. ",
        "#.###.#.#####.###.#####.#.###.#",
        "#.....#.###....#....###.#.....#",
        "###.###.###.#######.###.###.###",
        "#....##................##.....#",
        "#.#####.#####.###.#####.#####.#",
        "#.............SSSS............#",
        "#######.#####.###.#####.#######",
        "#######.#####.###.#####.#######",
        "#.......###....#....###.......#",
        "#.#####.###.#######.###.#####.#",
        "#.............................#",
        "#.#####.#####..#..#####.#####.#",
        "#.#####.#####..#..#####.#####.#",
        "#..............#..............#",
        "#.###########################.#"
    };

    public static void main(String[] args) {

        Pacman gameConfig = new Pacman();

        Runtime.boot(
            initialWindowSize(gameConfig),
            new oop.tasks.Runnable() {

                @Override
                public void run() {

                    Task task = Runtime.task();
                    Canvas canvas = (Canvas) task.find("canvas");

                    Pacman game = gameConfig;

                    /*
                     * Création des entités principales du jeu.
                     */
                    PacMan pacman = new PacMan(game);

                    List<Ghost> ghosts = new ArrayList<>();
                    ghosts.add(new Ghost(game, Ghost.Type.BLINKY));
                    ghosts.add(new Ghost(game, Ghost.Type.SPEEDY));
                    ghosts.add(new Ghost(game, Ghost.Type.INKY));
                    ghosts.add(new Ghost(game, Ghost.Type.CLYDE));

                    List<Obstacle> obstacles = new ArrayList<>();
                    List<Gum> gums = new ArrayList<>();
                    List<Cherry> cherries = new ArrayList<>();
                    List<Grid.Position> spawners = new ArrayList<>();

                    /*
                     * La carte texte est transformée en entités du modèle.
                     */
                    Grid.Position pacmanSpawn = buildMap(game, pacman, obstacles, gums, cherries, spawners);
                    placeGhostsOnSpawners(ghosts, spawners);

                    /*
                     * Le Model contient l'état logique du jeu.
                     */
                    Model model = new Model();
                    model.addEntity(pacman);

                    for (Ghost ghost : ghosts) {
                        model.addEntity(ghost);
                    }

                    for (Obstacle obstacle : obstacles) {
                        model.addObstacle(obstacle);
                    }

                    for (Gum gum : gums) {
                        model.addCollectible(gum);
                    }

                    for (Cherry cherry : cherries) {
                        model.addCollectible(cherry);
                    }

                    /*
                     * Les Stunts exécutent les déplacements.
                     */
                    double speed = game.cmPerCell() / 350.0;

                    BasicStunt pacmanStunt = new BasicStunt(pacman, speed, true);
                    model.addStunt(pacmanStunt);

                    List<BasicStunt> ghostStunts = new ArrayList<>();

                    for (Ghost ghost : ghosts) {
                        BasicStunt stunt = new BasicStunt(ghost, speed * 0.55, false);
                        ghostStunts.add(stunt);
                        model.addStunt(stunt);
                    }

                    /*
                     * Les Bots contrôlent les fantômes.
                     */
                    List<GhostBot> ghostBots = new ArrayList<>();

                    for (int i = 0; i < ghosts.size(); i++) {
                        GhostBot bot = new GhostBot(
                            ghostStunts.get(i),
                            ghosts.get(i),
                            pacman,
                            pacmanStunt,
                            obstacles
                        );

                        ghostBots.add(bot);
                        bot.think();
                    }

                    /*
                     * La View dessine les avatars.
                     */
                    View view = new View(canvas, game.pict());

                    for (Obstacle obstacle : obstacles) {
                        view.add(new ObstacleAvatar(obstacle));
                    }

                    for (Gum gum : gums) {
                        view.add(new GumAvatar(gum));
                    }

                    for (Cherry cherry : cherries) {
                        view.add(new CherryAvatar(cherry));
                    }

                    view.add(new PacManAvatar(pacman, pacmanStunt));

                    for (Ghost ghost : ghosts) {
                        view.add(new GhostAvatar(ghost));
                    }

                    /*
                     * Painter redessine régulièrement la vue.
                     * Le controller reçoit les touches du joueur.
                     */
                    Painter painter = new Painter(canvas, view, 33);
                    painter.start();

                    new PacManController(canvas, pacmanStunt);

                    /*
                     * Boucle principale logique.
                     *
                     * Elle met à jour le modèle, les collisions, le score,
                     * les bonus temporaires et le HUD.
                     */
                    Runtime.post(new oop.tasks.Runnable() {

                        private long last = System.currentTimeMillis();

                        private final Random random = new Random();

                        private int score = 0;
                        private int lives = INITIAL_LIVES;

                        private long invulnerableUntil = 0;
                        private long powerUntil = 0;

                        private long nextPowerSpawn = System.currentTimeMillis() + POWER_SPAWN_MS;
                        private long nextCherrySpawn = System.currentTimeMillis() + CHERRY_SPAWN_MS;

                        private boolean finished = false;

                        private String message = "";
                        private long messageUntil = 0;

                        @Override
                        public void run() {

                            long now = System.currentTimeMillis();
                            long elapsed = now - last;
                            last = now;

                            /*
                             * On limite elapsed pour éviter un trop grand saut
                             * si l'application a ralenti.
                             */
                            if (elapsed > 100) {
                                elapsed = 100;
                            }

                            if (!finished) {
                                model.tick(elapsed);

                                handleCollectibles(now);
                                handleGhostCollisions(now);
                                handleTimedBonusSpawns(now);
                                checkEndOfGame();

                                if (messageUntil > 0 && now >= messageUntil) {
                                    message = "";
                                    messageUntil = 0;
                                }
                            }

                            long powerRemaining = finished ? 0 : Math.max(0, powerUntil - now);
                            long invulnerableRemaining = finished ? 0 : Math.max(0, invulnerableUntil - now);

                            view.setHud(score, lives, message, powerRemaining, invulnerableRemaining);

                            Runtime.post(this, 33);
                        }

                        /**
                         * Gère les objets ramassés par PacMan.
                         */
                        private void handleCollectibles(long now) {
                            List<Entity> touchedCollectibles = model.touchedCollectibles(pacman);

                            for (Entity collectible : touchedCollectibles) {
                                model.removeCollectible(collectible);
                                view.removeAvatarOf(collectible);

                                if (collectible instanceof Gum) {
                                    Gum gum = (Gum) collectible;
                                    gums.remove(gum);

                                    score += gum.scoreValue();

                                    if (gum.power()) {
                                        powerUntil = now + POWER_MS;
                                        showMessage("POWER !", now, 1000);
                                    }

                                } else if (collectible instanceof Cherry) {
                                    Cherry cherry = (Cherry) collectible;
                                    cherries.remove(cherry);

                                    score += cherry.scoreValue();
                                    showMessage("CHERRY +" + cherry.scoreValue(), now, 1200);
                                }
                            }
                        }

                        /**
                         * Fait apparaître les bonus temporaires.
                         */
                        private void handleTimedBonusSpawns(long now) {
                            if (now >= nextPowerSpawn) {
                                spawnRandomPowerGum(now);
                                nextPowerSpawn = now + POWER_SPAWN_MS;
                            }

                            if (now >= nextCherrySpawn) {
                                spawnRandomCherry(now);
                                nextCherrySpawn = now + CHERRY_SPAWN_MS;
                            }
                        }

                        private void spawnRandomPowerGum(long now) {
                            List<Gum> normalGums = new ArrayList<>();

                            for (Gum gum : gums) {
                                if (!gum.power()) {
                                    normalGums.add(gum);
                                }
                            }

                            if (normalGums.isEmpty()) {
                                return;
                            }

                            Gum oldGum = normalGums.get(random.nextInt(normalGums.size()));
                            Grid.Position position = oldGum.position();

                            model.removeCollectible(oldGum);
                            view.removeAvatarOf(oldGum);
                            gums.remove(oldGum);

                            Gum powerGum = new Gum(game, true);
                            powerGum.setPosition(position);
                            powerGum.setBounding();

                            gums.add(powerGum);
                            model.addCollectible(powerGum);
                            view.add(new GumAvatar(powerGum));

                            showMessage("POWER APPARU", now, 1200);
                        }

                        private void spawnRandomCherry(long now) {
                            removeCurrentCherries();

                            Grid.Position position = randomWalkablePosition(game);

                            Cherry cherry = new Cherry(game);
                            cherry.setPosition(position);
                            cherry.setBounding();

                            cherries.add(cherry);
                            model.addCollectible(cherry);
                            view.add(new CherryAvatar(cherry));

                            showMessage("CHERRY APPARUE", now, 1200);
                        }

                        private void removeCurrentCherries() {
                            List<Cherry> oldCherries = new ArrayList<>(cherries);

                            for (Cherry cherry : oldCherries) {
                                model.removeCollectible(cherry);
                                view.removeAvatarOf(cherry);
                                cherries.remove(cherry);
                            }
                        }

                        /**
                         * Gère les collisions PacMan / fantômes.
                         */
                        private void handleGhostCollisions(long now) {
                            for (int i = 0; i < ghosts.size(); i++) {
                                Ghost ghost = ghosts.get(i);

                                if (!ghost.alive()) {
                                    continue;
                                }

                                if (!model.intersects(pacman, ghost)) {
                                    continue;
                                }

                                if (now < powerUntil) {
                                    score += GHOST_SCORE;

                                    ghost.eatenByPacman();
                                    ghostStunts.get(i).stopNow();

                                    if (!ghost.alive()) {
                                        model.removeEntity(ghost);
                                        model.removeStunt(ghostStunts.get(i));
                                        view.removeAvatarOf(ghost);
                                        showMessage(ghost.type() + " fini !", now, 1200);
                                    } else {
                                        ghostBots.get(i).think();
                                        showMessage(
                                            ghost.type() + " mangé, respawn " + ghost.remainingSpawns(),
                                            now,
                                            1200
                                        );
                                    }

                                } else if (now >= invulnerableUntil) {
                                    lives--;

                                    if (lives <= 0) {
                                        lives = 0;
                                        finished = true;
                                        invulnerableUntil = 0;
                                        powerUntil = 0;
                                        pacmanStunt.stopNow();
                                        showPermanentMessage("PERDU !");
                                        return;
                                    }

                                    pacman.setPosition(pacmanSpawn);
                                    pacman.setBounding();
                                    pacmanStunt.stopNow();

                                    invulnerableUntil = now + INVULNERABLE_MS;
                                    showMessage("Touché !", now, 1200);
                                }
                            }
                        }

                        private void showMessage(String text, long now, long duration_ms) {
                            message = text;
                            messageUntil = now + duration_ms;
                        }

                        private void showPermanentMessage(String text) {
                            message = text;
                            messageUntil = 0;
                        }

                        private void checkEndOfGame() {
                            if (gums.isEmpty()) {
                                finished = true;
                                showPermanentMessage("GAGNÉ !");
                            }

                            if (lives <= 0) {
                                finished = true;
                                lives = 0;
                                invulnerableUntil = 0;
                                powerUntil = 0;
                                showPermanentMessage("PERDU !");
                            }
                        }
                    }, 30);
                }
            }
        );
    }

    /**
     * Construit les entités du niveau à partir de MAP.
     */
    private static Grid.Position buildMap(
            Pacman game,
            PacMan pacman,
            List<Obstacle> obstacles,
            List<Gum> gums,
            List<Cherry> cherries,
            List<Grid.Position> spawners) {

        checkMap(game);

        Grid.Position pacmanSpawn = null;

        for (int y = 0; y < MAP.length; y++) {
            String row = MAP[y];

            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);

                switch (c) {
                    case '#': {
                        Obstacle obstacle = new Obstacle(game, x, y);
                        obstacle.setBounding();
                        obstacles.add(obstacle);
                        break;
                    }

                    case '.':
                    case 'o':
                    case 'C': {
                        Gum gum = new Gum(game, false);
                        gum.setPosition(game.grid().new Position(x, y));
                        gum.setBounding();
                        gums.add(gum);
                        break;
                    }

                    case 'P': {
                        pacmanSpawn = game.grid().new Position(x, y);
                        pacman.setPosition(pacmanSpawn);
                        pacman.setBounding();
                        break;
                    }

                    case 'S': {
                        spawners.add(game.grid().new Position(x, y));
                        break;
                    }

                    default:
                        break;
                }
            }
        }

        if (pacmanSpawn == null) {
            throw new IllegalStateException("PacMan must be placed in the map with P");
        }

        if (spawners.isEmpty()) {
            throw new IllegalStateException("At least one ghost spawner must be placed with S");
        }

        return pacmanSpawn.copy();
    }

    /**
     * Choisit une taille initiale adaptée à l'écran.
     */
    private static Dimension initialWindowSize(Pacman gameConfig) {
        int wantedWidth = gameConfig.pict().width() + HUD_WIDTH;
        int wantedHeight = gameConfig.pict().height();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int maxWidth = Math.max(640, screen.width - 160);
        int maxHeight = Math.max(480, screen.height - 160);

        int width = Math.min(wantedWidth, maxWidth);
        int height = Math.min(wantedHeight, maxHeight);

        return new Dimension(width, height);
    }

    /**
     * Renvoie une position libre aléatoire pour faire apparaître un bonus.
     */
    private static Grid.Position randomWalkablePosition(Pacman game) {
        Random random = new Random();
        List<Grid.Position> positions = new ArrayList<>();

        for (int y = 0; y < MAP.length; y++) {
            for (int x = 0; x < MAP[y].length(); x++) {
                char c = MAP[y].charAt(x);

                if (c != '#' && c != 'S') {
                    positions.add(game.grid().new Position(x, y));
                }
            }
        }

        if (positions.isEmpty()) {
            throw new IllegalStateException("No walkable position available for random bonus");
        }

        return positions.get(random.nextInt(positions.size()));
    }

    /**
     * Place les fantômes sur les cases marquées S.
     */
    private static void placeGhostsOnSpawners(List<Ghost> ghosts, List<Grid.Position> spawners) {
        assert ghosts != null : "ghosts must not be null";
        assert spawners != null : "spawners must not be null";
        assert !spawners.isEmpty() : "spawners must not be empty";

        for (int i = 0; i < ghosts.size(); i++) {
            Grid.Position spawn = spawners.get(i % spawners.size());
            ghosts.get(i).setSpawnPosition(spawn);
        }
    }

    /**
     * Vérifie que la carte texte correspond à la configuration du jeu.
     */
    private static void checkMap(Pacman game) {
        int expectedHeight = game.height_ncell();
        int expectedWidth = game.width_ncell();

        if (MAP.length != expectedHeight) {
            throw new IllegalStateException(
                "MAP height = " + MAP.length + " but game height = " + expectedHeight
            );
        }

        for (int y = 0; y < MAP.length; y++) {
            if (MAP[y].length() != expectedWidth) {
                throw new IllegalStateException(
                    "MAP row " + y + " length = " + MAP[y].length()
                    + " but game width = " + expectedWidth
                );
            }
        }

        if (!game.torusOnYaxis()) {
            for (int x = 0; x < expectedWidth; x++) {
                if (MAP[0].charAt(x) != '#') {
                    throw new IllegalStateException("Top border is not closed at x = " + x);
                }

                if (MAP[expectedHeight - 1].charAt(x) != '#') {
                    throw new IllegalStateException("Bottom border is not closed at x = " + x);
                }
            }
        }

        if (!game.torusOnXaxis()) {
            for (int y = 0; y < expectedHeight; y++) {
                if (MAP[y].charAt(0) != '#') {
                    throw new IllegalStateException("Left border is not closed at y = " + y);
                }

                if (MAP[y].charAt(expectedWidth - 1) != '#') {
                    throw new IllegalStateException("Right border is not closed at y = " + y);
                }
            }
        }

        if (game.torusOnXaxis()) {
            boolean hasHorizontalTunnel = false;

            for (int y = 0; y < expectedHeight; y++) {
                boolean leftOpen = MAP[y].charAt(0) != '#';
                boolean rightOpen = MAP[y].charAt(expectedWidth - 1) != '#';

                if (leftOpen && rightOpen) {
                    hasHorizontalTunnel = true;
                    break;
                }
            }

            if (!hasHorizontalTunnel) {
                throw new IllegalStateException(
                    "torusOnXaxis is true, but no horizontal tunnel is open"
                );
            }
        }

        if (game.torusOnYaxis()) {
            boolean hasVerticalTunnel = false;

            for (int x = 0; x < expectedWidth; x++) {
                boolean topOpen = MAP[0].charAt(x) != '#';
                boolean bottomOpen = MAP[expectedHeight - 1].charAt(x) != '#';

                if (topOpen && bottomOpen) {
                    hasVerticalTunnel = true;
                    break;
                }
            }

            if (!hasVerticalTunnel) {
                throw new IllegalStateException(
                    "torusOnYaxis is true, but no vertical tunnel is open"
                );
            }
        }
    }
}