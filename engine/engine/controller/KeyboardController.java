package engine.controller;

import engine.brain.PlayerBot;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

/**
 * Contrôleur clavier qui transforme les touches directionnelles en souhaits de
 * déplacement.
 */
public class KeyboardController extends Controller {

	private static final int EAST = 0;
	private static final int SOUTH = 90;
	private static final int WEST = 180;
	private static final int NORTH = 270;

	private final PlayerBot playerBot;

	public KeyboardController(PlayerBot playerBot) {
		this.playerBot = playerBot;
	}

	@Override
	protected void keyPressed(Canvas canvas, int keyCode, char keyChar) {
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT:
		case VirtualKeyCodes.VK_D:
			this.playerBot.wish(EAST);
			break;

		case VirtualKeyCodes.VK_DOWN:
		case VirtualKeyCodes.VK_S:
			this.playerBot.wish(SOUTH);
			break;

		case VirtualKeyCodes.VK_LEFT:
		case VirtualKeyCodes.VK_Q:
			this.playerBot.wish(WEST);
			break;

		case VirtualKeyCodes.VK_UP:
		case VirtualKeyCodes.VK_Z:
			this.playerBot.wish(NORTH);
			break;

		default:
			break;
		}
	}
}