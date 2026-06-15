package game;

import java.awt.Dimension;

import engine.brain.AttackBot;
import engine.brain.Brain;
import engine.brain.PlayerBot;
import engine.controller.MouseController;
import engine.model.BasicStunt;
import engine.model.Game;
import engine.model.ISU;
import engine.model.Model;
import engine.model.RotationStunt;
import engine.model.ViewPort;
import engine.view.Painter;
import engine.view.PainterTask;
import engine.view.Ticker;
import engine.view.View;
import game.controller.PacManController;
import game.level.LevelBuilder;
import game.model.Ghost;
import game.model.PacMan;
import game.model.PacManGame;
import game.view.PacManHud;
import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Runnable;
import oop.tasks.Task;

public final class Main {

	private static final int MAP_WIDTH_PIXEL = 760;
	private static final int MAP_HEIGHT_PIXEL = 840;

	private static final int TICK_DELAY_MS = 16;
	private static final int PAINT_DELAY_MS = 33;
	private static final int VIEWPORT_WIDTH_CELLS = 15;

	private Main() {
		// Classe utilitaire : aucune instance.
	}

	public static void main(String[] args) {
		Dimension windowDimension = new Dimension(MAP_WIDTH_PIXEL + PacManHud.WIDTH_PIXEL, MAP_HEIGHT_PIXEL);

		Runtime.boot(windowDimension, new Runnable() {
			@Override
			public void run() {
				bootGame();
			}
		});
	}

	private static void bootGame() {
		/*
		 * Création du jeu et des composants principaux.
		 */
		Game game = new PacManGame(19, 21, true, false, 3.7, 20);

		Model model = game.model();

		ISU isu = model.isu();

		/*
		 * Pac-Man possède un tore horizontal.
		 *
		 * Le viewport montre 15 cellules sur les 19. Verticalement, la carte n'est pas
		 * torique : on peut afficher toute sa hauteur.
		 */
		ViewPort viewPort = new ViewPort(model, isu.new Coord(0.0, 0.0),
				isu.new Dimension(VIEWPORT_WIDTH_CELLS * isu.cmPerCell(),

						isu.height()));

		model.setViewPort(viewPort);

		Brain brain = new Brain();

		PacManHud hud = new PacManHud();

		View view = new View(model, MAP_WIDTH_PIXEL, MAP_HEIGHT_PIXEL, hud);

		Painter painter = new Painter(view);

		/*
		 * Construction du niveau : entités, positions, bounding et avatars.
		 */
		LevelBuilder levelBuilder = new LevelBuilder(game, view);

		levelBuilder.build();

		PacMan pacman = levelBuilder.pacman();

		/*
		 * Position initiale de la caméra.
		 */
		viewPort.centerOn(pacman);

		assert pacman != null;
		assert !levelBuilder.ghosts().isEmpty();

		/*
		 * Configuration du joueur.
		 */
		BasicStunt pacmanWalkingStunt = new BasicStunt(model, pacman);

		pacman.setStunt(pacmanWalkingStunt);

		RotationStunt pacmanRotationStunt = new RotationStunt(model, pacman);

		PlayerBot playerBot = new PlayerBot(brain, pacmanWalkingStunt, pacmanRotationStunt);

		brain.add(playerBot);

		/*
		 * Configuration de tous les fantômes du niveau.
		 *
		 * Chaque fantôme possède : - son propre BasicStunt ; - son propre AttackBot ; -
		 * Pac-Man comme cible.
		 */
		for (Ghost ghost : levelBuilder.ghosts()) {

			BasicStunt ghostWalkingStunt = new BasicStunt(model, ghost);

			ghost.setStunt(ghostWalkingStunt);

			AttackBot attackBot = new AttackBot(brain, ghostWalkingStunt, pacman);

			brain.add(attackBot);
		}

		/*
		 * Contrôleurs.
		 *
		 * PacManController est spécifique au jeu. MouseController appartient au moteur.
		 */
		PacManController keyboardController = new PacManController(playerBot);

		MouseController mouseController = new MouseController(playerBot);

		/*
		 * Connexion au Canvas fourni par la plateforme.
		 */
		Task task = Runtime.task();

		Canvas canvas = (Canvas) task.find("canvas");

		assert canvas != null;

		canvas.set(painter);

		canvas.set((Canvas.KeyListener) keyboardController);

		canvas.set((Canvas.MouseListener) mouseController);

		/*
		 * Deux tâches séparées : - simulation ; - demande de repaint.
		 */
		Runtime.post(new Ticker(model, brain, view, TICK_DELAY_MS, pacman));

		Runtime.post(new PainterTask(painter, PAINT_DELAY_MS));
	}
}