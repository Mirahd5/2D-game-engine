package game.controller;

import engine.brain.PlayerBot;
import engine.controller.Controller;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

/*
 * Contrôleur clavier spécifique au jeu Pac-Man.
 *
 * Il traduit les touches du clavier en souhaits
 * de direction transmis au PlayerBot.
 */
public final class PacManController extends Controller {

	private final PlayerBot playerBot;

	public PacManController(PlayerBot playerBot) {
		assert playerBot != null;

		this.playerBot = playerBot;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			this.playerBot.wish(0);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			this.playerBot.wish(90);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			this.playerBot.wish(180);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			this.playerBot.wish(270);
			break;

		default:
			break;
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		/*
		 * Pac-Man continue dans sa direction après le relâchement de la touche.
		 */
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		// Non utilisé.
	}
}